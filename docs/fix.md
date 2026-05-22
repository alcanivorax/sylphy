
# Sylphy Player Refactor — Updated Code

This refactor fixes:

* Pager/tab desync
* Player centering
* Empty player swipe bug
* Misaligned tab indicator
* Typography weight issues
* Overstretched layout

---

# 1. Replace `SwipePager.kt`

```kotlin
package io.sylphy.app.ui.components.shared

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipePager(
    pageCount: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit,
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Layout(
        modifier = modifier.pointerInput(currentPage, pageCount) {
            var totalDrag = 0f

            detectHorizontalDragGestures(
                onDragEnd = {
                    val threshold = size.width * 0.20f

                    when {
                        totalDrag < -threshold && currentPage < pageCount - 1 -> {
                            onPageChanged(currentPage + 1)
                        }

                        totalDrag > threshold && currentPage > 0 -> {
                            onPageChanged(currentPage - 1)
                        }
                    }

                    dragOffset = 0f
                    totalDrag = 0f
                },
                onDragCancel = {
                    dragOffset = 0f
                    totalDrag = 0f
                },
            ) { change, dragAmount ->
                totalDrag += dragAmount
                dragOffset = totalDrag / size.width
                change.consume()
            }
        },
        content = {
            repeat(pageCount) { page ->
                val distance = abs(page - currentPage - dragOffset)

                val scale by animateFloatAsState(
                    targetValue = 1f - (distance * 0.04f).coerceIn(0f, 0.08f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    label = "pager_scale",
                )

                val alpha by animateFloatAsState(
                    targetValue = (1f - distance * 0.25f).coerceIn(0.5f, 1f),
                    animationSpec = spring(),
                    label = "pager_alpha",
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .graphicsLayer {
                            this.alpha = alpha
                        },
                ) {
                    content(page)
                }
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val offsetPx = dragOffset * constraints.maxWidth

            placeables.forEachIndexed { index, placeable ->
                val x = (
                    (index - currentPage) * constraints.maxWidth + offsetPx
                ).roundToInt()

                placeable.placeRelative(x, 0)
            }
        }
    }
}
```

---

# 2. Replace `SylphyTabBar.kt`

```kotlin
package io.sylphy.app.ui.components.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import io.sylphy.app.ui.theme.ActiveBackground
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType
import kotlin.math.roundToInt

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
            .height(Layout.topBarHeight),
    ) {
        val tabWidth = maxWidth / tabs.size
        val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }

        Row(modifier = Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, label ->
                val active = index == selectedIndex

                val textColor by animateColorAsState(
                    targetValue = if (active) ActiveBackground else FgMuted,
                    animationSpec = spring(),
                    label = "tab_color",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            onTabSelected(index)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label.uppercase(),
                        style = SylphyType.CodeSmall.copy(
                            letterSpacing = 3.sp,
                        ),
                        color = textColor,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .width(tabWidth * 0.32f)
                .height(Spacing.px1)
                .offset {
                    IntOffset(
                        x = (
                            selectedIndex * tabWidthPx +
                                (tabWidthPx * 0.34f)
                            ).roundToInt(),
                        y = (Layout.topBarHeight - Spacing.px1).roundToPx(),
                    )
                }
                .background(ActiveBackground, RectangleShape),
        )
    }
}
```

---

# 3. Update `PlayerScreen.kt`

Replace the MAIN content layout section.

Find:

```kotlin
Column(modifier = Modifier.fillMaxSize())
```

Replace the entire internal structure with:

```kotlin
Column(modifier = Modifier.fillMaxSize()) {

    TopNav(onBack = { navController.popBackStack() })

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VinylArtwork(
                artworkUri = track.artworkPath,
                isPlaying = uiState.isPlaying,
            )

            Spacer(Modifier.height(36.dp))

            TrackInfoRow(
                track = track,
                isFavourite = track.isFavorite,
                onFavouriteToggle = viewModel::toggleFavorite,
            )

            Spacer(Modifier.height(14.dp))

            QualityBadgeRow(track = track)

            Spacer(Modifier.height(32.dp))

            ScrubberSection(
                elapsedMs = uiState.position,
                durationMs = uiState.duration,
                onSeek = viewModel::seekTo,
            )

            Spacer(Modifier.height(36.dp))

            ControlsRow(
                isPlaying = uiState.isPlaying,
                isShuffle = uiState.shuffleEnabled,
                repeatMode = uiState.repeatMode,
                onPlayPause = viewModel::playPause,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                onShuffle = viewModel::toggleShuffle,
                onRepeat = viewModel::cycleRepeat,
            )

            Spacer(Modifier.height(28.dp))

            SecondaryRow(
                speed = uiState.speed,
                volume = uiState.volume,
                onSpeedCycle = viewModel::cycleSpeed,
                onVolumeChange = viewModel::adjustVolume,
            )
        }
    }

    BottomNav(
        activeTab = BottomNavTab.PLAYER,
        onLibrary = { navController.navigate(Screen.Library.route) },
        onQueue = { navController.navigate(Screen.Queue.route) },
        onPlayer = {},
    )
}
```

---

# 4. Improve Typography

Update `Type.kt`

Replace:

```kotlin
val GeistMono = FontFamily.Monospace
val GeistSans = FontFamily.SansSerif
```

with actual fonts later.

Recommended:

* IBM Plex Mono
* Space Grotesk

For now change heading sizes:

```kotlin
val Heading = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 2.2.sp,
)
```

And:

```kotlin
val Body = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.2).sp,
)
```

---

# 5. Fix Empty Player Swipe Bug

In your navigation host / pager setup:

Instead of always including:

```kotlin
Player
```

conditionally include it:

```kotlin
if (hasActiveTrack) {
    pages.add(Player)
}
```

OR

redirect player tab:

```kotlin
if (!hasActiveTrack && selectedTab == PLAYER) {
    selectedTab = LIBRARY
}
```

This removes ghost player pages.

---

# 6. Optional Visual Polish

For premium atmosphere:

## Artwork

Use:

```kotlin
Modifier.graphicsLayer {
    shadowElevation = 24f
}
```

## Background

Add subtle vertical gradient:

```kotlin
Brush.verticalGradient(
    listOf(
        Color.Black,
        Color(0xFF0E0E0E),
        Color.Black,
    )
)
```

## Metadata

Reduce opacity:

```kotlin
alpha = 0.55f
```

## Seekbar

Use thinner lines.

The mockup is extremely minimal.

---

# Result

After these changes:

* tabs will sync properly
* swipe feels stable
* player centers correctly
* UI becomes cinematic
* typography feels intentional
* spacing breathes properly
* no more invisible player page

Most importantly:

the UI stops feeling like a Compose layout
and starts feeling like a designed product.
