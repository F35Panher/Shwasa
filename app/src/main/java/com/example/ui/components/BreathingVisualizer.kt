package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BreathingTimerState
import com.example.engine.TimerPhaseStatus
import com.example.model.BreathingModuleType
import com.example.model.NostrilSide
import com.example.model.StageType
import com.example.model.TimerDisplayStyle
import com.example.model.parseHexColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BreathingVisualizer(
    moduleType: BreathingModuleType,
    timerState: BreathingTimerState,
    displayStyle: TimerDisplayStyle,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val stage = timerState.currentStage
    val stageColor = stage?.let { parseHexColor(it.colorHex, MaterialTheme.colorScheme.primary) }
        ?: MaterialTheme.colorScheme.primary
    val animatedStageColor by animateColorAsState(
        targetValue = stageColor,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 450),
        label = "stage_color"
    )

    if (displayStyle == TimerDisplayStyle.MINIMAL_NUMBERS) {
        MinimalTimerDisplay(
            timerState = timerState,
            stageColor = animatedStageColor,
            modifier = modifier
        )
        return
    }

    when (moduleType) {
        BreathingModuleType.BOX_BREATHING -> BoxBreathingVisualizer(
            timerState = timerState,
            stageColor = animatedStageColor,
            reducedMotion = reducedMotion,
            modifier = modifier
        )
        BreathingModuleType.ANULOM_VILOM -> AnulomVilomVisualizer(
            timerState = timerState,
            stageColor = animatedStageColor,
            reducedMotion = reducedMotion,
            modifier = modifier
        )
        BreathingModuleType.BHRAMARI -> BhramariVisualizer(
            timerState = timerState,
            stageColor = animatedStageColor,
            reducedMotion = reducedMotion,
            modifier = modifier
        )
        BreathingModuleType.KAPALBHATI -> KapalbhatiVisualizer(
            timerState = timerState,
            stageColor = animatedStageColor,
            reducedMotion = reducedMotion,
            modifier = modifier
        )
        BreathingModuleType.THREE_STAGE,
        BreathingModuleType.CUSTOM -> CircularBreathingClock(
            timerState = timerState,
            stageColor = animatedStageColor,
            reducedMotion = reducedMotion,
            showInstructionProminent = displayStyle == TimerDisplayStyle.BREATH_GUIDE_FULL,
            modifier = modifier
        )
    }
}

@Composable
private fun MinimalTimerDisplay(
    timerState: BreathingTimerState,
    stageColor: Color,
    modifier: Modifier = Modifier
) {
    val stage = timerState.currentStage
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = stageColor.copy(alpha = 0.14f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = stage?.label ?: "Ready",
                style = MaterialTheme.typography.titleMedium,
                color = stageColor,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = timerState.stageRemainingSeconds.toString().padStart(2, '0'),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 84.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stage?.instruction ?: "Tap Start when you feel settled",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun CircularBreathingClock(
    timerState: BreathingTimerState,
    stageColor: Color,
    reducedMotion: Boolean,
    showInstructionProminent: Boolean,
    modifier: Modifier = Modifier
) {
    val stage = timerState.currentStage
    val targetScale = when {
        reducedMotion -> 0.78f
        timerState.status == TimerPhaseStatus.IDLE -> 0.68f
        timerState.status == TimerPhaseStatus.COMPLETED -> 0.75f
        stage?.type == StageType.INHALE -> 0.56f + 0.38f * timerState.stageProgressFraction
        stage?.type == StageType.HOLD_IN -> 0.94f
        stage?.type == StageType.EXHALE || stage?.type == StageType.HUMMING ->
            0.94f - 0.38f * timerState.stageProgressFraction
        stage?.type == StageType.HOLD_OUT || stage?.type == StageType.REST -> 0.56f
        else -> 0.68f + 0.20f * sin(timerState.stageProgressFraction * PI).toFloat()
    }

    val smoothScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 120, easing = FastOutSlowInEasing),
        label = "breath_scale"
    )

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val trackBg = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .widthIn(max = 310.dp)
            .fillMaxWidth(0.84f)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f - 14.dp.toPx()

            // Outer ambient ring
            drawCircle(
                color = trackBg.copy(alpha = 0.45f),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 6.dp.toPx())
            )

            // Progress arc around outer ring
            drawArc(
                color = stageColor,
                startAngle = -90f,
                sweepAngle = 360f * timerState.stageProgressFraction,
                useCenter = false,
                topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                size = Size(maxRadius * 2f, maxRadius * 2f),
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            )

            // Expanding / contracting soft breath halo
            val breathRadius = maxRadius * smoothScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        stageColor.copy(alpha = 0.28f),
                        stageColor.copy(alpha = 0.12f),
                        surfaceVariant.copy(alpha = 0.04f)
                    ),
                    center = center,
                    radius = breathRadius.coerceAtLeast(1f)
                ),
                radius = breathRadius,
                center = center
            )

            // Inner crisp core ring
            drawCircle(
                color = stageColor.copy(alpha = 0.55f),
                radius = (breathRadius * 0.78f).coerceAtLeast(24f),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = when (timerState.status) {
                    TimerPhaseStatus.IDLE -> "READY"
                    TimerPhaseStatus.COMPLETED -> "COMPLETE"
                    else -> (stage?.label ?: "BREATHE").uppercase()
                },
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.8.sp),
                color = stageColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (timerState.status == TimerPhaseStatus.COMPLETED) {
                    "✓"
                } else {
                    timerState.stageRemainingSeconds.toString().padStart(2, '0')
                },
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (showInstructionProminent && stage != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stage.instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BoxBreathingVisualizer(
    timerState: BreathingTimerState,
    stageColor: Color,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val activeSide = timerState.currentStageIndex % 4
    val progress = if (timerState.status == TimerPhaseStatus.RUNNING || timerState.status == TimerPhaseStatus.PAUSED) {
        timerState.stageProgressFraction
    } else 0f

    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val stage = timerState.currentStage

    Box(
        modifier = modifier
            .widthIn(max = 290.dp)
            .fillMaxWidth(0.8f)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pad = 26.dp.toPx()
            val left = pad
            val top = pad
            val right = size.width - pad
            val bottom = size.height - pad
            val boxW = right - left
            val boxH = bottom - top

            // Base rounded box frame
            drawRoundRect(
                color = trackColor.copy(alpha = 0.5f),
                topLeft = Offset(left, top),
                size = Size(boxW, boxH),
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                style = Stroke(width = 8.dp.toPx())
            )

            // Draw completed and active sides clearly
            val corners = listOf(
                Offset(left, top),
                Offset(right, top),
                Offset(right, bottom),
                Offset(left, bottom)
            )

            for (side in 0 until 4) {
                val start = corners[side]
                val end = corners[(side + 1) % 4]
                val sideFraction = when {
                    timerState.status == TimerPhaseStatus.IDLE -> 0f
                    timerState.status == TimerPhaseStatus.COMPLETED -> 1f
                    side < activeSide -> 1f
                    side == activeSide -> progress
                    else -> 0f
                }
                if (sideFraction > 0f) {
                    val curEnd = Offset(
                        start.x + (end.x - start.x) * sideFraction,
                        start.y + (end.y - start.y) * sideFraction
                    )
                    drawLine(
                        color = if (side == activeSide) stageColor else stageColor.copy(alpha = 0.4f),
                        start = start,
                        end = curEnd,
                        strokeWidth = 9.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Traveling glowing dot on active side
            if (timerState.status == TimerPhaseStatus.RUNNING || timerState.status == TimerPhaseStatus.PAUSED) {
                val start = corners[activeSide]
                val end = corners[(activeSide + 1) % 4]
                val dotPos = Offset(
                    start.x + (end.x - start.x) * progress,
                    start.y + (end.y - start.y) * progress
                )
                if (!reducedMotion) {
                    drawCircle(
                        color = stageColor.copy(alpha = 0.25f),
                        radius = 18.dp.toPx(),
                        center = dotPos
                    )
                }
                drawCircle(
                    color = stageColor,
                    radius = 9.dp.toPx(),
                    center = dotPos
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (timerState.status) {
                    TimerPhaseStatus.IDLE -> "BOX 4-4-4-4"
                    TimerPhaseStatus.COMPLETED -> "COMPLETE"
                    else -> (stage?.label ?: "").uppercase()
                },
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.6.sp),
                color = stageColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (timerState.status == TimerPhaseStatus.COMPLETED) "✓"
                else timerState.stageRemainingSeconds.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun AnulomVilomVisualizer(
    timerState: BreathingTimerState,
    stageColor: Color,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val stage = timerState.currentStage
    val activeNostril = stage?.nostrilSide ?: NostrilSide.NONE
    val progress = timerState.stageProgressFraction
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NostrilColumnCard(
                title = "Left Nostril",
                subtitle = "Ida • Cooling",
                isActive = activeNostril == NostrilSide.LEFT || activeNostril == NostrilSide.BOTH,
                stageType = stage?.type,
                progress = progress,
                accent = stageColor,
                reducedMotion = reducedMotion,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            // Center countdown badge
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = stageColor.copy(alpha = 0.14f),
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (timerState.status == TimerPhaseStatus.COMPLETED) "✓"
                        else timerState.stageRemainingSeconds.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = stageColor
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            NostrilColumnCard(
                title = "Right Nostril",
                subtitle = "Pingala • Vital",
                isActive = activeNostril == NostrilSide.RIGHT || activeNostril == NostrilSide.BOTH,
                stageType = stage?.type,
                progress = progress,
                accent = stageColor,
                reducedMotion = reducedMotion,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = stage?.label ?: "Sit Comfortably",
            style = MaterialTheme.typography.titleLarge,
            color = stageColor
        )
        Spacer(Modifier.height(4.dp))
        Surface(
            color = outlineColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = when (activeNostril) {
                    NostrilSide.LEFT -> "Active: Left Nostril (thumb softly closes Right)"
                    NostrilSide.RIGHT -> "Active: Right Nostril (ring finger closes Left)"
                    NostrilSide.BOTH -> "Both nostrils gently closed"
                    NostrilSide.NONE -> "Prepare hand in Vishnu or Chin mudra"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun NostrilColumnCard(
    title: String,
    subtitle: String,
    isActive: Boolean,
    stageType: StageType?,
    progress: Float,
    accent: Color,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val borderColor = if (isActive) accent else MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier.height(176.dp),
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(if (isActive) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Airflow vertical indicator
            Canvas(modifier = Modifier.size(width = 36.dp, height = 74.dp)) {
                val cx = size.width / 2f
                drawLine(
                    color = borderColor.copy(alpha = 0.5f),
                    start = Offset(cx, 8f),
                    end = Offset(cx, size.height - 8f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                if (isActive && !reducedMotion) {
                    val yPos = when (stageType) {
                        StageType.INHALE -> size.height - 10f - (size.height - 20f) * progress
                        StageType.EXHALE -> 10f + (size.height - 20f) * progress
                        else -> size.height / 2f
                    }
                    drawCircle(
                        color = accent,
                        radius = 8.dp.toPx(),
                        center = Offset(cx, yPos)
                    )
                } else if (isActive) {
                    drawCircle(
                        color = accent,
                        radius = 7.dp.toPx(),
                        center = Offset(cx, size.height / 2f)
                    )
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BhramariVisualizer(
    timerState: BreathingTimerState,
    stageColor: Color,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val stage = timerState.currentStage
    val isHumming = stage?.type == StageType.HUMMING && timerState.status == TimerPhaseStatus.RUNNING
    val infiniteTransition = rememberInfiniteTransition(label = "bhramari_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = modifier
            .widthIn(max = 310.dp)
            .fillMaxWidth(0.84f)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension / 2f - 16.dp.toPx()

            // Outer progress ring
            drawCircle(
                color = stageColor.copy(alpha = 0.14f),
                radius = maxR,
                center = center,
                style = Stroke(width = 6.dp.toPx())
            )
            drawArc(
                color = stageColor,
                startAngle = -90f,
                sweepAngle = 360f * timerState.stageProgressFraction,
                useCenter = false,
                topLeft = Offset(center.x - maxR, center.y - maxR),
                size = Size(maxR * 2f, maxR * 2f),
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            )

            // Concentric resonance sound waves during humming
            val ringCount = 3
            for (i in 0 until ringCount) {
                val baseR = maxR * (0.48f + i * 0.14f)
                val osc = if (isHumming && !reducedMotion) {
                    sin(wavePhase + i * 1.1f) * 8.dp.toPx()
                } else 0f
                drawCircle(
                    color = stageColor.copy(alpha = if (isHumming) 0.28f - i * 0.06f else 0.12f),
                    radius = (baseR + osc).coerceAtLeast(16f),
                    center = center,
                    style = Stroke(width = if (isHumming) 3.dp.toPx() else 1.5.dp.toPx())
                )
            }

            // Gentle acoustic sine wave across bottom inside ring during humming
            if (isHumming && !reducedMotion) {
                val wavePath = Path()
                val waveWidth = maxR * 1.1f
                val startX = center.x - waveWidth / 2f
                val baseY = center.y + maxR * 0.42f
                val steps = 40
                for (s in 0..steps) {
                    val x = startX + (waveWidth * s / steps)
                    val norm = s.toFloat() / steps
                    val envelope = sin(norm * PI).toFloat()
                    val y = baseY + sin(norm * 6f * PI + wavePhase).toFloat() * 10.dp.toPx() * envelope
                    if (s == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                }
                drawPath(
                    path = wavePath,
                    color = stageColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isHumming) "HUMMING RESONANCE" else (stage?.label ?: "READY").uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.4.sp),
                color = stageColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (timerState.status == TimerPhaseStatus.COMPLETED) "✓"
                else timerState.stageRemainingSeconds.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun KapalbhatiVisualizer(
    timerState: BreathingTimerState,
    stageColor: Color,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val stage = timerState.currentStage
    val isActivePhase = stage?.type == StageType.KAPALBHATI_ACTIVE && timerState.status == TimerPhaseStatus.RUNNING
    val targetStrokes = timerState.kapalbhatiConfig.targetExhalationsPerRound
    val currentStroke = timerState.kapalbhatiCurrentExhalation

    val pulseScale by animateFloatAsState(
        targetValue = if (isActivePhase && !reducedMotion) {
            if (currentStroke % 2 == 0) 0.92f else 0.78f
        } else 0.82f,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 220),
        label = "kapalbhati_pulse"
    )

    Box(
        modifier = modifier
            .widthIn(max = 300.dp)
            .fillMaxWidth(0.82f)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension / 2f - 16.dp.toPx()

            drawCircle(
                color = stageColor.copy(alpha = 0.14f),
                radius = maxR,
                center = center,
                style = Stroke(width = 6.dp.toPx())
            )
            drawArc(
                color = stageColor,
                startAngle = -90f,
                sweepAngle = 360f * timerState.stageProgressFraction,
                useCenter = false,
                topLeft = Offset(center.x - maxR, center.y - maxR),
                size = Size(maxR * 2f, maxR * 2f),
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(
                color = stageColor.copy(alpha = 0.18f),
                radius = maxR * pulseScale,
                center = center
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = (stage?.label ?: "GENTLE ROUNDS").uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.3.sp),
                color = stageColor
            )
            Spacer(Modifier.height(4.dp))
            if (stage?.type == StageType.KAPALBHATI_ACTIVE) {
                Text(
                    text = "$currentStroke / $targetStrokes",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${timerState.stageRemainingSeconds}s left in round",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = timerState.stageRemainingSeconds.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Rest naturally",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
