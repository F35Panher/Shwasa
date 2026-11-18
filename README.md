# Shwasa — Guided Breathing & Wellness (Android)

**Tagline:** *Breathe Better. Live Better.*

A native Kotlin & Jetpack Compose Android application for guided pranayama breathing, audio-visual breath pacing, custom breathing patterns, and daily practice tracking.

## Architecture & Modules

- `com.example.model` — Domain models (`BreathingExercise`, `BreathingStage`, `BreathingPattern`, `PracticeSession`, `DailyProgress`, `UserSettings`).
- `com.example.engine` — Reusable monotonic-clock `BreathingTimerEngine` shared across all six breathing modules with drift-free pause/resume, 5-second interval chimes, optional stage support, and completion callbacks.
- `com.example.audio` — `PranayamaAudioEngine` synthesizing gentle sine-wave chimes via Android `AudioTrack` and optional spoken cues via `TextToSpeech` with overlap protection.
- `com.example.data` — `ExerciseCatalog`, versioned Room database (`PranayamaDatabase`), `SettingsDataStore` (Preferences DataStore), `ProgressCalculator`, and `PranayamaRepository` interface (designed for future Firebase/Supabase cloud sync).
- `com.example.viewmodel` — `PranayamaViewModel` exposing lifecycle-safe `StateFlow` state.
- `com.example.ui` — Material 3 botanical sage/teal/cream light & dark theme (`Theme.kt`), adaptive bottom bar / tablet navigation rail (`AppNavigation.kt`), custom Canvas visualizers (`BreathingVisualizer.kt`), statistics charts (`PracticeCharts.kt`), and screens (`HomeScreen`, `ExploreScreen`, `ExerciseSessionScreen`, `CustomPatternsScreen`, `PracticeHistoryScreen`, `ProgressScreen`, `SettingsScreen`).

## Six Guided Breathing Modules

1. **Three-Stage Breathing Clock** — Configurable Inhale / Hold / Exhale (5s-5s-5s default), expanding/contracting circular clock, 5s interval chimes, and distinct stage transitions.
2. **Guided Anulom Vilom** — Visual Left/Right nostril airflow indicator, optional breath retention (off by default), configurable rounds, and beginner guide.
3. **Bhramari Pranayama** — Nasal inhale & humming-bee exhale with live resonance sound-wave visualization.
4. **Kapalbhati Timer** — Gentle-by-default active exhalation counter, comfortable pace selector, rest intervals, and safety guidance.
5. **Box Breathing (4-4-4-4)** — Animated square perimeter tracing Inhale, Hold, Exhale, and Hold Empty with independent side durations.
6. **Custom Breathing Patterns** — Create, reorder, color-code, edit, and delete multi-stage patterns with repetitions and audio toggles.

## Building & Testing

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```
