package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.ProgressCalculator
import com.example.model.DailyProgress
import com.example.model.ExerciseStat
import kotlin.math.max

@Composable
fun DailyActivityBarChart(
    points: List<DailyProgress>,
    goalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    val maxMinutes = max(goalMinutes, points.maxOfOrNull { it.totalMinutes } ?: 0).coerceAtLeast(10)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 4.dp)
        ) {
            if (points.isEmpty()) return@Canvas
            val barSlot = size.width / points.size
            val barW = (barSlot * 0.46f).coerceAtMost(28.dp.toPx())

            // Daily Goal dashed line
            val goalY = size.height - (goalMinutes.toFloat() / maxMinutes) * (size.height - 16.dp.toPx())
            drawLine(
                color = tertiary.copy(alpha = 0.7f),
                start = Offset(0f, goalY),
                end = Offset(size.width, goalY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
            )

            points.forEachIndexed { idx, day ->
                val x = idx * barSlot + (barSlot - barW) / 2f
                // Background track
                drawRoundRect(
                    color = track.copy(alpha = 0.55f),
                    topLeft = Offset(x, 8.dp.toPx()),
                    size = Size(barW, size.height - 8.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                val minutes = day.totalMinutes.toFloat()
                val frac = (minutes / maxMinutes).coerceIn(0f, 1f)
                val barH = frac * (size.height - 16.dp.toPx())
                if (barH > 2f) {
                    drawRoundRect(
                        color = if (day.goalMet) primary else primary.copy(alpha = 0.65f),
                        topLeft = Offset(x, size.height - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = ProgressCalculator.shortWeekdayLabel(day.dateIso),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${day.totalMinutes}m",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (day.totalMinutes > 0) primary else outline
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressLineChart(
    weeklyPoints: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val maxVal = (weeklyPoints.maxOfOrNull { it.second } ?: 0).coerceAtLeast(10)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (weeklyPoints.size < 2) return@Canvas
            val stepX = size.width / (weeklyPoints.size - 1)
            val offsets = weeklyPoints.mapIndexed { idx, (_, min) ->
                val x = idx * stepX
                val y = size.height - (min.toFloat() / maxVal) * (size.height - 16.dp.toPx()) - 8.dp.toPx()
                Offset(x, y)
            }

            val areaPath = Path().apply {
                moveTo(offsets.first().x, size.height)
                offsets.forEach { lineTo(it.x, it.y) }
                lineTo(offsets.last().x, size.height)
                close()
            }
            drawPath(areaPath, color = primaryContainer.copy(alpha = 0.45f))

            val linePath = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                offsets.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(
                path = linePath,
                color = primary,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            offsets.forEach { pt ->
                drawCircle(color = primary, radius = 5.dp.toPx(), center = pt)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyPoints.forEach { (label, mins) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${mins}m",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyCalendarHeatmap(
    days: List<DailyProgress>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val emptyCell = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val rows = days.chunked(7)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until 7) {
                    val day = week.getOrNull(i)
                    if (day == null) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val intensity = when {
                            day.totalSeconds <= 0 -> 0f
                            day.goalMet -> 1f
                            day.totalSeconds >= (day.goalMinutes * 30) -> 0.65f
                            else -> 0.35f
                        }
                        val cellColor = if (intensity == 0f) emptyCell else primary.copy(alpha = intensity)
                        val dayNum = day.dateIso.takeLast(2).trimStart('0').ifEmpty { "0" }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cellColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (intensity >= 0.65f) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            listOf(0f, 0.35f, 0.65f, 1f).forEach { alpha ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (alpha == 0f) emptyCell else primary.copy(alpha = alpha))
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = "Goal Met",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExerciseBreakdownList(
    stats: List<ExerciseStat>,
    modifier: Modifier = Modifier
) {
    val maxSeconds = (stats.maxOfOrNull { it.totalSeconds } ?: 1).coerceAtLeast(1)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { stat ->
            val mins = (stat.totalSeconds / 60).coerceAtLeast(if (stat.totalSeconds > 0) 1 else 0)
            val fraction = (stat.totalSeconds.toFloat() / maxSeconds).coerceIn(0.08f, 1f)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stat.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${mins}m • ${stat.sessionCount} sessions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
