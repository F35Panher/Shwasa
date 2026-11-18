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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ProgressSummary
import com.example.model.UserSettings
import com.example.ui.components.DailyActivityBarChart
import com.example.ui.components.ExerciseBreakdownList
import com.example.ui.components.MonthlyCalendarHeatmap
import com.example.ui.components.WeeklyProgressLineChart

@Composable
fun ProgressScreen(
    summary: ProgressSummary,
    settings: UserSettings,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Progress & Statistics",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Daily goal: ${settings.dailyGoalMinutes} minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("progress_open_history")
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("History")
                    }
                }
            }

            // KPI Stat Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiStatCard(
                            icon = Icons.Filled.LocalFireDepartment,
                            label = "Current Streak",
                            value = "${summary.currentStreakDays} days",
                            modifier = Modifier.weight(1f)
                        )
                        KpiStatCard(
                            icon = Icons.Filled.EmojiEvents,
                            label = "Longest Streak",
                            value = "${summary.longestStreakDays} days",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiStatCard(
                            icon = Icons.Filled.Schedule,
                            label = "Lifetime Practice",
                            value = "${summary.lifetimeSeconds / 60} min",
                            modifier = Modifier.weight(1f)
                        )
                        KpiStatCard(
                            icon = Icons.Filled.Spa,
                            label = "Total Cycles",
                            value = "${summary.totalCycles} (${summary.totalSessions} sess.)",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Daily Activity Chart
            item {
                ChartSectionCard(
                    title = "Daily Activity (Last 7 Days)",
                    subtitle = "Dashed line marks your ${settings.dailyGoalMinutes}m daily goal"
                ) {
                    DailyActivityBarChart(
                        points = summary.dailyChartPoints,
                        goalMinutes = settings.dailyGoalMinutes
                    )
                }
            }

            // Weekly Progress Chart
            item {
                ChartSectionCard(
                    title = "Weekly Trend",
                    subtitle = "Weekly total: ${summary.weeklySeconds / 60}m • Monthly: ${summary.monthlySeconds / 60}m"
                ) {
                    WeeklyProgressLineChart(weeklyPoints = summary.weeklyChartPoints)
                }
            }

            // Monthly Calendar Heatmap
            item {
                ChartSectionCard(
                    title = "30-Day Consistency Heatmap",
                    subtitle = "Each tile reflects daily practice intensity"
                ) {
                    MonthlyCalendarHeatmap(days = summary.monthlyHeatmap)
                }
            }

            // Exercise Breakdown
            item {
                ChartSectionCard(
                    title = "Exercise-Wise Statistics",
                    subtitle = "Time and sessions by pranayama technique"
                ) {
                    if (summary.exerciseStats.isEmpty()) {
                        Text(
                            text = "Complete your first session to see technique-by-technique insights.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ExerciseBreakdownList(stats = summary.exerciseStats)
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun KpiStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChartSectionCard(
    title: String,
    subtitle: String,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}
