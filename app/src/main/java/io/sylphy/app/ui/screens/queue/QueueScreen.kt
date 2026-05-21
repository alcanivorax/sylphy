package io.sylphy.app.ui.screens.queue

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.components.shared.EmptyState
import io.sylphy.app.ui.components.shared.SylphyDivider
import io.sylphy.app.ui.theme.ActiveBackground
import io.sylphy.app.ui.theme.ActiveForeground
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType
import kotlin.math.roundToInt

@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val itemHeightPx = with(LocalDensity.current) { Layout.queueItemHeight.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase),
    ) {
        if (uiState.tracks.isEmpty()) {
            EmptyState(
                title = "Queue empty",
                description = "Play a track from Library to start a queue.",
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = Spacing.md),
        ) {
            itemsIndexed(
                items = uiState.tracks,
                key = { _, track -> track.id },
            ) { index, track ->
                val isActive = index == uiState.activeIndex
                val rowModifier = if (draggingIndex == index) {
                    Modifier.offset(y = with(LocalDensity.current) { dragOffset.toDp() })
                } else {
                    Modifier
                }

                QueueItem(
                    track = track,
                    index = index + 1,
                    isActive = isActive,
                    modifier = rowModifier
                        .fillMaxWidth()
                        .then(
                            if (!isActive) {
                                Modifier.pointerInput(index, uiState.tracks.size) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingIndex = index
                                            dragOffset = 0f
                                        },
                                        onDragEnd = {
                                            val target = (index + (dragOffset / itemHeightPx).roundToInt())
                                                .coerceIn(0, uiState.tracks.lastIndex)
                                            viewModel.move(index, target)
                                            draggingIndex = -1
                                            dragOffset = 0f
                                        },
                                        onDragCancel = {
                                            draggingIndex = -1
                                            dragOffset = 0f
                                        },
                                    ) { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y
                                    }
                                }
                            } else {
                                Modifier
                            },
                        ),
                    onPlay = { viewModel.playAt(index) },
                    onRemove = { viewModel.removeAt(index) },
                )
                SylphyDivider()
            }
        }
    }
}

@Composable
private fun QueueItem(
    track: Track,
    index: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 800f),
        label = "queue_item_scale",
    )
    val bg = if (isActive) ActiveBackground else BgBase
    val primary = if (isActive) ActiveForeground else FgPrimary
    val secondary = if (isActive) ActiveForeground.copy(alpha = 0.55f) else FgMuted
    val tertiary = if (isActive) ActiveForeground.copy(alpha = 0.4f) else FgMuted

    Row(
        modifier = modifier
            .height(Layout.queueItemHeight)
            .background(bg)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isActive,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPlay()
                },
            )
            .padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isActive) {
            Spacer(Modifier.width(40.dp))
        } else {
            Text(
                text = "::",
                style = SylphyType.Code,
                color = FgMuted,
                modifier = Modifier.width(40.dp),
            )
        }

        Text(
            text = index.toString().padStart(2, '0'),
            style = SylphyType.CodeSmall,
            color = tertiary,
            modifier = Modifier.width(36.dp),
        )

        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = SylphyType.Code,
                color = primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = SylphyType.BodySmall,
                color = secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = track.durationMs.toMmSs(),
            style = SylphyType.CodeSmall,
            color = tertiary,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End,
        )

        if (isActive) {
            Spacer(Modifier.width(40.dp))
        } else {
            Text(
                text = "x",
                style = SylphyType.Code,
                color = FgMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(40.dp)
                    .height(Layout.queueItemHeight)
                    .background(BgElevated)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRemove,
                    )
                    .padding(top = Spacing.lg),
            )
        }
    }
}
