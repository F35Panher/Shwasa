package com.example.engine

import android.os.SystemClock
import com.example.audio.AudioFeedback
import com.example.model.BreathingModuleType
import com.example.model.BreathingPattern
import com.example.model.BreathingStage
import com.example.model.StageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.ceil

enum class TimerPhaseStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

data class KapalbhatiConfig(
    val rounds: Int = 3,
    val targetExhalationsPerRound: Int = 20,
    val paceMillisPerExhalation: Long = 1200L, // Gentle default ~1.2s per active exhalation
    val restSecondsBetweenRounds: Int = 20
)

data class BreathingTimerState(
    val status: TimerPhaseStatus = TimerPhaseStatus.IDLE,
    val pattern: BreathingPattern? = null,
    val activeStages: List<BreathingStage> = emptyList(),
    val currentStageIndex: Int = 0,
    val stageDurationSeconds: Int = 0,
    val stageRemainingSeconds: Int = 0,
    val stageProgressFraction: Float = 0f, // 0f..1f within current stage
    val completedCycles: Int = 0,
    val targetCycles: Int = 0, // 0 = continuous
    val totalElapsedSeconds: Int = 0,
    val kapalbhatiConfig: KapalbhatiConfig = KapalbhatiConfig(),
    val kapalbhatiCurrentExhalation: Int = 0
) {
    val currentStage: BreathingStage?
        get() = activeStages.getOrNull(currentStageIndex)

    val canSaveSession: Boolean
        get() = status == TimerPhaseStatus.COMPLETED ||
            completedCycles >= 1 ||
            totalElapsedSeconds >= 15
}

fun interface TimeSource {
    fun elapsedRealtimeMs(): Long
}

class BreathingTimerEngine(
    private val scope: CoroutineScope,
    private val audioFeedback: AudioFeedback,
    private val timeSource: TimeSource = TimeSource { SystemClock.elapsedRealtime() },
    private val onSessionAutoCompleted: (pattern: BreathingPattern, durationSeconds: Int, completedCycles: Int) -> Unit = { _, _, _ -> }
) {
    private val _state = MutableStateFlow(BreathingTimerState())
    val state: StateFlow<BreathingTimerState> = _state.asStateFlow()

    private var tickerJob: Job? = null
    private var stageStartTimeMs: Long = 0L
    private var pauseStartedTimeMs: Long = 0L
    private var sessionActiveAccumulatedMs: Long = 0L
    private var sessionSegmentStartMs: Long = 0L
    private var lastFiveSecBeepMark: Int = 0
    private var lastKapalbhatiStroke: Int = 0
    private var patternAudioEnabled: Boolean = true

    fun loadPattern(
        pattern: BreathingPattern,
        kapalbhatiConfig: KapalbhatiConfig = KapalbhatiConfig()
    ) {
        stopTicker()
        val resolvedPattern = if (pattern.moduleType == BreathingModuleType.KAPALBHATI) {
            val activeDuration = ceil(
                (kapalbhatiConfig.targetExhalationsPerRound * kapalbhatiConfig.paceMillisPerExhalation) / 1000.0
            ).toInt().coerceAtLeast(5)
            val updatedStages = listOf(
                BreathingStage(
                    id = "kp_active",
                    label = "Active Exhalations",
                    durationSeconds = activeDuration,
                    type = StageType.KAPALBHATI_ACTIVE,
                    colorHex = "#C0734F",
                    instruction = "Gentle active exhalations, effortless passive inhales"
                ),
                BreathingStage(
                    id = "kp_rest",
                    label = "Rest & Observe",
                    durationSeconds = kapalbhatiConfig.restSecondsBetweenRounds.coerceAtLeast(5),
                    type = StageType.REST,
                    colorHex = "#3E7B6D",
                    instruction = "Rest quietly and breathe naturally"
                )
            )
            pattern.copy(
                stages = updatedStages,
                repetitions = kapalbhatiConfig.rounds.coerceAtLeast(1)
            )
        } else {
            pattern
        }

        val stages = resolvedPattern.activeStages
        val firstStage = stages.firstOrNull()
        patternAudioEnabled = resolvedPattern.audioCuesEnabled
        sessionActiveAccumulatedMs = 0L
        lastFiveSecBeepMark = 0
        lastKapalbhatiStroke = 0

        _state.value = BreathingTimerState(
            status = TimerPhaseStatus.IDLE,
            pattern = resolvedPattern,
            activeStages = stages,
            currentStageIndex = 0,
            stageDurationSeconds = firstStage?.durationSeconds ?: 0,
            stageRemainingSeconds = firstStage?.durationSeconds ?: 0,
            stageProgressFraction = 0f,
            completedCycles = 0,
            targetCycles = resolvedPattern.repetitions,
            totalElapsedSeconds = 0,
            kapalbhatiConfig = kapalbhatiConfig,
            kapalbhatiCurrentExhalation = 0
        )
    }

    fun start() {
        val current = _state.value
        val firstStage = current.activeStages.firstOrNull() ?: return
        stopTicker()

        val now = timeSource.elapsedRealtimeMs()
        stageStartTimeMs = now
        sessionSegmentStartMs = now
        sessionActiveAccumulatedMs = 0L
        lastFiveSecBeepMark = 0
        lastKapalbhatiStroke = 0

        _state.update {
            it.copy(
                status = TimerPhaseStatus.RUNNING,
                currentStageIndex = 0,
                stageDurationSeconds = firstStage.durationSeconds,
                stageRemainingSeconds = firstStage.durationSeconds,
                stageProgressFraction = 0f,
                completedCycles = 0,
                totalElapsedSeconds = 0,
                kapalbhatiCurrentExhalation = if (firstStage.type == StageType.KAPALBHATI_ACTIVE) 1 else 0
            )
        }

        triggerStageCue(firstStage)
        startTicker()
    }

    fun pause() {
        val current = _state.value
        if (current.status != TimerPhaseStatus.RUNNING) return
        stopTicker()

        val now = timeSource.elapsedRealtimeMs()
        pauseStartedTimeMs = now
        sessionActiveAccumulatedMs += (now - sessionSegmentStartMs).coerceAtLeast(0L)

        _state.update { it.copy(status = TimerPhaseStatus.PAUSED) }
    }

    fun resume() {
        val current = _state.value
        if (current.status != TimerPhaseStatus.PAUSED) return

        val now = timeSource.elapsedRealtimeMs()
        val pausedDelta = (now - pauseStartedTimeMs).coerceAtLeast(0L)
        stageStartTimeMs += pausedDelta
        sessionSegmentStartMs = now

        _state.update { it.copy(status = TimerPhaseStatus.RUNNING) }
        startTicker()
    }

    fun reset() {
        val pattern = _state.value.pattern ?: return
        val kConfig = _state.value.kapalbhatiConfig
        loadPattern(pattern, kConfig)
    }

    fun finishManually(): Pair<Int, Int>? {
        val current = _state.value
        if (!current.canSaveSession) return null
        stopTicker()
        val elapsedSec = computeCurrentTotalElapsedSeconds(current.status)
        _state.update {
            it.copy(
                status = TimerPhaseStatus.COMPLETED,
                totalElapsedSeconds = elapsedSec
            )
        }
        return elapsedSec to current.completedCycles
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && _state.value.status == TimerPhaseStatus.RUNNING) {
                stepFrame(timeSource.elapsedRealtimeMs())
                delay(50L)
            }
        }
    }

    /**
     * Deterministic frame update based on monotonic clock timestamps.
     * Exposed internally for unit testing without drift.
     */
    internal fun stepFrame(nowMs: Long) {
        val current = _state.value
        if (current.status != TimerPhaseStatus.RUNNING) return
        val stages = current.activeStages
        if (stages.isEmpty()) return

        val stage = stages.getOrNull(current.currentStageIndex) ?: return
        val stageDurationMs = stage.durationSeconds * 1000L
        val elapsedInStageMs = (nowMs - stageStartTimeMs).coerceAtLeast(0L)
        val totalElapsedSec = ((sessionActiveAccumulatedMs + (nowMs - sessionSegmentStartMs)) / 1000L).toInt()

        if (elapsedInStageMs >= stageDurationMs) {
            advanceStage(nowMs, totalElapsedSec)
            return
        }

        val elapsedWholeSec = (elapsedInStageMs / 1000L).toInt()
        val remainingSec = ceil((stageDurationMs - elapsedInStageMs) / 1000.0).toInt()
            .coerceIn(1, stage.durationSeconds)
        val fraction = (elapsedInStageMs.toFloat() / stageDurationMs.toFloat()).coerceIn(0f, 1f)

        // Play soft beep every 5 seconds inside stages longer than 5 seconds
        if (patternAudioEnabled && stage.type != StageType.KAPALBHATI_ACTIVE) {
            val fiveSecMark = elapsedWholeSec / 5
            if (fiveSecMark > lastFiveSecBeepMark && elapsedWholeSec > 0 && elapsedWholeSec < stage.durationSeconds) {
                lastFiveSecBeepMark = fiveSecMark
                audioFeedback.playIntervalBeep()
            }
        }

        // Kapalbhati active stroke counting & gentle cue
        var kapalbhatiStroke = current.kapalbhatiCurrentExhalation
        if (stage.type == StageType.KAPALBHATI_ACTIVE) {
            val pace = current.kapalbhatiConfig.paceMillisPerExhalation.coerceAtLeast(500L)
            val strokeIndex = ((elapsedInStageMs / pace).toInt() + 1)
                .coerceIn(1, current.kapalbhatiConfig.targetExhalationsPerRound)
            if (strokeIndex > lastKapalbhatiStroke) {
                lastKapalbhatiStroke = strokeIndex
                if (patternAudioEnabled && strokeIndex > 1) {
                    audioFeedback.playKapalbhatiPulse()
                }
            }
            kapalbhatiStroke = strokeIndex
        }

        _state.update {
            it.copy(
                stageRemainingSeconds = remainingSec,
                stageProgressFraction = fraction,
                totalElapsedSeconds = totalElapsedSec,
                kapalbhatiCurrentExhalation = kapalbhatiStroke
            )
        }
    }

    private fun advanceStage(nowMs: Long, totalElapsedSec: Int) {
        val current = _state.value
        val stages = current.activeStages
        val nextIndex = current.currentStageIndex + 1

        if (nextIndex < stages.size) {
            val nextStage = stages[nextIndex]
            stageStartTimeMs = nowMs
            lastFiveSecBeepMark = 0
            lastKapalbhatiStroke = if (nextStage.type == StageType.KAPALBHATI_ACTIVE) 1 else 0

            _state.update {
                it.copy(
                    currentStageIndex = nextIndex,
                    stageDurationSeconds = nextStage.durationSeconds,
                    stageRemainingSeconds = nextStage.durationSeconds,
                    stageProgressFraction = 0f,
                    totalElapsedSeconds = totalElapsedSec,
                    kapalbhatiCurrentExhalation = if (nextStage.type == StageType.KAPALBHATI_ACTIVE) 1 else 0
                )
            }
            triggerStageCue(nextStage)
        } else {
            // Cycle completed
            val newCompletedCycles = current.completedCycles + 1
            val target = current.targetCycles

            if (target > 0 && newCompletedCycles >= target) {
                stopTicker()
                sessionActiveAccumulatedMs += (nowMs - sessionSegmentStartMs).coerceAtLeast(0L)
                val finalDuration = (sessionActiveAccumulatedMs / 1000L).toInt().coerceAtLeast(1)

                _state.update {
                    it.copy(
                        status = TimerPhaseStatus.COMPLETED,
                        stageRemainingSeconds = 0,
                        stageProgressFraction = 1f,
                        completedCycles = newCompletedCycles,
                        totalElapsedSeconds = finalDuration
                    )
                }
                if (patternAudioEnabled) {
                    audioFeedback.playCompletionChime("Practice complete. Well done.")
                }
                current.pattern?.let { pattern ->
                    onSessionAutoCompleted(pattern, finalDuration, newCompletedCycles)
                }
            } else {
                // Start next cycle
                val firstStage = stages.first()
                stageStartTimeMs = nowMs
                lastFiveSecBeepMark = 0
                lastKapalbhatiStroke = if (firstStage.type == StageType.KAPALBHATI_ACTIVE) 1 else 0

                _state.update {
                    it.copy(
                        currentStageIndex = 0,
                        stageDurationSeconds = firstStage.durationSeconds,
                        stageRemainingSeconds = firstStage.durationSeconds,
                        stageProgressFraction = 0f,
                        completedCycles = newCompletedCycles,
                        totalElapsedSeconds = totalElapsedSec,
                        kapalbhatiCurrentExhalation = if (firstStage.type == StageType.KAPALBHATI_ACTIVE) 1 else 0
                    )
                }
                triggerStageCue(firstStage)
            }
        }
    }

    private fun triggerStageCue(stage: BreathingStage) {
        if (!patternAudioEnabled) return
        val spoken = stage.instruction.ifBlank { stage.label }
        audioFeedback.playStageTransition(stage.type, spoken)
    }

    private fun computeCurrentTotalElapsedSeconds(status: TimerPhaseStatus): Int {
        return if (status == TimerPhaseStatus.RUNNING) {
            val now = timeSource.elapsedRealtimeMs()
            ((sessionActiveAccumulatedMs + (now - sessionSegmentStartMs)) / 1000L).toInt()
        } else {
            (sessionActiveAccumulatedMs / 1000L).toInt()
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun release() {
        stopTicker()
    }
}
