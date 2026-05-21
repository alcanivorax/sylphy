package io.sylphy.app.ui.components.shared

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipePager(
    pageCount: Int,
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val pageOffset = remember { Animatable(0f) }
    var currentPage by remember { mutableIntStateOf(initialPage) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableFloatStateOf(0f) }

    val velocityThreshold = with(density) { 500.dp.toPx() }
    val swipeThreshold = with(density) { 40.dp.toPx() }

    LaunchedEffect(currentPage) {
        onPageChanged(currentPage)
    }

    val effectiveOffset = if (isDragging != 0f) {
        pageOffset.value + dragOffset
    } else {
        pageOffset.value
    }

    Layout(
        modifier = modifier.pointerInput(pageCount) {
            var totalDrag = 0f
            var lastVelocity = 0f

            detectHorizontalDragGestures(
                onDragStart = {
                    totalDrag = 0f
                    lastVelocity = 0f
                    isDragging = 1f
                },
                onDragEnd = {
                    isDragging = 0f
                    val flingVelocity = lastVelocity * 3f

                    val shouldNavigateLeft = currentPage < pageCount - 1 &&
                        (totalDrag < -swipeThreshold || flingVelocity < -velocityThreshold)

                    val shouldNavigateRight = currentPage > 0 &&
                        (totalDrag > swipeThreshold || flingVelocity > velocityThreshold)

                    val targetPage = when {
                        shouldNavigateLeft -> currentPage + 1
                        shouldNavigateRight -> currentPage - 1
                        else -> currentPage
                    }

                    if (targetPage != currentPage) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }

                    dragOffset = 0f
                    scope.launch {
                        pageOffset.snapTo(-targetPage.toFloat())
                        currentPage = targetPage
                    }
                },
                onDragCancel = {
                    isDragging = 0f
                    dragOffset = 0f
                    scope.launch {
                        pageOffset.snapTo(-currentPage.toFloat())
                    }
                },
            ) { change, dragAmount ->
                totalDrag += dragAmount
                lastVelocity = dragAmount
                dragOffset = totalDrag / size.width
                change.consume()
            }
        },
        content = {
            repeat(pageCount) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .swipePagerTransform(effectiveOffset, page, currentPage),
                ) {
                    content(page)
                }
            }
        },
        measurePolicy = { measurables, constraints ->
            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }

            layout(constraints.maxWidth, constraints.maxHeight) {
                val offsetPx = effectiveOffset * constraints.maxWidth
                placeables.forEachIndexed { index, placeable ->
                    val x = (index * constraints.maxWidth + offsetPx).roundToInt()
                    placeable.placeRelative(x, 0)
                }
            }
        },
    )
}

private fun Modifier.swipePagerTransform(
    effectiveOffset: Float,
    page: Int,
    currentPage: Int,
): Modifier = composed {
    val distance = abs(page - effectiveOffset - currentPage)
    val scale = 1f - (distance * 0.04f).coerceIn(0f, 0.08f)
    val alpha = (1f - (distance * 0.3f)).coerceIn(0.3f, 1f)

    this
        .scale(scale)
        .graphicsLayer {
            this.alpha = alpha
        }
}
