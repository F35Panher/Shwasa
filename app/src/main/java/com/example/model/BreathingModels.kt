package com.example.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class StageType {
    INHALE,
    HOLD_IN,
    EXHALE,
    HOLD_OUT,
    HUMMING,
    KAPALBHATI_ACTIVE,
    REST,
    CUSTOM
}

enum class NostrilSide {
    NONE,
    LEFT,
    RIGHT,
    BOTH
}

enum class BreathingModuleType(val displayName: String, val shortName: String) {
    THREE_STAGE("Three-Stage Breathing Clock", "3-Stage"),
    ANULOM_VILOM("Guided Anulom Vilom", "Anulom Vilom"),
    BHRAMARI("Bhramari Pranayama", "Bhramari"),
    KAPALBHATI("Kapalbhati Timer", "Kapalbhati"),
    BOX_BREATHING("Box Breathing (4-4-4-4)", "Box Breath"),
    CUSTOM("Custom Breathing Pattern", "Custom")
}

data class BreathingStage(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val durationSeconds: Int,
    val type: StageType,
    val colorHex: String = "#3E7B6D",
    val nostrilSide: NostrilSide = NostrilSide.NONE,
    val instruction: String = "",
    val isOptional: Boolean = false,
    val isEnabled: Boolean = true
)

data class BreathingPattern(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val moduleType: BreathingModuleType,
    val stages: List<BreathingStage>,
    val repetitions: Int = 0, // 0 = continuous
    val audioCuesEnabled: Boolean = true,
    val isCustom: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val activeStages: List<BreathingStage>
        get() = stages.filter { it.isEnabled && it.durationSeconds > 0 }

    val cycleDurationSeconds: Int
        get() = activeStages.sumOf { it.durationSeconds }
}

data class BreathingExercise(
    val id: String,
    val moduleType: BreathingModuleType,
    val title: String,
    val subtitle: String,
    val focusTag: String,
    val difficultyTag: String,
    val description: String,
    val stepByStepGuide: List<String>,
    val benefits: List<String>,
    val safetyNote: String,
    val defaultPattern: BreathingPattern,
    val accentHex: String
)

data class PracticeSession(
    val id: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val exerciseTitle: String,
    val moduleType: BreathingModuleType,
    val patternName: String,
    val durationSeconds: Int,
    val completedCycles: Int,
    val completedAtMillis: Long,
    val dateIso: String, // yyyy-MM-dd
    val wasCompletedExplicitly: Boolean = true
)

data class DailyProgress(
    val dateIso: String,
    val totalSeconds: Int,
    val sessionsCount: Int,
    val cyclesCount: Int,
    val goalMinutes: Int
) {
    val totalMinutes: Int get() = totalSeconds / 60
    val goalMet: Boolean get() = totalSeconds >= goalMinutes * 60
}

enum class BeepTone(val label: String, val baseFrequencyHz: Int) {
    SOFT_CHIME("Soft Chime", 528),
    SINGING_BOWL("Singing Bowl", 432),
    WOOD_BLOCK("Warm Wood", 396),
    GENTLE_BELL("Crystal Bell", 639)
}

enum class TimerDisplayStyle(val label: String) {
    RING_AND_COUNTDOWN("Visual Ring & Timer"),
    BREATH_GUIDE_FULL("Guided Cue Focus"),
    MINIMAL_NUMBERS("Minimalist Numbers")
}

enum class ThemePreference(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

data class UserSettings(
    val schemaVersion: Int = 1,
    val soundEnabled: Boolean = true,
    val beepTone: BeepTone = BeepTone.SOFT_CHIME,
    val beepFrequencyHz: Int = 528,
    val soundVolume: Float = 0.7f,
    val voiceGuidanceEnabled: Boolean = false,
    val timerDisplayStyle: TimerDisplayStyle = TimerDisplayStyle.RING_AND_COUNTDOWN,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val dailyGoalMinutes: Int = 10,
    val defaultInhaleSeconds: Int = 5,
    val defaultHoldSeconds: Int = 5,
    val defaultExhaleSeconds: Int = 5,
    val reducedMotion: Boolean = false
)

data class ExerciseStat(
    val moduleType: BreathingModuleType,
    val title: String,
    val totalSeconds: Int,
    val sessionCount: Int,
    val totalCycles: Int
)

data class ProgressSummary(
    val todaySeconds: Int = 0,
    val todaySessions: Int = 0,
    val todayCycles: Int = 0,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val weeklySeconds: Int = 0,
    val monthlySeconds: Int = 0,
    val lifetimeSeconds: Int = 0,
    val totalSessions: Int = 0,
    val totalCycles: Int = 0,
    val dailyChartPoints: List<DailyProgress> = emptyList(),
    val weeklyChartPoints: List<Pair<String, Int>> = emptyList(),
    val monthlyHeatmap: List<DailyProgress> = emptyList(),
    val exerciseStats: List<ExerciseStat> = emptyList()
)

fun parseHexColor(hex: String, fallback: Color = Color(0xFF3E7B6D)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val value = clean.toLong(16)
        when (clean.length) {
            6 -> Color((0xFF000000L or value).toInt())
            8 -> Color(value.toInt())
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}
