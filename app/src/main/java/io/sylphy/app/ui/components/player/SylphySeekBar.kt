package io.sylphy.app.ui.components.player

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.ui.theme.FgMuted
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
    val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    var localProgress by remember { mutableFloatStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }
    val displayProgress = if (isDragging) localProgress else progress
    val displayPositionMs = (displayProgress * durationMs).toLong()
    val dotRadius = with(LocalDensity.current) { Layout.seekDotRadius.toPx() }

    LaunchedEffect(progress, isDragging) {
        if (!isDragging) localProgress = progress
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Layout.seekBarHeight)
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        localProgress = fraction
                        onSeek((fraction * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            localProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((localProgress * durationMs).toLong())
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
                val splitX = displayProgress * size.width
                val baseHeight = 2f

                if (waveformData != null && waveformData.isNotEmpty()) {
                    val segWidth = size.width / waveformData.size
                    waveformData.forEachIndexed { i, amplitude ->
                        val x = i * segWidth
                        val h = (baseHeight + amplitude * 4f).coerceIn(2f, 6f)
                        val color = if (x < splitX) ProgressFilled else ProgressEmpty
                        drawRect(
                            color = color,
                            topLeft = Offset(x, y - h / 2f),
                            size = Size((segWidth - 1f).coerceAtLeast(1f), h),
                        )
                    }
                } else {
                    drawLine(ProgressEmpty,  Offset(0f, y), Offset(size.width, y), baseHeight)
                    drawLine(ProgressFilled, Offset(0f, y), Offset(splitX, y),     baseHeight)
                }

                drawCircle(
                    color = ProgressPlayhead,
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
                color = FgMuted,
            )
            Text(
                text = "-${(durationMs - displayPositionMs).coerceAtLeast(0L).toMmSs()}",
                style = SylphyType.CodeSmall,
                color = FgMuted,
            )
        }
    }
}
