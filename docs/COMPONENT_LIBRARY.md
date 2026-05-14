# Component Library — Monochrome Brutalist Composables

> Read `design/DESIGN_TOKENS.md` fully before implementing anything.
> The inversion system is the active-state language — no color, ever.

---

## Shared Components

### `SylphyScaffold`
```kotlin
// ui/components/shared/SylphyScaffold.kt
// The root wrapper for every screen.
// Provides: dark background, top border line, consistent padding.

@Composable
fun SylphyScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = BgBase,
        topBar         = topBar,
        bottomBar      = bottomBar,
        modifier       = modifier,
        content        = content,
    )
}
```

### `SylphyText`
```kotlin
// ui/components/shared/SylphyText.kt
// Thin enforcer — ensures all text uses correct style + color.
// Prefer using Text() with SylphyType.* directly.
// Use SylphyText when you need to guarantee the token system is applied.

@Composable
fun SylphyText(
    text: String,
    style: TextStyle = SylphyType.Body,
    color: Color = FgPrimary,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    modifier: Modifier = Modifier,
)
```

### `SylphyTabBar`
```kotlin
// ui/components/shared/TabBar.kt
// Full implementation in ANIMATION_SPECS.md §4
//
// Params:
//   tabs: List<String>
//   selectedIndex: Int
//   onTabSelected: (Int) -> Unit
//
// Visual:
//   height = Layout.topBarHeight (56.dp)
//   background = BgBase
//   bottom border = 1.dp BorderDefault
//   active: white inverted pill slides between tabs
//   inactive: FgMuted text, no background
//   font: SylphyType.Heading (no uppercase enforcement — labels are title case)
```

### `SylphySearchBar`
```kotlin
// ui/components/shared/SearchBar.kt
//
// Params:
//   value: String
//   onValueChange: (String) -> Unit
//   placeholder: String = "Search..."
//   onFocusChange: (Boolean) -> Unit = {}
//
// Visual:
//   height: 44.dp
//   shape: ContainerCorner (4.dp)
//   border: 1.dp BorderDefault — becomes BorderStrong (2.dp) when focused
//   background: BgSunken
//   leading icon: search icon, FgMuted, 16.dp
//   trailing icon: × clear button (visible when query non-empty), FgMuted
//   font: SylphyType.Body, FgPrimary
//   placeholder: SylphyType.Body, FgMuted
//   cursor: FgPrimary
//   NO rounded search-pill shape — 4.dp ContainerCorner only
//
// Focused state:
//   border weight changes from 1.dp → 2.dp (BorderStrong)
//   animate with animateDpAsState
```

### `EmptyState`
```kotlin
// ui/components/shared/EmptyState.kt
//
// Params:
//   title: String
//   description: String? = null
//   action: Pair<String, () -> Unit>? = null
//
// Visual:
//   centered Column, padding Spacing.xxl
//   title: SylphyType.Heading, FgPrimary
//   description: SylphyType.Caption, FgMuted, marginTop Spacing.sm
//   action button (if provided): bordered button (see SylphyButton below)
//   marginTop Spacing.lg before button
```

### `SylphyButton`
```kotlin
// ui/components/shared/SylphyButton.kt
// The single button style used throughout.
//
// Params:
//   text: String
//   onClick: () -> Unit
//   variant: ButtonVariant = ButtonVariant.Outline
//   enabled: Boolean = true
//
// enum class ButtonVariant { Solid, Outline }
//
// Solid: white background, black text — used for primary CTAs
// Outline: transparent bg, white border (1.dp BorderDefault), white text
//
// Both: ContainerCorner shape, 4.dp radius
// Size: height 40.dp, horizontal padding Spacing.lg
// Font: SylphyType.Heading (compact, slightly spaced)
// Disabled: opacity 0.4f on the entire button
// Press: scale 0.97f spring, no ripple
```

### `SylphyDivider`
```kotlin
// Alias for HorizontalDivider with correct color
@Composable
fun SylphyDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = Separator, thickness = Layout.borderThin)
}
```

### `SectionHeader`
```kotlin
// ui/components/shared/SectionHeader.kt
//
// Params:
//   title: String      — letter (A, B, C) or group name
//   modifier: Modifier
//
// Visual:
//   height: 32.dp
//   background: BgBase (sticky — opaque, so it covers list items)
//   left padding: Spacing.md
//   text: SylphyType.CodeSmall, FgMuted — NOT uppercase, just the letter
//   bottom border: 1.dp Separator
//   NO background fill — transparent except for the base color
```

### `LoadingDots`
```kotlin
// ui/components/shared/LoadingDots.kt
// 3 squares (not circles), 5.dp each, FgMuted
// Staggered fade 0.1f→0.9f with 200ms offset
// Use for scan progress and loading states
```

### `ContextMenuSheet`
```kotlin
// ui/components/shared/ContextMenuSheet.kt
//
// ModalBottomSheet with:
//   containerColor = BgElevated
//   shape = RoundedCornerShape(topStart=8.dp, topEnd=8.dp)
//   scrimColor = Black.copy(0.7f)
//
// Track header (inside sheet):
//   Row { ArtworkThumbnail(40.dp) | Column { title, artist } }
//   border bottom: 1.dp Separator
//   padding: Spacing.md
//
// Action rows:
//   height: 52.dp
//   horizontal padding: Spacing.md
//   font: SylphyType.Body, FgPrimary
//   no divider between rows (breathing room comes from height)
//   leading icon: 20.dp, FgMuted
//   press: no ripple — fade to FgSubtle background on press
//
// Destructive action (delete):
//   font color: FgPrimary (no red — destructive = same text + "Danger" label annotation)
//   Add a "(irreversible)" caption in FgMuted below the label
```

---

## Player Components

### `AlbumArtwork`
```kotlin
// ui/components/player/AlbumArtwork.kt
//
// Params:
//   artworkPath: String?
//   size: Dp = Layout.albumArtSize
//
// Visual:
//   AsyncImage with contentScale = ContentScale.Crop
//   shape: ContainerCorner (4.dp) — one of the few rounded elements
//   border: 1.dp BorderDefault
//   fallback: BgElevated fill + centered initials in SylphyType.Display, FgMuted
//
// Crossfade: AnimatedContent wrapping the AsyncImage (see ANIMATION_SPECS.md §10)
// Coil: crossfade(Duration.Normal)
```

### `ProgressRing`
```kotlin
// ui/components/player/ProgressRing.kt
// Full implementation in ANIMATION_SPECS.md §1
//
// Params:
//   size: Dp
//   progress: Float    — 0f..1f, pre-animated via animateFloatAsState
//   strokeWidth: Dp = Layout.progressRingStroke (2.dp)
//
// Visual:
//   Clean geometric arc, Butt cap (sharp ends)
//   Full circle ghost track (FgSubtle)
//   Filled arc from 12 o'clock clockwise (FgPrimary)
//   4.dp dot at leading edge (FgPrimary)
//   No glow, no animation within the Canvas itself
```

### `SylphySeekBar`
```kotlin
// ui/components/player/SeekBar.kt
// Full implementation in ANIMATION_SPECS.md §2
//
// Params:
//   positionMs: Long
//   durationMs: Long
//   waveformData: List<Float>?   — modulates line thickness if present
//   onSeek: (Long) -> Unit
//
// Visual:
//   Flat line (2dp) with optional per-segment thickness from waveform
//   Active: FgPrimary, inactive: FgSubtle
//   Playhead: 4.dp filled dot, FgPrimary
//   Time stamps: SylphyType.CodeSmall, FgMuted
//   Tap anywhere: seek to position
//   Drag: continuous seek
```

### `PlayButton`
```kotlin
// ui/components/player/PlayButton.kt
// Full implementation in ANIMATION_SPECS.md §3
//
// Params:
//   isPlaying: Boolean
//   onPress: () -> Unit
//   size: Dp = Layout.playButtonSize (64.dp)
//
// Paused:  White bg, Black icon, no border
// Playing: BgBase bg (black), White icon, BorderStrong border (2.dp)
// Shape: ContainerCorner (4.dp)
// Press: scale 0.94f spring, haptic LongPress
```

### `TransportControls`
```kotlin
// ui/components/player/TransportControls.kt
//
// Params:
//   isPlaying: Boolean
//   shuffleEnabled: Boolean
//   repeatMode: RepeatMode
//   onPlayPause: () -> Unit
//   onNext: () -> Unit
//   onPrevious: () -> Unit
//   onToggleShuffle: () -> Unit
//   onCycleRepeat: () -> Unit
//
// Layout: Row, Arrangement.SpaceBetween, verticalAlignment = CenterVertically
// Order: [Shuffle] [Prev] [Play] [Next] [Repeat]
//
// Icon buttons (Shuffle, Prev, Next, Repeat):
//   size: Layout.transportTapTarget (44.dp) tap target
//   icon: Layout.transportIconSize (24.dp)
//   Inactive: FgMuted tint
//   Active (shuffle on / repeat active): FgPrimary tint
//   Press: opacity flash to FgSubtle (no scale — keep it flat)
//
// Active state indicator for shuffle/repeat:
//   Small 4×4dp square dot below icon — FgPrimary when active, transparent when not
//   NOT a color change — just a dot indicator
```

### `TrackInfoSection`
```kotlin
// ui/components/player/TrackInfo.kt
//
// Params:
//   track: Track?
//
// Layout: Column, centered
//
// Title:
//   SylphyType.DisplayLarge (28.sp, Bold, GeistMono)
//   FgPrimary, single line
//   Marquee if overflows: Modifier.basicMarquee(velocity = 28.dp)
//   AnimatedContent cross-fade on track change
//
// Artist:
//   SylphyType.Body (14.sp, GeistSans)
//   FgSecondary, single line, ellipsis
//
// Spacing between title and artist: Spacing.xs (4.dp)
//
// Null state (no track):
//   Title: "—" in FgMuted
//   Artist: empty
```

### `TickerTape`
```kotlin
// ui/components/player/TickerTape.kt
// Full implementation in ANIMATION_SPECS.md §8
//
// Content: "Track Title  ·  Artist  ·  Album  ·  Year  ·  Format"
// Font: SylphyType.CodeSmall, FgMuted
// No uppercase
// Separator: "  ·  " (space-dot-space)
// Speed: 32.dp/s
// Position: bottom of player screen, padding to nav bar
```

### `SpeedControl`
```kotlin
// ui/components/player/SpeedControl.kt
//
// Params:
//   speed: Float
//   onSpeedChange: (Float) -> Unit
//
// Visual:
//   Small chip: border 1.dp BorderDefault, ContainerCorner, BgBase
//   Text: "1.0×" SylphyType.CodeSmall, FgMuted when 1.0×, FgPrimary otherwise
//   Padding: Spacing.xs vertical, Spacing.sm horizontal
//   Tap: cycles speeds [0.5, 0.75, 1.0, 1.25, 1.5, 2.0]
//   Min touch target: 44.dp
```

### `VolumeIndicator`
```kotlin
// ui/components/player/VolumeIndicator.kt
//
// Brief overlay shown during two-finger volume gesture
// Visual:
//   Thin horizontal bar, centered, 200.dp wide
//   Track: FgSubtle, 2.dp height
//   Fill: FgPrimary, animates to volume * 200.dp
//   Label below: "42%" SylphyType.CodeSmall, FgMuted
//   Background: BgElevated with border 1.dp BorderDefault, ContainerCorner
//   Padding: Spacing.md all around
//   Auto-dismiss: fade out after 1.5s of no gesture update
```

---

## Queue Components

### `QueueItem`
```kotlin
// ui/components/queue/QueueItem.kt
//
// Params:
//   track: Track
//   index: Int            — position in queue (0-based, display as 1-based)
//   isActive: Boolean
//   onRemove: () -> Unit
//   isDragging: Boolean
//
// Height: Layout.queueItemHeight (64.dp)
//
// Layout: Row {
//   drag handle (≡, 40.dp tap) | index (SylphyType.CodeSmall, FgMuted, 36.dp) |
//   Column(title + artist, weight 1) | duration (SylphyType.CodeSmall, FgMuted, 44.dp) |
//   remove × (40.dp tap, FgMuted)
// }
//
// isActive state (INVERTED):
//   background = ActiveBackground (white)
//   title = ActiveForeground (black), SylphyType.Code
//   artist = Black.copy(alpha = 0.5f)
//   index = Black.copy(alpha = 0.4f)
//   drag handle hidden (active track can't be reordered)
//   remove button hidden
//
// isDragging:
//   background = BgElevated
//   border = 1.dp BorderStrong
//   scale = 1.02f (subtle lift)
//
// Default:
//   background = Color.Transparent
//   bottom border = 1.dp Separator
```

### `DraggableQueueList`
```kotlin
// ui/components/queue/DraggableQueueList.kt
//
// LazyColumn + compose-reorderable (sh.calvin.reorderable:reorderable:2.4.0)
//
// Drop indicator:
//   2.dp horizontal line, FgPrimary, full width
//   Appears between items at the insertion point
//
// Auto-scroll: within 80.dp of top/bottom edge
//
// Section structure:
//   No "NOW PLAYING" / "UP NEXT" headers — just a continuous list
//   Active item's visual inversion is enough to identify current track
```

### `QueueHeader`
```kotlin
// ui/components/queue/QueueHeader.kt
//
// Params:
//   trackCount: Int
//   onClear: () -> Unit
//
// Layout: Row SpaceBetween, height 52.dp, horizontal padding Spacing.md
// Left: "Queue" SylphyType.Heading, FgPrimary + "  ${trackCount}" SylphyType.CodeSmall, FgMuted
// Right: "Clear" SylphyType.Body, FgMuted — tap to clear upcoming tracks
// Bottom: 1.dp Separator
```

---

## Library Components

### `TrackRow`
```kotlin
// ui/components/library/TrackRow.kt
//
// Params:
//   track: Track
//   isActive: Boolean
//   onClick: () -> Unit
//   onLongClick: () -> Unit
//
// Height: Layout.trackRowHeight (64.dp)
//
// Layout: Row(verticalAlignment = CenterVertically) {
//   ArtworkThumbnail(48.dp) |
//   Column(weight 1) { title, artist } |
//   Text(duration) — SylphyType.CodeSmall, FgMuted
// }
//
// Thumbnail:
//   AsyncImage, ContainerCorner (4.dp), border 1.dp BorderDefault
//   Fallback: BgElevated + 1-letter monogram
//
// Default state:
//   background: transparent
//   title: SylphyType.Code, FgPrimary
//   artist: SylphyType.BodySmall, FgSecondary
//   bottom divider: 1.dp Separator
//
// isActive state (INVERTED):
//   background: ActiveBackground (white)
//   title: SylphyType.Code, ActiveForeground (black)
//   artist: Black.copy(0.55f)
//   duration: Black.copy(0.4f)
//   Thumbnail border: not visible (covered by white bg)
//
// Press feedback: no ripple — background fades to FgGhost briefly
```

### `AlbumCard`
```kotlin
// ui/components/library/AlbumCard.kt
//
// Params:
//   album: Album
//   onClick: () -> Unit
//
// Grid: 2 columns
// Width: (screenWidth - Spacing.md * 3) / 2  (margin on both sides + between)
//
// Structure: Column {
//   AsyncImage(square, ContainerCorner, border 1.dp BorderDefault)
//   Spacer(Spacing.sm)
//   Text(album.title, SylphyType.Code, FgPrimary, 1 line)
//   Text(album.artist, SylphyType.BodySmall, FgSecondary, 1 line)
//   if (album.year != null) Text(year, SylphyType.CodeSmall, FgMuted)
// }
//
// Press: scale 0.97f spring (same as all buttons)
```

### `ArtistRow`
```kotlin
// ui/components/library/ArtistRow.kt
//
// Params:
//   artist: Artist
//   onClick: () -> Unit
//
// Height: 60.dp
//
// Layout: Row {
//   Monogram square (48×48, BgElevated, ContainerCorner, border 1.dp BorderDefault) |
//   Column { artist.name (SylphyType.Code, FgPrimary), subtitle (SylphyType.BodySmall, FgMuted) }
// }
//
// Monogram: first 2 chars of name, SylphyType.Code, FgSecondary, centered
// Subtitle: "${artist.albumCount} albums  ·  ${artist.trackCount} tracks"
// Bottom divider: 1.dp Separator
```

### `PlaylistCard`
```kotlin
// ui/components/library/PlaylistCard.kt
//
// Params:
//   playlist: Playlist
//   coverArtPaths: List<String?>   — first 4 artwork paths from ViewModel
//   onClick: () -> Unit
//
// Same grid as AlbumCard
//
// Cover: 2×2 grid of artworks (each = 50% of card width, no clip between inner images)
//   Missing slots: BgElevated
//   Outer shape: ContainerCorner (4.dp)
//   Outer border: 1.dp BorderDefault
//
// Name: SylphyType.Code, FgPrimary, 1 line
// Count: "${playlist.trackCount} tracks" SylphyType.CodeSmall, FgMuted
//
// Smart playlist badge: chip below name — "Auto" SylphyType.CodeSmall, FgMuted
//   chip: border 1.dp BorderDefault, ChipCorner, transparent bg
```

### `LibrarySubTabs`
```kotlin
// ui/components/library/LibraryTabs.kt
//
// Params:
//   selected: LibraryTab
//   onSelect: (LibraryTab) -> Unit
//
// enum class LibraryTab { SONGS, ALBUMS, ARTISTS, PLAYLISTS }
//
// Visual: horizontal Row, no background, height 44.dp
// Each tab: text only, no border, no background
// Active: SylphyType.Heading, FgPrimary, with animated 2.dp underline (FgPrimary)
// Inactive: SylphyType.Heading, FgMuted
// Underline slides with animation — NOT inverted pill (different from main nav)
// Differentiation: main nav = inverted pill, sub-tabs = underline indicator
```

### `RecentlyPlayedStrip`
```kotlin
// ui/components/library/RecentlyPlayedStrip.kt
//
// Params:
//   tracks: List<Track>    — max 20, most recent first
//   onTrackClick: (Track) -> Unit
//
// LazyRow, horizontal scroll, showsHorizontalScrollIndicator = false
// Each item: 56×56 artwork + title below (SylphyType.CodeSmall, FgMuted, 1 line, 56.dp width)
// Artwork: ContainerCorner, border 1.dp BorderDefault
// Gap between items: Spacing.sm
// Horizontal padding: Spacing.md
// Above the row: Text("Recently Played", SylphyType.CodeSmall, FgMuted)
```

### `ScanProgressBar`
```kotlin
// ui/components/library/ScanProgressBar.kt
//
// Params:
//   progress: Float    — 0f..1f
//
// Visual:
//   Full-width thin bar (2.dp height)
//   Track: BgElevated (very subtle)
//   Fill: FgPrimary, animates via animateFloatAsState
//   Below bar: "Scanning  ${(progress * 100).toInt()}%" SylphyType.CodeSmall, FgMuted
//   No ContainerCorner — straight flat bar
//   Fade out on completion (500ms)
```

---

## Stats Components (Phase 5)

### `StatsHeatmap`
```kotlin
// ui/components/stats/StatsHeatmap.kt
//
// Params:
//   data: List<DayListening>    — 84 days (12 weeks)
//
// Grid: 7 cols × 12 rows, 12.dp squares, 3.dp gap
// Shape: RectangleShape (0.dp — brutal grid)
//
// Color scale (monochrome — opacity only):
//   0 min:   BgElevated
//   1–15 min: FgSubtle  (FgPrimary at 20%)
//   15–30 min: FgMuted  (FgPrimary at 40%)
//   30–60 min: FgSecondary (FgPrimary at 66%)
//   60+ min:  FgPrimary (100%)
//
// Labels: M T W T F S S — SylphyType.CodeSmall, FgMuted, above grid
// Month label: left column — abbreviated month, rotated 90° or horizontal (fit to space)
```

### `TopTracks`
```kotlin
// ui/components/stats/TopTracks.kt
//
// Params:
//   items: List<TrackWithStats>
//
// 10 rows in LazyColumn
// Each row:
//   Row {
//     rank (SylphyType.CodeSmall, FgMuted, 24.dp fixed) |
//     Column(title, artist) |
//     play count (SylphyType.CodeSmall, FgMuted)
//   }
//   Below title/artist: thin fill bar (4.dp height, FgPrimary fill proportional to count)
//   Rank 1: title in FgPrimary + Medium weight, bar fully white
//   Others: title FgSecondary, bar FgSubtle to FgMuted
```

### `StatCard`
```kotlin
// ui/components/stats/StatCard.kt
//
// Params:
//   value: Int
//   unit: String      — "min", "tracks", "days"
//   label: String
//
// Visual:
//   Box with border 1.dp BorderDefault, ContainerCorner, padding Spacing.lg
//   AnimatedStatNumber (count-up) at top: SylphyType.Stat, FgPrimary
//   Unit inline: SylphyType.Code, FgMuted
//   Label below: SylphyType.Caption, FgMuted
//   Generous internal padding
```
