package io.sylphy.app.ui.screens.player

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.sylphy.app.R
import io.sylphy.app.data.model.RepeatMode
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.theme.DmSans
import io.sylphy.app.ui.theme.PlayerTheme
import io.sylphy.app.ui.theme.SpaceMono
import kotlinx.coroutines.delay

@Composable
fun BlurredArtBackground(artworkUri: String?) {
    val painter = rememberAsyncImagePainter(artworkUri)

    var currentUri by remember { mutableStateOf(artworkUri) }
    var targetAlpha by remember { mutableFloatStateOf(1f) }
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(300),
        label = "bgCrossfade"
    )
    LaunchedEffect(artworkUri) {
        targetAlpha = 0f
        delay(150)
        currentUri = artworkUri
        targetAlpha = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
                scaleX = 1.15f
                scaleY = 1.15f
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(currentUri),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(60.dp)
                .graphicsLayer {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        renderEffect = BlurEffect(60f, 60f)
                    }
                }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.82f)
                )
        )
    }
}

@Composable
fun VinylArtwork(
    artworkUri: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val discSize = 284.dp
    val artSize  = 172.dp
    val spindleSize = 14.dp

    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotation.animateTo(
                targetValue = rotation.value + 3600f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 120_000,
                        easing = LinearEasing
                    ),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                )
            )
        } else {
            rotation.stop()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(discSize)
            .graphicsLayer { rotationZ = rotation.value }
            .clip(CircleShape)
            .background(PlayerTheme.Surface)
            .border(1.dp, PlayerTheme.Border, CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f
            val grooveColor = Color.White.copy(alpha = 0.025f)
            var r = 30.dp.toPx()
            while (r < maxRadius) {
                drawCircle(
                    color = grooveColor,
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
                r += 5.dp.toPx()
            }
        }

        Box(
            modifier = Modifier
                .size(artSize)
                .graphicsLayer { rotationZ = -rotation.value }
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    color = PlayerTheme.Surface,
                    shape = CircleShape
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album art",
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_sylphy_background), // placeholder
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .size(spindleSize)
                .graphicsLayer { rotationZ = -rotation.value }
                .clip(CircleShape)
                .background(PlayerTheme.Black)
                .border(2.dp, PlayerTheme.Border, CircleShape)
        )
    }
}

@Composable
fun TrackInfoRow(
    track: Track,
    isFavourite: Boolean,
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
                color = PlayerTheme.White,
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
                color = PlayerTheme.Muted,
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
                tint = if (isFavourite) PlayerTheme.Red else PlayerTheme.Muted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun QualityBadgeRow(track: Track) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        // Derive format from mimeType or use a default
        val format = when {
            track.mimeType?.contains("flac", ignoreCase = true) == true -> "FLAC"
            track.mimeType?.contains("wav", ignoreCase = true) == true -> "WAV"
            track.mimeType?.contains("aac", ignoreCase = true) == true -> "AAC"
            track.mimeType?.contains("ogg", ignoreCase = true) == true -> "OGG"
            else -> "MP3"
        }
        QualityBadge(label = format, isAccent = false)
        
        track.bitRate?.let {
            QualityBadge(label = "${it / 1000} kbps", isAccent = true)
        }
        
        track.albumArtist?.let {
            if (it != track.artist) {
                QualityBadge(label = it, isAccent = false)
            }
        }
    }
}

@Composable
fun QualityBadge(label: String, isAccent: Boolean) {
    val borderColor = if (isAccent) PlayerTheme.Red else PlayerTheme.Border
    val textColor   = if (isAccent) PlayerTheme.Red else PlayerTheme.Muted

    Box(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = SpaceMono,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 1.08.sp
        )
    }
}

@Composable
fun ScrubberSection(
    elapsedMs: Long,
    durationMs: Long,
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
                    .background(PlayerTheme.Border)
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
                        .background(PlayerTheme.Red)
                )
            }

            val thumbOffset = (trackWidth * progress) - 7.dp
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset.coerceAtLeast(0.dp), y = (-5.5).dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(PlayerTheme.White)
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
                color = PlayerTheme.Muted
            )
            Text(
                text = durationMs.toTimestamp(),
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                color = PlayerTheme.Muted
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
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActiveDotIconButton(
            active = isShuffle,
            onClick = onShuffle,
            contentDescription = "Shuffle"
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shuffle),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        }

        IconButton(onClick = onPrevious, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                tint = PlayerTheme.WhiteDim,
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
                .background(PlayerTheme.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onPlayPause() }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = PlayerTheme.Black,
                modifier = Modifier.size(26.dp)
            )
        }

        IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = PlayerTheme.WhiteDim,
                modifier = Modifier.size(28.dp)
            )
        }

        ActiveDotIconButton(
            active = repeatMode != RepeatMode.OFF,
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
fun ActiveDotIconButton(
    active: Boolean,
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
                LocalContentColor provides if (active) PlayerTheme.Red else PlayerTheme.WhiteDim
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
                    .background(PlayerTheme.Red)
            )
        }
    }
}

@Composable
fun SecondaryRow(
    speed: Float,
    volume: Float,
    onSpeedCycle: () -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(PlayerTheme.Surface2)
                .border(1.dp, PlayerTheme.Border, RoundedCornerShape(3.dp))
                .clickable { onSpeedCycle() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "${if (speed == speed.toLong().toFloat()) speed.toInt() else speed}×",
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PlayerTheme.Muted,
                letterSpacing = 1.1.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                contentDescription = null,
                tint = PlayerTheme.Muted,
                modifier = Modifier.size(16.dp)
            )

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PlayerTheme.Border)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                onVolumeChange((offset.x / size.width).coerceIn(0f, 1f))
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, _ ->
                                onVolumeChange((change.position.x / size.width).coerceIn(0f, 1f))
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
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = PlayerTheme.Muted,
                modifier = Modifier.size(16.dp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .drawBehind {
                drawLine(
                    color = Color(0xFF2A2A2A),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        listOf(
            Triple(BottomNavTab.LIBRARY, "LIBRARY", Icons.Filled.LibraryMusic),
            Triple(BottomNavTab.PLAYER,  "PLAYER",  Icons.Filled.PlayArrow),
            Triple(BottomNavTab.QUEUE,   "QUEUE",   Icons.Filled.QueueMusic),
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
                    modifier = Modifier.fillMaxSize().padding(bottom = 10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) PlayerTheme.White else PlayerTheme.Muted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontFamily = SpaceMono,
                        fontSize = 9.sp,
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
fun GrainOverlay() {
    // Optional: Add a subtle grain overlay if desired
}

@Composable
fun TopNav(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious, // use a back icon if available
                contentDescription = "Back",
                tint = PlayerTheme.White
            )
        }
    }
}
