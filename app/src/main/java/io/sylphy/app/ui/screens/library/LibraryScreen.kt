package io.sylphy.app.ui.screens.library

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sylphy.app.core.util.toHhMm
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.core.util.toTrackCountLabel
import io.sylphy.app.data.local.scanner.ScanProgress
import io.sylphy.app.data.model.Album
import io.sylphy.app.data.model.Artist
import io.sylphy.app.data.model.Playlist
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.components.player.AlbumArtwork
import io.sylphy.app.ui.components.shared.ButtonVariant
import io.sylphy.app.ui.components.shared.ContextMenuAction
import io.sylphy.app.ui.components.shared.ContextMenuSheet
import io.sylphy.app.ui.components.shared.SylphyButton
import io.sylphy.app.ui.components.shared.SylphyDivider
import io.sylphy.app.ui.components.shared.SylphySearchBar
import io.sylphy.app.ui.components.shared.SylphyTabBar
import io.sylphy.app.ui.navigation.Screen
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BgElevated
import io.sylphy.app.ui.theme.BgSunken
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ContainerCorner
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgGhost
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.FgSubtle
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var contextMenuTrack by remember { mutableStateOf<Track?>(null) }
    var playlistTarget by remember { mutableStateOf<Track?>(null) }
    var showCreatePlaylist by remember { mutableStateOf(false) }

    // ── Permission handling ──────────────────────────────────────────────────
    val mediaPermission =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    var hasMediaPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, mediaPermission,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasMediaPermission = granted
        if (granted) viewModel.scanLibrary()
    }

    // ── Root layout ──────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            // top = md (16dp) gives the header room without eating too much space
            // no bottom padding — nav bar inset is handled by the pager / NavGraph
            .padding(top = Spacing.md),
    ) {
        // Header row
        LibraryHeader(
            hasPermission = hasMediaPermission,
            isScanning = uiState.scanStatus is ScanProgress.Scanning,
            onStats = { navController.navigate(Screen.Stats.route) },
            onSettings = { navController.navigate(Screen.Settings.route) },
            onScan = {
                if (hasMediaPermission) viewModel.scanLibrary()
                else permissionLauncher.launch(mediaPermission)
            },
            modifier = Modifier.padding(horizontal = Spacing.md),
        )

        Spacer(Modifier.height(Spacing.md))

        // Search bar
        SylphySearchBar(
            value = uiState.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = "Search songs, albums, artists",
            modifier = Modifier.padding(horizontal = Spacing.md),
        )

        // Scan progress bar — appears below search when scanning
        ScanProgressBar(
            scanStatus = uiState.scanStatus,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )

        Spacer(Modifier.height(Spacing.sm))

        // Segment tab bar — uses SylphyTabBar (replaces custom LibrarySubTabs)
        val tabLabels = LibraryTab.entries.map { it.name }
        SylphyTabBar(
            tabs = tabLabels,
            selectedIndex = uiState.selectedTab.ordinal,
            onTabSelected = { index ->
                viewModel.selectTab(LibraryTab.entries[index])
            },
        )

        // Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val searchResults = uiState.searchResults
            if (searchResults != null) {
                SearchResultsList(
                    results = searchResults,
                    onTrackClick = { viewModel.playTrack(it, searchResults.tracks) },
                    onTrackLongClick = { contextMenuTrack = it },
                    onAlbumClick = { navController.navigate(Screen.AlbumDetail.route(it.id)) },
                    onArtistClick = { navController.navigate(Screen.ArtistDetail.route(it.id)) },
                )
            } else {
                when (uiState.selectedTab) {
                    LibraryTab.Songs -> SongsTab(
                        tracks = uiState.tracks,
                        recentlyPlayed = uiState.recentlyPlayed,
                        onTrackClick = { viewModel.playTrack(it, uiState.tracks) },
                        onTrackLongClick = { contextMenuTrack = it },
                    )
                    LibraryTab.Albums -> AlbumsTab(
                        albums = uiState.albums,
                        onAlbumClick = { navController.navigate(Screen.AlbumDetail.route(it.id)) },
                    )
                    LibraryTab.Artists -> ArtistsTab(
                        artists = uiState.artists,
                        onArtistClick = { navController.navigate(Screen.ArtistDetail.route(it.id)) },
                    )
                    LibraryTab.Playlists -> PlaylistsTab(
                        playlists = uiState.playlists,
                        onCreate = { showCreatePlaylist = true },
                        onPlaylistClick = { navController.navigate(Screen.PlaylistDetail.route(it.id)) },
                    )
                }
            }
        }
    }

    // ── Overlays (rendered outside the Column so they sit above everything) ──

    contextMenuTrack?.let { track ->
        ContextMenuSheet(
            track = track,
            onDismiss = { contextMenuTrack = null },
            actions = listOf(
                ContextMenuAction(
                    label = "Play next",
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.playTrack(track, listOf(track)) },
                ),
                ContextMenuAction(
                    label = "Add to playlist",
                    icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                    onClick = { playlistTarget = track },
                ),
                ContextMenuAction(
                    label = if (track.isFavorite) "Remove favourite" else "Favourite",
                    icon = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    onClick = { viewModel.toggleFavorite(track) },
                ),
            ),
        )
    }

    playlistTarget?.let { track ->
        PlaylistPickerDialog(
            playlists = uiState.playlists,
            onDismiss = { playlistTarget = null },
            onCreate = {
                playlistTarget = null
                showCreatePlaylist = true
            },
            onPick = {
                viewModel.addToPlaylist(it.id, track.id)
                playlistTarget = null
            },
        )
    }

    if (showCreatePlaylist) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylist = false },
            onCreate = {
                viewModel.createPlaylist(it)
                showCreatePlaylist = false
            },
        )
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun LibraryHeader(
    hasPermission: Boolean,
    isScanning: Boolean,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "LIBRARY",
            style = SylphyType.DisplayLarge,
            color = FgPrimary,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = onStats,
            modifier = Modifier.size(Layout.transportTapTarget),
        ) {
            Icon(
                imageVector = Icons.Default.QueryStats,
                contentDescription = "Listening stats",
                tint = FgMuted,
                modifier = Modifier.size(Layout.transportIconSize),
            )
        }

        IconButton(
            onClick = onSettings,
            modifier = Modifier.size(Layout.transportTapTarget),
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = FgMuted,
                modifier = Modifier.size(Layout.transportIconSize),
            )
        }

        Spacer(Modifier.width(Spacing.xs))

        SylphyButton(
            text = when {
                isScanning      -> "Scanning…"
                hasPermission   -> "Scan"
                else            -> "Allow"
            },
            variant = ButtonVariant.Outline,
            onClick = onScan,
        )
    }
}

// ─── Scan progress bar ────────────────────────────────────────────────────────

@Composable
private fun ScanProgressBar(
    scanStatus: ScanProgress,
    modifier: Modifier = Modifier,
) {
    val scanning = scanStatus as? ScanProgress.Scanning
    AnimatedVisibility(
        visible = scanning != null,
        enter = fadeIn(tween(Duration.Normal)),
        exit = fadeOut(tween(Duration.Slow)),
    ) {
        scanning?.let {
            Column(modifier = modifier) {
                Spacer(Modifier.height(Spacing.sm))
                LinearProgressIndicator(
                    progress = { it.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(CircleShape),
                    color = FgPrimary,
                    trackColor = FgGhost,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "SCANNING ${it.found} TRACKS · ${(it.progress * 100).toInt()}%",
                    style = SylphyType.CodeSmall,
                    color = FgMuted,
                )
            }
        }
    }
}

// ─── Songs tab ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongsTab(
    tracks: List<Track>,
    recentlyPlayed: List<Track>,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
) {
    // Group alphabetically; non-letter first chars bucket into "#"
    val sectioned = remember(tracks) {
        tracks
            .groupBy {
                it.title
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.takeIf { c -> c.isLetter() }
                    ?.toString() ?: "#"
            }
            .toSortedMap(compareBy { if (it == "#") "\u0000" else it })
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Recently played horizontal strip — sticky so it stays visible while
        // the user scrolls down, then scrolls away with the list
        if (recentlyPlayed.isNotEmpty()) {
            stickyHeader(key = "recent_header") {
                RecentlyPlayedStrip(
                    tracks = recentlyPlayed,
                    onTrackClick = onTrackClick,
                )
            }
        }

        sectioned.forEach { (letter, sectionTracks) ->
            stickyHeader(key = "header_$letter") {
                SectionHeader(title = letter)
            }
            items(sectionTracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    onTrackClick = onTrackClick,
                    onTrackLongClick = onTrackLongClick,
                )
            }
        }
    }
}

// ─── Recently played strip ────────────────────────────────────────────────────

@Composable
private fun RecentlyPlayedStrip(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
) {
    // Background matches BgBase so the sticky header blends with the list
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBase)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    ) {
        Text(
            text = "RECENT",
            style = SylphyType.CodeSmall,
            color = FgMuted,
            modifier = Modifier.padding(bottom = Spacing.sm),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            items(tracks, key = { it.id }) { track ->
                RecentCard(track = track, onClick = { onTrackClick(track) })
            }
        }
    }
}

@Composable
private fun RecentCard(track: Track, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        // AlbumArtwork handles fallback + border + corner — no wrapping needed
        AlbumArtwork(
            artworkPath = track.artworkPath,
            size = 96.dp,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = track.title,
            style = SylphyType.CodeSmall,
            color = FgPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = track.artist,
            style = SylphyType.Caption,
            color = FgMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─── Albums tab ───────────────────────────────────────────────────────────────

@Composable
private fun AlbumsTab(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(Layout.albumGridColumns),
        contentPadding = PaddingValues(
            horizontal = Spacing.md,
            vertical = Spacing.sm,
        ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumCard(album = album, onClick = onAlbumClick)
        }
    }
}

@Composable
private fun AlbumCard(album: Album, onClick: (Album) -> Unit) {
    Column(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onClick(album) },
        ),
    ) {
        // size = Dp.Unspecified → AlbumArtwork uses the modifier for sizing
        AlbumArtwork(
            artworkPath = album.artworkPath,
            size = Dp.Unspecified,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = album.title,
            style = SylphyType.Code,
            color = FgPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = album.artist,
            style = SylphyType.BodySmall,
            color = FgMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─── Artists tab ──────────────────────────────────────────────────────────────

@Composable
private fun ArtistsTab(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(artists, key = { it.id }) { artist ->
            ListEntityRow(
                title = artist.name,
                subtitle = "${artist.albumCount} albums · ${artist.trackCount.toTrackCountLabel()}",
                leading = null,
                onClick = { onArtistClick(artist) },
            )
        }
    }
}

// ─── Playlists tab ────────────────────────────────────────────────────────────

@Composable
private fun PlaylistsTab(
    playlists: List<Playlist>,
    onCreate: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "create_playlist") {
            ListEntityRow(
                title = "Create playlist",
                subtitle = "New custom queue",
                leading = Icons.Default.Add,
                onClick = onCreate,
            )
        }
        items(playlists, key = { it.id }) { playlist ->
            ListEntityRow(
                title = playlist.name,
                subtitle = "${playlist.trackCount.toTrackCountLabel()} · ${playlist.durationMs.toHhMm()}",
                leading = null,
                onClick = { onPlaylistClick(playlist) },
            )
        }
    }
}

// ─── Search results ───────────────────────────────────────────────────────────

@Composable
private fun SearchResultsList(
    results: SearchResults,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (results.tracks.isEmpty() && results.albums.isEmpty() && results.artists.isEmpty()) {
            item(key = "no_results") {
                SectionHeader(title = "NO RESULTS")
            }
        }

        if (results.tracks.isNotEmpty()) {
            item(key = "songs_header") { SectionHeader(title = "SONGS") }
            items(results.tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    onTrackClick = onTrackClick,
                    onTrackLongClick = onTrackLongClick,
                )
            }
        }

        if (results.albums.isNotEmpty()) {
            item(key = "albums_header") { SectionHeader(title = "ALBUMS") }
            items(results.albums, key = { it.id }) { album ->
                ListEntityRow(
                    title = album.title,
                    subtitle = album.artist,
                    leading = null,
                    onClick = { onAlbumClick(album) },
                )
            }
        }

        if (results.artists.isNotEmpty()) {
            item(key = "artists_header") { SectionHeader(title = "ARTISTS") }
            items(results.artists, key = { it.id }) { artist ->
                ListEntityRow(
                    title = artist.name,
                    subtitle = artist.trackCount.toTrackCountLabel(),
                    leading = null,
                    onClick = { onArtistClick(artist) },
                )
            }
        }
    }
}

// ─── Track row ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackRow(
    track: Track,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 800f),
        label = "track_row_scale",
    )
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Use the Layout constant — was hardcoded to 72dp in the original
            .height(Layout.trackRowHeight)
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTrackClick(track)
                },
                onLongClick = { onTrackLongClick(track) },
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArtwork(
            artworkPath = track.artworkPath,
            size = Layout.albumArtSizeSm,
        )
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = SylphyType.Code,
                color = FgPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = SylphyType.BodySmall,
                color = FgMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = track.durationMs.toMmSs(),
            style = SylphyType.CodeSmall,
            color = FgGhost,
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }

    // Inset divider — starts after the artwork column to match the design
    SylphyDivider(
        modifier = Modifier.padding(
            start = Spacing.md + Layout.albumArtSizeSm + Spacing.md,
        ),
    )
}

// ─── Section header ───────────────────────────────────────────────────────────

@Composable
internal fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Layout.sectionHeaderHeight)
            .background(BgBase)
            // Horizontal padding was missing in the original — text started at x=0
            .padding(horizontal = Spacing.md),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            style = SylphyType.CodeSmall,
            color = FgMuted,
        )
    }
}

// ─── Generic entity row (Artists, Playlists, Albums in search) ────────────────

@Composable
private fun ListEntityRow(
    title: String,
    subtitle: String,
    leading: ImageVector?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Layout.trackRowHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading icon or first-letter avatar
        Box(
            modifier = Modifier
                .size(Layout.albumArtSizeSm)
                .clip(ContainerCorner)
                .border(Layout.borderThin, BorderDefault, ContainerCorner)
                .background(if (leading != null) BgSunken else BgElevated),
            contentAlignment = Alignment.Center,
        ) {
            if (leading != null) {
                Icon(
                    imageVector = leading,
                    contentDescription = null,
                    tint = FgPrimary,
                    modifier = Modifier.size(Layout.transportIconSize),
                )
            } else {
                Text(
                    text = title.take(1).uppercase(),
                    style = SylphyType.Display,
                    color = FgSubtle,
                )
            }
        }

        Spacer(Modifier.width(Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SylphyType.Code,
                color = FgPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = SylphyType.BodySmall,
                color = FgMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    SylphyDivider(
        modifier = Modifier.padding(
            start = Spacing.md + Layout.albumArtSizeSm + Spacing.md,
        ),
    )
}

// ─── Playlist picker dialog ───────────────────────────────────────────────────

@Composable
private fun PlaylistPickerDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onPick: (Playlist) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgElevated,
        title = {
            Text(
                text = "Add to playlist",
                style = SylphyType.Heading,
                color = FgPrimary,
            )
        },
        text = {
            LazyColumn {
                item(key = "create_new") {
                    Text(
                        text = "Create new playlist",
                        style = SylphyType.Body,
                        color = FgPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onCreate)
                            .padding(vertical = Spacing.sm),
                    )
                    SylphyDivider()
                }
                items(playlists, key = { it.id }) { playlist ->
                    Text(
                        text = playlist.name,
                        style = SylphyType.Body,
                        color = FgPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(playlist) }
                            .padding(vertical = Spacing.sm),
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", style = SylphyType.Body, color = FgMuted)
            }
        },
    )
}

// ─── Create playlist dialog ───────────────────────────────────────────────────

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgElevated,
        title = {
            Text(
                text = "Create playlist",
                style = SylphyType.Heading,
                color = FgPrimary,
            )
        },
        text = {
            // Styled text input — replaces raw Material3 TextField which
            // ignores the app theme and renders with wrong colors + shape
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = SylphyType.Body.copy(color = FgPrimary),
                cursorBrush = SolidColor(FgPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (name.isNotBlank()) onCreate(name.trim())
                }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(ContainerCorner)
                            .background(BgSunken)
                            .border(Layout.borderThin, BorderDefault, ContainerCorner)
                            .padding(horizontal = Spacing.md),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (name.isEmpty()) {
                            Text(
                                text = "Playlist name",
                                style = SylphyType.Body,
                                color = FgMuted,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text(
                    text = "Create",
                    style = SylphyType.Body,
                    color = if (name.isNotBlank()) FgPrimary else FgSubtle,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", style = SylphyType.Body, color = FgMuted)
            }
        },
    )
}
