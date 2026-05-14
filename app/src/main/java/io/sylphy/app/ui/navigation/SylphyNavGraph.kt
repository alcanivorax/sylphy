package io.sylphy.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.sylphy.app.ui.components.shared.SylphyTabBar
import io.sylphy.app.ui.screens.ambient.AmbientScreen
import io.sylphy.app.ui.screens.library.AlbumDetailScreen
import io.sylphy.app.ui.screens.library.ArtistDetailScreen
import io.sylphy.app.ui.screens.library.LibraryScreen
import io.sylphy.app.ui.screens.library.PlaylistDetailScreen
// All three detail composables are defined in AlbumDetailScreen.kt
import io.sylphy.app.ui.screens.player.PlayerScreen
import io.sylphy.app.ui.screens.queue.QueueScreen
import io.sylphy.app.ui.screens.settings.EqScreen
import io.sylphy.app.ui.screens.settings.SleepTimerScreen
import io.sylphy.app.ui.screens.stats.StatsScreen
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.SylphyEasing

private val topLevelRoutes = listOf(
    Screen.Player.route,
    Screen.Queue.route,
    Screen.Library.route,
)

private val tabs = listOf("Player", "Queue", "Library")

@Composable
fun SylphyNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedIndex = topLevelRoutes.indexOf(currentRoute).coerceAtLeast(0)
    val showTabBar = currentRoute in topLevelRoutes

    Scaffold(
        containerColor = BgBase,
        topBar = {
            if (showTabBar) {
                SylphyTabBar(
                    tabs = tabs,
                    selectedIndex = selectedIndex,
                    onTabSelected = { index ->
                        navController.navigate(topLevelRoutes[index]) {
                            popUpTo(Screen.Player.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Player.route,
            modifier = Modifier
                .padding(padding)
                .background(BgBase),
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
        ) {
            composable(Screen.Player.route)  { PlayerScreen() }
            composable(Screen.Queue.route)   { QueueScreen() }
            composable(Screen.Library.route) { LibraryScreen(navController) }

            composable(
                route = Screen.AlbumDetail.ROUTE,
                arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
            ) { AlbumDetailScreen(it.arguments!!.getString("albumId")!!, navController) }

            composable(
                route = Screen.ArtistDetail.ROUTE,
                arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
            ) { ArtistDetailScreen(it.arguments!!.getString("artistId")!!, navController) }

            composable(
                route = Screen.PlaylistDetail.ROUTE,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
            ) { PlaylistDetailScreen(it.arguments!!.getString("playlistId")!!, navController) }

            composable(Screen.Eq.route)         { EqScreen() }
            composable(Screen.SleepTimer.route) { SleepTimerScreen() }
            composable(Screen.Stats.route)      { StatsScreen() }
            composable(Screen.Ambient.route)    { AmbientScreen() }
        }
    }
}
