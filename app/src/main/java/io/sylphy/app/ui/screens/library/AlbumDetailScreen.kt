package io.sylphy.app.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sylphy.app.core.util.toHhMm
import io.sylphy.app.core.util.toTrackCountLabel
import io.sylphy.app.data.model.Album
import io.sylphy.app.data.model.Track
import io.sylphy.app.ui.components.player.AlbumArtwork
import io.sylphy.app.ui.components.shared.ButtonVariant
import io.sylphy.app.ui.components.shared.SylphyButton
import io.sylphy.app.ui.navigation.Screen
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun AlbumDetailScreen(
    albumId: String,
    navController: NavController,
    viewModel: LibraryDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScaffold(navController) {
        uiState.album?.let { album ->
            AlbumHeader(album)
            DetailActions(onPlayAll = { viewModel.playAll() }, onShuffle = { viewModel.playAll(shuffle = true) })
            NumberedTrackList(uiState.tracks, viewModel::playTrack)
        }
    }
}

@Composable
fun ArtistDetailScreen(
    artistId: String,
    navController: NavController,
    viewModel: LibraryDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScaffold(navController) {
        uiState.artist?.let { artist ->
            Text(artist.name, style = SylphyType.Display, color = FgPrimary)
            Text("${artist.albumCount} albums · ${artist.trackCount.toTrackCountLabel()}", style = SylphyType.BodySmall, color = FgMuted)
            Spacer(Modifier.height(Spacing.lg))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                items(uiState.albums, key = { it.id }) { album ->
                    ArtistAlbumCard(album) { navController.navigate(Screen.AlbumDetail.route(album.id)) }
                }
            }
            Spacer(Modifier.height(Spacing.md))
            SectionHeader("ALL TRACKS")
            NumberedTrackList(uiState.tracks, viewModel::playTrack)
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    navController: NavController,
    viewModel: LibraryDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScaffold(navController) {
        uiState.playlist?.let { playlist ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(playlist.name, style = SylphyType.Display, color = FgPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${playlist.trackCount.toTrackCountLabel()} · ${playlist.durationMs.toHhMm()}", style = SylphyType.BodySmall, color = FgMuted)
                }
                IconButton(onClick = { viewModel.deletePlaylistOrConfirm { navController.popBackStack() } }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FgPrimary)
                }
            }
            Text(
                text = if (uiState.confirmDeletePlaylist) "Tap delete again to confirm" else " ",
                style = SylphyType.Caption,
                color = FgMuted,
            )
            Spacer(Modifier.height(Spacing.md))
            DetailActions(onPlayAll = { viewModel.playAll() }, onShuffle = { viewModel.playAll(shuffle = true) })
            LazyColumn {
                items(uiState.tracks, key = { it.id }) { track ->
                    TrackRow(track, onTrackClick = viewModel::playTrack, onTrackLongClick = viewModel::removeFromPlaylist)
                }
            }
        }
    }
}

@Composable
private fun DetailScaffold(navController: NavController, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(Spacing.md),
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FgPrimary)
        }
        Spacer(Modifier.height(Spacing.sm))
        content()
    }
}

@Composable
private fun AlbumHeader(album: Album) {
    AlbumArtwork(
        artworkPath = album.artworkPath,
        size = 260.dp,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(Spacing.md))
    Text(album.title, style = SylphyType.Display, color = FgPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
    val year = album.year?.toString()
    Text(
        listOfNotNull(album.artist, year, album.trackCount.toTrackCountLabel(), album.durationMs.toHhMm()).joinToString(" · "),
        style = SylphyType.BodySmall,
        color = FgMuted,
    )
}

@Composable
private fun DetailActions(onPlayAll: () -> Unit, onShuffle: () -> Unit) {
    Row(
        modifier = Modifier.padding(vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SylphyButton(text = "Play all", variant = ButtonVariant.Solid, onClick = onPlayAll, modifier = Modifier.weight(1f))
        SylphyButton(text = "Shuffle", variant = ButtonVariant.Outline, onClick = onShuffle, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NumberedTrackList(tracks: List<Track>, onTrackClick: (Track) -> Unit) {
    LazyColumn {
        items(tracks, key = { it.id }) { track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Layout.trackRowHeight)
                    .clickable { onTrackClick(track) }
                    .padding(vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text((track.trackNumber ?: tracks.indexOf(track) + 1).toString().padStart(2, '0'), style = SylphyType.CodeSmall, color = FgMuted)
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(track.title, style = SylphyType.Code, color = FgPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(track.durationMs.toHhMm(), style = SylphyType.BodySmall, color = FgMuted)
                }
            }
        }
    }
}

@Composable
private fun ArtistAlbumCard(album: Album, onClick: () -> Unit) {
    Column(Modifier.width(160.dp).clickable(onClick = onClick)) {
        AlbumArtwork(album.artworkPath, size = 160.dp)
        Spacer(Modifier.height(Spacing.sm))
        Text(album.title, style = SylphyType.CodeSmall, color = FgPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
