# Shwasa — Feature & Functional Documentation

## 1. Six Guided Breathing Modules

Every exercise runs on the shared `BreathingTimerEngine` and begins with an introductory safety note plus an optional beginner step-by-step guide modal (`ExerciseGuideBottomSheet`).

### 1.1 Three-Stage Breathing Clock
- **Default Cadence**: `Inhale 5s` → `Hold 5s` → `Exhale 5s` (inherits user defaults from Settings).
- **Controls**: Configure each stage duration (`0–30s` for Hold, `1–30s` for Inhale/Exhale) and set continuous or fixed cycles (`1–30`).
- **Audio & Visuals**: Expanding/contracting circular breathing clock, soft 5-second interval chimes, distinct stage-transition tones, elapsed time (`MM:SS`), and completed cycle count.

### 1.2 Guided Anulom Vilom (Alternate Nostril)
- **Sequence**: `Inhale Left` → *(optional Hold)* → `Exhale Right` → `Inhale Right` → *(optional Hold)* → `Exhale Left`.
- **Defaults**: Breath retention is **disabled by default** for safe, beginner-friendly practice and can be toggled on in the session configurator.
- **Visuals**: Dual nostril airflow indicator highlighting the active nostril (`Left`, `Right`, or `Both`) with animated upward/downward breath flow.

### 1.3 Bhramari Pranayama (Humming Bee Breath)
- **Sequence**: `Inhale Slowly` (`4s` default) → `Humming Exhale` (`8s` default), configurable across rounds (`1–20`).
- **Visuals**: Concentric resonance rings and an acoustic sine-wave curve pulsing during the humming stage.

### 1.4 Kapalbhati Timer
- **Gentle-First Design**: Emphasizes comfortable, unhurried pacing rather than rapid breathing.
- **Configuration**: Rounds (`1–10`), target gentle exhalations per round (`10–60`), pace presets (`Gentle 1.5s`, `Calm 1.2s`, `Steady 0.9s`), and rest intervals (`10–60s`).
- **Safety**: Prominent contraindication and comfort guidance accessible before and during practice.

### 1.5 Box Breathing (4-4-4-4)
- **Sequence**: `Inhale` (`4s`) → `Hold` (`4s`) → `Exhale` (`4s`) → `Hold Empty` (`4s`).
- **Visuals**: Four-sided square perimeter visualizer with a traveling indicator dot highlighting each active side. Each side duration (`1–20s`) is independently configurable.

### 1.6 Custom Breathing Patterns
- **Builder Capabilities**:
  - Name and describe a pattern
  - Add, remove (minimum 2 stages), rename, and reorder stages
  - Assign stage types (`Inhale`, `Hold`, `Exhale`, `Rest`) and color swatches
  - Configure stage durations (`1–30s`), repetitions (`Continuous` or `1–30`), and pattern-level audio cues
- **Pre-Seeded Starters**: *Restorative 4-4-6-2* and *4-7-8 Evening Calm*.

---

## 2. Audio & Voice Guidance Engine
- **Tone Synthesis**: `PranayamaAudioEngine` generates enveloped sine-wave chimes using Android `AudioTrack` (`MODE_STATIC`) so no external audio assets are required.
- **Tone Presets**: `Soft Chime` (`528 Hz`), `Singing Bowl` (`432 Hz`), `Warm Wood` (`396 Hz`), and `Crystal Bell` (`639 Hz`), plus custom frequency (`280–760 Hz`) and volume sliders.
- **Overlap Guard**: Enforces a minimum cooldown (`450 ms`) between interval beeps and stage transitions, and uses `TextToSpeech.QUEUE_FLUSH` for optional spoken instructions.

---

## 3. Daily Practice Tracker & History
- **Session Recording Rule**: Sessions are saved only when a target-round session completes automatically or when the user taps **Finish & Save** after completing at least 1 cycle or 15 seconds of practice. Accidental resets or quick exits do not pollute history.
- **Metrics Computed (`ProgressCalculator`)**:
  - Today's practice minutes vs. daily goal (`5–30 min`)
  - Current and longest consecutive day streaks
  - 7-day daily activity bar chart
  - 4-week rolling trend chart
  - 30-day consistency heatmap
  - Exercise-wise session and duration breakdown

---

## 4. Settings & Versioned Backup Export
Settings are stored with `schemaVersion = 1` and can be exported as formatted JSON via the Android Share Sheet (`SettingsScreen`):
```json
{
  "schemaVersion": 1,
  "exportedAtMillis": 1763464000000,
  "settings": { "...": "..." },
  "customPatterns": [ "..." ],
  "sessions": [ "..." ]
}
```
Users can also reset all local data (sessions, custom patterns, and preferences) back to initial defaults.
