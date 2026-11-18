package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioFeedback
import com.example.audio.PranayamaAudioEngine
import com.example.data.ExerciseCatalog
import com.example.data.LocalPranayamaRepository
import com.example.data.PranayamaRepository
import com.example.data.ProgressCalculator
import com.example.data.local.PranayamaDatabase
import com.example.data.local.SettingsDataStore
import com.example.engine.BreathingTimerEngine
import com.example.engine.BreathingTimerState
import com.example.engine.KapalbhatiConfig
import com.example.model.BeepTone
import com.example.model.BreathingExercise
import com.example.model.BreathingModuleType
import com.example.model.BreathingPattern
import com.example.model.BreathingStage
import com.example.model.PracticeSession
import com.example.model.ProgressSummary
import com.example.model.StageType
import com.example.model.ThemePreference
import com.example.model.TimerDisplayStyle
import com.example.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PranayamaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PranayamaDatabase.getInstance(application)
    private val settingsStore = SettingsDataStore(application)
    private val repository: PranayamaRepository = LocalPranayamaRepository(
        patternDao = database.customPatternDao(),
        sessionDao = database.practiceSessionDao(),
        settingsStore = settingsStore
    )

    private val audioEngine: AudioFeedback = PranayamaAudioEngine(application)

    val settings: StateFlow<UserSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val customPatterns: StateFlow<List<BreathingPattern>> = repository.customPatternsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<PracticeSession>> = repository.sessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progressSummary: StateFlow<ProgressSummary> = repository.progressSummaryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressSummary())

    val recentExercises: StateFlow<List<BreathingExercise>> = sessions
        .map { list ->
            val recentModules = list.map { it.moduleType }.distinct().take(4)
            if (recentModules.isEmpty()) {
                listOf(
                    ExerciseCatalog.byModuleType(BreathingModuleType.THREE_STAGE),
                    ExerciseCatalog.byModuleType(BreathingModuleType.BOX_BREATHING),
                    ExerciseCatalog.byModuleType(BreathingModuleType.ANULOM_VILOM)
                )
            } else {
                recentModules.map { ExerciseCatalog.byModuleType(it) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeExercise = MutableStateFlow(ExerciseCatalog.byModuleType(BreathingModuleType.THREE_STAGE))
    val activeExercise: StateFlow<BreathingExercise> = _activeExercise.asStateFlow()

    private val _completedSessionDialog = MutableStateFlow<PracticeSession?>(null)
    val completedSessionDialog: StateFlow<PracticeSession?> = _completedSessionDialog.asStateFlow()

    private val timerEngine = BreathingTimerEngine(
        scope = viewModelScope,
        audioFeedback = audioEngine,
        onSessionAutoCompleted = { pattern, durationSec, cycles ->
            saveCompletedSession(pattern, durationSec, cycles)
        }
    )
    val timerState: StateFlow<BreathingTimerState> = timerEngine.state

    init {
        viewModelScope.launch {
            repository.ensureInitialPatternsSeeded()
            val initialSettings = repository.settingsFlow.first()
            audioEngine.updateSettings(initialSettings)
            loadExercise(BreathingModuleType.THREE_STAGE)
        }
        viewModelScope.launch {
            repository.settingsFlow.collect { latest ->
                audioEngine.updateSettings(latest)
            }
        }
    }

    fun loadExercise(moduleType: BreathingModuleType, customPatternId: String? = null) {
        val exercise = ExerciseCatalog.byModuleType(moduleType)
        _activeExercise.value = exercise
        _completedSessionDialog.value = null

        viewModelScope.launch {
            val currentSettings = settings.value
            val pattern = when {
                customPatternId != null -> {
                    customPatterns.value.firstOrNull { it.id == customPatternId }
                        ?: repository.customPatternsFlow.first().firstOrNull { it.id == customPatternId }
                        ?: exercise.defaultPattern
                }
                moduleType == BreathingModuleType.THREE_STAGE -> {
                    val base = exercise.defaultPattern
                    base.copy(
                        stages = base.stages.map { st ->
                            when (st.type) {
                                StageType.INHALE -> st.copy(durationSeconds = currentSettings.defaultInhaleSeconds)
                                StageType.HOLD_IN -> st.copy(
                                    durationSeconds = currentSettings.defaultHoldSeconds,
                                    isEnabled = currentSettings.defaultHoldSeconds > 0
                                )
                                StageType.EXHALE -> st.copy(durationSeconds = currentSettings.defaultExhaleSeconds)
                                else -> st
                            }
                        }
                    )
                }
                else -> exercise.defaultPattern
            }
            timerEngine.loadPattern(pattern, KapalbhatiConfig())
        }
    }

    fun updateActivePatternConfig(
        updatedPattern: BreathingPattern,
        kapalbhatiConfig: KapalbhatiConfig = timerState.value.kapalbhatiConfig
    ) {
        timerEngine.loadPattern(updatedPattern, kapalbhatiConfig)
    }

    fun startTimer() {
        _completedSessionDialog.value = null
        timerEngine.start()
    }

    fun pauseTimer() = timerEngine.pause()

    fun resumeTimer() = timerEngine.resume()

    fun resetTimer() {
        _completedSessionDialog.value = null
        timerEngine.reset()
    }

    fun finishAndSaveSession() {
        val result = timerEngine.finishManually() ?: return
        val pattern = timerState.value.pattern ?: return
        val (durationSec, cycles) = result
        saveCompletedSession(pattern, durationSec, cycles)
    }

    private fun saveCompletedSession(
        pattern: BreathingPattern,
        durationSeconds: Int,
        completedCycles: Int
    ) {
        val exercise = _activeExercise.value
        val now = System.currentTimeMillis()
        val session = PracticeSession(
            exerciseId = exercise.id,
            exerciseTitle = exercise.title,
            moduleType = pattern.moduleType,
            patternName = pattern.name,
            durationSeconds = durationSeconds.coerceAtLeast(1),
            completedCycles = completedCycles,
            completedAtMillis = now,
            dateIso = ProgressCalculator.todayIso(now),
            wasCompletedExplicitly = true
        )
        viewModelScope.launch {
            repository.recordSession(session)
            _completedSessionDialog.value = session
        }
    }

    fun dismissCompletedSessionDialog() {
        _completedSessionDialog.value = null
    }

    fun toggleQuickSound() {
        val current = settings.value.soundEnabled
        updateSettings { it.copy(soundEnabled = !current) }
    }

    fun toggleQuickVoice() {
        val current = settings.value.voiceGuidanceEnabled
        updateSettings { it.copy(voiceGuidanceEnabled = !current) }
    }

    fun saveCustomPattern(pattern: BreathingPattern) {
        viewModelScope.launch {
            repository.saveCustomPattern(pattern.copy(isCustom = true, moduleType = BreathingModuleType.CUSTOM))
        }
    }

    fun deleteCustomPattern(patternId: String) {
        viewModelScope.launch {
            repository.deleteCustomPattern(patternId)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun updateSettings(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            repository.updateSettings(transform)
        }
    }

    fun setThemePreference(pref: ThemePreference) = updateSettings { it.copy(themePreference = pref) }
    fun setBeepTone(tone: BeepTone) {
        updateSettings { it.copy(beepTone = tone, beepFrequencyHz = tone.baseFrequencyHz) }
        audioEngine.previewBeep(tone, tone.baseFrequencyHz, settings.value.soundVolume)
    }
    fun previewBeep() {
        val s = settings.value
        audioEngine.previewBeep(s.beepTone, s.beepFrequencyHz, s.soundVolume)
    }
    fun setTimerDisplayStyle(style: TimerDisplayStyle) = updateSettings { it.copy(timerDisplayStyle = style) }

    suspend fun exportDataJson(): String = repository.exportVersionedJson()

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerEngine.release()
        audioEngine.release()
    }
}
