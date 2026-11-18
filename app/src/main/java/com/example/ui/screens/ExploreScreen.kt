package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ExerciseCatalog
import com.example.model.BreathingExercise
import com.example.model.BreathingModuleType
import com.example.model.parseHexColor
import com.example.ui.components.ExerciseGuideBottomSheet

@Composable
fun ExploreScreen(
    onStartExercise: (BreathingModuleType) -> Unit,
    onOpenCustomPatterns: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("All") }
    var guideExercise by remember { mutableStateOf<BreathingExercise?>(null) }

    val tags = listOf("All", "Calm", "Balance", "Focus", "Energy", "Personalized")

    val filtered = remember(searchQuery, selectedTag) {
        ExerciseCatalog.allExercises.filter { ex ->
            val matchesQuery = searchQuery.isBlank() ||
                ex.title.contains(searchQuery, ignoreCase = true) ||
                ex.description.contains(searchQuery, ignoreCase = true) ||
                ex.focusTag.contains(searchQuery, ignoreCase = true)
            val matchesTag = selectedTag == "All" ||
                ex.focusTag.contains(selectedTag, ignoreCase = true)
            matchesQuery && matchesTag
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
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
                        text = "Explore Exercises",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Choose a breathwork technique tailored to your state of mind.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search pranayama techniques...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_input")
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { selectedTag = tag },
                            label = { Text(tag) }
                        )
                    }
                }
            }

            items(filtered, key = { it.id }) { exercise ->
                val accent = parseHexColor(exercise.accentHex, MaterialTheme.colorScheme.primary)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = accent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = moduleIcon(exercise.moduleType),
                                        contentDescription = null,
                                        tint = accent
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${exercise.focusTag} • ${exercise.difficultyTag}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent
                                )
                            }
                        }

                        Text(
                            text = exercise.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            exercise.benefits.take(2).forEach { benefit ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = benefit,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { guideExercise = exercise },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Guide")
                            }
                            Button(
                                onClick = {
                                    if (exercise.moduleType == BreathingModuleType.CUSTOM) {
                                        onOpenCustomPatterns()
                                    } else {
                                        onStartExercise(exercise.moduleType)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("explore_start_${exercise.id}")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (exercise.moduleType == BreathingModuleType.CUSTOM) "Open Builder" else "Practice",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    guideExercise?.let { ex ->
        ExerciseGuideBottomSheet(
            exercise = ex,
            onDismiss = { guideExercise = null }
        )
    }
}
