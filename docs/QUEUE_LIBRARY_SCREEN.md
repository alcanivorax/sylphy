# Queue Screen — Monochrome Brutalist Layout

## File: `ui/screens/queue/QueueScreen.kt`

---

## Visual Layout

```
┌──────────────────────────────────────────┐
│  PLAYER      QUEUE      LIBRARY          │  ← SylphyTabBar
│              ▓▓▓▓▓                       │
├──────────────────────────────────────────┤
│                                          │
│  Queue              12 tracks   Clear    │  ← QueueHeader (52dp)
│──────────────────────────────────────────│
│                                          │
│  ≡   1   Blinding Lights           3:22 ×│  ← Active item (INVERTED)
│         The Weeknd                       │  ← white bg, black text
│──────────────────────────────────────────│
│  ≡   2   Save Your Tears           3:35 ×│  ← Standard item
│         The Weeknd                       │
│  ≡   3   Starboy                   3:50 ×│
│         The Weeknd ft. Daft Punk         │
│  ≡   4   In Your Eyes              3:57 ×│
│         The Weeknd                       │
│  ≡   5   Die For You               4:20 ×│
│  ≡   6   I Feel It Coming          4:09 ×│
│  ≡   7   Earned It                 4:13 ×│
│  ...                                     │
│                                          │
└──────────────────────────────────────────┘
```

---

## Design Notes

**The active item inverts** — full white background fills the row, all text goes black. This is immediately readable with zero ambiguity. No chevron, no icon, no color — just the inversion.

**Drag handles are FgMuted** — they should not compete with content. Only visible on non-active rows.

**Remove button is a simple ×** — FgMuted. Not red, not alarming. The action is reversible (tracks can be re-added), so it doesn't need to feel dangerous.

**No "NOW PLAYING" / "UP NEXT" section headers** — the inverted active row provides enough context. Headers add visual noise.

---

## Implementation

```kotlin
@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.queue.isEmpty()) {
        EmptyState(
            title       = "Queue is empty",
            description = "Add tracks from the library to build a queue.",
        )
        return
    }

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> viewModel.reorderQueue(from.index, to.index) }
    )

    Column(Modifier.fillMaxSize().background(BgBase)) {

        QueueHeader(
            trackCount = uiState.queue.size,
            onClear    = viewModel::clearUpcoming,
        )

        LazyColumn(
            state   = reorderState.listState,
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(
                items = uiState.queue,
                key   = { _, track -> track.id },
            ) { index, track ->
                ReorderableItem(reorderState, key = track.id) { isDragging ->
                    QueueItem(
                        track      = track,
                        index      = index + 1,
                        isActive   = index == uiState.currentIndex,
                        isDragging = isDragging,
                        onRemove   = { viewModel.removeFromQueue(index) },
                        onTap      = { viewModel.skipToIndex(index) },
                        dragHandle = {
                            Icon(
                                painter = painterResource(R.drawable.ic_drag_handle),
                                contentDescription = "Drag to reorder",
                                tint   = FgMuted,
                                modifier = Modifier
                                    .detectReorder(reorderState)
                                    .size(Layout.transportTapTarget),
                            )
                        },
                    )
                }
                if (index != uiState.queue.lastIndex) {
                    SylphyDivider(Modifier.padding(start = 40.dp + 36.dp + Spacing.md))
                    // indent divider past drag handle + index number
                }
            }
        }
    }
}
```

---

## Edge Cases

| Scenario | Behavior |
|---|---|
| 1 track in queue | No drag handle, no divider, full inversion on single row |
| Dragging over active item | Cannot drop — active item is immovable |
| Clearing queue | Removes all but currently playing track, list collapses to 1 item |
| Tapping active track row | Seeks to 0 (restart) |
| Tapping non-active row | Skips to that index, starts playing |

---
---

# Library Screen — Monochrome Brutalist Layout

## File: `ui/screens/library/LibraryScreen.kt`

---

## Visual Layout

```
┌──────────────────────────────────────────┐
│  PLAYER      QUEUE      LIBRARY          │  ← SylphyTabBar
│                          ▓▓▓▓▓▓▓         │
├──────────────────────────────────────────┤
│                                          │
│  ┌────────────────────────────────────┐  │  ← SylphySearchBar
│  │ 🔍 Search tracks, albums...        │  │    BgSunken, 4dp corners
│  └────────────────────────────────────┘  │
│                                          │
│  Songs   Albums   Artists   Playlists    │  ← LibrarySubTabs (underline style)
│  ──────                                  │
│                                          │
│  Recently Played                         │  ← Section label (FgMuted)
│  [art][art][art][art][art]→              │  ← RecentlyPlayedStrip
│                                          │
│  A                                       │  ← SectionHeader (sticky)
│  [art] Adagio for Strings     11:09     │
│         Samuel Barber                    │
│  [art] All Falls Down          3:32     │
│         Kanye West                       │
│  B                                       │
│  [art] Bohemian Rhapsody       5:55     │
│         Queen                            │
│  ...                                     │
└──────────────────────────────────────────┘
```

---

## Design Notes

**Search bar first** — Vercel-style: utility before decoration. The search bar is always visible, not hidden behind a button.

**Sub-tabs use underline, not pill** — differentiated from main navigation (which uses the inverted pill). This hierarchical difference in indicator style signals "you are inside LIBRARY" vs "you are at the top level."

**Section headers are minimal** — just the letter in FgMuted. No heavy backgrounds. They exist only to provide alphabet orientation.

**Track rows are content-dense but not cramped** — 64dp height with generous internal padding. The 48dp thumbnail provides visual interest without requiring much horizontal space.

---

## Implementation

```kotlin
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery  by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeTrackId = // observe from shared player state

    val showSearch = searchQuery.length >= 2

    Column(Modifier.fillMaxSize().background(BgBase)) {

        // Search bar — always visible
        SylphySearchBar(
            value         = searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder   = "Search tracks, albums, artists...",
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        )

        if (showSearch) {
            // Search results overlay
            SearchResults(
                results     = uiState.searchResults,
                activeId    = activeTrackId,
                onTrackClick = { track ->
                    viewModel.playTrack(track, uiState.tracks)
                },
                onAlbumClick = { navController.navigate("album/${it.id}") },
                onArtistClick = { navController.navigate("artist/${it.id}") },
            )
        } else {
            // Sub-tabs
            LibrarySubTabs(
                selected = uiState.activeTab,
                onSelect = viewModel::setActiveTab,
                modifier = Modifier.padding(horizontal = Spacing.md),
            )

            // Scan progress (shown during/after first scan)
            if (uiState.scanStatus is ScanProgress.Scanning) {
                val p = (uiState.scanStatus as ScanProgress.Scanning).progress
                ScanProgressBar(progress = p, modifier = Modifier.padding(Spacing.md))
            }

            // Empty library prompt
            if (uiState.tracks.isEmpty() && uiState.scanStatus !is ScanProgress.Scanning) {
                EmptyLibraryState(onScan = viewModel::scanLibrary)
            } else {
                // Content by active tab
                when (uiState.activeTab) {
                    LibraryTab.SONGS    -> SongsTab(uiState, activeTrackId, viewModel)
                    LibraryTab.ALBUMS   -> AlbumsTab(uiState, navController)
                    LibraryTab.ARTISTS  -> ArtistsTab(uiState, navController)
                    LibraryTab.PLAYLISTS -> PlaylistsTab(uiState, navController, viewModel)
                }
            }
        }
    }
}
```

---

## Songs Tab

```kotlin
@Composable
private fun SongsTab(
    uiState: LibraryUiState,
    activeTrackId: String?,
    viewModel: LibraryViewModel,
) {
    val sectioned = remember(uiState.tracks) {
        buildSectionedList(uiState.tracks)
    }

    LazyColumn(Modifier.fillMaxSize()) {

        // Recently played strip (if any)
        if (uiState.recentlyPlayed.isNotEmpty()) {
            item {
                RecentlyPlayedStrip(
                    tracks       = uiState.recentlyPlayed,
                    onTrackClick = { viewModel.playTrack(it, uiState.tracks) },
                    modifier     = Modifier.padding(bottom = Spacing.md),
                )
            }
        }

        // Alphabetic sections
        sectioned.forEach { (letter, tracks) ->
            stickyHeader(key = "header_$letter") {
                SectionHeader(title = letter)
            }
            items(tracks, key = { it.id }) { track ->
                TrackRow(
                    track        = track,
                    isActive     = track.id == activeTrackId,
                    onClick      = { viewModel.playTrack(track, uiState.tracks) },
                    onLongClick  = { /* show context menu */ },
                    modifier     = Modifier.animateItem(),
                )
            }
        }

        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

// Sectioned data builder
fun buildSectionedList(tracks: List<Track>): Map<String, List<Track>> {
    return tracks
        .sortedBy { it.title.trimArticle() }
        .groupBy { track ->
            val first = track.title.trimArticle().firstOrNull()?.uppercaseChar() ?: '#'
            if (first.isLetter()) first.toString() else "#"
        }
        .toSortedMap(compareBy { if (it == "#") "\uFFFF" else it })
    // "#" sorts to end
}
```

---

## Albums Tab

```kotlin
@Composable
private fun AlbumsTab(uiState: LibraryUiState, navController: NavController) {
    LazyVerticalGrid(
        columns  = GridCells.Fixed(Layout.albumGridColumns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalArrangement   = Arrangement.spacedBy(Spacing.md),
    ) {
        items(uiState.albums, key = { it.id }) { album ->
            AlbumCard(
                album   = album,
                onClick = { navController.navigate("album/${album.id}") },
                modifier = Modifier.animateItem(),
            )
        }
        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
```

---

## Album Detail Screen

```kotlin
// ui/screens/library/AlbumDetailScreen.kt

@Composable
fun AlbumDetailScreen(albumId: String, navController: NavController) {
    // Layout:
    // LazyColumn with header as first item (not CollapsingToolbar — too much complexity)
    // Header: artwork (full width, aspect 1:1) + metadata below
    // Track list: numbered rows

    LazyColumn(Modifier.fillMaxSize().background(BgBase)) {

        // Header item
        item {
            Column {
                AsyncImage(
                    model  = artworkPath,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .border(Layout.borderThin, BorderDefault),
                )

                Column(Modifier.padding(Spacing.lg)) {
                    Text(album.title, style = SylphyType.Display, color = FgPrimary)
                    Spacer(Modifier.height(Spacing.xs))
                    Text(album.artist, style = SylphyType.Body, color = FgSecondary)
                    Spacer(Modifier.height(Spacing.xs))
                    Row {
                        Text("${album.trackCount} tracks", style = SylphyType.CodeSmall, color = FgMuted)
                        Text("  ·  ", style = SylphyType.CodeSmall, color = FgMuted)
                        Text(album.durationMs.toHhMm(), style = SylphyType.CodeSmall, color = FgMuted)
                        if (album.year != null) {
                            Text("  ·  ", style = SylphyType.CodeSmall, color = FgMuted)
                            Text("${album.year}", style = SylphyType.CodeSmall, color = FgMuted)
                        }
                    }

                    Spacer(Modifier.height(Spacing.lg))

                    // Action row
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        SylphyButton("Play all",  onClick = { viewModel.playAll() }, variant = ButtonVariant.Solid)
                        SylphyButton("Shuffle",   onClick = { viewModel.shufflePlay() }, variant = ButtonVariant.Outline)
                    }
                }

                SylphyDivider()
            }
        }

        // Track list
        itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
            AlbumTrackRow(
                track    = track,
                number   = index + 1,
                isActive = track.id == activeTrackId,
                onClick  = { viewModel.playFrom(tracks, index) },
            )
        }

        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

// AlbumTrackRow — no thumbnail (album context is established)
@Composable
private fun AlbumTrackRow(track: Track, number: Int, isActive: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Layout.trackRowHeight)
            .background(if (isActive) ActiveBackground else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Track number (or playing indicator)
        Text(
            text  = if (isActive) "▶" else number.toString().padStart(2, '0'),
            style = SylphyType.CodeSmall,
            color = if (isActive) ActiveForeground else FgMuted,
            modifier = Modifier.width(28.dp),
        )
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style    = SylphyType.Code,
                color    = if (isActive) ActiveForeground else FgPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            track.durationMs.toMmSs(),
            style = SylphyType.CodeSmall,
            color = if (isActive) ActiveForeground.copy(alpha = 0.5f) else FgMuted,
        )
    }
    if (!isActive) SylphyDivider(Modifier.padding(start = 28.dp + Spacing.md + Spacing.lg))
}
```

---

## Search Results

```kotlin
// SearchResults composable — replaces library tabs when search active

@Composable
private fun SearchResults(
    results: SearchResults?,
    activeId: String?,
    onTrackClick: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
) {
    if (results == null) {
        // Searching... state
        Box(Modifier.fillMaxSize(), Alignment.Center) { LoadingDots() }
        return
    }

    if (results.isEmpty()) {
        EmptyState(title = "No results", description = "Try different keywords.")
        return
    }

    LazyColumn(Modifier.fillMaxSize()) {
        // Tracks section
        if (results.tracks.isNotEmpty()) {
            stickyHeader {
                SectionHeader(title = "Tracks (${results.tracks.size})")
            }
            items(results.tracks, key = { "track_${it.id}" }) { track ->
                TrackRow(
                    track       = track,
                    isActive    = track.id == activeId,
                    onClick     = { onTrackClick(track) },
                    onLongClick = {},
                )
            }
        }

        // Albums section
        if (results.albums.isNotEmpty()) {
            stickyHeader {
                SectionHeader(title = "Albums (${results.albums.size})")
            }
            items(results.albums, key = { "album_${it.id}" }) { album ->
                AlbumSearchRow(album = album, onClick = { onAlbumClick(album) })
            }
        }

        // Artists section
        if (results.artists.isNotEmpty()) {
            stickyHeader {
                SectionHeader(title = "Artists (${results.artists.size})")
            }
            items(results.artists, key = { "artist_${it.id}" }) { artist ->
                ArtistRow(artist = artist, onClick = { onArtistClick(artist) })
            }
        }

        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}
```

---

## Context Menu Design

Long press any `TrackRow` → `ContextMenuSheet` slides up:

```
┌────────────────────────────────────┐
│         ———  (drag handle)         │
│  [art]  Track Title                │  ← track header
│         Artist Name                │
├────────────────────────────────────┤
│  ▶  Play next                      │
│  +  Add to queue                   │
│  +  Add to playlist...             │
│  ♡  Add to favourites              │
│  ℹ  Track info                     │
└────────────────────────────────────┘
```

Track info bottom sheet (secondary sheet from Track Info action):
```
Track Title            3:32
Artist Name

Format         MP3
Bitrate        320 kbps
Sample rate    44100 Hz
File size      7.4 MB
Added          12 Jan 2024
Last played    Yesterday
```

All in GeistMono. Label left FgMuted, value right FgPrimary. Two-column grid layout.
