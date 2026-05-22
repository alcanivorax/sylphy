package io.sylphy.app.ui.components.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.keyframes
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.NothingRed
import io.sylphy.app.ui.theme.ProgressEmpty
import io.sylphy.app.ui.theme.ProgressFilled
import io.sylphy.app.ui.theme.ProgressPlayhead
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun OrganicSeekBar(
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
    waveformData: List<Float>? = null,
    isPlaying: Boolean = false,
    onSeek: (Long) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    var localProgress by remember { mutableFloatStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }
    val displayProgress = if (isDragging) localProgress else progress
    val displayPositionMs = (displayProgress * durationMs).toLong()

    val animatedProgress = remember { Animatable(progress) }
    LaunchedEffect(progress, isDragging) {
        if (!isDragging) {
            animatedProgress.animateTo(
                targetValue = progress,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
            )
        } else {
            animatedProgress.snapTo(localProgress)
        }
    }

    val breathingPhase = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                breathingPhase.animateTo(
                    targetValue = breathingPhase.value + 2f * PI.toFloat(),
                    animationSpec = tween(durationMillis = 3000),
                )
                breathingPhase.snapTo(breathingPhase.value % (2f * PI.toFloat()))
            }
        } else {
            breathingPhase.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800),
            )
        }
    }

    val idleBreath = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            idleBreath.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2000),
            )
            idleBreath.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 2000),
            )
        }
    }

    val dotRadius = with(LocalDensity.current) {
        (if (isDragging) Layout.seekDotRadius * 2f else Layout.seekDotRadius).toPx()
    }

    val colorEmpty = ProgressEmpty
    val colorFilled = ProgressFilled
    val colorPlayhead = ProgressPlayhead
    val colorMuted = FgMuted
    val colorPrimary = FgPrimary

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Layout.seekBarHeight)
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
                val baseLineHeight = 4.dp.toPx()
                val waveAmplitude = if (isPlaying) 3.dp.toPx() else 1.dp.toPx()
                val breathFactor = if (isPlaying) 1f else idleBreath.value * 0.3f

                if (waveformData != null && waveformData.isNotEmpty()) {
                    val barWidth = 2.5.dp.toPx()
                    val gap = 2.dp.toPx()
                    val step = barWidth + gap
                    val maxBars = (size.width / step).toInt()

                    for (i in 0 until maxBars) {
                        val x = i * step
                        val sampleIdx = (i.toFloat() / maxBars * waveformData.size).toInt()
                            .coerceIn(0, waveformData.size - 1)
                        val baseAmplitude = waveformData[sampleIdx]

                        val breathOffset = sin(breathingPhase.value + i * 0.15f) * waveAmplitude * breathFactor
                        val h = (baseLineHeight + (baseAmplitude + breathOffset) * 14.dp.toPx())
                            .coerceIn(baseLineHeight, 22.dp.toPx())

                        val color = if (x < splitX) colorFilled else colorEmpty

                        val path = Path()
                        val left = x
                        val right = x + barWidth
                        val top = y - h / 2f
                        val bottom = y + h / 2f
                        val corner = barWidth * 0.4f

                        path.moveTo(left + corner, top)
                        path.lineTo(right - corner, top)
                        path.quadraticTo(right, top, right, top + corner)
                        path.lineTo(right, bottom - corner)
                        path.quadraticTo(right, bottom, right - corner, bottom)
                        path.lineTo(left + corner, bottom)
                        path.quadraticTo(left, bottom, left, bottom - corner)
                        path.lineTo(left, top + corner)
                        path.quadraticTo(left, top, left + corner, top)
                        path.close()

                        drawPath(path, color)
                    }
                } else {
                    val segments = 60
                    val segmentWidth = size.width / segments

                    val emptyPath = Path()
                    val filledPath = Path()

                    emptyPath.moveTo(0f, y)
                    filledPath.moveTo(0f, y)

                    for (i in 0..segments) {
                        val x = i * segmentWidth
                        val wave = sin(breathingPhase.value + i * 0.2f) * waveAmplitude * breathFactor
                        val waveY = y + wave

                        if (x <= splitX) {
                            filledPath.lineTo(x, waveY)
                        }
                        emptyPath.lineTo(x, waveY)
                    }

                    drawPath(
                        path = emptyPath,
                        color = colorEmpty,
                        style = Stroke(width = baseLineHeight, cap = StrokeCap.Round),
                    )

                    drawPath(
                        path = filledPath,
                        color = colorFilled,
                        style = Stroke(width = baseLineHeight, cap = StrokeCap.Round),
                    )
                }

                val thumbWave = sin(breathingPhase.value) * 1.5f * breathFactor
                val thumbRadius = dotRadius + thumbWave

                drawCircle(
                    color = colorPlayhead,
                    radius = thumbRadius,
                    center = Offset(splitX, y + thumbWave * 0.5f),
                )

                if (isDragging) {
                    drawCircle(
                        color = colorPlayhead.copy(alpha = 0.15f),
                        radius = thumbRadius * 2.5f,
                        center = Offset(splitX, y),
                    )
                }
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
