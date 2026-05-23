package io.sylphy.app.ui.screens.queue

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.res.painterResource
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.sylphy.app.R
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.data.model.ThemeMode
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.theme.DmSans
import io.sylphy.app.ui.theme.QueueChromeColors
import io.sylphy.app.ui.theme.SpaceMono
import io.sylphy.app.ui.theme.queueChromeColors
import kotlin.math.roundToInt

private val PanelShape = RoundedCornerShape(3.dp)
private val RowHeight = 60.dp

@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel(),
    themeMode: ThemeMode = ThemeMode.MONOCHROME_DARK,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = queueChromeColors(themeMode)
    val systemUiController = rememberSystemUiController()
    val darkIcons = colors.bg.luminance() > 0.5f

    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = darkIcons)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        if (uiState.tracks.isEmpty()) {
            EmptyQueueState(colors = colors, modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize()) {
            FadeUp(delayMillis = 40) {
                QueueHeader(
                    colors = colors,
                    trackCount = uiState.tracks.size,
                    totalDurationMs = uiState.tracks.sumOf { it.durationMs },
                    onAdd = {},
                )
            }

            uiState.nowPlaying?.let { track ->
                FadeUp(delayMillis = 90) {
                    NowPlayingCard(
                        track = track,
                        colors = colors,
                        themeMode = themeMode,
                        positionMs = uiState.positionMs,
                        durationMs = uiState.durationMs.takeIf { it > 0L } ?: track.durationMs,
                    )
                }
            }

            FadeUp(delayMillis = 130) {
                UpNextHeader(
                    colors = colors,
                    remainingCount = uiState.remainingCount,
                    onClear = viewModel::clearUpNext,
                )
            }

            FadeUp(
                delayMillis = 170,
                modifier = Modifier.weight(1f),
            ) {
                QueueList(
                    colors = colors,
                    upNext = uiState.upNext,
                    indexOffset = (uiState.activeIndex + 1).coerceAtLeast(0),
                    onPlay = viewModel::playAt,
                    onRemove = viewModel::removeAt,
                    onMove = viewModel::move,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            QueueFooter(colors = colors, totalDurationMs = uiState.upNextDurationMs)
        }
    }
}

@Composable
private fun QueueHeader(
    colors: QueueChromeColors,
    trackCount: Int,
    totalDurationMs: Long,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "QUEUE",
                fontFamily = SpaceMono,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.fg,
                letterSpacing = 0.88.sp,
                lineHeight = 22.sp,
            )
            Text(
                text = "$trackCount tracks · ${totalDurationMs.toQueueDuration()}",
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                color = colors.muted2,
                letterSpacing = 1.26.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add to queue",
                    tint = colors.muted2,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
private fun NowPlayingCard(
    track: Track,
    colors: QueueChromeColors,
    themeMode: ThemeMode,
    positionMs: Long,
    durationMs: Long,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 16.dp)
            .clip(PanelShape)
            .background(colors.playingBg)
            .border(1.dp, colors.playingBorder, PanelShape)
            .drawBehind {
                drawRect(color = colors.accent, size = Size(2.dp.toPx(), size.height))
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(PanelShape)
                    .background(colors.surface2)
                    .border(1.dp, colors.border2, PanelShape),
            )
            NowPlayingEqBars(
                color = colors.accent,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (themeMode == ThemeMode.MONOCHROME_LIGHT) {
                            colors.bg.copy(alpha = 0.7f)
                        } else {
                            Color.Black.copy(alpha = 0.55f)
                        }
                    )
                    .padding(start = 4.dp, end = 4.dp, top = 3.dp, bottom = 2.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Now Playing",
                fontFamily = SpaceMono,
                fontSize = 8.sp,
                color = colors.accent,
                letterSpacing = 1.44.sp,
                lineHeight = 10.sp,
            )
            Text(
                text = track.title,
                fontFamily = DmSans,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = track.artist,
                fontFamily = SpaceMono,
                fontSize = 9.5.sp,
                color = colors.muted2,
                letterSpacing = 0.57.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
            MiniScrubber(
                colors = colors,
                positionMs = positionMs,
                durationMs = durationMs,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

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
        listOf(8.dp, 12.dp, 6.dp).forEachIndexed { index, maxHeight ->
            val scale = remember { Animatable(0.25f) }
            LaunchedEffect(index) {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 700,
                            delayMillis = index * 180,
                            easing = LinearEasing,
                        ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                )
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(maxHeight * scale.value)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun MiniScrubber(
    colors: QueueChromeColors,
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    val progress = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.border2),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(colors.progressFill),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = positionMs.coerceAtLeast(0L).toMmSs(),
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                color = colors.muted,
                letterSpacing = 0.36.sp,
            )
            Text(
                text = durationMs.coerceAtLeast(0L).toMmSs(),
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                color = colors.muted,
                letterSpacing = 0.36.sp,
            )
        }
    }
}

@Composable
private fun UpNextHeader(
    colors: QueueChromeColors,
    remainingCount: Int,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
            Text(
                text = "UP NEXT",
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = colors.muted,
                letterSpacing = 1.8.sp,
                lineHeight = 12.sp,
            )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "$remainingCount remaining",
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                color = colors.muted,
                letterSpacing = 0.9.sp,
            )
            Text(
                text = "CLEAR",
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = colors.muted2,
                letterSpacing = 0.9.sp,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClear,
                    )
                    .padding(vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun QueueList(
    colors: QueueChromeColors,
    upNext: List<Track>,
    indexOffset: Int,
    onPlay: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val itemHeightPx = with(LocalDensity.current) { RowHeight.toPx() }

    if (upNext.isEmpty()) {
        EmptyQueueState(colors = colors, modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        itemsIndexed(
            items = upNext,
            key = { _, track -> track.id },
        ) { localIndex, track ->
            val absoluteIndex = localIndex + indexOffset
            val isDragging = draggingIndex == localIndex

            QueueRowItem(
                colors = colors,
                track = track,
                position = absoluteIndex + 1,
                showTopDivider = localIndex > 0,
                isDragging = isDragging,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDragging) {
                            Modifier.offset(y = with(LocalDensity.current) { dragOffset.toDp() })
                        } else {
                            Modifier
                        },
                    )
                    .pointerInput(localIndex, upNext.size) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingIndex = localIndex
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                val targetLocal = (localIndex + (dragOffset / itemHeightPx).roundToInt())
                                    .coerceIn(0, upNext.lastIndex)
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
        }
    }
}

@Composable
private fun QueueRowItem(
    colors: QueueChromeColors,
    track: Track,
    position: Int,
    showTopDivider: Boolean,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            isDragging -> 1.01f
            pressed -> 0.985f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "queue_row_scale",
    )

    Row(
        modifier = modifier
            .height(RowHeight)
            .scale(scale)
            .background(if (isDragging) colors.accentDim else colors.bg)
            .drawBehind {
                if (showTopDivider) {
                    drawLine(
                        color = colors.border,
                        start = Offset(68.dp.toPx(), 0f),
                        end = Offset(size.width - 22.dp.toPx(), 0f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPlay()
                },
            )
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DragHandle(colors = colors, modifier = Modifier.width(18.dp))

        Text(
            text = position.toString().padStart(2, '0'),
            fontFamily = SpaceMono,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = colors.muted,
            letterSpacing = 0.4.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(18.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontFamily = DmSans,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.5.sp,
            )
            Text(
                text = track.artist,
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                color = colors.muted2,
                letterSpacing = 0.54.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Text(
            text = track.durationMs.toMmSs(),
            fontFamily = SpaceMono,
            fontSize = 10.sp,
            color = colors.muted,
            letterSpacing = 0.4.sp,
        )

        val removeInteractionSource = remember { MutableInteractionSource() }
        val isRemovePressed by removeInteractionSource.collectIsPressedAsState()
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = "Remove from queue",
                tint = if (isRemovePressed) colors.removeHover else colors.removeColor,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Composable
private fun DragHandle(
    colors: QueueChromeColors,
    modifier: Modifier = Modifier,
) {
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
                    .background(colors.dragHandle),
            )
        }
    }
}

@Composable
private fun QueueFooter(
    colors: QueueChromeColors,
    totalDurationMs: Long,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = colors.border,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(start = 22.dp, end = 22.dp, top = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Total",
            fontFamily = SpaceMono,
            fontSize = 9.sp,
            color = colors.muted,
            letterSpacing = 1.08.sp,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = totalDurationMs.toQueueDuration(),
            fontFamily = SpaceMono,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = colors.muted2,
            letterSpacing = 0.9.sp,
        )
    }
}

@Composable
private fun EmptyQueueState(
    colors: QueueChromeColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 22.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Canvas(modifier = Modifier.size(42.dp)) {
            val stroke = 1.6.dp.toPx()
            repeat(3) { index ->
                val y = 10.dp.toPx() + index * 10.dp.toPx()
                drawLine(
                    color = colors.muted.copy(alpha = 0.42f),
                    start = Offset(12.dp.toPx(), y),
                    end = Offset(size.width - 8.dp.toPx(), y),
                    strokeWidth = stroke,
                )
                drawCircle(
                    color = colors.muted.copy(alpha = 0.42f),
                    radius = 1.6.dp.toPx(),
                    center = Offset(6.dp.toPx(), y),
                )
            }
        }
        Text(
            text = "Queue empty",
            fontFamily = SpaceMono,
            fontSize = 10.sp,
            color = colors.muted,
            letterSpacing = 1.4.sp,
        )
    }
}

@Composable
private fun FadeUp(
    delayMillis: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    val y = remember { Animatable(10f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(durationMillis = 420, delayMillis = delayMillis))
    }
    LaunchedEffect(Unit) {
        y.animateTo(0f, tween(durationMillis = 420, delayMillis = delayMillis))
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = y.value
        },
    ) {
        content()
    }
}

private fun Long.toQueueDuration(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m ${seconds}s"
    }
}
