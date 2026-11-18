package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BeepTone
import com.example.model.ThemePreference
import com.example.model.TimerDisplayStyle
import com.example.model.UserSettings
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onUpdateSettings: ((UserSettings) -> UserSettings) -> Unit,
    onSetBeepTone: (BeepTone) -> Unit,
    onPreviewBeep: () -> Unit,
    onSetTheme: (ThemePreference) -> Unit,
    onSetTimerDisplay: (TimerDisplayStyle) -> Unit,
    onExportJson: suspend () -> String,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exportedJson by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 880.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Personalize audio cues, voice guidance, theme, and daily goals.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Audio & Voice Section
            item {
                SettingsSectionCard(title = "Audio & Voice Guidance") {
                    SettingSwitchRow(
                        title = "Sound Cues",
                        subtitle = "Soft 5-second interval chimes and stage transitions",
                        checked = settings.soundEnabled,
                        onCheckedChange = { enabled -> onUpdateSettings { it.copy(soundEnabled = enabled) } }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Beep Tone", style = MaterialTheme.typography.bodyLarge)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BeepTone.entries.forEach { tone ->
                                FilterChip(
                                    selected = settings.beepTone == tone,
                                    onClick = { onSetBeepTone(tone) },
                                    label = { Text(tone.label) }
                                )
                            }
                        }
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Beep Frequency", style = MaterialTheme.typography.bodyLarge)
                            Text("${settings.beepFrequencyHz} Hz", style = MaterialTheme.typography.labelLarge)
                        }
                        Slider(
                            value = settings.beepFrequencyHz.toFloat(),
                            onValueChange = { hz -> onUpdateSettings { it.copy(beepFrequencyHz = hz.roundToInt()) } },
                            valueRange = 280f..760f
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sound Volume", style = MaterialTheme.typography.bodyLarge)
                            Text("${(settings.soundVolume * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
                        }
                        Slider(
                            value = settings.soundVolume,
                            onValueChange = { vol -> onUpdateSettings { it.copy(soundVolume = vol) } },
                            valueRange = 0.05f..1f
                        )
                    }

                    OutlinedButton(
                        onClick = onPreviewBeep,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Preview Tone")
                    }

                    SettingSwitchRow(
                        title = "Voice Guidance",
                        subtitle = "Spoken stage instructions via text-to-speech",
                        checked = settings.voiceGuidanceEnabled,
                        onCheckedChange = { enabled -> onUpdateSettings { it.copy(voiceGuidanceEnabled = enabled) } }
                    )
                }
            }

            // Appearance & Accessibility
            item {
                SettingsSectionCard(title = "Appearance & Accessibility") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Theme", style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemePreference.entries.forEach { pref ->
                                FilterChip(
                                    selected = settings.themePreference == pref,
                                    onClick = { onSetTheme(pref) },
                                    label = { Text(pref.label) },
                                    modifier = Modifier.testTag("theme_chip_${pref.name.lowercase()}")
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Preferred Timer Display", style = MaterialTheme.typography.bodyLarge)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimerDisplayStyle.entries.forEach { style ->
                                FilterChip(
                                    selected = settings.timerDisplayStyle == style,
                                    onClick = { onSetTimerDisplay(style) },
                                    label = { Text(style.label) }
                                )
                            }
                        }
                    }

                    SettingSwitchRow(
                        title = "Reduced Motion",
                        subtitle = "Minimize expanding/pulsing animation transitions",
                        checked = settings.reducedMotion,
                        onCheckedChange = { reduced -> onUpdateSettings { it.copy(reducedMotion = reduced) } }
                    )
                }
            }

            // Practice Goal & Default Durations
            item {
                SettingsSectionCard(title = "Daily Goal & Default Durations") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Daily Practice Goal (${settings.dailyGoalMinutes} min)", style = MaterialTheme.typography.bodyLarge)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 20, 30).forEach { mins ->
                                FilterChip(
                                    selected = settings.dailyGoalMinutes == mins,
                                    onClick = { onUpdateSettings { it.copy(dailyGoalMinutes = mins) } },
                                    label = { Text("${mins}m") }
                                )
                            }
                        }
                    }

                    SettingDurationRow(
                        label = "Default Inhale (3-Stage)",
                        seconds = settings.defaultInhaleSeconds,
                        min = 2,
                        onChange = { s -> onUpdateSettings { it.copy(defaultInhaleSeconds = s) } }
                    )
                    SettingDurationRow(
                        label = "Default Hold (3-Stage)",
                        seconds = settings.defaultHoldSeconds,
                        min = 0,
                        onChange = { s -> onUpdateSettings { it.copy(defaultHoldSeconds = s) } }
                    )
                    SettingDurationRow(
                        label = "Default Exhale (3-Stage)",
                        seconds = settings.defaultExhaleSeconds,
                        min = 2,
                        onChange = { s -> onUpdateSettings { it.copy(defaultExhaleSeconds = s) } }
                    )
                }
            }

            // Data Export & Reset
            item {
                SettingsSectionCard(title = "Data & Storage (Schema v${settings.schemaVersion})") {
                    Text(
                        text = "Your patterns, sessions, and settings are stored locally in a versioned database ready for export or future cloud sync.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    exportedJson = onExportJson()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("settings_export_button")
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export JSON")
                        }
                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("settings_reset_button")
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reset Data")
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    exportedJson?.let { jsonText ->
        AlertDialog(
            onDismissRequest = { exportedJson = null },
            title = { Text("Exported Shwasa Data") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = jsonText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_SUBJECT, "Shwasa Wellness Backup")
                            putExtra(Intent.EXTRA_TEXT, jsonText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Shwasa Backup"))
                        exportedJson = null
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { exportedJson = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Local Data?") },
            text = {
                Text("This clears your practice history, streaks, custom breathing patterns, and restores default settings.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingDurationRow(
    label: String,
    seconds: Int,
    min: Int,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange((seconds - 1).coerceAtLeast(min)) }, enabled = seconds > min) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease $label")
            }
            Text("${seconds}s", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { onChange((seconds + 1).coerceAtMost(20)) }, enabled = seconds < 20) {
                Icon(Icons.Filled.Add, contentDescription = "Increase $label")
            }
        }
    }
}
