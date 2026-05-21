package io.sylphy.app.ui.screens.library

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import io.sylphy.app.ui.theme.SylphyEasing
import io.sylphy.app.ui.theme.SylphyType
import kotlin.math.roundToInt

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

    val mediaPermission =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    var hasMediaPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, mediaPermission) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasMediaPermission = granted
        if (granted) viewModel.scanLibrary()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = Spacing.md, vertical = Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("LIBRARY", style = SylphyType.Display, color = FgPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = { navController.navigate(Screen.Stats.route) }) {
                Icon(Icons.Default.QueryStats, contentDescription = "Stats", tint = FgPrimary)
            }
            SylphyButton(
                text = if (hasMediaPermission) "Scan" else "Permission",
                variant = ButtonVariant.Outline,
                onClick = {
                    if (hasMediaPermission) viewModel.scanLibrary()
                    else permissionLauncher.launch(mediaPermission)
                },
            )
        }

        Spacer(Modifier.height(Spacing.md))
        SylphySearchBar(
            value = uiState.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = "Search songs, albums, artists",
        )
        ScanProgressBar(uiState.scanStatus)
        LibrarySubTabs(uiState.selectedTab, viewModel::selectTab)

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

    contextMenuTrack?.let { track ->
        ContextMenuSheet(
            track = track,
            onDismiss = { contextMenuTrack = null },
            actions = listOf(
                ContextMenuAction("Play next", Icons.Default.PlayArrow) {
                    viewModel.playTrack(track, listOf(track))
                },
                ContextMenuAction("Add to playlist", Icons.AutoMirrored.Filled.PlaylistAdd) {
                    playlistTarget = track
                },
                ContextMenuAction(
                    if (track.isFavorite) "Remove favorite" else "Favorite",
                    if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                ) {
                    viewModel.toggleFavorite(track)
                },
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

@Composable
private fun ScanProgressBar(scanStatus: ScanProgress) {
    val scanning = scanStatus as? ScanProgress.Scanning
    AnimatedVisibility(
        visible = scanning != null,
        enter = fadeIn(tween(Duration.Normal)),
        exit = fadeOut(tween(Duration.Slow))
    ) {
        scanning?.let {
            Column {
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
                Text("SCANNING ${it.found} TRACKS · ${(it.progress * 100).toInt()}%", style = SylphyType.CodeSmall, color = FgMuted)
            }
        }
    }
}

@Composable
private fun LibrarySubTabs(selected: LibraryTab, onSelect: (LibraryTab) -> Unit) {
    val tabs = LibraryTab.entries
    val density = LocalDensity.current
    val underlineOffset = remember { Animatable(0f) }

    BoxWithConstraints(Modifier.padding(top = Spacing.lg, bottom = Spacing.md)) {
        val tabWidth = maxWidth / tabs.size
        val tabWidthPx = with(density) { tabWidth.toPx() }

        LaunchedEffect(selected, tabWidthPx) {
            underlineOffset.animateTo(
                targetValue = selected.ordinal * tabWidthPx,
                animationSpec = tween(Duration.Slow, easing = SylphyEasing.Standard),
            )
        }

        Column {
            Row(Modifier.fillMaxWidth()) {
                tabs.forEach { tab ->
                    val active = tab == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelect(tab) }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab.name.uppercase(), 
                            style = SylphyType.CodeSmall, 
                            color = if (active) FgPrimary else FgMuted,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(FgGhost)) {
                Box(
                    Modifier
                        .offset { IntOffset(underlineOffset.value.roundToInt(), 0) }
                        .width(tabWidth)
                        .height(1.dp)
                        .background(FgPrimary),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongsTab(
    tracks: List<Track>,
    recentlyPlayed: List<Track>,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
) {
    val sectioned = remember(tracks) {
        tracks.groupBy { it.title.firstOrNull()?.uppercaseChar()?.takeIf { c -> c.isLetter() }?.toString() ?: "#" }
            .toSortedMap()
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (recentlyPlayed.isNotEmpty()) {
            stickyHeader(key = "recent") {
                RecentlyPlayedStrip(recentlyPlayed, onTrackClick)
            }
        }
        sectioned.forEach { (letter, sectionTracks) ->
            stickyHeader(key = "header_$letter") { SectionHeader(letter) }
            items(sectionTracks, key = { it.id }) { track ->
                TrackRow(track, onTrackClick = onTrackClick, onTrackLongClick = onTrackLongClick)
            }
        }
    }
}

@Composable
private fun RecentlyPlayedStrip(tracks: List<Track>, onTrackClick: (Track) -> Unit) {
    Column(Modifier.background(BgBase).padding(bottom = Spacing.sm)) {
        Text("RECENT", style = SylphyType.CodeSmall, color = FgMuted, modifier = Modifier.padding(vertical = Spacing.xs))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            items(tracks, key = { it.id }) { track ->
                Column(
                    modifier = Modifier.width(112.dp).clickable { onTrackClick(track) },
                ) {
                    AlbumArtwork(track.artworkPath, size = 96.dp)
                    Spacer(Modifier.height(Spacing.sm))
                    Text(track.title, style = SylphyType.CodeSmall, color = FgPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(track.artist, style = SylphyType.Caption, color = FgMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun AlbumsTab(albums: List<Album>, onAlbumClick: (Album) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(Layout.albumGridColumns),
        contentPadding = PaddingValues(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(albums, key = { it.id }) { album -> AlbumCard(album, onAlbumClick) }
    }
}

@Composable
private fun AlbumCard(album: Album, onClick: (Album) -> Unit) {
    Column(Modifier.clickable { onClick(album) }) {
        AlbumArtwork(album.artworkPath, modifier = Modifier.fillMaxWidth().aspectRatio(1f), size = Dp.Unspecified)
        Spacer(Modifier.height(Spacing.sm))
        Text(album.title, style = SylphyType.Code, color = FgPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(album.artist, style = SylphyType.BodySmall, color = FgMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ArtistsTab(artists: List<Artist>, onArtistClick: (Artist) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(artists, key = { it.id }) { artist ->
            ListEntityRow(
                title = artist.name,
                subtitle = "${artist.albumCount} albums · ${artist.trackCount.toTrackCountLabel()}",
                onClick = { onArtistClick(artist) },
            )
        }
    }
}

@Composable
private fun PlaylistsTab(playlists: List<Playlist>, onCreate: () -> Unit, onPlaylistClick: (Playlist) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ListEntityRow("Create playlist", "New custom queue", onCreate, leading = Icons.Default.Add)
        }
        items(playlists, key = { it.id }) { playlist ->
            ListEntityRow(
                title = playlist.name,
                subtitle = "${playlist.trackCount.toTrackCountLabel()} · ${playlist.durationMs.toHhMm()}",
                onClick = { onPlaylistClick(playlist) },
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: SearchResults,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        if (results.tracks.isNotEmpty()) {
            item(key = "songs_header") { SectionHeader("SONGS") }
            items(results.tracks, key = { it.id }) { TrackRow(it, onTrackClick, onTrackLongClick) }
        }
        if (results.albums.isNotEmpty()) {
            item(key = "albums_header") { SectionHeader("ALBUMS") }
            items(results.albums, key = { it.id }) { album ->
                ListEntityRow(album.title, album.artist, { onAlbumClick(album) })
            }
        }
        if (results.artists.isNotEmpty()) {
            item(key = "artists_header") { SectionHeader("ARTISTS") }
            items(results.artists, key = { it.id }) { artist ->
                ListEntityRow(artist.name, artist.trackCount.toTrackCountLabel(), { onArtistClick(artist) })
            }
        }
        if (results.tracks.isEmpty() && results.albums.isEmpty() && results.artists.isEmpty()) {
            item(key = "empty_search") { SectionHeader("NO RESULTS") }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TrackRow(track: Track, onTrackClick: (Track) -> Unit, onTrackLongClick: (Track) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onTrackClick(track) },
                onLongClick = { onTrackLongClick(track) },
            )
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArtwork(track.artworkPath, size = 48.dp)
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(track.title, style = SylphyType.Code, color = FgPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, style = SylphyType.BodySmall, color = FgMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(track.durationMs.toMmSs(), style = SylphyType.CodeSmall, color = FgGhost)
    }
    SylphyDivider()
}

@Composable
internal fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Layout.sectionHeaderHeight)
            .background(BgBase),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(title, style = SylphyType.CodeSmall, color = FgMuted)
    }
}

@Composable
private fun ListEntityRow(title: String, subtitle: String, onClick: () -> Unit, leading: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Layout.trackRowHeight)
            .clickable { onClick() }
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(Layout.albumArtSizeSm)
                .clip(ContainerCorner)
                .border(Layout.borderThin, BorderDefault, ContainerCorner)
                .background(if (leading == null) BgElevated else BgSunken),
            contentAlignment = Alignment.Center,
        ) {
            if (leading != null) Icon(leading, contentDescription = null, tint = FgPrimary)
            else Text(title.take(1).uppercase(), style = SylphyType.Display, color = FgSubtle)
        }
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(title, style = SylphyType.Code, color = FgPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = SylphyType.BodySmall, color = FgMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    SylphyDivider()
}

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
        title = { Text("Add to playlist", style = SylphyType.Heading, color = FgPrimary) },
        text = {
            Column {
                Text("Create playlist", style = SylphyType.Body, color = FgPrimary, modifier = Modifier.fillMaxWidth().clickable(onClick = onCreate).padding(Spacing.md))
                playlists.forEach { playlist ->
                    Text(playlist.name, style = SylphyType.Body, color = FgPrimary, modifier = Modifier.fillMaxWidth().clickable { onPick(playlist) }.padding(Spacing.md))
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = FgPrimary) } },
    )
}

@Composable
private fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgElevated,
        title = { Text("Create playlist", style = SylphyType.Heading, color = FgPrimary) },
        text = { TextField(value = name, onValueChange = { name = it }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onCreate(name) }) { Text("Create", color = FgPrimary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = FgPrimary) } },
    )
}
