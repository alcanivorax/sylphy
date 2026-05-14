package io.sylphy.app.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun AlbumDetailScreen(albumId: String, navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize().background(BgBase),
        contentAlignment = Alignment.Center,
    ) {
        Text("Album · $albumId", style = SylphyType.Heading, color = FgMuted)
    }
}

@Composable
fun ArtistDetailScreen(artistId: String, navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize().background(BgBase),
        contentAlignment = Alignment.Center,
    ) {
        Text("Artist · $artistId", style = SylphyType.Heading, color = FgMuted)
    }
}

@Composable
fun PlaylistDetailScreen(playlistId: String, navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize().background(BgBase),
        contentAlignment = Alignment.Center,
    ) {
        Text("Playlist · $playlistId", style = SylphyType.Heading, color = FgMuted)
    }
}
