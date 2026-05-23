package io.sylphy.app.ui.screens.player

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sylphy.app.R
import io.sylphy.app.data.model.RepeatMode
import io.sylphy.app.data.model.ThemeMode
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.components.player.rememberPremiumDiscSpinState
import io.sylphy.app.ui.theme.DmSans
import io.sylphy.app.ui.theme.PlayerChromeColors
import io.sylphy.app.ui.theme.PlayerTheme
import io.sylphy.app.ui.theme.SpaceMono

@Composable
fun BlurredArtBackground(
    artworkUri: String?,
    themeMode: ThemeMode,
    colors: PlayerChromeColors,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    )
}

@Composable
fun VinylArtwork(
    artworkUri: String?,
    isPlaying: Boolean,
    colors: PlayerChromeColors,
    modifier: Modifier = Modifier
) {
    val discSize = 284.dp
    val artSize  = 172.dp
    val spindleSize = 14.dp
    val spinState = rememberPremiumDiscSpinState(isPlaying = isPlaying)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(discSize)
            .drawBehind {
                drawCircle(
                    color = colors.fg.copy(alpha = 0.035f),
                    radius = size.minDimension * 0.51f,
                    center = Offset(size.width / 2f, size.height / 2f),
                )
                drawCircle(
                    color = colors.bg.copy(alpha = 0.55f),
                    radius = size.minDimension * 0.5f,
                    center = Offset(size.width / 2f, size.height / 2f + 8.dp.toPx()),
                )
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = spinState.rotationDegrees
                }
                .clip(CircleShape)
                .background(colors.discOuter)
                .border(1.dp, colors.border2, CircleShape),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.minDimension / 2f
                var r = 30.dp.toPx()
                while (r < maxRadius) {
                    drawCircle(
                        color = colors.groove,
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    r += 5.dp.toPx()
                }
            }

            Box(
                modifier = Modifier
                    .size(spindleSize)
                    .clip(CircleShape)
                    .background(colors.bg)
                    .border(2.dp, colors.border2, CircleShape)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            drawCircle(
                color = colors.fg.copy(alpha = 0.055f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )
            drawArc(
                color = colors.fg.copy(alpha = 0.07f),
                startAngle = -52f,
                sweepAngle = 72f,
                useCenter = false,
                topLeft = Offset.Zero,
                size = size,
                style = Stroke(width = 1.2.dp.toPx()),
            )
        }
    }
}

@Composable
fun TrackInfoRow(
    track: Track,
    isFavourite: Boolean,
    colors: PlayerChromeColors,
    onFavouriteToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontFamily = DmSans,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = colors.fg,
                letterSpacing = (-0.44).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${track.artist} · ${track.album}",
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = colors.muted2,
                letterSpacing = 1.1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(12.dp))

        val heartScale by animateFloatAsState(
            targetValue = if (isFavourite) 1.15f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "heartScale"
        )
        IconButton(
            onClick = onFavouriteToggle,
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer { scaleX = heartScale; scaleY = heartScale }
        ) {
            Icon(
                imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favourite",
                tint = if (isFavourite) colors.accent else colors.muted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun ScrubberSection(
    elapsedMs: Long,
    durationMs: Long,
    colors: PlayerChromeColors,
    onSeek: (Long) -> Unit
) {
    val progress = if (durationMs > 0) elapsedMs.toFloat() / durationMs else 0f

    Column {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val trackWidth = maxWidth

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.border2)
                    .pointerInput(durationMs) {
                        detectTapGestures { offset ->
                            val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((ratio * durationMs).toLong())
                        }
                    }
                    .pointerInput(durationMs) {
                        detectHorizontalDragGestures { change, _ ->
                            val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                            onSeek((ratio * durationMs).toLong())
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.progress)
                )
            }

            val thumbOffset = (trackWidth * progress) - 7.dp
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset.coerceAtLeast(0.dp), y = (-5.5).dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(colors.fg)
                    .pointerInput(durationMs) {
                        detectHorizontalDragGestures { change, _ ->
                            val trackPx = trackWidth.toPx()
                            val ratio = (change.position.x / trackPx).coerceIn(0f, 1f)
                            onSeek((ratio * durationMs).toLong())
                        }
                    }
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedMs.toTimestamp(),
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                color = colors.muted
            )
            Text(
                text = durationMs.toTimestamp(),
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                color = colors.muted
            )
        }
    }
}

fun Long.toTimestamp(): String {
    val totalSecs = this / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}

@Composable
fun ControlsRow(
    isPlaying: Boolean,
    repeatMode: RepeatMode,
    colors: PlayerChromeColors,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                tint = colors.fgDim,
                modifier = Modifier.size(28.dp)
            )
        }

        val playScale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "playScale"
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(68.dp)
                .graphicsLayer { scaleX = playScale; scaleY = playScale }
                .clip(RoundedCornerShape(3.dp))
                .background(colors.playBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onPlayPause() }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = colors.playFg,
                modifier = Modifier.size(26.dp)
            )
        }

        IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = colors.fgDim,
                modifier = Modifier.size(28.dp)
            )
        }

        ActiveDotIconButton(
            active = repeatMode != RepeatMode.OFF,
            colors = colors,
            onClick = onRepeat,
            contentDescription = "Repeat"
        ) {
            Icon(
                painter = painterResource(
                    if (repeatMode == RepeatMode.ONE) R.drawable.ic_repeat_one
                    else R.drawable.ic_repeat
                ),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun SecondaryRow(
    speed: Float,
    volume: Float,
    colors: PlayerChromeColors,
    onCycleSpeed: () -> Unit,
    onVolumeChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(PlayerTheme.Surface2)
                .border(1.dp, PlayerTheme.Border, RoundedCornerShape(3.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCycleSpeed,
                )
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(
                text = "${"%.1f".format(speed)}×",
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = colors.muted,
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                contentDescription = "Volume",
                tint = colors.muted,
                modifier = Modifier.size(16.dp),
            )

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.border2)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                            onVolumeChange(ratio)
                        }
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                            onVolumeChange(ratio)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(volume)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PlayerTheme.WhiteDim)
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Volume",
                tint = colors.muted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
@Composable
fun ActiveDotIconButton(
    active: Boolean,
    colors: PlayerChromeColors,
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Box(contentAlignment = Alignment.BottomCenter) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (active) colors.accent else colors.fgDim
            ) {
                content()
            }
        }
        if (active) {
            Box(
                modifier = Modifier
                    .offset(y = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
            )
        }
    }
}

enum class BottomNavTab { LIBRARY, PLAYER, QUEUE }

@Composable
fun BottomNav(
    activeTab: BottomNavTab,
    onLibrary: () -> Unit,
    onPlayer: () -> Unit,
    onQueue: () -> Unit,
) {
    val borderColor = PlayerTheme.Border
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .navigationBarsPadding()
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        listOf(
            Triple(BottomNavTab.LIBRARY, "LIBRARY", Icons.Filled.LibraryMusic),
            Triple(BottomNavTab.PLAYER,  "PLAYER",  Icons.Filled.PlayArrow),
            Triple(BottomNavTab.QUEUE,   "QUEUE",   Icons.AutoMirrored.Filled.QueueMusic),
        ).forEach { (tab, label, icon) ->
            val isActive = tab == activeTab
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        when (tab) {
                            BottomNavTab.LIBRARY -> onLibrary()
                            BottomNavTab.PLAYER  -> onPlayer()
                            BottomNavTab.QUEUE   -> onQueue()
                        }
                    }
            ) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(PlayerTheme.Red)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) PlayerTheme.White else PlayerTheme.Muted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontFamily = SpaceMono,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isActive) PlayerTheme.White else PlayerTheme.Muted,
                        letterSpacing = 1.62.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GrainOverlay(colors: PlayerChromeColors) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 3.dp.toPx()
        var y = 0f
        var row = 0
        while (y < size.height) {
            var x = if (row % 2 == 0) 0f else step / 2f
            while (x < size.width) {
                drawCircle(
                    color = colors.fg.copy(alpha = 0.018f),
                    radius = 0.35.dp.toPx(),
                    center = Offset(x, y),
                )
                x += step
            }
            y += step
            row++
        }
    }
}

@Composable
fun TopNav(
    colors: PlayerChromeColors,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.muted2,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = "Now Playing",
            fontFamily = SpaceMono,
            fontSize = 9.sp,
            color = colors.fgDim,
            letterSpacing = 1.98.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint = colors.muted2,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
