package io.sylphy.app.ui.screens.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.sylphy.app.data.model.ThemeMode
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType
import io.sylphy.app.ui.theme.playerChromeColors

@Composable
fun PlayerScreen(
    navController: NavController,
    viewModel: PlayerViewModel = hiltViewModel(),
    themeMode: ThemeMode = ThemeMode.MONOCHROME_DARK,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val track = uiState.activeTrack
    val colors = playerChromeColors(themeMode)
    HidePlatformStatusBar()

    // Edge-to-edge: transparent status + nav bars
    val systemUiController = rememberSystemUiController()
    val isLight = colors.bg.luminance() > 0.5f
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = isLight)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        if (track == null) {
            EmptyPlayerState()
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(46.dp))

            TopNav(
                colors = colors,
                onBack = { navController.popBackStack() },
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth(),
                ) {
                    val compactHeight = maxHeight < 600.dp
                    val discGap = if (compactHeight) 10.dp else 18.dp
                    val infoGap = if (compactHeight) 16.dp else 24.dp
                    val badgeGap = 8.dp
                    val scrubberGap = if (compactHeight) 16.dp else 20.dp
                    val controlsGap = if (compactHeight) 16.dp else 20.dp

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(discGap))

                        FadeUp(delayMillis = 50) {
                            VinylArtwork(
                                artworkUri = track.artworkPath,
                                isPlaying = uiState.isPlaying,
                                colors = colors,
                                themeMode = themeMode,
                                seed = track.title,
                            )
                        }

                        Spacer(Modifier.height(infoGap))

                        FadeUp(delayMillis = 120) {
                            TrackInfoRow(
                                track = track,
                                isFavourite = track.isFavorite,
                                colors = colors,
                                onFavouriteToggle = viewModel::toggleFavorite,
                            )
                        }

                        Spacer(Modifier.height(badgeGap))

                        FadeUp(delayMillis = 150) {
                            QualityBadgeRow(track = track, colors = colors)
                        }

                        Spacer(Modifier.height(scrubberGap))

                        FadeUp(delayMillis = 200) {
                            ScrubberSection(
                                elapsedMs = uiState.position,
                                durationMs = uiState.duration,
                                colors = colors,
                                onSeek = viewModel::seekTo,
                            )
                        }

                        Spacer(Modifier.height(controlsGap))

                        FadeUp(delayMillis = 250) {
                            ControlsRow(
                                isPlaying = uiState.isPlaying,
                                shuffleEnabled = uiState.shuffleEnabled,
                                repeatMode = uiState.repeatMode,
                                colors = colors,
                                onShuffle = viewModel::toggleShuffle,
                                onPlayPause = viewModel::playPause,
                                onNext = viewModel::next,
                                onPrevious = viewModel::previous,
                                onRepeat = viewModel::cycleRepeat,
                            )
                        }

                        FadeUp(delayMillis = 300) {
                            SecondaryRow(
                                speed = uiState.speed,
                                volume = uiState.volume,
                                colors = colors,
                                onCycleSpeed = viewModel::cycleSpeed,
                                onVolumeChange = { ratio ->
                                    val delta = ratio - uiState.volume
                                    viewModel.adjustVolume(delta)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FadeUp(
    delayMillis: Int,
    content: @Composable () -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    val y = remember { Animatable(14f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(durationMillis = 500, delayMillis = delayMillis))
    }
    LaunchedEffect(Unit) {
        y.animateTo(0f, tween(durationMillis = 500, delayMillis = delayMillis))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha.value
                translationY = y.value
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun EmptyPlayerState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        GeneratedArtwork(
            seed = "Sylphy",
            colors = playerChromeColors(ThemeMode.MONOCHROME_DARK),
            themeMode = ThemeMode.MONOCHROME_DARK,
            modifier = Modifier
                .size(Layout.albumArtSize)
                .graphicsLayer { alpha = 0.15f },
        )

        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = "No track selected",
            style = SylphyType.Heading,
            color = FgMuted,
        )

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = "Queue a track to begin",
            style = SylphyType.Body,
            color = FgMuted.copy(alpha = 0.6f),
        )
    }
}
