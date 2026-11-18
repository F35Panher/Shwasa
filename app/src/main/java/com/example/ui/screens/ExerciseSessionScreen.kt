package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.engine.BreathingTimerState
import com.example.engine.KapalbhatiConfig
import com.example.engine.TimerPhaseStatus
import com.example.model.BreathingExercise
import com.example.model.BreathingModuleType
import com.example.model.BreathingPattern
import com.example.model.PracticeSession
import com.example.model.StageType
import com.example.model.UserSettings
import com.example.ui.components.BreathingVisualizer
import com.example.ui.components.ExerciseGuideBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSessionScreen(
    exercise: BreathingExercise,
    timerState: BreathingTimerState,
    settings: UserSettings,
    completedSessionDialog: PracticeSession?,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onFinishAndSave: () -> Unit,
    onUpdatePatternConfig: (BreathingPattern, KapalbhatiConfig) -> Unit,
    onToggleSound: () -> Unit,
    onToggleVoice: () -> Unit,
    onDismissCompletionDialog: () -> Unit
) {
    BackHandler {
        onReset()
        onBack()
    }

    var showGuideSheet by remember { mutableStateOf(false) }
    var showConfigPanel by remember { mutableStateOf(false) }
    val pattern = timerState.pattern ?: exercise.defaultPattern

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = exercise.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1
                        )
                        Text(
                            text = pattern.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onReset()
                            onBack()
                        },
                        modifier = Modifier.testTag("session_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier.testTag("session_sound_toggle")
                    ) {
                        Icon(
                            imageVector = if (settings.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (settings.soundEnabled) "Mute sound cues" else "Enable sound cues"
                        )
                    }
                    IconButton(
                        onClick = onToggleVoice,
                        modifier = Modifier.testTag("session_voice_toggle")
                    ) {
                        Icon(
                            imageVector = if (settings.voiceGuidanceEnabled) Icons.Filled.RecordVoiceOver else Icons.Filled.VoiceOverOff,
                            contentDescription = if (settings.voiceGuidanceEnabled) "Disable voice instructions" else "Enable voice instructions"
                        )
                    }
                    IconButton(
                        onClick = { showGuideSheet = true },
                        modifier = Modifier.testTag("session_info_button")
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = "Technique & Safety Guide")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pre-start intro banner
                AnimatedVisibility(visible = timerState.status == TimerPhaseStatus.IDLE) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HealthAndSafety,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = exercise.safetyNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { showGuideSheet = true }) {
                                Text("Guide")
                            }
                        }
                    }
                }

                // Live Stats Row (Elapsed Time & Completed Cycles)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatPillCard(
                        label = "Elapsed",
                        value = formatMinutesSeconds(timerState.totalElapsedSeconds),
                        modifier = Modifier.weight(1f)
                    )
                    StatPillCard(
                        label = if (exercise.moduleType == BreathingModuleType.ANULOM_VILOM ||
                            exercise.moduleType == BreathingModuleType.BHRAMARI ||
                            exercise.moduleType == BreathingModuleType.KAPALBHATI
                        ) "Rounds" else "Cycles",
                        value = if (timerState.targetCycles > 0) {
                            "${timerState.completedCycles} / ${timerState.targetCycles}"
                        } else {
                            "${timerState.completedCycles}"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Visual Breathing Clock / Nostril / Square / Sound Wave / Kapalbhati Counter
                BreathingVisualizer(
                    moduleType = exercise.moduleType,
                    timerState = timerState,
                    displayStyle = settings.timerDisplayStyle,
                    reducedMotion = settings.reducedMotion
                )

                // Current Instruction Text
                Text(
                    text = timerState.currentStage?.instruction
                        ?: "Settle into a comfortable posture and tap Start.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Primary Timer Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (timerState.status) {
                        TimerPhaseStatus.IDLE, TimerPhaseStatus.COMPLETED -> {
                            Button(
                                onClick = onStart,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("timer_start_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (timerState.status == TimerPhaseStatus.COMPLETED) "Practice Again" else "Start Practice",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        TimerPhaseStatus.RUNNING -> {
                            Button(
                                onClick = onPause,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("timer_pause_button")
                            ) {
                                Icon(Icons.Filled.Pause, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Pause", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        TimerPhaseStatus.PAUSED -> {
                            Button(
                                onClick = onResume,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("timer_resume_button")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Resume", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onReset,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("timer_reset_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset timer")
                        Spacer(Modifier.width(6.dp))
                        Text("Reset")
                    }
                }

                // Explicit Finish & Save button (only enabled after real practice >= 1 cycle or >= 15s)
                if (timerState.canSaveSession && timerState.status != TimerPhaseStatus.COMPLETED) {
                    OutlinedButton(
                        onClick = onFinishAndSave,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("timer_finish_save_button")
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Finish & Save Session")
                    }
                }

                // Configuration Toggle & Panel (Available when IDLE or PAUSED)
                OutlinedButton(
                    onClick = { showConfigPanel = !showConfigPanel },
                    shape = RoundedCornerShape(16.dp),
                    enabled = timerState.status == TimerPhaseStatus.IDLE,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("session_configure_toggle")
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (timerState.status == TimerPhaseStatus.IDLE) {
                            if (showConfigPanel) "Hide Duration & Round Settings" else "Configure Durations & Rounds"
                        } else {
                            "Reset timer to edit durations"
                        }
                    )
                }

                AnimatedVisibility(visible = showConfigPanel && timerState.status == TimerPhaseStatus.IDLE) {
                    ModuleConfigCard(
                        moduleType = exercise.moduleType,
                        pattern = pattern,
                        kapalbhatiConfig = timerState.kapalbhatiConfig,
                        onUpdate = onUpdatePatternConfig
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showGuideSheet) {
        ExerciseGuideBottomSheet(
            exercise = exercise,
            onDismiss = { showGuideSheet = false }
        )
    }

    completedSessionDialog?.let { session ->
        AlertDialog(
            onDismissRequest = onDismissCompletionDialog,
            icon = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Session Saved", style = MaterialTheme.typography.headlineMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${session.exerciseTitle} (${session.patternName})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Duration: ${formatMinutesSeconds(session.durationSeconds)} • Completed Cycles: ${session.completedCycles}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Take a quiet moment to notice how your breath feels before moving on.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = onDismissCompletionDialog) {
                    Text("Continue")
                }
            }
        )
    }
}

@Composable
private fun StatPillCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ModuleConfigCard(
    moduleType: BreathingModuleType,
    pattern: BreathingPattern,
    kapalbhatiConfig: KapalbhatiConfig,
    onUpdate: (BreathingPattern, KapalbhatiConfig) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Session Configuration",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            when (moduleType) {
                BreathingModuleType.THREE_STAGE -> {
                    pattern.stages.forEachIndexed { idx, stage ->
                        DurationStepperRow(
                            label = stage.label,
                            value = stage.durationSeconds,
                            min = if (stage.type == StageType.HOLD_IN) 0 else 1,
                            max = 30,
                            unit = "s",
                            onValueChange = { newDur ->
                                val updatedStages = pattern.stages.toMutableList()
                                updatedStages[idx] = stage.copy(
                                    durationSeconds = newDur,
                                    isEnabled = newDur > 0
                                )
                                onUpdate(pattern.copy(stages = updatedStages), kapalbhatiConfig)
                            }
                        )
                    }
                    RepetitionsStepperRow(
                        repetitions = pattern.repetitions,
                        onChange = { reps -> onUpdate(pattern.copy(repetitions = reps), kapalbhatiConfig) }
                    )
                }

                BreathingModuleType.ANULOM_VILOM -> {
                    val inhaleSec = pattern.stages.firstOrNull { it.type == StageType.INHALE }?.durationSeconds ?: 4
                    val exhaleSec = pattern.stages.firstOrNull { it.type == StageType.EXHALE }?.durationSeconds ?: 4
                    val retentionEnabled = pattern.stages.any { it.isOptional && it.isEnabled }
                    val retentionSec = pattern.stages.firstOrNull { it.isOptional }?.durationSeconds ?: 4

                    DurationStepperRow(
                        label = "Inhale Duration",
                        value = inhaleSec,
                        min = 2,
                        max = 15,
                        unit = "s",
                        onValueChange = { newVal ->
                            val updated = pattern.stages.map {
                                if (it.type == StageType.INHALE) it.copy(durationSeconds = newVal) else it
                            }
                            onUpdate(pattern.copy(stages = updated), kapalbhatiConfig)
                        }
                    )
                    DurationStepperRow(
                        label = "Exhale Duration",
                        value = exhaleSec,
                        min = 2,
                        max = 20,
                        unit = "s",
                        onValueChange = { newVal ->
                            val updated = pattern.stages.map {
                                if (it.type == StageType.EXHALE) it.copy(durationSeconds = newVal) else it
                            }
                            onUpdate(pattern.copy(stages = updated), kapalbhatiConfig)
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Optional Breath Retention", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Off by default for comfortable practice",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = retentionEnabled,
                            onCheckedChange = { enabled ->
                                val updated = pattern.stages.map {
                                    if (it.isOptional) it.copy(isEnabled = enabled) else it
                                }
                                onUpdate(pattern.copy(stages = updated), kapalbhatiConfig)
                            }
                        )
                    }
                    if (retentionEnabled) {
                        DurationStepperRow(
                            label = "Retention Duration",
                            value = retentionSec,
                            min = 1,
                            max = 15,
                            unit = "s",
                            onValueChange = { newVal ->
                                val updated = pattern.stages.map {
                                    if (it.isOptional) it.copy(durationSeconds = newVal) else it
                                }
                                onUpdate(pattern.copy(stages = updated), kapalbhatiConfig)
                            }
                        )
                    }
                    RepetitionsStepperRow(
                        repetitions = pattern.repetitions,
                        allowContinuous = true,
                        onChange = { reps -> onUpdate(pattern.copy(repetitions = reps), kapalbhatiConfig) }
                    )
                }

                BreathingModuleType.BHRAMARI -> {
                    val inhaleSec = pattern.stages.getOrNull(0)?.durationSeconds ?: 4
                    val humSec = pattern.stages.getOrNull(1)?.durationSeconds ?: 8
                    DurationStepperRow(
                        label = "Inhale Duration",
                        value = inhaleSec,
                        min = 2,
                        max = 12,
                        unit = "s",
                        onValueChange = { newVal ->
                            val updated = pattern.stages.mapIndexed { i, st ->
                                if (i == 0) st.copy(durationSeconds = newVal) else st
                            }
                            onUpdate(pattern.copy(stages = updated), kapalbhatiConfig)
                        }
                    )
                    DurationStepperRow(
                        label = "Humming Duration",
                        value = humSec,
                        min = 3,
                        max = 20,
                        unit = "s",
                        onValueChange = { newVal ->
                            val updated = pattern.stages.mapIndexed { i, st ->
                                if (i == 1) st.copy(durationSeconds = newVal) else st
                            }
                            onUpdate(pattern.copy(stages = updated), kapalbhatiConfig)
                        }
                    )
                    RepetitionsStepperRow(
                        repetitions = pattern.repetitions,
                        onChange = { reps -> onUpdate(pattern.copy(repetitions = reps), kapalbhatiConfig) }
                    )
                }

                BreathingModuleType.KAPALBHATI -> {
                    DurationStepperRow(
                        label = "Rounds",
                        value = kapalbhatiConfig.rounds,
                        min = 1,
                        max = 10,
                        unit = "",
                        onValueChange = { rounds ->
                            onUpdate(pattern, kapalbhatiConfig.copy(rounds = rounds))
                        }
                    )
                    DurationStepperRow(
                        label = "Gentle Exhalations / Round",
                        value = kapalbhatiConfig.targetExhalationsPerRound,
                        min = 10,
                        max = 60,
                        step = 5,
                        unit = "",
                        onValueChange = { count ->
                            onUpdate(pattern, kapalbhatiConfig.copy(targetExhalationsPerRound = count))
                        }
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Comfortable Pace",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "Gentle (1.5s)" to 1500L,
                                "Calm (1.2s)" to 1200L,
                                "Steady (0.9s)" to 900L
                            ).forEach { (label, ms) ->
                                FilterChip(
                                    selected = kapalbhatiConfig.paceMillisPerExhalation == ms,
                                    onClick = {
                                        onUpdate(pattern, kapalbhatiConfig.copy(paceMillisPerExhalation = ms))
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                    DurationStepperRow(
                        label = "Rest Between Rounds",
                        value = kapalbhatiConfig.restSecondsBetweenRounds,
                        min = 10,
                        max = 60,
                        step = 5,
                        unit = "s",
                        onValueChange = { rest ->
                            onUpdate(pattern, kapalbhatiConfig.copy(restSecondsBetweenRounds = rest))
                        }
                    )
                }

                BreathingModuleType.BOX_BREATHING -> {
                    val labels = listOf("Inhale (Top)", "Hold (Right)", "Exhale (Bottom)", "Hold Empty (Left)")
                    pattern.stages.forEachIndexed { idx, stage ->
                        DurationStepperRow(
                            label = labels.getOrElse(idx) { stage.label },
                            value = stage.durationSeconds,
                            min = 1,
                            max = 20,
                            unit = "s",
                            onValueChange = { newDur ->
                                val updatedStages = pattern.stages.toMutableList()
                                updatedStages[idx] = stage.copy(durationSeconds = newDur)
                                onUpdate(pattern.copy(stages = updatedStages), kapalbhatiConfig)
                            }
                        )
                    }
                    RepetitionsStepperRow(
                        repetitions = pattern.repetitions,
                        onChange = { reps -> onUpdate(pattern.copy(repetitions = reps), kapalbhatiConfig) }
                    )
                }

                BreathingModuleType.CUSTOM -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Audio Cues for Pattern", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = pattern.audioCuesEnabled,
                            onCheckedChange = { enabled ->
                                onUpdate(pattern.copy(audioCuesEnabled = enabled), kapalbhatiConfig)
                            }
                        )
                    }
                    RepetitionsStepperRow(
                        repetitions = pattern.repetitions,
                        onChange = { reps -> onUpdate(pattern.copy(repetitions = reps), kapalbhatiConfig) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationStepperRow(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    unit: String,
    step: Int = 1,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onValueChange((value - step).coerceAtLeast(min)) },
                enabled = value > min
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease $label")
            }
            Text(
                text = "$value$unit",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.widthIn(min = 44.dp),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { onValueChange((value + step).coerceAtMost(max)) },
                enabled = value < max
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Increase $label")
            }
        }
    }
}

@Composable
private fun RepetitionsStepperRow(
    repetitions: Int,
    allowContinuous: Boolean = true,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Cycles / Rounds",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    val minVal = if (allowContinuous) 0 else 1
                    onChange((repetitions - 1).coerceAtLeast(minVal))
                },
                enabled = repetitions > if (allowContinuous) 0 else 1
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease cycles")
            }
            Text(
                text = if (repetitions == 0) "Continuous" else "$repetitions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.widthIn(min = 78.dp),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { onChange((repetitions + 1).coerceAtMost(30)) },
                enabled = repetitions < 30
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Increase cycles")
            }
        }
    }
}

fun formatMinutesSeconds(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%02d:%02d".format(mins, secs)
}
