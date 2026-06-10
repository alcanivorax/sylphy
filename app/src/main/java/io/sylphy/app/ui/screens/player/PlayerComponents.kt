package io.sylphy.app.ui.screens.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import io.sylphy.app.R
import io.sylphy.app.data.model.RepeatMode
import io.sylphy.app.data.model.ThemeMode
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.components.player.rememberPremiumDiscSpinState
import io.sylphy.app.ui.theme.DmSans
import io.sylphy.app.ui.theme.PlayerChromeColors
import io.sylphy.app.ui.theme.SpaceMono
import io.sylphy.app.ui.theme.libraryChromeColors

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
    themeMode: ThemeMode,
    seed: String,
    modifier: Modifier = Modifier
) {
    val discSize = 258.dp
    val artSize = if (themeMode == ThemeMode.MONOCHROME_LIGHT) 80.dp else 110.dp
    val spindleSize = if (themeMode == ThemeMode.MONOCHROME_LIGHT) 8.dp else if (themeMode == ThemeMode.MONOCHROME_DARK) 8.dp else 10.dp
    val spinState = rememberPremiumDiscSpinState(isPlaying = isPlaying)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(discSize)
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
                .border(1.dp, colors.discBorder, CircleShape),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.minDimension / 2f
                var r = 4.dp.toPx()
                while (r < maxRadius) {
                    drawCircle(
                        color = colors.groove,
                        radius = r,
                        center = center,
                        style = Stroke(width = 0.5.dp.toPx())
                    )
                    r += 4.5.dp.toPx()
                }
            }

            when (themeMode) {
                ThemeMode.NOTHING_OS -> {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(1.dp, colors.discBorder, CircleShape)
                            .drawBehind {
                                drawCircle(
                                    color = Color(0xFF1C1C1C),
                                    radius = size.minDimension / 2f - 8.dp.toPx(),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    style = Stroke(width = 1.dp.toPx()),
                                )
                            },
                    )
                }
                ThemeMode.MONOCHROME_DARK -> {
                    Ring(size = 192.dp, color = Color(0xFF1C1C1C))
                    Ring(size = 148.dp, color = Color(0xFF1C1C1C))
                }
                ThemeMode.MONOCHROME_LIGHT -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(130.dp)
                            .background(colors.labelBg)
                            .border(1.dp, colors.discBorder)
                            .drawBehind {
                                drawRect(
                                    color = colors.labelBorder,
                                    topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                                    size = Size(size.width - 20.dp.toPx(), size.height - 20.dp.toPx()),
                                    style = Stroke(width = 1.dp.toPx()),
                                )
                                drawRect(
                                    color = colors.labelBorder,
                                    topLeft = Offset(20.dp.toPx(), 20.dp.toPx()),
                                    size = Size(size.width - 40.dp.toPx(), size.height - 40.dp.toPx()),
                                    style = Stroke(width = 1.dp.toPx()),
                                )
                            },
                    ) {}
                }
            }

            Box(
                modifier = Modifier
                    .size(artSize)
                    .graphicsLayer { rotationZ = -spinState.rotationDegrees }
                    .clip(if (themeMode == ThemeMode.MONOCHROME_LIGHT) RoundedCornerShape(0.dp) else CircleShape)
                    .border(1.dp, colors.discBorder, if (themeMode == ThemeMode.MONOCHROME_LIGHT) RoundedCornerShape(0.dp) else CircleShape),
            ) {
                GeneratedArtwork(
                    seed = seed,
                    colors = colors,
                    themeMode = themeMode,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Box(
                modifier = Modifier
                    .size(spindleSize)
                    .clip(if (themeMode == ThemeMode.MONOCHROME_LIGHT) RoundedCornerShape(0.dp) else CircleShape)
                    .background(colors.accent)
            )
        }
    }
}

@Composable
private fun Ring(size: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, color, CircleShape),
    )
}

@Composable
fun GeneratedArtwork(
    seed: String,
    colors: PlayerChromeColors,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier,
) {
    val hash = remember(seed) { stableHash(seed) }
    val background = when (themeMode) {
        ThemeMode.NOTHING_OS -> Color(0xFF111111)
        ThemeMode.MONOCHROME_DARK -> Color(0xFF0E0E0E)
        ThemeMode.MONOCHROME_LIGHT -> Color(0xFFE2DFD9)
    }
    val stroke = when (themeMode) {
        ThemeMode.NOTHING_OS -> Color(0xFF2A2A2A)
        ThemeMode.MONOCHROME_DARK -> Color(0xFF222222)
        ThemeMode.MONOCHROME_LIGHT -> Color(0xFFBFBBB4)
    }
    val mark = when (themeMode) {
        ThemeMode.NOTHING_OS -> Color(0xE6FF3B3B)
        ThemeMode.MONOCHROME_DARK -> Color(0xB3E0E0E0)
        ThemeMode.MONOCHROME_LIGHT -> Color(0x8C1A1816)
    }

    Canvas(modifier = modifier.background(background)) {
        val s = size.minDimension
        val center = Offset(size.width / 2f, size.height / 2f)
        when (hash % 5) {
            0 -> {
                val gap = (8 + (hash % 4) * 3).dp.toPx()
                var x = -s
                while (x < s * 2f) {
                    drawLine(stroke.copy(alpha = 0.5f), Offset(x, 0f), Offset(x + s, s), 1.dp.toPx())
                    x += gap
                }
                val start = (hash % (s.toInt().coerceAtLeast(1) / 2).coerceAtLeast(1)).toFloat()
                drawLine(mark, Offset(start, 0f), Offset(start + s, s), 2.dp.toPx())
            }
            1 -> {
                val count = 4 + hash % 3
                val step = s * 0.44f / count
                for (i in 1..count) {
                    drawCircle(
                        color = stroke.copy(alpha = 0.18f + (i.toFloat() / count) * 0.55f),
                        radius = step * i,
                        center = center,
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
                drawCircle(mark, radius = 3.dp.toPx(), center = center)
            }
            2 -> {
                val cols = 6 + hash % 3
                val gap = s / (cols + 1)
                val rows = (s / gap).toInt().coerceAtLeast(1)
                for (r in 1..rows) {
                    for (c in 1..cols) {
                        val rr = stableHash(seed + r * 31 + c * 17) % 3
                        val alpha = if (rr == 0) 0.6f else if (rr == 1) 0.25f else 0.1f
                        drawCircle(stroke.copy(alpha = alpha), 1.2.dp.toPx(), Offset(gap * c, gap * r))
                    }
                }
                drawCircle(mark, 3.dp.toPx(), Offset(gap * (1 + hash % cols), gap * (1 + (hash shr 4) % rows)))
            }
            3 -> {
                val bars = 16 + hash % 8
                val bw = (s - 8.dp.toPx()) / bars
                for (i in 0 until bars) {
                    val bh = 4.dp.toPx() + (stableHash(seed + i * 7) % (s * 0.52f).toInt().coerceAtLeast(1))
                    drawRect(
                        color = stroke.copy(alpha = 0.18f + if (i % 3 == 0) 0.52f else 0.1f),
                        topLeft = Offset(4.dp.toPx() + i * bw + bw * 0.1f, center.y - bh / 2f),
                        size = Size(bw * 0.75f, bh),
                    )
                }
                val hi = hash % bars
                val hh = 4.dp.toPx() + (stableHash(seed + hi * 7) % (s * 0.52f).toInt().coerceAtLeast(1))
                drawRect(
                    color = mark,
                    topLeft = Offset(4.dp.toPx() + hi * bw + bw * 0.1f, center.y - hh / 2f),
                    size = Size(bw * 0.75f, hh),
                )
            }
            else -> {
                val count = 4 + hash % 3
                val step = s / (count + 1)
                for (i in 1..count) {
                    val y = step * i
                    val halfWidth = s * 0.36f
                    val halfHeight = s * 0.08f
                    drawLine(stroke.copy(alpha = 0.1f + (i.toFloat() / count) * 0.48f), Offset(center.x - halfWidth, y + halfHeight), Offset(center.x, y - halfHeight), 1.2.dp.toPx())
                    drawLine(stroke.copy(alpha = 0.1f + (i.toFloat() / count) * 0.48f), Offset(center.x, y - halfHeight), Offset(center.x + halfWidth, y + halfHeight), 1.2.dp.toPx())
                }
                val y = step * (1 + hash % count)
                val halfWidth = s * 0.36f
                val halfHeight = s * 0.08f
                drawLine(mark, Offset(center.x - halfWidth, y + halfHeight), Offset(center.x, y - halfHeight), 2.dp.toPx())
                drawLine(mark, Offset(center.x, y - halfHeight), Offset(center.x + halfWidth, y + halfHeight), 2.dp.toPx())
            }
        }
    }
}

private fun stableHash(value: String): Int {
    var h = 5381
    value.forEach { h = ((h shl 5) + h) xor it.code }
    return kotlin.math.abs(h)
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
                letterSpacing = 0.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${track.artist}",
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
fun QualityBadgeRow(
    track: Track,
    colors: PlayerChromeColors,
) {
    val mimeLabel = when {
        track.mimeType?.contains("flac", ignoreCase = true) == true -> "FLAC"
        track.mimeType?.contains("wav", ignoreCase = true) == true -> "WAV"
        track.mimeType?.contains("aac", ignoreCase = true) == true -> "AAC"
        else -> "MP3"
    }
    val bitRateLabel = track.bitRate?.takeIf { it > 0 }?.let { "${it / 1000} kbps" } ?: "320 kbps"
    val sourceLabel = track.albumArtist?.takeIf { it.isNotBlank() }
        ?: track.album.takeIf { it.isNotBlank() }
        ?: "Local"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        PlayerBadge(text = mimeLabel, colors = colors, highlight = false)
        PlayerBadge(text = bitRateLabel, colors = colors, highlight = true)
        PlayerBadge(text = sourceLabel, colors = colors, highlight = false)
    }
}

@Composable
private fun PlayerBadge(
    text: String,
    colors: PlayerChromeColors,
    highlight: Boolean,
) {
    Text(
        text = text.uppercase(),
        fontFamily = SpaceMono,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = if (highlight) colors.accent else colors.muted2,
        letterSpacing = 1.02.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .border(1.dp, if (highlight) colors.accent else colors.border2, RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
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
                    .size(13.dp)
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
                fontSize = 10.5.sp,
                color = colors.muted
            )
            Text(
                text = durationMs.toTimestamp(),
                fontFamily = SpaceMono,
                fontSize = 10.5.sp,
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
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    colors: PlayerChromeColors,
    onShuffle: () -> Unit,
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
        ActiveDotIconButton(
            active = shuffleEnabled,
            colors = colors,
            onClick = onShuffle,
            contentDescription = "Shuffle"
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shuffle),
                contentDescription = null,
                tint = if (shuffleEnabled) colors.accent else colors.fgDim,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onPrevious, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                tint = colors.fgDim,
                modifier = Modifier.size(26.dp)
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
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = colors.fgDim,
                modifier = Modifier.size(26.dp)
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
            .padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surface2)
                .border(1.dp, colors.border2, RoundedCornerShape(3.dp))
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
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = colors.muted2,
            )
        }

        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                contentDescription = "Volume",
                tint = colors.muted,
                modifier = Modifier.size(14.dp),
            )

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
                        .background(colors.volumeFill)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Volume",
                tint = colors.muted,
                modifier = Modifier.size(14.dp),
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
    themeMode: ThemeMode,
    onLibrary: () -> Unit,
    onPlayer: () -> Unit,
    onQueue: () -> Unit,
) {
    val navColors = libraryChromeColors(themeMode)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .drawBehind {
                drawLine(
                    color = navColors.border,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        listOf(
            BottomNavTab.LIBRARY to "LIBRARY",
            BottomNavTab.PLAYER to "PLAYER",
            BottomNavTab.QUEUE to "QUEUE",
        ).forEach { (tab, label) ->
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
                            .background(navColors.accent)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)
                ) {
                    BottomNavGlyph(
                        tab = tab,
                        color = if (isActive) navColors.fg else navColors.muted2,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontFamily = SpaceMono,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isActive) navColors.fg else navColors.muted2,
                        letterSpacing = 1.62.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavGlyph(
    tab: BottomNavTab,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val sx = size.width / 24f
        val sy = size.height / 24f
        fun p(x: Float, y: Float) = Offset(x * sx, y * sy)
        val stroke = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        when (tab) {
            BottomNavTab.LIBRARY -> {
                drawLine(color, p(6.5f, 17f), p(20f, 17f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, p(6.5f, 2f), p(20f, 2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, p(20f, 2f), p(20f, 22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, p(6.5f, 2f), p(6.5f, 17f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, p(6.5f, 22f), p(20f, 22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = p(4f, 17f),
                    size = Size(5f * sx, 5f * sy),
                    style = stroke,
                )
            }
            BottomNavTab.PLAYER -> {
                drawCircle(color = color, radius = 10f * sx, center = p(12f, 12f), style = stroke)
                drawCircle(color = color, radius = 3f * sx, center = p(12f, 12f), style = stroke)
            }
            BottomNavTab.QUEUE -> {
                listOf(6f, 12f, 18f).forEach { y ->
                    drawLine(color, p(8f, y), p(21f, y), strokeWidth = stroke.width, cap = StrokeCap.Round)
                    drawLine(color, p(3f, y), p(3.01f, y), strokeWidth = stroke.width, cap = StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
fun MockStatusBar(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(start = 26.dp, end = 26.dp, top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "9:41",
            fontFamily = SpaceMono,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 0.48.sp,
            lineHeight = 14.sp,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SignalIcon(color)
            WifiGlyph(color)
            BatteryIcon(color)
        }
    }
}

@Composable
fun HidePlatformStatusBar() {
    val view = LocalView.current
    DisposableEffect(view) {
        val activity = view.context.findActivity()
        val controller = activity?.window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.hide(WindowInsetsCompat.Type.statusBars())
        onDispose {
            controller?.show(WindowInsetsCompat.Type.statusBars())
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun SignalIcon(color: Color) {
    Canvas(modifier = Modifier.size(width = 15.dp, height = 11.dp)) {
        val bars = listOf(5.dp, 7.dp, 9.dp, 11.dp)
        bars.forEachIndexed { index, height ->
            drawRoundRect(
                color = color.copy(alpha = if (index == bars.lastIndex) 0.25f else 1f),
                topLeft = Offset(index * 4.dp.toPx(), size.height - height.toPx()),
                size = Size(3.dp.toPx(), height.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.5.dp.toPx()),
            )
        }
    }
}

@Composable
private fun WifiGlyph(color: Color) {
    Text(
        text = "⌒",
        fontFamily = SpaceMono,
        fontSize = 16.sp,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(15.dp),
        lineHeight = 11.sp,
    )
}

@Composable
private fun BatteryIcon(color: Color) {
    Canvas(modifier = Modifier.size(width = 24.dp, height = 11.dp)) {
        drawRoundRect(
            color = color.copy(alpha = 0.4f),
            topLeft = Offset(0.5.dp.toPx(), 0.5.dp.toPx()),
            size = Size(20.dp.toPx(), 10.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5.dp.toPx()),
            style = Stroke(width = 1.dp.toPx()),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
            size = Size(15.dp.toPx(), 7.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx()),
        )
        drawRect(
            color = color.copy(alpha = 0.35f),
            topLeft = Offset(22.dp.toPx(), 3.5.dp.toPx()),
            size = Size(2.dp.toPx(), 4.dp.toPx()),
        )
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
