package io.sylphy.app.ui.screens.queue

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.sylphy.app.R
import io.sylphy.app.core.util.toHhMm
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.components.shared.EmptyState
import io.sylphy.app.ui.components.shared.SylphyDivider
import io.sylphy.app.ui.theme.ActiveBackground
import io.sylphy.app.ui.theme.ActiveForeground
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.PlayerTheme
import io.sylphy.app.ui.theme.ProgressFilled
import io.sylphy.app.ui.theme.ProgressEmpty
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType
import kotlin.math.roundToInt

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ──────────────────────────────────────────────────────
            QueueHeader(
                trackCount = uiState.tracks.size,
                upNextDurationMs = uiState.upNextDurationMs,
                onAdd = { /* TODO: open track picker */ },
            )

            // ── Now Playing card ─────────────────────────────────────────────
            uiState.nowPlaying?.let { track ->
                NowPlayingCard(
                    track = track,
                    modifier = Modifier.padding(
                        horizontal = Spacing.md,
                        vertical = Spacing.sm,
                    ),
                )
            }

            // ── Up Next section label ────────────────────────────────────────
            if (uiState.upNext.isNotEmpty()) {
                UpNextSectionHeader(
                    remainingCount = uiState.remainingCount,
                    onClear = viewModel::clearUpNext,
                )
            }

            // ── Draggable queue list ─────────────────────────────────────────
            QueueList(
                upNext = uiState.upNext,
                // Absolute index offset: upNext starts at activeIndex + 1
                indexOffset = (uiState.activeIndex + 1).coerceAtLeast(0),
                onPlay = { absoluteIndex -> viewModel.playAt(absoluteIndex) },
                onRemove = { absoluteIndex -> viewModel.removeAt(absoluteIndex) },
                onMove = { from, to -> viewModel.move(from, to) },
                modifier = Modifier.weight(1f),
            )

            // ── Footer: total up-next duration ───────────────────────────────
            if (uiState.upNext.isNotEmpty()) {
                QueueFooter(totalDurationMs = uiState.upNextDurationMs)
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun QueueHeader(
    trackCount: Int,
    upNextDurationMs: Long,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Layout.topBarHeight)
            .padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "QUEUE",
                style = SylphyType.DisplayLarge,
                color = FgPrimary,
            )
            Text(
                // e.g. "5 tracks · 21 min"
                text = "$trackCount tracks · ${upNextDurationMs.toHhMm()}",
                style = SylphyType.CodeSmall,
                color = FgMuted,
            )
        }

        IconButton(onClick = onAdd, modifier = Modifier.size(Layout.transportTapTarget)) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add to queue",
                tint = FgMuted,
                modifier = Modifier.size(Layout.transportIconSize),
            )
        }
    }
}

// ─── Now Playing Card ─────────────────────────────────────────────────────────

@Composable
private fun NowPlayingCard(
    track: Track,
    modifier: Modifier = Modifier,
) {
    val accentColor = PlayerTheme.Red
    val borderColor = accentColor.copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(accentColor.copy(alpha = 0.07f))
            .border(Layout.borderThin, borderColor, RoundedCornerShape(4.dp))
            // Left accent bar drawn via drawBehind to avoid extra Box nesting
            .drawBehind {
                drawRect(
                    color = accentColor,
                    size = androidx.compose.ui.geometry.Size(
                        width = Layout.borderThick.toPx(),
                        height = size.height,
                    ),
                )
            }
            .padding(start = Spacing.sm + Layout.borderThick, end = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Artwork with EQ bars overlay
            Box(modifier = Modifier.size(Layout.albumArtSizeSm)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.artworkPath)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_sylphy_background),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                        .border(Layout.borderThin, BorderDefault, RoundedCornerShape(3.dp)),
                )
                // EQ bars in the bottom-left corner of the artwork
                NowPlayingEqBars(
                    color = accentColor,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                )
            }

            // Track info + mini scrubber
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NOW PLAYING",
                    style = SylphyType.Heading,
                    color = accentColor,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = track.title,
                    style = SylphyType.Body,
                    color = FgPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.artist,
                    style = SylphyType.CodeSmall,
                    color = FgMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Spacing.xs))
                // Static mini scrubber — progress driven by PlayerViewModel via shared state.
                // QueueViewModel does not own elapsed time; show only the total duration.
                MiniDurationBar(durationMs = track.durationMs)
            }
        }
    }
}

// ─── Animated EQ bars ─────────────────────────────────────────────────────────

@Composable
private fun NowPlayingEqBars(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // Three bars with staggered infinite animations
        listOf(
            Triple(8.dp, 0, 0),
            Triple(12.dp, 180, 0),
            Triple(6.dp, 90, 0),
        ).forEachIndexed { i, (maxH, _, _) ->
            val anim = remember { Animatable(0.3f) }
            LaunchedEffect(i) {
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 600 + i * 150,
                            easing = LinearEasing,
                        ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                )
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(maxH * anim.value)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color),
            )
        }
    }
}

// ─── Mini duration bar (static — no seek on queue screen) ─────────────────────

@Composable
private fun MiniDurationBar(durationMs: Long) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(ProgressEmpty),
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "0:00",
                style = SylphyType.CodeSmall,
                color = FgSubtle,
            )
            Text(
                text = durationMs.toMmSs(),
                style = SylphyType.CodeSmall,
                color = FgSubtle,
            )
        }
    }
}

// ─── Up Next section header ────────────────────────────────────────────────────

@Composable
private fun UpNextSectionHeader(
    remainingCount: Int,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "UP NEXT",
            style = SylphyType.Heading,
            color = FgMuted,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$remainingCount remaining",
                style = SylphyType.CodeSmall,
                color = FgSubtle,
            )
            Spacer(Modifier.width(Spacing.md))
            Text(
                text = "CLEAR",
                style = SylphyType.Heading,
                color = FgMuted,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClear,
                    )
                    .padding(vertical = Spacing.xs),
            )
        }
    }
}

// ─── Draggable list ───────────────────────────────────────────────────────────

@Composable
private fun QueueList(
    upNext: List<Track>,
    indexOffset: Int,
    onPlay: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val itemHeightPx = with(LocalDensity.current) { Layout.queueItemHeight.toPx() }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(
            items = upNext,
            key = { _, track -> track.id },
        ) { localIndex, track ->
            val absoluteIndex = localIndex + indexOffset
            val isDragging = draggingIndex == localIndex

            QueueRowItem(
                track = track,
                position = absoluteIndex + 1,          // 1-based display number
                isDragging = isDragging,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDragging) Modifier.offset(
                            y = with(LocalDensity.current) { dragOffset.toDp() }
                        ) else Modifier,
                    )
                    .pointerInput(localIndex, upNext.size) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingIndex = localIndex
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                val targetLocal = (localIndex + (dragOffset / itemHeightPx)
                                    .roundToInt()).coerceIn(0, upNext.lastIndex)
                                onMove(absoluteIndex, targetLocal + indexOffset)
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
                    },
                onPlay = { onPlay(absoluteIndex) },
                onRemove = { onRemove(absoluteIndex) },
            )

            SylphyDivider(
                modifier = Modifier.padding(start = Spacing.md + Layout.albumArtSizeSm + Spacing.sm),
            )
        }
    }
}

// ─── Queue Row Item ───────────────────────────────────────────────────────────

@Composable
private fun QueueRowItem(
    track: Track,
    position: Int,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val rowScale by animateFloatAsState(
        targetValue = when {
            isDragging -> 1.02f
            pressed    -> 0.97f
            else       -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "queue_row_scale",
    )

    Row(
        modifier = modifier
            .height(Layout.queueItemHeight)
            .scale(rowScale)
            .background(if (isDragging) BgElevated else BgBase)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPlay()
                },
            )
            .padding(horizontal = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        // Drag handle — 3 horizontal bars
        DragHandleIcon(modifier = Modifier.width(Spacing.md))

        // Position number
        Text(
            text = position.toString().padStart(2, '0'),
            style = SylphyType.CodeSmall,
            color = FgSubtle,
            modifier = Modifier.width(24.dp),
        )

        // Artwork thumbnail
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.artworkPath)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_sylphy_background),
            modifier = Modifier
                .size(Layout.albumArtSizeSm)
                .clip(RoundedCornerShape(3.dp))
                .border(Layout.borderThin, BorderDefault, RoundedCornerShape(3.dp)),
        )

        // Title + artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = SylphyType.Code,
                color = FgPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = SylphyType.BodySmall,
                color = FgMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Duration
        Text(
            text = track.durationMs.toMmSs(),
            style = SylphyType.CodeSmall,
            color = FgSubtle,
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(Layout.transportTapTarget),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_remove), // 'x' icon
                contentDescription = "Remove from queue",
                tint = FgSubtle,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ─── Drag handle ──────────────────────────────────────────────────────────────

@Composable
private fun DragHandleIcon(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(1.5.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(FgSubtle),
            )
        }
    }
}

// ─── Footer ───────────────────────────────────────────────────────────────────

@Composable
private fun QueueFooter(totalDurationMs: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color(0xFF222222),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "TOTAL  ",
            style = SylphyType.Heading,
            color = FgSubtle,
        )
        Text(
            text = totalDurationMs.toHhMm(),
            style = SylphyType.CodeSmall,
            color = FgMuted,
        )
    }
}
