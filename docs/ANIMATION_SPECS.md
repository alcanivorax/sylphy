# Animation Specs — Monochrome Brutalist Motion Language

## Motion Philosophy

Motion in Sylphy is **purposeful and restrained**. It communicates state change and provides spatial orientation — it never entertains. 

Rules:
- Fast enter (≤150ms), slow settle (250–400ms)
- No bounce, no overshoot — `CubicBezierEasing(0.16, 1, 0.3, 1)` is the workhorse
- Nothing moves unless it earns the right to move
- Duration scales with distance: small movements are fast, full-screen transitions are slow
- All animations respect `LocalReduceMotion` (check `reduceMotion.enabled` and skip animation if true)

---

## 1. Arc Progress Ring (replaces sine wave ring)

### Visual Description
A clean geometric arc centered on album art. Thin stroke (2dp). Played portion is full white (`FgPrimary`). Unplayed is ghost white (`FgSubtle`). A small filled circle (4dp radius) sits at the leading edge of the played arc — the playhead.

No sine wave. No glow. Precision geometry only.

```kotlin
// ui/components/player/ProgressRing.kt

@Composable
fun ProgressRing(
    modifier: Modifier = Modifier,
    size: Dp,
    progress: Float,           // 0f..1f
    strokeWidth: Dp = Layout.progressRingStroke,
) {
    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }
    val dotRadius = with(LocalDensity.current) { Layout.seekDotRadius.toPx() }

    Canvas(modifier = modifier.size(size)) {
        val diameter = size.toPx() - strokePx
        val topLeft  = Offset(strokePx / 2, strokePx / 2)
        val arcSize  = androidx.compose.ui.geometry.Size(diameter, diameter)
        val startAngle = -90f          // 12 o'clock
        val sweepTotal  = 360f

        // Empty track (full circle, ghost)
        drawArc(
            color      = ProgressEmpty,
            startAngle = startAngle,
            sweepAngle = sweepTotal,
            useCenter  = false,
            topLeft    = topLeft,
            size       = arcSize,
            style      = Stroke(strokePx, cap = StrokeCap.Butt),
        )

        // Filled track (progress arc, full white)
        if (progress > 0f) {
            drawArc(
                color      = ProgressFilled,
                startAngle = startAngle,
                sweepAngle = sweepTotal * progress,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(strokePx, cap = StrokeCap.Butt),
            )
        }

        // Playhead dot — sits at leading edge of filled arc
        val angleRad = Math.toRadians((startAngle + sweepTotal * progress).toDouble())
        val radius   = diameter / 2f
        val cx       = center.x + radius * cos(angleRad).toFloat()
        val cy       = center.y + radius * sin(angleRad).toFloat()
        drawCircle(color = ProgressPlayhead, radius = dotRadius, center = Offset(cx, cy))
    }
}
```

### Progress Animation
Progress updates come from `PlayerViewModel` at 500ms intervals. Use `animateFloatAsState` for smooth visual interpolation between polling ticks:

```kotlin
val animatedProgress by animateFloatAsState(
    targetValue    = progress,
    animationSpec  = tween(durationMillis = 500, easing = LinearEasing),
    label          = "progress_ring",
)
ProgressRing(size = Layout.albumArtSize + 28.dp, progress = animatedProgress)
```

---

## 2. Linear Seek Bar (replaces sine wave seek bar)

### Visual Description
A full-width horizontal line. Left of playhead: `FgPrimary`. Right of playhead: `FgSubtle`. Playhead: 4dp filled dot, `FgPrimary`. Time stamps sit below — left is elapsed, right is `-remaining`.

No wave. The waveform data (stored per track) is used optionally to modulate the line thickness — subtle, not decorative.

```kotlin
// ui/components/player/SeekBar.kt

@Composable
fun SylphySeekBar(
    modifier: Modifier = Modifier,
    positionMs: Long,
    durationMs: Long,
    waveformData: List<Float>? = null,
    onSeek: (Long) -> Unit,
) {
    val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    val dotRadius = with(LocalDensity.current) { Layout.seekDotRadius.toPx() }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Layout.seekBarHeight)
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures { change, _ ->
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = center.y
                val splitX = progress * size.width
                val baseTrackHeight = 2f

                // Waveform-informed thickness (optional)
                // If waveformData present, vary line height between 2f and 6f per segment
                if (waveformData != null) {
                    val numPoints = waveformData.size
                    val segWidth = size.width / numPoints
                    waveformData.forEachIndexed { i, amplitude ->
                        val x = i * segWidth
                        val h = (baseTrackHeight + amplitude * 4f).coerceIn(2f, 6f)
                        val segColor = if (x < splitX) ProgressFilled else ProgressEmpty
                        drawRect(
                            color   = segColor,
                            topLeft = Offset(x, y - h / 2),
                            size    = Size(segWidth - 1f, h),
                        )
                    }
                } else {
                    // Simple flat line
                    // Inactive segment
                    drawLine(ProgressEmpty,   Offset(0f, y), Offset(size.width, y), baseTrackHeight)
                    // Active segment
                    drawLine(ProgressFilled,  Offset(0f, y), Offset(splitX, y),     baseTrackHeight)
                }

                // Playhead dot
                drawCircle(ProgressPlayhead, dotRadius, Offset(splitX, y))
            }
        }

        // Time labels
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(positionMs.toMmSs(),             style = SylphyType.CodeSmall, color = FgMuted)
            Text("-${(durationMs - positionMs).toMmSs()}", style = SylphyType.CodeSmall, color = FgMuted)
        }
    }
}
```

---

## 3. Play Button

### Visual Description
A 64×64dp square button. Paused state: white background, black icon — clean, punchy inversion. Playing state: black background, white icon, white 1dp border — inverted back.

No glow. No color. The inversion is the statement.

```kotlin
// ui/components/player/PlayButton.kt

@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onPress: () -> Unit,
) {
    val scale      = remember { Animatable(1f) }
    val haptic     = LocalHapticFeedback.current
    val scope      = rememberCoroutineScope()

    val bgColor    = if (isPlaying) BgBase    else White
    val fgColor    = if (isPlaying) FgPrimary else Black
    val borderColor= if (isPlaying) BorderStrong else Color.Transparent

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(Layout.playButtonSize)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .background(bgColor, ContainerCorner)
            .border(Layout.borderThick, borderColor, ContainerCorner)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    scale.animateTo(
                        0.94f,
                        tween(Duration.Instant, easing = SylphyEasing.Exit)
                    )
                    scale.animateTo(
                        1f,
                        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                    )
                }
                onPress()
            }
    ) {
        Icon(
            painter           = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint              = fgColor,
            modifier          = Modifier.size(28.dp),
        )
    }
}
```

---

## 4. Tab Bar — Inverted Pill Indicator

### Visual Description
Three tabs at the top. Selected tab gets an inverted pill — white background, black text. Unselected tabs: no background, secondary-opacity text. Pill slides horizontally between tabs.

```kotlin
// ui/components/shared/TabBar.kt

@Composable
fun SylphyTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(Layout.topBarHeight)
            .background(BgBase)
            .border(width = Layout.borderThin, color = BorderDefault,
                    shape = RectangleShape)  // bottom border only — use clip trick:
    ) {
        val tabWidth    = maxWidth / tabs.size
        val tabWidthPx  = with(LocalDensity.current) { tabWidth.toPx() }
        val pillOffset  = remember { Animatable(selectedIndex * tabWidthPx) }

        LaunchedEffect(selectedIndex) {
            pillOffset.animateTo(
                targetValue   = selectedIndex * tabWidthPx,
                animationSpec = tween(Duration.Normal, easing = SylphyEasing.Standard),
            )
        }

        // Sliding inverted pill
        Box(
            Modifier
                .width(tabWidth - Spacing.sm)
                .fillMaxHeight()
                .padding(vertical = Spacing.sm, horizontal = Spacing.xs)
                .offset { IntOffset(pillOffset.value.roundToInt() + Spacing.xs.roundToPx(), 0) }
                .background(ActiveBackground, ChipCorner)
        )

        // Tab labels
        Row(Modifier.fillMaxSize()) {
            tabs.forEachIndexed { i, label ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                        ) { onTabSelected(i) }
                ) {
                    Text(
                        text  = label,
                        style = SylphyType.Heading,
                        color = if (i == selectedIndex) ActiveForeground else FgMuted,
                    )
                }
            }
        }
    }
}
```

---

## 5. List Item Entrance Animation

When library loads and items appear in `LazyColumn`, items fade in and slide up slightly:

```kotlin
// Apply to each LazyColumn item:
modifier = Modifier.animateItem(
    fadeInSpec  = tween(Duration.Normal, easing = SylphyEasing.Enter),
    placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
)
```

---

## 6. Screen Transitions (Navigation Compose)

```kotlin
NavHost(
    navController = navController,
    enterTransition = {
        fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) +
        slideInHorizontally(tween(Duration.Normal, easing = SylphyEasing.Enter)) { it / 12 }
    },
    exitTransition = {
        fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit))
    },
    popEnterTransition = {
        fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter))
    },
    popExitTransition = {
        fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit)) +
        slideOutHorizontally(tween(Duration.Normal, easing = SylphyEasing.Exit)) { it / 12 }
    },
)
```

Slide distance is only `width / 12` — barely perceptible. Just enough to communicate direction.

---

## 7. Bottom Sheet / Modal

```kotlin
// ModalBottomSheet custom shape: flat top, 4dp radius everywhere else
// Drag handle: 32×3dp centered white pill at top of sheet
// Enter: slide up + fade, 300ms
// Dismiss: slide down + fade, 200ms
// Backdrop: BgBase at 70% opacity (not a color — black at 70%)

ModalBottomSheet(
    onDismissRequest  = onDismiss,
    containerColor    = BgElevated,
    shape             = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    scrimColor        = Black.copy(alpha = 0.7f),
    dragHandle = {
        Box(Modifier.fillMaxWidth().padding(vertical = Spacing.sm), Alignment.Center) {
            Box(Modifier.size(width = 32.dp, height = 3.dp)
                .background(FgSubtle, ChipCorner))
        }
    },
)
```

---

## 8. Ticker Tape

Unchanged mechanically, completely restyled:

```kotlin
// No marquee — clean horizontal scroll

// Content: "TRACK TITLE  ·  ARTIST  ·  ALBUM  ·  YEAR  ·  FORMAT"
// Separator: "  ·  " (spaces + middle dot — not em-dash)
// Font: SylphyType.CodeSmall, FgMuted
// No uppercase
// Speed: 32dp/s (slightly slower than v1 — more editorial)
```

---

## 9. Stat Number Counter Animation

For stats dashboard — numbers count up from 0 on screen enter:

```kotlin
@Composable
fun AnimatedStatNumber(target: Int, suffix: String = "") {
    var displayValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(target) {
        val duration = Duration.Deliberate
        val steps = 40
        val stepDelay = duration / steps
        for (i in 1..steps) {
            displayValue = (target * (i.toFloat() / steps)).roundToInt()
            delay(stepDelay.toLong())
        }
        displayValue = target
    }

    Text(
        text  = "$displayValue$suffix",
        style = SylphyType.Stat,
        color = FgPrimary,
    )
}
```

---

## 10. Album Art Crossfade

When track changes, album art crossfades:

```kotlin
AnimatedContent(
    targetState   = artworkPath,
    transitionSpec = {
        fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) togetherWith
        fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit))
    },
    label = "artwork_crossfade",
) { path ->
    AlbumArtwork(artworkPath = path, size = Layout.albumArtSize)
}
```

---

## What Was Removed vs v1

| v1 Animation | v2 Replacement | Reason |
|---|---|---|
| Sine wave ring | Clean geometric arc | Sine wave was decorative, not informative |
| Sine wave seek bar | Flat line (± waveform thickness) | Cleaner, more legible |
| Red glow on play button | Border inversion | No color in v2 |
| CRT scanline overlay | Nothing | Scanlines were texture for texture's sake |
| Dot grid background | Nothing | Same |
| Corner brackets | Nothing | Was a Nothing OS-specific motif |
| BPM pulse dot | Removed entirely | Feature cut |
| Red tab underline | Inverted pill | Inversion is the v2 selection language |
