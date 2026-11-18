package com.example

import com.example.audio.AudioFeedback
import com.example.data.ExerciseCatalog
import com.example.data.ProgressCalculator
import com.example.engine.BreathingTimerEngine
import com.example.engine.TimeSource
import com.example.engine.TimerPhaseStatus
import com.example.model.BeepTone
import com.example.model.BreathingModuleType
import com.example.model.PracticeSession
import com.example.model.StageType
import com.example.model.UserSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeAudioFeedback : AudioFeedback {
    val intervalBeeps = mutableListOf<Int>()
    val stageTransitions = mutableListOf<StageType>()
    var completionCount = 0

    override fun updateSettings(settings: UserSettings) {}
    override fun playIntervalBeep() { intervalBeeps.add(1) }
    override fun playStageTransition(stageType: StageType, spokenInstruction: String?) {
        stageTransitions.add(stageType)
    }
    override fun playKapalbhatiPulse() {}
    override fun playCompletionChime(spokenSummary: String?) { completionCount++ }
    override fun previewBeep(tone: BeepTone, frequencyHz: Int, volume: Float) {}
    override fun release() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class BreathingTimerEngineTest {

    @Test
    fun `three stage clock transitions inhale hold exhale and completes cycle accurately`() {
        var fakeNow = 1000L
        val audio = FakeAudioFeedback()
        val scope = TestScope(UnconfinedTestDispatcher())
        val engine = BreathingTimerEngine(
            scope = scope,
            audioFeedback = audio,
            timeSource = TimeSource { fakeNow }
        )

        val threeStage = ExerciseCatalog.byModuleType(BreathingModuleType.THREE_STAGE).defaultPattern
        engine.loadPattern(threeStage)
        assertEquals(TimerPhaseStatus.IDLE, engine.state.value.status)
        assertEquals(5, engine.state.value.stageRemainingSeconds)

        engine.start()
        assertEquals(StageType.INHALE, engine.state.value.currentStage?.type)
        assertEquals(listOf(StageType.INHALE), audio.stageTransitions)

        // Advance 3s into 5s Inhale
        fakeNow += 3000L
        engine.stepFrame(fakeNow)
        assertEquals(2, engine.state.value.stageRemainingSeconds)

        // Advance past 5s -> transitions to Hold
        fakeNow += 2050L
        engine.stepFrame(fakeNow)
        assertEquals(StageType.HOLD_IN, engine.state.value.currentStage?.type)
        assertEquals(listOf(StageType.INHALE, StageType.HOLD_IN), audio.stageTransitions)

        // Advance past 5s Hold -> transitions to Exhale
        fakeNow += 5050L
        engine.stepFrame(fakeNow)
        assertEquals(StageType.EXHALE, engine.state.value.currentStage?.type)

        // Advance past 5s Exhale -> completes cycle 1 and loops back to Inhale
        fakeNow += 5050L
        engine.stepFrame(fakeNow)
        assertEquals(1, engine.state.value.completedCycles)
        assertEquals(StageType.INHALE, engine.state.value.currentStage?.type)

        engine.release()
    }

    @Test
    fun `pause and resume preserves exact stage remaining time without drift`() {
        var fakeNow = 5000L
        val audio = FakeAudioFeedback()
        val scope = TestScope(UnconfinedTestDispatcher())
        val engine = BreathingTimerEngine(
            scope = scope,
            audioFeedback = audio,
            timeSource = TimeSource { fakeNow }
        )

        val pattern = ExerciseCatalog.byModuleType(BreathingModuleType.THREE_STAGE).defaultPattern
        engine.loadPattern(pattern)
        engine.start()

        // 2 seconds elapsed in 5s Inhale -> 3s remaining
        fakeNow += 2000L
        engine.stepFrame(fakeNow)
        assertEquals(3, engine.state.value.stageRemainingSeconds)

        engine.pause()
        assertEquals(TimerPhaseStatus.PAUSED, engine.state.value.status)

        // Simulate 60 seconds passing while paused (e.g., backgrounded)
        fakeNow += 60_000L
        engine.resume()
        assertEquals(TimerPhaseStatus.RUNNING, engine.state.value.status)
        engine.stepFrame(fakeNow)
        assertEquals(3, engine.state.value.stageRemainingSeconds)
        assertEquals(2, engine.state.value.totalElapsedSeconds)

        engine.release()
    }

    @Test
    fun `five second interval beep fires inside longer stage without overlapping transition`() {
        var fakeNow = 0L
        val audio = FakeAudioFeedback()
        val scope = TestScope(UnconfinedTestDispatcher())
        val engine = BreathingTimerEngine(
            scope = scope,
            audioFeedback = audio,
            timeSource = TimeSource { fakeNow }
        )

        val base = ExerciseCatalog.byModuleType(BreathingModuleType.THREE_STAGE).defaultPattern
        val longStagePattern = base.copy(
            stages = listOf(base.stages.first().copy(durationSeconds = 12))
        )
        engine.loadPattern(longStagePattern)
        engine.start()

        fakeNow = 5100L
        engine.stepFrame(fakeNow)
        assertEquals(1, audio.intervalBeeps.size)

        fakeNow = 10100L
        engine.stepFrame(fakeNow)
        assertEquals(2, audio.intervalBeeps.size)

        engine.release()
    }

    @Test
    fun `anulom vilom skips optional breath retention by default`() {
        val audio = FakeAudioFeedback()
        val scope = TestScope(UnconfinedTestDispatcher())
        val engine = BreathingTimerEngine(
            scope = scope,
            audioFeedback = audio
        )
        val anulom = ExerciseCatalog.byModuleType(BreathingModuleType.ANULOM_VILOM).defaultPattern
        engine.loadPattern(anulom)

        assertEquals(4, engine.state.value.activeStages.size)
        assertFalse(engine.state.value.activeStages.any { it.type == StageType.HOLD_IN })
        engine.release()
    }

    @Test
    fun `progress calculator computes streaks and totals accurately`() {
        val now = 1_763_464_000_000L
        val today = ProgressCalculator.todayIso(now)
        val yesterday = ProgressCalculator.todayIso(now - 86_400_000L)

        val sessions = listOf(
            PracticeSession(
                exerciseId = "three_stage_clock",
                exerciseTitle = "Three-Stage Breathing Clock",
                moduleType = BreathingModuleType.THREE_STAGE,
                patternName = "Balanced 5-5-5",
                durationSeconds = 300,
                completedCycles = 20,
                completedAtMillis = now,
                dateIso = today
            ),
            PracticeSession(
                exerciseId = "box_breathing",
                exerciseTitle = "Box Breathing (4-4-4-4)",
                moduleType = BreathingModuleType.BOX_BREATHING,
                patternName = "Classic Box 4-4-4-4",
                durationSeconds = 240,
                completedCycles = 15,
                completedAtMillis = now - 86_400_000L,
                dateIso = yesterday
            )
        )

        val summary = ProgressCalculator.computeSummary(sessions, dailyGoalMinutes = 5, nowMillis = now)
        assertEquals(300, summary.todaySeconds)
        assertEquals(2, summary.currentStreakDays)
        assertEquals(2, summary.longestStreakDays)
        assertEquals(540, summary.lifetimeSeconds)
        assertTrue(summary.dailyChartPoints.last().goalMet)
    }
}
