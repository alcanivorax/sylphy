package io.sylphy.app.ui.components.shared

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipePager(
    pageCount: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit,
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Layout(
        modifier = modifier.pointerInput(currentPage, pageCount) {
            var totalDrag = 0f

            detectHorizontalDragGestures(
                onDragEnd = {
                    val threshold = size.width * 0.20f

                    when {
                        totalDrag < -threshold && currentPage < pageCount - 1 -> {
                            onPageChanged(currentPage + 1)
                        }

                        totalDrag > threshold && currentPage > 0 -> {
                            onPageChanged(currentPage - 1)
                        }
                    }

                    dragOffset = 0f
                    totalDrag = 0f
                },
                onDragCancel = {
                    dragOffset = 0f
                    totalDrag = 0f
                },
            ) { change, dragAmount ->
                totalDrag += dragAmount
                dragOffset = totalDrag / size.width
                change.consume()
            }
        },
        content = {
            repeat(pageCount) { page ->
                val distance = abs(page - currentPage - dragOffset)

                val scale by animateFloatAsState(
                    targetValue = 1f - (distance * 0.04f).coerceIn(0f, 0.08f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    label = "pager_scale",
                )

                val alpha by animateFloatAsState(
                    targetValue = (1f - distance * 0.25f).coerceIn(0.5f, 1f),
                    animationSpec = spring(),
                    label = "pager_alpha",
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .graphicsLayer {
                            this.alpha = alpha
                        },
                ) {
                    content(page)
                }
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val offsetPx = dragOffset * constraints.maxWidth

            placeables.forEachIndexed { index, placeable ->
                val x = (
                    (index - currentPage) * constraints.maxWidth + offsetPx
                ).roundToInt()

                placeable.placeRelative(x, 0)
            }
        }
    }
}
