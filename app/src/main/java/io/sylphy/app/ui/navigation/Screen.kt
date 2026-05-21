package io.sylphy.app.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Player  : Screen("player")
    object Queue   : Screen("queue")
    object Library : Screen("library")
    object Eq      : Screen("eq")
    object SleepTimer : Screen("sleep_timer")
    object Stats   : Screen("stats")
    object Ambient : Screen("ambient")
    object Settings : Screen("settings")

    object AlbumDetail : Screen("album/{albumId}") {
        const val ROUTE = "album/{albumId}"
        fun route(albumId: String) = "album/${Uri.encode(albumId)}"
    }

    object ArtistDetail : Screen("artist/{artistId}") {
        const val ROUTE = "artist/{artistId}"
        fun route(artistId: String) = "artist/${Uri.encode(artistId)}"
    }

    object PlaylistDetail : Screen("playlist/{playlistId}") {
        const val ROUTE = "playlist/{playlistId}"
        fun route(playlistId: String) = "playlist/${Uri.encode(playlistId)}"
    }
}
