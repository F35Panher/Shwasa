package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.PatternJsonMapper.enumValueOrDefault
import com.example.model.BeepTone
import com.example.model.ThemePreference
import com.example.model.TimerDisplayStyle
import com.example.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pranayamaPrefs: DataStore<Preferences> by preferencesDataStore(name = "pranayama_settings_v1")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val SCHEMA_VERSION = intPreferencesKey("schema_version")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val BEEP_TONE = stringPreferencesKey("beep_tone")
        val BEEP_FREQ = intPreferencesKey("beep_freq_hz")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val VOICE_GUIDANCE = booleanPreferencesKey("voice_guidance")
        val TIMER_DISPLAY = stringPreferencesKey("timer_display")
        val THEME_PREF = stringPreferencesKey("theme_pref")
        val DAILY_GOAL_MIN = intPreferencesKey("daily_goal_minutes")
        val DEFAULT_INHALE = intPreferencesKey("default_inhale_sec")
        val DEFAULT_HOLD = intPreferencesKey("default_hold_sec")
        val DEFAULT_EXHALE = intPreferencesKey("default_exhale_sec")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val SEEDED_DEFAULTS = booleanPreferencesKey("seeded_defaults_v1")
    }

    val settingsFlow: Flow<UserSettings> = context.pranayamaPrefs.data.map { prefs ->
        UserSettings(
            schemaVersion = prefs[Keys.SCHEMA_VERSION] ?: 1,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            beepTone = enumValueOrDefault(prefs[Keys.BEEP_TONE], BeepTone.SOFT_CHIME),
            beepFrequencyHz = prefs[Keys.BEEP_FREQ] ?: 528,
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.7f,
            voiceGuidanceEnabled = prefs[Keys.VOICE_GUIDANCE] ?: false,
            timerDisplayStyle = enumValueOrDefault(
                prefs[Keys.TIMER_DISPLAY],
                TimerDisplayStyle.RING_AND_COUNTDOWN
            ),
            themePreference = enumValueOrDefault(prefs[Keys.THEME_PREF], ThemePreference.SYSTEM),
            dailyGoalMinutes = prefs[Keys.DAILY_GOAL_MIN] ?: 10,
            defaultInhaleSeconds = prefs[Keys.DEFAULT_INHALE] ?: 5,
            defaultHoldSeconds = prefs[Keys.DEFAULT_HOLD] ?: 5,
            defaultExhaleSeconds = prefs[Keys.DEFAULT_EXHALE] ?: 5,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false
        )
    }

    val isSeededFlow: Flow<Boolean> = context.pranayamaPrefs.data.map { prefs ->
        prefs[Keys.SEEDED_DEFAULTS] ?: false
    }

    suspend fun markSeeded() {
        context.pranayamaPrefs.edit { it[Keys.SEEDED_DEFAULTS] = true }
    }

    suspend fun updateSettings(transform: (UserSettings) -> UserSettings) {
        context.pranayamaPrefs.edit { prefs ->
            val current = UserSettings(
                schemaVersion = prefs[Keys.SCHEMA_VERSION] ?: 1,
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
                beepTone = enumValueOrDefault(prefs[Keys.BEEP_TONE], BeepTone.SOFT_CHIME),
                beepFrequencyHz = prefs[Keys.BEEP_FREQ] ?: 528,
                soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.7f,
                voiceGuidanceEnabled = prefs[Keys.VOICE_GUIDANCE] ?: false,
                timerDisplayStyle = enumValueOrDefault(
                    prefs[Keys.TIMER_DISPLAY],
                    TimerDisplayStyle.RING_AND_COUNTDOWN
                ),
                themePreference = enumValueOrDefault(prefs[Keys.THEME_PREF], ThemePreference.SYSTEM),
                dailyGoalMinutes = prefs[Keys.DAILY_GOAL_MIN] ?: 10,
                defaultInhaleSeconds = prefs[Keys.DEFAULT_INHALE] ?: 5,
                defaultHoldSeconds = prefs[Keys.DEFAULT_HOLD] ?: 5,
                defaultExhaleSeconds = prefs[Keys.DEFAULT_EXHALE] ?: 5,
                reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false
            )
            val updated = transform(current)
            prefs[Keys.SCHEMA_VERSION] = updated.schemaVersion
            prefs[Keys.SOUND_ENABLED] = updated.soundEnabled
            prefs[Keys.BEEP_TONE] = updated.beepTone.name
            prefs[Keys.BEEP_FREQ] = updated.beepFrequencyHz
            prefs[Keys.SOUND_VOLUME] = updated.soundVolume
            prefs[Keys.VOICE_GUIDANCE] = updated.voiceGuidanceEnabled
            prefs[Keys.TIMER_DISPLAY] = updated.timerDisplayStyle.name
            prefs[Keys.THEME_PREF] = updated.themePreference.name
            prefs[Keys.DAILY_GOAL_MIN] = updated.dailyGoalMinutes
            prefs[Keys.DEFAULT_INHALE] = updated.defaultInhaleSeconds
            prefs[Keys.DEFAULT_HOLD] = updated.defaultHoldSeconds
            prefs[Keys.DEFAULT_EXHALE] = updated.defaultExhaleSeconds
            prefs[Keys.REDUCED_MOTION] = updated.reducedMotion
        }
    }

    suspend fun resetSettings() {
        context.pranayamaPrefs.edit { prefs ->
            prefs.clear()
            prefs[Keys.SEEDED_DEFAULTS] = true
        }
    }
}
