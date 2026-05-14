# Player Screen — Monochrome Brutalist Layout

## File: `ui/screens/player/PlayerScreen.kt`

---

## Visual Layout

```
┌──────────────────────────────────────────┐
│  Status bar (transparent, white icons)   │
├──────────────────────────────────────────┤
│  PLAYER      QUEUE      LIBRARY          │  ← SylphyTabBar (56dp)
│     ▓▓▓▓▓                               │    active pill under PLAYER
├──────────────────────────────────────────┤
│                                          │
│                  48dp                   │  ← Top padding
│                                          │
│  Track Title Here                        │  ← DisplayLarge, bold, GeistMono
│  Artist Name                             │  ← Body, FgSecondary, GeistSans
│                                          │
│              24dp                       │
│                                          │
│      ╭──────────────────────╮            │
│      │  ┌────────────────┐  │  ← ProgressRing (art + 28dp margin)
│      │  │                │  │
│      │  │   Album Art    │  │  ← AlbumArtwork (280dp, 4dp corner)
│      │  │                │  │
│      │  └────────────────┘  │
│      ●──────────────────────│  ← Playhead dot on ring arc
│      ╰──────────────────────╯
│                                          │
│              32dp                       │
│                                          │
│  1:23  ————●—————————————————————  -2:11 │  ← SylphySeekBar
│                                          │
│              24dp                       │
│                                          │
│   ⇄        ⏮       ▶       ⏭       ↻   │  ← TransportControls
│             ·                            │    active dots below shuffle/repeat
│                                          │
│              16dp                       │
│                                          │
│                          1.0×            │  ← SpeedControl (right-aligned)
│                                          │
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │  ← flex spacer
│                                          │
│  Track Title  ·  Artist  ·  Album  ·     │  ← TickerTape (FgMuted, scrolling)
└──────────────────────────────────────────┘
```

---

## Key Design Decisions

**Track info above album art** — establishes what you're about to see before you see it. Vercel-style: label before content.

**Progress ring is subtle** — 2dp stroke. Not a statement — a quiet indicator. The album art is the hero.

**Transport controls are muted by default** — FgMuted icons. Play button is the only element with weight (64dp, inverted). The hierarchy is unmistakable: Play > Skip > Shuffle/Repeat.

**Spacer grows to fill** — the ticker tape is pinned to the bottom. The flexible spacer ensures it never overlaps controls regardless of screen height.

**No background decoration** — the album art provides all the visual interest the screen needs.

---

## Implementation

```kotlin
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = if (uiState.duration > 0L)
        uiState.position.toFloat() / uiState.duration
    else 0f

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(500, easing = LinearEasing),
        label         = "playback_progress",
    )

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.height(Spacing.xxl))

            // ── Track Info ────────────────────────────────────────────────
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,  // left-aligned — editorial style
            ) {
                AnimatedContent(
                    targetState   = uiState.activeTrack?.title ?: "—",
                    transitionSpec = {
                        fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) togetherWith
                        fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit))
                    },
                    label = "title_anim",
                ) { title ->
                    Text(
                        text      = title,
                        style     = SylphyType.DisplayLarge,
                        color     = FgPrimary,
                        maxLines  = 1,
                        overflow  = TextOverflow.Ellipsis,
                        modifier  = Modifier.basicMarquee(velocity = 28.dp),
                    )
                }
                Spacer(Modifier.height(Spacing.xs))
                AnimatedContent(
                    targetState   = uiState.activeTrack?.artist ?: "",
                    transitionSpec = {
                        fadeIn(tween(Duration.Normal)) togetherWith fadeOut(tween(Duration.Fast))
                    },
                    label = "artist_anim",
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

            Spacer(Modifier.height(Spacing.lg))

            // ── Album Art + Progress Ring ─────────────────────────────────
            Box(
                modifier        = Modifier.size(Layout.albumArtSize + 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Album art (slightly smaller, ring sits in margin)
                AnimatedContent(
                    targetState = uiState.activeTrack?.artworkPath,
                    transitionSpec = {
                        fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) togetherWith
                        fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit))
                    },
                    label = "artwork_anim",
                ) { path ->
                    AlbumArtwork(
                        artworkPath = path,
                        size        = Layout.albumArtSize,
                    )
                }

                // Progress ring overlays the margin zone
                ProgressRing(
                    size     = Layout.albumArtSize + 28.dp,
                    progress = animatedProgress,
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            // ── Seek Bar ─────────────────────────────────────────────────
            SylphySeekBar(
                modifier      = Modifier.fillMaxWidth(),
                positionMs    = uiState.position,
                durationMs    = uiState.duration,
                waveformData  = uiState.activeTrack?.waveformData,
                onSeek        = viewModel::seekTo,
            )

            Spacer(Modifier.height(Spacing.lg))

            // ── Transport Controls ────────────────────────────────────────
            TransportControls(
                isPlaying       = uiState.isPlaying,
                shuffleEnabled  = uiState.shuffleEnabled,
                repeatMode      = uiState.repeatMode,
                onPlayPause     = { if (uiState.isPlaying) viewModel.pause() else viewModel.play() },
                onNext          = viewModel::skipToNext,
                onPrevious      = viewModel::skipToPrevious,
                onToggleShuffle = viewModel::toggleShuffle,
                onCycleRepeat   = viewModel::cycleRepeat,
            )

            Spacer(Modifier.height(Spacing.md))

            // ── Speed Control (right-aligned) ────────────────────────────
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                SpeedControl(
                    speed         = uiState.speed,
                    onSpeedChange = viewModel::setPlaybackSpeed,
                )
            }

            Spacer(Modifier.weight(1f))  // push ticker to bottom
        }

        // ── Ticker Tape (absolute bottom) ─────────────────────────────────
        TickerTape(
            track    = uiState.activeTrack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = Spacing.md),
        )
    }
}
```

---

## Empty State (no track selected)

```kotlin
// When activeTrack == null
Box(Modifier.fillMaxSize().background(BgBase), Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("—", style = SylphyType.Clock, color = FgSubtle)
        Spacer(Modifier.height(Spacing.md))
        Text(
            "No track selected",
            style = SylphyType.Caption,
            color = FgMuted,
        )
    }
}
```

A long dash. Nothing else. The rest of the screen is silence.

---

## Loading / Buffering State

When `playingState == PlaybackState.BUFFERING`:

- Album art: still visible
- Progress ring: progress holds at current value
- Play button: shows `LoadingDots` instead of icon (3 squares, 5dp each, FgMuted)
- All other controls: remain interactive

---

## Accessibility

```kotlin
// Play button
.semantics {
    contentDescription = if (isPlaying) "Pause" else "Play"
    role = Role.Button
}

// Seek bar
.semantics {
    contentDescription = "Seek bar. ${positionMs.toMmSs()} of ${durationMs.toMmSs()}"
    stateDescription   = "${(progress * 100).toInt()}% complete"
    setProgress { fraction ->
        onSeek((fraction * durationMs).toLong())
        true
    }
}
```

---

## State Dependencies Map

```
PlayerScreen reads from PlayerViewModel.uiState:
├── activeTrack.title       → TrackInfoSection title
├── activeTrack.artist      → TrackInfoSection artist
├── activeTrack.artworkPath → AlbumArtwork
├── activeTrack.waveformData → SylphySeekBar thickness modulation
├── isPlaying               → PlayButton, TransportControls
├── position                → SylphySeekBar, ProgressRing
├── duration                → SylphySeekBar, ProgressRing
├── speed                   → SpeedControl
├── shuffleEnabled          → TransportControls
└── repeatMode              → TransportControls

PlayerScreen calls PlayerViewModel:
├── play() / pause()        → onPlayPause
├── seekTo(Long)            → SylphySeekBar.onSeek
├── skipToNext()            → onNext
├── skipToPrevious()        → onPrevious
├── toggleShuffle()         → onToggleShuffle
├── cycleRepeat()           → onCycleRepeat
└── setPlaybackSpeed(Float) → SpeedControl.onSpeedChange
```

---

## Performance Requirements

| Metric | Target |
|---|---|
| Progress ring update | Smooth at 60fps via `animateFloatAsState` + Canvas |
| Album art crossfade | 250ms, no jank |
| Track info update | < 100ms from `onMediaItemTransition` event |
| Initial render | < 200ms from tab focus |
| Seek response | < 50ms visual, < 200ms audio |
