# Phase 2 — Player Screen + Queue Screen

## Goal
Full player screen in the monochrome brutalist theme, driven by live `MediaController` state. Queue screen with drag-to-reorder. Shuffle and repeat working.

**Phase 2 complete when:** The player screen looks like it was designed by Vercel. Clean, precise, zero color. The arc ring tracks playback. The play button inverts on state change. Seeking works. The queue is draggable.

---

## Task 2.1 — PlayerViewModel

Full implementation from `architecture/STATE_MANAGEMENT.md`.

Key implementation notes for v2:
- Remove any BPM references from the state
- `shuffleEnabled` and `repeatMode` live in `PlayerUiState` (v2 colocates them)
- Progress polling: `delay(500)` loop in `viewModelScope`

```kotlin
// Add to PlayerUiState:
data class PlayerUiState(
    val activeTrack: Track? = null,
    val isPlaying: Boolean = false,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val position: Long = 0L,
    val duration: Long = 0L,
    val buffered: Long = 0L,
    val speed: Float = 1.0f,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    // No bpm field
)
```

Sync `shuffleEnabled` and `repeatMode` from `Player.Listener`:
```kotlin
override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    _uiState.update { it.copy(shuffleEnabled = shuffleModeEnabled) }
}
override fun onRepeatModeChanged(repeatMode: Int) {
    _uiState.update { it.copy(repeatMode = repeatMode.toRepeatMode()) }
}
```

**Acceptance criteria:**
- [ ] `isPlaying` changes when media plays/pauses
- [ ] `position` updates every 500ms
- [ ] `activeTrack` updates on track change
- [ ] Shuffle and repeat state synced from MediaController

---

## Task 2.2 — AlbumArtwork Component

```kotlin
// ui/components/player/AlbumArtwork.kt

@Composable
fun AlbumArtwork(
    artworkPath: String?,
    size: Dp = Layout.albumArtSize,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(size)
            .clip(ContainerCorner)         // 4.dp — subtle, not square
            .border(Layout.borderThin, BorderDefault, ContainerCorner)
            .background(BgElevated),
        contentAlignment = Alignment.Center,
    ) {
        if (artworkPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file://$artworkPath")
                    .crossfade(Duration.Normal)
                    .build(),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
        } else {
            // Fallback monogram
            Text(
                text  = "—",
                style = SylphyType.Display,
                color = FgSubtle,
            )
        }
    }
}
```

**`ArtworkExtractor.kt`** — extract and cache embedded artwork:
```kotlin
// data/local/scanner/ArtworkExtractor.kt

@Singleton
class ArtworkExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun extractAndCache(trackId: String, contentUri: String): String? =
        withContext(Dispatchers.IO) {
            val cacheFile = File(context.cacheDir, "artwork/$trackId.jpg")
            if (cacheFile.exists()) return@withContext cacheFile.absolutePath
            cacheFile.parentFile?.mkdirs()

            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, Uri.parse(contentUri))
                val bytes = retriever.embeddedPicture ?: return@withContext null
                cacheFile.writeBytes(bytes)
                cacheFile.absolutePath
            } catch (e: Exception) {
                Timber.w(e, "Artwork extraction failed: $trackId")
                null
            } finally {
                retriever.release()
            }
        }
}
```

Trigger extraction in a background enrichment pass after initial scan:
```kotlin
// After scanLibrary() completes:
viewModelScope.launch(Dispatchers.IO) {
    val unenriched = trackDao.getTracksWithoutArtwork()
    unenriched.forEach { entity ->
        val path = artworkExtractor.extractAndCache(entity.id, entity.contentUri)
        if (path != null) trackDao.updateArtworkPath(entity.id, path)
        delay(30) // don't starve the main thread
    }
}
```

**Acceptance criteria:**
- [ ] Album art displays for tracks that have embedded artwork
- [ ] Shape: 4.dp rounded corners, thin border
- [ ] Fallback: `BgElevated` background with `—` in `FgSubtle`
- [ ] Crossfade when track changes

---

## Task 2.3 — Progress Ring

Full Canvas implementation from `design/ANIMATION_SPECS.md §1`.

```kotlin
// ui/components/player/ProgressRing.kt

@Composable
fun ProgressRing(
    size: Dp,
    progress: Float,          // already animated by caller via animateFloatAsState
    modifier: Modifier = Modifier,
    strokeWidth: Dp = Layout.progressRingStroke,
) {
    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }
    val dotRadius = with(LocalDensity.current) { Layout.seekDotRadius.toPx() }

    Canvas(modifier = modifier.size(size)) {
        val inset  = strokePx / 2f
        val diameter = this.size.width - strokePx
        val topLeft  = Offset(inset, inset)
        val arcSize  = Size(diameter, diameter)

        // Ghost track (full circle)
        drawArc(
            color      = ProgressEmpty,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter  = false,
            topLeft    = topLeft,
            size       = arcSize,
            style      = Stroke(strokePx, cap = StrokeCap.Butt),
        )

        // Filled arc
        if (progress > 0.001f) {
            drawArc(
                color      = ProgressFilled,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(strokePx, cap = StrokeCap.Butt),
            )
        }

        // Playhead dot at leading edge
        val radius  = diameter / 2f
        val angleDeg = -90f + 360f * progress
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val dotX = center.x + radius * cos(angleRad).toFloat()
        val dotY = center.y + radius * sin(angleRad).toFloat()
        drawCircle(
            color  = ProgressPlayhead,
            radius = dotRadius,
            center = Offset(dotX, dotY),
        )
    }
}
```

**Acceptance criteria:**
- [ ] Ghost ring visible around entire album art
- [ ] Filled arc grows clockwise from 12 o'clock as track plays
- [ ] Playhead dot sits precisely at the leading edge of the arc
- [ ] Progress animates smoothly (no visible 500ms jump)
- [ ] 60fps — verified with GPU profiler

---

## Task 2.4 — Seek Bar

Full implementation from `design/ANIMATION_SPECS.md §2`.

Critical detail — waveform-informed thickness:
```kotlin
// When waveformData != null:
// Each of the 200 segments gets a height proportional to waveform amplitude
// Minimum 2f, maximum 6f — subtle variation, not dramatic
// This makes the seek bar unique per track without adding color

// When waveformData == null:
// Flat 2dp line — clean, simple
```

Gesture handling must feel instantaneous:
```kotlin
// Optimistic local update during drag:
var localProgress by remember { mutableFloatStateOf(progress) }
var isDragging by remember { mutableStateOf(false) }

val displayProgress = if (isDragging) localProgress else progress

// During drag: update localProgress immediately, call onSeek throttled
// On drag end: call onSeek with final position, set isDragging = false
```

**Acceptance criteria:**
- [ ] Tap to seek — audio jumps to tapped position
- [ ] Drag — smooth continuous update, no lag
- [ ] Time stamps update: left = elapsed, right = `-remaining`
- [ ] Waveform thickness variation visible when data available

---

## Task 2.5 — Play Button

Full implementation from `design/ANIMATION_SPECS.md §3`.

**Custom vector drawables** — simple, geometric, no rounded paths:

`res/drawable/ic_play.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="28dp" android:height="28dp"
    android:viewportWidth="28" android:viewportHeight="28">
    <path
        android:pathData="M8,5 L8,23 L23,14 Z"
        android:fillColor="#FAFAFA"/>
</vector>
```

`res/drawable/ic_pause.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="28dp" android:height="28dp"
    android:viewportWidth="28" android:viewportHeight="28">
    <path android:pathData="M6,5 L11,5 L11,23 L6,23 Z" android:fillColor="#FAFAFA"/>
    <path android:pathData="M17,5 L22,5 L22,23 L17,23 Z" android:fillColor="#FAFAFA"/>
</vector>
```

Button states:
- **Paused:** `White` background, `Black` icon, no border
- **Playing:** `BgBase` background, `FgPrimary` icon, `BorderStrong` 2dp border

The inversion is the entire visual language of "playing." No animation inside the button beyond the press scale — the state change itself is the statement.

**Acceptance criteria:**
- [ ] White bg when paused, black bg when playing
- [ ] Border appears when playing
- [ ] Scale spring on press (0.94f, no bounce)
- [ ] Haptic on every press
- [ ] 64×64dp, 4.dp corners

---

## Task 2.6 — Transport Controls

```kotlin
// ui/components/player/TransportControls.kt

@Composable
fun TransportControls(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        // Shuffle
        TransportIconButton(
            icon      = R.drawable.ic_shuffle,
            active    = shuffleEnabled,
            onClick   = onToggleShuffle,
            contentDescription = "Shuffle",
        )

        // Previous
        TransportIconButton(
            icon  = R.drawable.ic_skip_prev,
            onClick = onPrevious,
            contentDescription = "Previous",
        )

        // Play / Pause (larger, centered)
        PlayButton(isPlaying = isPlaying, onPress = onPlayPause)

        // Next
        TransportIconButton(
            icon  = R.drawable.ic_skip_next,
            onClick = onNext,
            contentDescription = "Next",
        )

        // Repeat
        TransportIconButton(
            icon   = when (repeatMode) {
                RepeatMode.ONE -> R.drawable.ic_repeat_one
                else           -> R.drawable.ic_repeat
            },
            active  = repeatMode != RepeatMode.OFF,
            onClick = onCycleRepeat,
            contentDescription = "Repeat",
        )
    }
}

@Composable
private fun TransportIconButton(
    @DrawableRes icon: Int,
    active: Boolean = false,
    onClick: () -> Unit,
    contentDescription: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(Layout.transportTapTarget)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter            = painterResource(icon),
            contentDescription = contentDescription,
            tint               = if (active) FgPrimary else FgMuted,
            modifier           = Modifier.size(Layout.transportIconSize),
        )
        Spacer(Modifier.height(3.dp))
        // Active indicator dot
        Box(
            Modifier
                .size(3.dp)
                .background(
                    if (active) FgPrimary else Color.Transparent,
                    CircleShape,
                )
        )
    }
}
```

**Icon files needed** (all FgPrimary fill, geometric SVG paths):
- `ic_shuffle.xml` — two crossed arrows
- `ic_skip_prev.xml` — skip to previous
- `ic_skip_next.xml` — skip to next
- `ic_repeat.xml` — circular repeat arrow
- `ic_repeat_one.xml` — repeat arrow with "1" overlay

**Acceptance criteria:**
- [ ] All 5 controls functional
- [ ] Shuffle / Repeat active: FgPrimary tint + small dot indicator below
- [ ] Inactive: FgMuted tint, no dot
- [ ] No ripple on any transport button
- [ ] Press: opacity flash to FgGhost background on touch (brief, < 100ms)

---

## Task 2.7 — Track Info with Marquee

```kotlin
// ui/components/player/TrackInfo.kt

@Composable
fun TrackInfoSection(
    track: Track?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState   = track?.title ?: "—",
            transitionSpec = {
                fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) togetherWith
                fadeOut(tween(Duration.Fast,  easing = SylphyEasing.Exit))
            },
            label = "title",
        ) { title ->
            Text(
                text     = title,
                style    = SylphyType.DisplayLarge,
                color    = FgPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(
                    iterations       = Int.MAX_VALUE,
                    velocity         = 28.dp,
                    delayMillis      = 2000,  // pause before scrolling
                    initialDelayMillis = 2000,
                ),
            )
        }

        Spacer(Modifier.height(Spacing.xs))

        AnimatedContent(
            targetState   = track?.artist ?: "",
            transitionSpec = {
                fadeIn(tween(Duration.Normal)) togetherWith fadeOut(tween(Duration.Fast))
            },
            label = "artist",
        ) { artist ->
            Text(
                text     = artist,
                style    = SylphyType.Body,
                color    = FgSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
```

**Acceptance criteria:**
- [ ] Title scrolls for long track names (starts scrolling after 2s pause)
- [ ] Artist text truncates with ellipsis
- [ ] Cross-fade when track changes
- [ ] Null state: "—" in FgMuted for title, empty artist

---

## Task 2.8 — Ticker Tape

```kotlin
// ui/components/player/TickerTape.kt

@Composable
fun TickerTape(
    track: Track?,
    modifier: Modifier = Modifier,
) {
    val content = remember(track) {
        if (track == null) return@remember "Sylphy  ·  No track playing  ·  "
        buildList {
            add(track.title)
            add(track.artist)
            if (!track.album.isNullOrBlank()) add(track.album)
            track.year?.let  { add(it.toString()) }
            track.mimeType?.substringAfterLast('/')?.uppercase()?.let { add(it) }
            track.bitRate?.let { add("${it / 1000} kbps") }
        }.joinToString("  ·  ") + "  ·  "
    }

    val textMeasurer = rememberTextMeasurer()
    val textStyle    = SylphyType.CodeSmall.copy(color = FgMuted)
    val singleWidth  = remember(content) {
        textMeasurer.measure(content, textStyle).size.width.toFloat()
    }

    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(content) {
        offsetX.snapTo(0f)
        offsetX.animateTo(
            targetValue   = -singleWidth,
            animationSpec = infiniteRepeatable(
                animation  = tween(
                    durationMillis = (singleWidth / 32f * 1000f).toInt(),  // 32dp/s
                    easing         = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .clipToBounds(),
    ) {
        Text(
            text     = content.repeat(3),
            style    = textStyle,
            maxLines = 1,
            modifier = Modifier
                .wrapContentWidth(Alignment.Start, unbounded = true)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) },
        )
    }
}
```

**Acceptance criteria:**
- [ ] Scrolls continuously at ~32dp/s
- [ ] Content: title · artist · album · year · format · bitrate
- [ ] Font: `CodeSmall`, `FgMuted` — understated
- [ ] Updates when track changes (resets and starts again)
- [ ] No visible seam during loop

---

## Task 2.9 — Full Player Screen

Assemble from `components/PLAYER_SCREEN.md`.

Critical layout details:
- Track info is **left-aligned** — editorial style, not centered
- Album art is centered
- Speed control is **right-aligned** below transport controls
- Ticker is **bottom-pinned**, absolute positioned
- `Spacer(Modifier.weight(1f))` between speed control and ticker

Verify on multiple screen sizes:
- Small screen (5"): all elements visible, ticker not overlapping controls
- Large screen (6.7"): generous spacing, no awkward stretching

**Acceptance criteria:**
- [ ] All components assembled and functional
- [ ] Left-aligned track info (not centered)
- [ ] Empty state: single `—` centered, nothing else
- [ ] No color violations in any state
- [ ] 60fps confirmed with profiler

---

## Task 2.10 — Queue Screen

Full implementation from `components/QUEUE_LIBRARY_SCREENS.md`.

Use `sh.calvin.reorderable:reorderable:2.4.0` for drag-to-reorder.

Add to `libs.versions.toml`:
```toml
[versions]
reorderable = "2.4.0"

[libraries]
reorderable = { group = "sh.calvin.reorderable", name = "reorderable", version.ref = "reorderable" }
```

Active item visual (inverted row):
```kotlin
// QueueItem.kt — isActive branch
Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(Layout.queueItemHeight)
        .background(ActiveBackground)
        .padding(horizontal = Spacing.md),
    verticalAlignment = Alignment.CenterVertically,
) {
    // No drag handle on active item
    Spacer(Modifier.width(40.dp))

    Text(
        text  = index.toString().padStart(2, '0'),
        style = SylphyType.CodeSmall,
        color = ActiveForeground.copy(alpha = 0.4f),
        modifier = Modifier.width(36.dp),
    )

    Column(Modifier.weight(1f)) {
        Text(track.title,  style = SylphyType.Code,      color = ActiveForeground)
        Text(track.artist, style = SylphyType.BodySmall, color = ActiveForeground.copy(alpha = 0.55f))
    }

    Text(
        track.durationMs.toMmSs(),
        style = SylphyType.CodeSmall,
        color = ActiveForeground.copy(alpha = 0.4f),
        modifier = Modifier.width(44.dp),
        textAlign = TextAlign.End,
    )

    // No remove button on active item
    Spacer(Modifier.width(40.dp))
}
```

**Acceptance criteria:**
- [ ] Active item: full white inversion row
- [ ] Drag to reorder works on non-active items
- [ ] Remove (×) button on non-active items
- [ ] MediaController queue updates after reorder
- [ ] Empty queue: EmptyState composable

---

## Phase 2 Definition of Done

- [ ] Player screen: monochrome brutalist — no color violations
- [ ] Track info left-aligned, marquee works
- [ ] Progress ring tracks playback precisely
- [ ] Play button: white when paused, bordered when playing
- [ ] All transport controls functional with dot indicators
- [ ] Seek bar tap and drag work
- [ ] Waveform thickness modulation visible (when data available)
- [ ] Ticker tape scrolling with correct content
- [ ] Speed control chip works
- [ ] Queue screen: active item inverted
- [ ] Drag to reorder queue works
- [ ] Shuffle and repeat synced
- [ ] 60fps on player screen
- [ ] Zero Kotlin compilation errors
- [ ] Zero color violations in screenshot audit
