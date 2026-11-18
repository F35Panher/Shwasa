# Shwasa — Technical Architecture

## 1. Architectural Pattern
Shwasa follows **MVVM + Repository** with unidirectional data flow (`StateFlow` → Jetpack Compose):

```
UI Layer (Compose Screens & Visualizers)
        │  collects StateFlow / dispatches intents
        ▼
PranayamaViewModel (AndroidViewModel)
   ├── BreathingTimerEngine (Shared monotonic state machine)
   ├── PranayamaAudioEngine (AudioTrack + TextToSpeech)
   └── PranayamaRepository (Interface)
           └── LocalPranayamaRepository
                   ├── Room (PranayamaDatabase: patterns & sessions)
                   └── Preferences DataStore (SettingsDataStore)
```

## 2. Package Breakdown (`com.example`)
- **`model/BreathingModels.kt`**: Immutable Kotlin data models (`BreathingExercise`, `BreathingStage`, `BreathingPattern`, `PracticeSession`, `DailyProgress`, `UserSettings`, `ProgressSummary`).
- **`engine/BreathingTimerEngine.kt`**: Single reusable timer engine parameterized by `BreathingPattern` and `KapalbhatiConfig`.
- **`audio/PranayamaAudioEngine.kt`**: `AudioFeedback` interface and Android PCM synthesizer + TTS controller.
- **`data/ExerciseCatalog.kt`**: Curated catalog of all six modules with beginner guides and safety reminders.
- **`data/local/PranayamaDatabase.kt`**: Room v1 database (`custom_patterns`, `practice_sessions`) with JSON stage serialization via `org.json`.
- **`data/local/SettingsDataStore.kt`**: Versioned Preferences DataStore persistence.
- **`data/PranayamaRepository.kt`**: Repository contract (`PranayamaRepository`), Room/DataStore implementation (`LocalPranayamaRepository`), and pure `ProgressCalculator`.
- **`viewmodel/PranayamaViewModel.kt`**: Lifecycle-aware state holder managing active session configuration, custom patterns, progress stats, and cleanup in `onCleared()`.
- **`ui/`**: Theme (`Color.kt`, `Type.kt`, `Theme.kt`), adaptive navigation (`AppNavigation.kt`), custom Canvas components (`BreathingVisualizer.kt`, `PracticeCharts.kt`), and screens.

## 3. Shared Monotonic Timer Engine
`BreathingTimerEngine` avoids `delay()` accumulation drift by calculating stage and session progress from an injected `TimeSource` (defaulting to `SystemClock.elapsedRealtime()`):
- **Single Active Job**: Starting or resetting cancels any existing ticker coroutine (`tickerJob?.cancel()`), preventing duplicate concurrent timers.
- **Pause/Resume Accuracy**: Tracks `pauseStartedTimeMs` and shifts `stageStartTimeMs` by the paused delta upon resume, preserving exact remaining stage seconds and total active duration across backgrounding.
- **Optional Stages**: Filters `stages.filter { it.isEnabled && it.durationSeconds > 0 }` so modules like Anulom Vilom seamlessly omit breath retention unless enabled.

## 4. Extensibility for Cloud Sync
All persistence flows through the `PranayamaRepository` interface. To integrate Firebase Firestore or Supabase in the future:
1. Implement `RemotePranayamaRepository : PranayamaRepository` (or a synchronizing wrapper around `LocalPranayamaRepository`).
2. Keep `schemaVersion` in `UserSettings` and `PranayamaDatabase` (`version = 1`) for deterministic data migrations.

## 5. Testing Strategy
Local JVM & Robolectric tests live in `app/src/test/java/com/example/`:
- `BreathingTimerEngineTest.kt`: Uses a fake `TimeSource` and `FakeAudioFeedback` to deterministically test 3-stage transitions, pause/resume drift immunity, 5-second interval cues, Anulom Vilom optional stage filtering, and `ProgressCalculator` streak math.
- `ExampleRobolectricTest.kt`: Validates Android resource resolution (`R.string.app_name == "Shwasa"`) under Robolectric SDK 36.
