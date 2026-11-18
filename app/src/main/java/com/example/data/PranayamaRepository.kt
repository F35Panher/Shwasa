package com.example.data

import com.example.data.local.CustomPatternDao
import com.example.data.local.PatternJsonMapper
import com.example.data.local.PatternJsonMapper.toDomain
import com.example.data.local.PatternJsonMapper.toEntity
import com.example.data.local.PracticeSessionDao
import com.example.data.local.SettingsDataStore
import com.example.model.BreathingModuleType
import com.example.model.BreathingPattern
import com.example.model.BreathingStage
import com.example.model.DailyProgress
import com.example.model.ExerciseStat
import com.example.model.PracticeSession
import com.example.model.ProgressSummary
import com.example.model.StageType
import com.example.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

interface PranayamaRepository {
    val settingsFlow: Flow<UserSettings>
    val customPatternsFlow: Flow<List<BreathingPattern>>
    val sessionsFlow: Flow<List<PracticeSession>>
    val progressSummaryFlow: Flow<ProgressSummary>

    suspend fun ensureInitialPatternsSeeded()
    suspend fun saveCustomPattern(pattern: BreathingPattern)
    suspend fun deleteCustomPattern(patternId: String)
    suspend fun recordSession(session: PracticeSession)
    suspend fun deleteSession(sessionId: String)
    suspend fun updateSettings(transform: (UserSettings) -> UserSettings)
    suspend fun exportVersionedJson(): String
    suspend fun resetAllData()
}

class LocalPranayamaRepository(
    private val patternDao: CustomPatternDao,
    private val sessionDao: PracticeSessionDao,
    private val settingsStore: SettingsDataStore
) : PranayamaRepository {

    override val settingsFlow: Flow<UserSettings> = settingsStore.settingsFlow

    override val customPatternsFlow: Flow<List<BreathingPattern>> =
        patternDao.observePatterns().map { entities -> entities.map { it.toDomain() } }

    override val sessionsFlow: Flow<List<PracticeSession>> =
        sessionDao.observeSessions().map { entities -> entities.map { it.toDomain() } }

    override val progressSummaryFlow: Flow<ProgressSummary> =
        combine(sessionsFlow, settingsFlow) { sessions, settings ->
            ProgressCalculator.computeSummary(sessions, settings.dailyGoalMinutes)
        }

    override suspend fun ensureInitialPatternsSeeded() {
        val alreadySeeded = settingsStore.isSeededFlow.first()
        if (!alreadySeeded && patternDao.getAllPatternsOnce().isEmpty()) {
            val starterPatterns = listOf(
                ExerciseCatalog.byModuleType(BreathingModuleType.CUSTOM).defaultPattern,
                BreathingPattern(
                    id = "starter_4_7_8",
                    name = "4-7-8 Evening Calm",
                    description = "Classic relaxing breath cadence before sleep",
                    moduleType = BreathingModuleType.CUSTOM,
                    repetitions = 4,
                    isCustom = true,
                    stages = listOf(
                        BreathingStage(
                            id = "s478_in",
                            label = "Inhale",
                            durationSeconds = 4,
                            type = StageType.INHALE,
                            colorHex = "#3E7B6D",
                            instruction = "Inhale quietly through your nose"
                        ),
                        BreathingStage(
                            id = "s478_hold",
                            label = "Hold",
                            durationSeconds = 7,
                            type = StageType.HOLD_IN,
                            colorHex = "#547AA5",
                            instruction = "Hold gently without strain"
                        ),
                        BreathingStage(
                            id = "s478_ex",
                            label = "Exhale",
                            durationSeconds = 8,
                            type = StageType.EXHALE,
                            colorHex = "#8B6D9C",
                            instruction = "Exhale slowly and smoothly"
                        )
                    )
                )
            )
            starterPatterns.forEach { patternDao.upsertPattern(it.toEntity()) }
            settingsStore.markSeeded()
        } else if (!alreadySeeded) {
            settingsStore.markSeeded()
        }
    }

    override suspend fun saveCustomPattern(pattern: BreathingPattern) {
        patternDao.upsertPattern(pattern.copy(updatedAtMillis = System.currentTimeMillis()).toEntity())
    }

    override suspend fun deleteCustomPattern(patternId: String) {
        patternDao.deletePatternById(patternId)
    }

    override suspend fun recordSession(session: PracticeSession) {
        if (session.durationSeconds <= 0) return
        sessionDao.insertSession(session.toEntity())
    }

    override suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteSessionById(sessionId)
    }

    override suspend fun updateSettings(transform: (UserSettings) -> UserSettings) {
        settingsStore.updateSettings(transform)
    }

    override suspend fun exportVersionedJson(): String {
        val settings = settingsStore.settingsFlow.first()
        val patterns = patternDao.getAllPatternsOnce().map { it.toDomain() }
        val sessions = sessionDao.getAllSessionsOnce().map { it.toDomain() }

        val root = JSONObject().apply {
            put("schemaVersion", settings.schemaVersion)
            put("exportedAtMillis", System.currentTimeMillis())
            put("settings", JSONObject().apply {
                put("soundEnabled", settings.soundEnabled)
                put("beepTone", settings.beepTone.name)
                put("beepFrequencyHz", settings.beepFrequencyHz)
                put("soundVolume", settings.soundVolume.toDouble())
                put("voiceGuidanceEnabled", settings.voiceGuidanceEnabled)
                put("timerDisplayStyle", settings.timerDisplayStyle.name)
                put("themePreference", settings.themePreference.name)
                put("dailyGoalMinutes", settings.dailyGoalMinutes)
                put("defaultInhaleSeconds", settings.defaultInhaleSeconds)
                put("defaultHoldSeconds", settings.defaultHoldSeconds)
                put("defaultExhaleSeconds", settings.defaultExhaleSeconds)
                put("reducedMotion", settings.reducedMotion)
            })
            put("customPatterns", JSONArray().apply {
                patterns.forEach { p ->
                    put(JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("description", p.description)
                        put("repetitions", p.repetitions)
                        put("audioCuesEnabled", p.audioCuesEnabled)
                        put("stages", JSONArray(PatternJsonMapper.stagesToJson(p.stages)))
                    })
                }
            })
            put("sessions", JSONArray().apply {
                sessions.forEach { s ->
                    put(JSONObject().apply {
                        put("id", s.id)
                        put("exerciseId", s.exerciseId)
                        put("exerciseTitle", s.exerciseTitle)
                        put("moduleType", s.moduleType.name)
                        put("patternName", s.patternName)
                        put("durationSeconds", s.durationSeconds)
                        put("completedCycles", s.completedCycles)
                        put("completedAtMillis", s.completedAtMillis)
                        put("dateIso", s.dateIso)
                    })
                }
            })
        }
        return root.toString(2)
    }

    override suspend fun resetAllData() {
        sessionDao.clearAll()
        patternDao.clearAll()
        settingsStore.resetSettings()
        ensureInitialPatternsSeeded()
    }
}

object ProgressCalculator {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val shortDayFormat = SimpleDateFormat("EEE", Locale.US)

    fun todayIso(nowMillis: Long = System.currentTimeMillis()): String =
        isoFormat.format(Date(nowMillis))

    fun computeSummary(
        sessions: List<PracticeSession>,
        dailyGoalMinutes: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): ProgressSummary {
        val today = todayIso(nowMillis)
        val byDate = sessions.groupBy { it.dateIso }

        val todayList = byDate[today].orEmpty()
        val todaySeconds = todayList.sumOf { it.durationSeconds }
        val todaySessions = todayList.size
        val todayCycles = todayList.sumOf { it.completedCycles }

        val lifetimeSeconds = sessions.sumOf { it.durationSeconds }
        val totalSessions = sessions.size
        val totalCycles = sessions.sumOf { it.completedCycles }

        // Last 7 days daily chart
        val last7Days = (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            val dateStr = isoFormat.format(cal.time)
            val list = byDate[dateStr].orEmpty()
            DailyProgress(
                dateIso = dateStr,
                totalSeconds = list.sumOf { it.durationSeconds },
                sessionsCount = list.size,
                cyclesCount = list.sumOf { it.completedCycles },
                goalMinutes = dailyGoalMinutes
            )
        }
        val weeklySeconds = last7Days.sumOf { it.totalSeconds }

        // Last 30 days monthly heatmap
        val last30Days = (29 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            val dateStr = isoFormat.format(cal.time)
            val list = byDate[dateStr].orEmpty()
            DailyProgress(
                dateIso = dateStr,
                totalSeconds = list.sumOf { it.durationSeconds },
                sessionsCount = list.size,
                cyclesCount = list.sumOf { it.completedCycles },
                goalMinutes = dailyGoalMinutes
            )
        }
        val monthlySeconds = last30Days.sumOf { it.totalSeconds }

        // Last 4 weeks bar/line chart (in minutes)
        val weeklyPoints = (3 downTo 0).map { weekOffset ->
            var weekSec = 0
            for (d in 0 until 7) {
                val daysBack = weekOffset * 7 + d
                val cal = Calendar.getInstance().apply {
                    timeInMillis = nowMillis
                    add(Calendar.DAY_OF_YEAR, -daysBack)
                }
                val key = isoFormat.format(cal.time)
                weekSec += byDate[key].orEmpty().sumOf { it.durationSeconds }
            }
            val label = when (weekOffset) {
                0 -> "This Wk"
                1 -> "Last Wk"
                else -> "${weekOffset}w ago"
            }
            label to (weekSec / 60)
        }

        // Streaks (any day with >= 1 completed session counts toward streak)
        val activeDates = byDate.filterValues { it.isNotEmpty() }.keys
        val (currentStreak, longestStreak) = calculateStreaks(activeDates, nowMillis)

        // Exercise-wise stats
        val statsByModule = sessions
            .groupBy { it.moduleType }
            .map { (moduleType, list) ->
                ExerciseStat(
                    moduleType = moduleType,
                    title = moduleType.displayName,
                    totalSeconds = list.sumOf { it.durationSeconds },
                    sessionCount = list.size,
                    totalCycles = list.sumOf { it.completedCycles }
                )
            }
            .sortedByDescending { it.totalSeconds }

        return ProgressSummary(
            todaySeconds = todaySeconds,
            todaySessions = todaySessions,
            todayCycles = todayCycles,
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            weeklySeconds = weeklySeconds,
            monthlySeconds = monthlySeconds,
            lifetimeSeconds = lifetimeSeconds,
            totalSessions = totalSessions,
            totalCycles = totalCycles,
            dailyChartPoints = last7Days,
            weeklyChartPoints = weeklyPoints,
            monthlyHeatmap = last30Days,
            exerciseStats = statsByModule
        )
    }

    fun shortWeekdayLabel(dateIso: String): String {
        return try {
            val d = isoFormat.parse(dateIso) ?: return dateIso.takeLast(2)
            shortDayFormat.format(d)
        } catch (_: Exception) {
            dateIso.takeLast(2)
        }
    }

    internal fun calculateStreaks(activeDates: Set<String>, nowMillis: Long): Pair<Int, Int> {
        if (activeDates.isEmpty()) return 0 to 0

        // Current streak: check today, or if not practiced today, check yesterday
        var currentStreak = 0
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val todayStr = isoFormat.format(cal.time)
        if (!activeDates.contains(todayStr)) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        while (activeDates.contains(isoFormat.format(cal.time))) {
            currentStreak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Longest streak across all sorted dates
        val sortedDays = activeDates.mapNotNull {
            try { isoFormat.parse(it)?.time } catch (_: Exception) { null }
        }.sorted()

        var longest = 0
        var run = 0
        var prevDayIndex: Long? = null
        val millisPerDay = 86_400_000L
        sortedDays.forEach { millis ->
            val dayIdx = (millis + 43_200_000L) / millisPerDay
            if (prevDayIndex == null || dayIdx == prevDayIndex!! + 1L) {
                run++
            } else if (dayIdx > prevDayIndex!!) {
                run = 1
            }
            if (run > longest) longest = run
            prevDayIndex = dayIdx
        }
        return currentStreak to maxOf(longest, currentStreak)
    }
}
