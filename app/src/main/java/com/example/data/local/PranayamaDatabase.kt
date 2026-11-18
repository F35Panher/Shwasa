package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.BreathingModuleType
import com.example.model.BreathingPattern
import com.example.model.BreathingStage
import com.example.model.NostrilSide
import com.example.model.PracticeSession
import com.example.model.StageType
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "custom_patterns")
data class CustomPatternEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val moduleTypeName: String,
    val stagesJson: String,
    val repetitions: Int,
    val audioCuesEnabled: Boolean,
    val isCustom: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey val id: String,
    val exerciseId: String,
    val exerciseTitle: String,
    val moduleTypeName: String,
    val patternName: String,
    val durationSeconds: Int,
    val completedCycles: Int,
    val completedAtMillis: Long,
    val dateIso: String,
    val wasCompletedExplicitly: Boolean
)

@Dao
interface CustomPatternDao {
    @Query("SELECT * FROM custom_patterns ORDER BY updatedAtMillis DESC")
    fun observePatterns(): Flow<List<CustomPatternEntity>>

    @Query("SELECT * FROM custom_patterns ORDER BY updatedAtMillis DESC")
    suspend fun getAllPatternsOnce(): List<CustomPatternEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPattern(entity: CustomPatternEntity)

    @Query("DELETE FROM custom_patterns WHERE id = :id")
    suspend fun deletePatternById(id: String)

    @Query("DELETE FROM custom_patterns")
    suspend fun clearAll()
}

@Dao
interface PracticeSessionDao {
    @Query("SELECT * FROM practice_sessions ORDER BY completedAtMillis DESC")
    fun observeSessions(): Flow<List<PracticeSessionEntity>>

    @Query("SELECT * FROM practice_sessions ORDER BY completedAtMillis DESC")
    suspend fun getAllSessionsOnce(): List<PracticeSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(entity: PracticeSessionEntity)

    @Query("DELETE FROM practice_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("DELETE FROM practice_sessions")
    suspend fun clearAll()
}

@Database(
    entities = [CustomPatternEntity::class, PracticeSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PranayamaDatabase : RoomDatabase() {
    abstract fun customPatternDao(): CustomPatternDao
    abstract fun practiceSessionDao(): PracticeSessionDao

    companion object {
        @Volatile
        private var INSTANCE: PranayamaDatabase? = null

        fun getInstance(context: Context): PranayamaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PranayamaDatabase::class.java,
                    "pranayama_wellness.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

object PatternJsonMapper {
    fun stagesToJson(stages: List<BreathingStage>): String {
        val arr = JSONArray()
        stages.forEach { s ->
            arr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("label", s.label)
                    put("durationSeconds", s.durationSeconds)
                    put("type", s.type.name)
                    put("colorHex", s.colorHex)
                    put("nostrilSide", s.nostrilSide.name)
                    put("instruction", s.instruction)
                    put("isOptional", s.isOptional)
                    put("isEnabled", s.isEnabled)
                }
            )
        }
        return arr.toString()
    }

    fun jsonToStages(json: String): List<BreathingStage> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { idx ->
                val obj = arr.getJSONObject(idx)
                BreathingStage(
                    id = obj.optString("id", "stage_$idx"),
                    label = obj.optString("label", "Breathe"),
                    durationSeconds = obj.optInt("durationSeconds", 4),
                    type = enumValueOrDefault(obj.optString("type"), StageType.CUSTOM),
                    colorHex = obj.optString("colorHex", "#3E7B6D"),
                    nostrilSide = enumValueOrDefault(obj.optString("nostrilSide"), NostrilSide.NONE),
                    instruction = obj.optString("instruction", ""),
                    isOptional = obj.optBoolean("isOptional", false),
                    isEnabled = obj.optBoolean("isEnabled", true)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun CustomPatternEntity.toDomain(): BreathingPattern = BreathingPattern(
        id = id,
        name = name,
        description = description,
        moduleType = enumValueOrDefault(moduleTypeName, BreathingModuleType.CUSTOM),
        stages = jsonToStages(stagesJson),
        repetitions = repetitions,
        audioCuesEnabled = audioCuesEnabled,
        isCustom = isCustom,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )

    fun BreathingPattern.toEntity(): CustomPatternEntity = CustomPatternEntity(
        id = id,
        name = name,
        description = description,
        moduleTypeName = moduleType.name,
        stagesJson = stagesToJson(stages),
        repetitions = repetitions,
        audioCuesEnabled = audioCuesEnabled,
        isCustom = isCustom,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )

    fun PracticeSessionEntity.toDomain(): PracticeSession = PracticeSession(
        id = id,
        exerciseId = exerciseId,
        exerciseTitle = exerciseTitle,
        moduleType = enumValueOrDefault(moduleTypeName, BreathingModuleType.THREE_STAGE),
        patternName = patternName,
        durationSeconds = durationSeconds,
        completedCycles = completedCycles,
        completedAtMillis = completedAtMillis,
        dateIso = dateIso,
        wasCompletedExplicitly = wasCompletedExplicitly
    )

    fun PracticeSession.toEntity(): PracticeSessionEntity = PracticeSessionEntity(
        id = id,
        exerciseId = exerciseId,
        exerciseTitle = exerciseTitle,
        moduleTypeName = moduleType.name,
        patternName = patternName,
        durationSeconds = durationSeconds,
        completedCycles = completedCycles,
        completedAtMillis = completedAtMillis,
        dateIso = dateIso,
        wasCompletedExplicitly = wasCompletedExplicitly
    )

    inline fun <reified T : Enum<T>> enumValueOrDefault(raw: String?, default: T): T {
        if (raw.isNullOrBlank()) return default
        return enumValues<T>().firstOrNull { it.name == raw } ?: default
    }
}
