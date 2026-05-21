package io.sylphy.app.ui.components.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.ProgressEmpty
import io.sylphy.app.ui.theme.ProgressFilled
import io.sylphy.app.ui.theme.ProgressPlayhead
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun SylphySeekBar(
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
    waveformData: List<Float>? = null,
    onSeek: (Long) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    var localProgress by remember { mutableFloatStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }
    val displayProgress = if (isDragging) localProgress else progress
    val displayPositionMs = (displayProgress * durationMs).toLong()
    
    // Animate playhead position smoothly
    val animatedProgress = remember { Animatable(progress) }
    LaunchedEffect(progress, isDragging) {
        if (!isDragging) {
            animatedProgress.animateTo(
                targetValue = progress,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
            )
        } else {
            animatedProgress.snapTo(localProgress)
        }
    }
    
    val dotRadius = with(LocalDensity.current) { 
        (if (isDragging) Layout.seekDotRadius * 1.8f else Layout.seekDotRadius).toPx() 
    }
    
    // Capture theme colors in composable scope
    val colorEmpty = ProgressEmpty
    val colorFilled = ProgressFilled
    val colorPlayhead = ProgressPlayhead
    val colorMuted = FgMuted
    val colorPrimary = FgPrimary

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // Larger hit target
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        localProgress = fraction
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            localProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = {
                            onSeek((localProgress * durationMs).toLong())
                            isDragging = false
                        },
                        onDragCancel = { isDragging = false },
                    ) { change, _ ->
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        localProgress = fraction
                        onSeek((fraction * durationMs).toLong())
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = center.y
                val splitX = animatedProgress.value * size.width
                val baseLineHeight = 3.dp.toPx()

                if (waveformData != null && waveformData.isNotEmpty()) {
                    val barWidth = 2.dp.toPx()
                    val gap = 1.5.dp.toPx()
                    val step = barWidth + gap
                    val maxBars = (size.width / step).toInt()
                    
                    // Sample waveform to fit maxBars
                    for (i in 0 until maxBars) {
                        val x = i * step
                        val sampleIdx = (i.toFloat() / maxBars * waveformData.size).toInt().coerceIn(0, waveformData.size - 1)
                        val amplitude = waveformData[sampleIdx]
                        val h = (baseLineHeight + amplitude * 16.dp.toPx()).coerceIn(baseLineHeight, 20.dp.toPx())
                        val color = if (x < splitX) colorFilled else colorEmpty
                        
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, y - h / 2f),
                            size = Size(barWidth, h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
                        )
                    }
                } else {
                    drawLine(
                        color = colorEmpty,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = baseLineHeight,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = colorFilled,
                        start = Offset(0f, y),
                        end = Offset(splitX, y),
                        strokeWidth = baseLineHeight,
                        cap = StrokeCap.Round
                    )
                }

                drawCircle(
                    color = colorPlayhead,
                    radius = dotRadius,
                    center = Offset(splitX, y),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = displayPositionMs.toMmSs(),
                style = SylphyType.CodeSmall,
                color = if (isDragging) colorPrimary else colorMuted,
            )
            Text(
                text = "-${(durationMs - displayPositionMs).coerceAtLeast(0L).toMmSs()}",
                style = SylphyType.CodeSmall,
                color = if (isDragging) colorPrimary else colorMuted,
            )
        }
    }
}
