AI Agent Implementation Note — Player Screen
Target: Kotlin + Jetpack Compose + Material3
Reference design: player.html

PROJECT DEPENDENCIES
toml# libs.versions.toml
[versions]
compose-bom = "2024.09.00"
coil = "2.7.0"
accompanist = "0.34.0"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }
compose-animation = { group = "androidx.compose.animation", name = "animation" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
accompanist-systemuicontroller = { group = "com.google.accompanist", name = "accompanist-systemuicontroller", version.ref = "accompanist" }

THEME & DESIGN TOKENS
kotlin// ui/theme/Theme.kt
object NothingPlayerTheme {
    val Black       = Color(0xFF000000)
    val OffBlack    = Color(0xFF0D0D0D)
    val Surface     = Color(0xFF141414)
    val Surface2    = Color(0xFF1E1E1E)
    val Border      = Color(0xFF2A2A2A)
    val Muted       = Color(0xFF555555)
    val White       = Color(0xFFF0F0F0)
    val WhiteDim    = Color(0x8CF0F0F0)  // 55% opacity
    val Red         = Color(0xFFFF3B3B)
    val RedDim      = Color(0x30FF3B3B)  // 18% opacity
}

// Typography — Space Mono + DM Sans
// Add fonts to res/font/:
//   space_mono_regular.ttf, space_mono_bold.ttf, dm_sans_regular.ttf, dm_sans_medium.ttf

val SpaceMono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold)
)
val DmSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium)
)

DATA MODEL
kotlin// domain/model/Track.kt
data class Track(
    val id: String,
    val title: String,           // clean name, e.g. "Tose Naina"
    val artist: String,          // e.g. "Arijit Singh"
    val album: String,           // e.g. "Mickey Virus"
    val source: String,          // e.g. "T-Series"
    val artworkUri: Uri?,
    val durationMs: Long,
    val format: AudioFormat,
    val bitrateKbps: Int?,       // null = unknown, omit badge
)

enum class AudioFormat { MP3, FLAC, AAC, OGG, WAV }

// Format display label — never show "MPEG"
fun AudioFormat.displayLabel() = when (this) {
    AudioFormat.MP3  -> "MP3"
    AudioFormat.FLAC -> "FLAC"
    AudioFormat.AAC  -> "AAC"
    AudioFormat.OGG  -> "OGG"
    AudioFormat.WAV  -> "WAV"
}

// Bitrate badge — only show if known
fun Int.bitrateLabel() = "${this} kbps"

// PlayerState
data class PlayerState(
    val track: Track,
    val elapsedMs: Long,
    val isPlaying: Boolean,
    val isShuffle: Boolean,
    val repeatMode: RepeatMode,
    val isFavourite: Boolean,
    val volumeFraction: Float,   // 0f..1f
    val speedMultiplier: Float,  // 0.5, 0.75, 1.0, 1.25, 1.5, 2.0
)

enum class RepeatMode { OFF, ALL, ONE }

VIEWMODEL
kotlin// ui/player/PlayerViewModel.kt
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val mediaController: MediaControllerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(/* initial */)
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    val speedCycle = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    fun onPlayPause()    { mediaController.togglePlayPause() }
    fun onNext()         { mediaController.skipNext() }
    fun onPrevious()     { mediaController.skipPrevious() }
    fun onSeek(ms: Long) { mediaController.seekTo(ms) }
    fun onShuffleToggle(){ mediaController.toggleShuffle() }
    fun onRepeatCycle()  { mediaController.cycleRepeat() }
    fun onFavouriteToggle() { /* persist to DB */ }
    fun onSpeedCycle()   {
        val current = _state.value.speedMultiplier
        val next = speedCycle[(speedCycle.indexOf(current) + 1) % speedCycle.size]
        mediaController.setSpeed(next)
    }
    fun onVolumeChange(fraction: Float) { mediaController.setVolume(fraction) }
}

ROOT SCREEN COMPOSABLE
kotlin// ui/player/PlayerScreen.kt
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateToLibrary: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Edge-to-edge: transparent status + nav bars
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = false)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(NothingPlayerTheme.Black)
    ) {
        // Layer 0: blurred art background
        BlurredArtBackground(artworkUri = state.track.artworkUri)

        // Layer 1: grain overlay
        GrainOverlay()

        // Layer 2: UI
        Column(modifier = Modifier.fillMaxSize()) {
            TopNav(onBack = onBack)

            // Scrollable body — but content fits; no real scroll needed
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 28.dp)
            ) {
                Spacer(Modifier.height(20.dp))
                VinylArtwork(
                    artworkUri = state.track.artworkUri,
                    isPlaying = state.isPlaying,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(28.dp))
                TrackInfoRow(
                    track = state.track,
                    isFavourite = state.isFavourite,
                    onFavouriteToggle = viewModel::onFavouriteToggle
                )
                Spacer(Modifier.height(10.dp))
                QualityBadgeRow(track = state.track)
                Spacer(Modifier.height(24.dp))
                ScrubberSection(
                    elapsedMs = state.elapsedMs,
                    durationMs = state.track.durationMs,
                    onSeek = viewModel::onSeek
                )
                Spacer(Modifier.height(28.dp))
                ControlsRow(
                    isPlaying = state.isPlaying,
                    isShuffle = state.isShuffle,
                    repeatMode = state.repeatMode,
                    onPlayPause = viewModel::onPlayPause,
                    onNext = viewModel::onNext,
                    onPrevious = viewModel::onPrevious,
                    onShuffle = viewModel::onShuffleToggle,
                    onRepeat = viewModel::onRepeatCycle
                )
                Spacer(Modifier.height(20.dp))
                SecondaryRow(
                    speed = state.speedMultiplier,
                    volume = state.volumeFraction,
                    onSpeedCycle = viewModel::onSpeedCycle,
                    onVolumeChange = viewModel::onVolumeChange
                )
            }

            BottomNav(
                activeTab = BottomNavTab.PLAYER,
                onLibrary = onNavigateToLibrary,
                onQueue = onNavigateToQueue,
                onPlayer = { /* already here */ }
            )
        }
    }
}

BLURRED ART BACKGROUND
kotlin@Composable
fun BlurredArtBackground(artworkUri: Uri?) {
    // Crossfade between tracks: animate alpha on key change
    val painter = rememberAsyncImagePainter(artworkUri)

    // Track changes for crossfade
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
                .blur(60.dp)                          // API 31+
                .graphicsLayer {
                    renderEffect = BlurEffect(         // fallback API 31
                        radiusX = 60f, radiusY = 60f
                    )
                }
        )
        // Dark + desaturate overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = 0.82f)
                )
        )
    }
}

// Note: For API < 31, use RenderScript blur or Coil's BlurTransformation:
// rememberAsyncImagePainter(
//     ImageRequest.Builder(context).data(artworkUri)
//         .transformations(BlurTransformation(context, radius = 25f, sampling = 4f))
//         .build()
// )

VINYL ARTWORK
kotlin@Composable
fun VinylArtwork(
    artworkUri: Uri?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val discSize = 284.dp
    val artSize  = 172.dp
    val spindleSize = 14.dp

    // Rotation animation
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            // Resume from current angle, loop continuously
            rotation.animateTo(
                targetValue = rotation.value + 3600f, // 10 full rotations
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 120_000, // 12s per revolution * 10
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
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
            .background(NothingPlayerTheme.Surface)
            .border(1.dp, NothingPlayerTheme.Border, CircleShape)
    ) {
        // Groove rings — drawn on Canvas
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

        // Album art — counter-rotates to stay upright
        Box(
            modifier = Modifier
                .size(artSize)
                .graphicsLayer { rotationZ = -rotation.value } // counter-rotate
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    color = NothingPlayerTheme.Surface,
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
                error = painterResource(R.drawable.ic_music_note_placeholder),
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }

        // Spindle hole — on top, also counter-rotates
        Box(
            modifier = Modifier
                .size(spindleSize)
                .graphicsLayer { rotationZ = -rotation.value }
                .clip(CircleShape)
                .background(NothingPlayerTheme.Black)
                .border(2.dp, NothingPlayerTheme.Border, CircleShape)
        )
    }
}

TRACK INFO ROW
kotlin@Composable
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
                color = NothingPlayerTheme.White,
                letterSpacing = (-0.44).sp,  // -0.02em at 22sp
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                // Format: "Artist · Album"
                text = "${track.artist} · ${track.album}",
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = NothingPlayerTheme.Muted,
                letterSpacing = 1.1.sp,       // 0.1em
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(12.dp))

        // Favourite button with spring animation
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
                tint = if (isFavourite) NothingPlayerTheme.Red else NothingPlayerTheme.Muted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

QUALITY BADGES
kotlin@Composable
fun QualityBadgeRow(track: Track) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        QualityBadge(label = track.format.displayLabel(), isAccent = false)
        track.bitrateKbps?.let {
            QualityBadge(label = it.bitrateLabel(), isAccent = true)  // red
        }
        if (track.source.isNotBlank()) {
            QualityBadge(label = track.source, isAccent = false)
        }
    }
}

@Composable
fun QualityBadge(label: String, isAccent: Boolean) {
    val borderColor = if (isAccent) NothingPlayerTheme.Red else NothingPlayerTheme.Border
    val textColor   = if (isAccent) NothingPlayerTheme.Red else NothingPlayerTheme.Muted

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
            letterSpacing = 1.08.sp   // 0.12em
        )
    }
}

SCRUBBER
kotlin@Composable
fun ScrubberSection(
    elapsedMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (durationMs > 0) elapsedMs.toFloat() / durationMs else 0f

    Column {
        // Track
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val trackWidth = maxWidth

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NothingPlayerTheme.Border)
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
                // Red fill
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NothingPlayerTheme.Red)
                )
            }

            // Thumb — offset to right edge of fill
            val thumbOffset = (trackWidth * progress) - 7.dp
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset.coerceAtLeast(0.dp), y = (-5.5).dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(NothingPlayerTheme.White)
                    // Expand touch target to 44dp without affecting visual size
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

        // Timestamps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedMs.toTimestamp(),
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                color = NothingPlayerTheme.Muted
            )
            Text(
                text = durationMs.toTimestamp(),
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                color = NothingPlayerTheme.Muted
            )
        }
    }
}

// Extension
fun Long.toTimestamp(): String {
    val totalSecs = this / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}

CONTROLS ROW
kotlin@Composable
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
        // Shuffle
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

        // Previous
        IconButton(onClick = onPrevious, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                tint = NothingPlayerTheme.WhiteDim,
                modifier = Modifier.size(28.dp)
            )
        }

        // Play / Pause — hero button
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
                .background(NothingPlayerTheme.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null  // custom press scale instead of ripple
                ) { onPlayPause() }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = NothingPlayerTheme.Black,
                modifier = Modifier.size(26.dp)
            )
        }

        // Next
        IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = NothingPlayerTheme.WhiteDim,
                modifier = Modifier.size(28.dp)
            )
        }

        // Repeat
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

// Reusable: icon button with red active-dot indicator below
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
                LocalContentColor provides if (active) NothingPlayerTheme.Red else NothingPlayerTheme.WhiteDim
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
                    .background(NothingPlayerTheme.Red)
            )
        }
    }
}

SECONDARY ROW
kotlin@Composable
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
        // Speed badge-button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(NothingPlayerTheme.Surface2)
                .border(1.dp, NothingPlayerTheme.Border, RoundedCornerShape(3.dp))
                .clickable { onSpeedCycle() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                // Format: "1.0×" — use × not x
                text = "${if (speed == speed.toLong().toFloat()) speed.toInt() else speed}×",
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NothingPlayerTheme.Muted,
                letterSpacing = 1.1.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        // Volume row
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                contentDescription = null,
                tint = NothingPlayerTheme.Muted,
                modifier = Modifier.size(16.dp)
            )

            // Volume track
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NothingPlayerTheme.Border)
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
                            .background(NothingPlayerTheme.WhiteDim)
                            // Note: volume fill is white-dim, NOT red
                            // Red = playback progress only
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = NothingPlayerTheme.Muted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

BOTTOM NAV
kotlinenum class BottomNavTab { LIBRARY, PLAYER, QUEUE }

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
                // Top border line only — no elevation, no shadow
                drawLine(
                    color = Color(0xFF2A2A2A),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
    ) {
        listOf(
            Triple(BottomNavTab.LIBRARY, "LIBRARY", painterResource(R.drawable.ic_nav_library)),
            Triple(BottomNavTab.PLAYER,  "PLAYER",  painterResource(R.drawable.ic_nav_player)),
            Triple(BottomNavTab.QUEUE,   "QUEUE",   painterResource(R.drawable.ic_nav_queue)),
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
                // Active tab: red top bar
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(NothingPlayerTheme.Red)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(bottom = 10.dp)
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = label,
                        tint = if (isActive) NothingPlayerTheme.White else NothingPlayerTheme.Muted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontFamily = SpaceMono,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isActive) NothingPlayerTheme.White else NothingPlayerTheme.Muted,
                        letterSpacing = 1.62.sp  // 0.18em
                    )
                }
            }
        }
    }
}

CRITICAL RULES FOR THE AGENT

Never left-clip the track title. TextOverflow.Ellipsis on the right. Never marquee unless explicitly asked.
Red is reserved for two things only: playback progress fill + active state indicators (shuffle dot, repeat dot, heart, bitrate badge). Nowhere else.
Volume fill is WhiteDim, not red. Volume is not playback state.
Play button is the only filled/backgrounded control. All others are icon-only with no background.
Duration timestamp is total, not remaining. Never show a negative number.
Artwork inside the vinyl counter-rotates so the cover image stays readable while the disc spins.
Blur background crossfades (150ms fade-out, swap image, 150ms fade-in) on track change.
Bottom nav is always 3 tabs, always LIBRARY | PLAYER | QUEUE, always same order regardless of current screen.
Active tab uses a red top-line bar, not an underline, not a filled background.
Font display labels: MP3 not MPEG, 320 kbps not 320, × not x for speed multiplier.
Minimum touch targets: 44×44dp on all interactive elements regardless of visual icon size.
windowSoftInputMode = adjustNothing — player screen must never resize or shift when keyboard appears elsewhere in the nav stack.
