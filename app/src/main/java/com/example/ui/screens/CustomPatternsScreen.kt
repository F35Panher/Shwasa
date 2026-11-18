package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BreathingModuleType
import com.example.model.BreathingPattern
import com.example.model.BreathingStage
import com.example.model.StageType
import com.example.model.parseHexColor
import com.example.ui.theme.StageSwatchHexes
import java.util.UUID

@Composable
fun CustomPatternsScreen(
    patterns: List<BreathingPattern>,
    onStartPattern: (BreathingPattern) -> Unit,
    onSavePattern: (BreathingPattern) -> Unit,
    onDeletePattern: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingPattern by remember { mutableStateOf<BreathingPattern?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingPattern = BreathingPattern(
                        id = UUID.randomUUID().toString(),
                        name = "",
                        description = "",
                        moduleType = BreathingModuleType.CUSTOM,
                        repetitions = 6,
                        audioCuesEnabled = true,
                        isCustom = true,
                        stages = listOf(
                            BreathingStage(label = "Inhale", durationSeconds = 4, type = StageType.INHALE, colorHex = "#3E7B6D"),
                            BreathingStage(label = "Hold", durationSeconds = 4, type = StageType.HOLD_IN, colorHex = "#547AA5"),
                            BreathingStage(label = "Exhale", durationSeconds = 6, type = StageType.EXHALE, colorHex = "#8B6D9C")
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("create_custom_pattern_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Custom Pattern")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "My Custom Patterns",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Design personalized breathing ratios with custom stages, colors, and cues.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (patterns.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("No custom patterns yet", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "Tap + to build a custom pattern like 4-4-6-2 or 4-7-8.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(patterns, key = { it.id }) { pattern ->
                        CustomPatternCard(
                            pattern = pattern,
                            onStart = { onStartPattern(pattern) },
                            onEdit = { editingPattern = pattern },
                            onDelete = { onDeletePattern(pattern.id) }
                        )
                    }
                }

                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    editingPattern?.let { draft ->
        PatternBuilderSheet(
            initialPattern = draft,
            onDismiss = { editingPattern = null },
            onSave = { saved ->
                onSavePattern(saved)
                editingPattern = null
            }
        )
    }
}

@Composable
private fun CustomPatternCard(
    pattern: BreathingPattern,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pattern.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (pattern.description.isNotBlank()) {
                        Text(
                            text = pattern.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${pattern.cycleDurationSeconds}s cycle • " +
                            if (pattern.repetitions == 0) "Continuous" else "${pattern.repetitions} rounds",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit ${pattern.name}")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete ${pattern.name}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Stage color chips preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pattern.stages.forEach { st ->
                    val col = parseHexColor(st.colorHex, MaterialTheme.colorScheme.primary)
                    Surface(
                        color = col.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${st.label} ${st.durationSeconds}s",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = col,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onStart,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_custom_${pattern.id}")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Start Pattern")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatternBuilderSheet(
    initialPattern: BreathingPattern,
    onDismiss: () -> Unit,
    onSave: (BreathingPattern) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(initialPattern.name) }
    var description by remember { mutableStateOf(initialPattern.description) }
    var repetitions by remember { mutableIntStateOf(initialPattern.repetitions) }
    var audioCues by remember { mutableStateOf(initialPattern.audioCuesEnabled) }
    var stages by remember { mutableStateOf(initialPattern.stages) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (initialPattern.name.isBlank()) "Create Breathing Pattern" else "Edit Breathing Pattern",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Pattern Name") },
                placeholder = { Text("e.g., Grounding 4-4-6-2") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pattern_name_input")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Audio Cues", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = audioCues, onCheckedChange = { audioCues = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Repetitions (0 = Continuous)", style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { repetitions = (repetitions - 1).coerceAtLeast(0) }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease repetitions")
                    }
                    Text(
                        text = if (repetitions == 0) "∞" else "$repetitions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { repetitions = (repetitions + 1).coerceAtMost(30) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase repetitions")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stages (${stages.sumOf { it.durationSeconds }}s per cycle)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                OutlinedButton(
                    onClick = {
                        stages = stages + BreathingStage(
                            label = "Breathe",
                            durationSeconds = 4,
                            type = StageType.CUSTOM,
                            colorHex = StageSwatchHexes[stages.size % StageSwatchHexes.size]
                        )
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Stage")
                }
            }

            stages.forEachIndexed { idx, stage ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = stage.label,
                                onValueChange = { newLabel ->
                                    stages = stages.toMutableList().also {
                                        it[idx] = stage.copy(label = newLabel)
                                    }
                                },
                                label = { Text("Stage ${idx + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (idx > 0) {
                                        val mutable = stages.toMutableList()
                                        val item = mutable.removeAt(idx)
                                        mutable.add(idx - 1, item)
                                        stages = mutable
                                    }
                                },
                                enabled = idx > 0
                            ) {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up")
                            }
                            IconButton(
                                onClick = {
                                    if (idx < stages.lastIndex) {
                                        val mutable = stages.toMutableList()
                                        val item = mutable.removeAt(idx)
                                        mutable.add(idx + 1, item)
                                        stages = mutable
                                    }
                                },
                                enabled = idx < stages.lastIndex
                            ) {
                                Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down")
                            }
                            IconButton(
                                onClick = {
                                    if (stages.size > 2) {
                                        stages = stages.toMutableList().also { it.removeAt(idx) }
                                    }
                                },
                                enabled = stages.size > 2
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove stage")
                            }
                        }

                        // Stage Type & Duration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    "Inhale" to StageType.INHALE,
                                    "Hold" to StageType.HOLD_IN,
                                    "Exhale" to StageType.EXHALE,
                                    "Rest" to StageType.HOLD_OUT
                                ).forEach { (chipLabel, stType) ->
                                    FilterChip(
                                        selected = stage.type == stType,
                                        onClick = {
                                            stages = stages.toMutableList().also {
                                                it[idx] = stage.copy(type = stType)
                                            }
                                        },
                                        label = { Text(chipLabel, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color Swatches
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StageSwatchHexes.forEach { hex ->
                                    val swatchColor = parseHexColor(hex)
                                    val selected = stage.colorHex.equals(hex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                            .then(
                                                if (selected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                                else Modifier
                                            )
                                            .clickable {
                                                stages = stages.toMutableList().also {
                                                    it[idx] = stage.copy(colorHex = hex)
                                                }
                                            }
                                    )
                                }
                            }

                            // Duration Stepper
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        stages = stages.toMutableList().also {
                                            it[idx] = stage.copy(durationSeconds = (stage.durationSeconds - 1).coerceAtLeast(1))
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease seconds")
                                }
                                Text("${stage.durationSeconds}s", style = MaterialTheme.typography.titleMedium)
                                IconButton(
                                    onClick = {
                                        stages = stages.toMutableList().also {
                                            it[idx] = stage.copy(durationSeconds = (stage.durationSeconds + 1).coerceAtMost(30))
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase seconds")
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val cleanName = name.trim().ifBlank { "Custom Breath ${stages.joinToString("-") { it.durationSeconds.toString() }}" }
                    onSave(
                        initialPattern.copy(
                            name = cleanName,
                            description = description.trim(),
                            repetitions = repetitions,
                            audioCuesEnabled = audioCues,
                            stages = stages.map { st ->
                                st.copy(
                                    label = st.label.trim().ifBlank { "Breathe" },
                                    instruction = "${st.label.trim().ifBlank { "Breathe" }} for ${st.durationSeconds} seconds"
                                )
                            }
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_custom_pattern_button")
            ) {
                Text("Save Custom Pattern")
            }
        }
    }
}
