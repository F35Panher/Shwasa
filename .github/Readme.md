# Shwasa — Project Documentation Hub

**Shwasa** (*Breathe Better. Live Better.*) is a native Android guided breathing and pranayama wellness application built with Kotlin, Jetpack Compose, Material 3, Room, and Preferences DataStore.

## Documentation Index

| Document | Focus |
| :--- | :--- |
| [Documentation.md](./Documentation.md) | Feature specifications, all six breathing modules, audio cues, tracker metrics, and JSON export schema |
| [Architecture.md](./Architecture.md) | MVVM + Repository layers, monotonic timer engine, audio synthesizer, Room/DataStore schema, and test suite |
| [Design.md](./Design.md) | Visual identity, botanical color tokens, typography scale, adaptive layouts, custom visualizers, and accessibility |

## Quick Start

### Prerequisites
- Android SDK (compileSdk `36`, minSdk `24`, targetSdk `36`)
- JDK 11+
- Gradle installed on `PATH`

### Build & Verify
```bash
# Build debug APK
gradle :app:assembleDebug

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest
```

## Module Overview
1. **Three-Stage Breathing Clock** (`5s-5s-5s` default)
2. **Guided Anulom Vilom** (alternate nostril breathing, retention optional/off by default)
3. **Bhramari Pranayama** (nasal inhale + resonant humming exhale)
4. **Kapalbhati Timer** (gentle active exhalation counter + rest intervals)
5. **Box Breathing (4-4-4-4)** (four-sided square cadence)
6. **Custom Breathing Patterns** (multi-stage pattern builder, editor, and presets)
