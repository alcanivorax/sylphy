package io.sylphy.app.ui.navigation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
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

    AmbientAutoNavigator(navController, currentRoute)

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            awaitEachGesture {
                while (true) {
                    awaitPointerEvent(PointerEventPass.Final)
                    AmbientIdleClock.lastInteractionMs = System.currentTimeMillis()
                }
            }
        },
        containerColor = BgBase,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
        ),
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
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                        ),
                    ),
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
                fadeIn(tween(Duration.Slow, easing = SylphyEasing.Enter)) +
                        slideInHorizontally(tween(Duration.Slow, easing = SylphyEasing.Standard)) { it / 8 }
            },
            exitTransition = {
                fadeOut(tween(Duration.Normal, easing = SylphyEasing.Exit)) +
                        slideOutHorizontally(tween(Duration.Normal, easing = SylphyEasing.Standard)) { -it / 12 }
            },
            popEnterTransition = {
                fadeIn(tween(Duration.Slow, easing = SylphyEasing.Enter)) +
                        slideInHorizontally(tween(Duration.Slow, easing = SylphyEasing.Standard)) { -it / 8 }
            },
            popExitTransition = {
                fadeOut(tween(Duration.Normal, easing = SylphyEasing.Exit)) +
                        slideOutHorizontally(tween(Duration.Normal, easing = SylphyEasing.Standard)) { it / 12 }
            },
        ) {
            composable(Screen.Player.route)  { PlayerScreen(navController) }
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
            composable(Screen.Ambient.route)    { AmbientScreen(navController) }
        }
    }
}

private object AmbientIdleClock {
    var lastInteractionMs: Long = System.currentTimeMillis()
}

@Composable
private fun AmbientAutoNavigator(navController: NavHostController, currentRoute: String?) {
    val context = LocalContext.current
    var charging by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
        val sticky = context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        sticky?.let { receiver.onReceive(context, it) }
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(charging, currentRoute) {
        while (true) {
            val idle = System.currentTimeMillis() - AmbientIdleClock.lastInteractionMs
            if (charging && currentRoute != Screen.Ambient.route && idle >= 60_000L) {
                navController.navigate(Screen.Ambient.route) { launchSingleTop = true }
            }
            delay(1000)
        }
    }
}
