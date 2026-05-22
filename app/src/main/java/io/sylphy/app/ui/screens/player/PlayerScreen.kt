package io.sylphy.app.ui.screens.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.sylphy.app.data.model.ThemeMode
import io.sylphy.app.ui.components.player.CDDisc
import io.sylphy.app.ui.navigation.Screen
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ChipCorner
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.PlayerTheme
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun PlayerScreen(
    navController: NavController,
    viewModel: PlayerViewModel = hiltViewModel(),
    themeMode: ThemeMode = ThemeMode.MONOCHROME_DARK,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val track = uiState.activeTrack

    // Edge-to-edge: transparent status + nav bars
    val systemUiController = rememberSystemUiController()
    val isLight = PlayerTheme.White.luminance() > 0.5f // check if current theme is light
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = isLight)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerTheme.Black)
    ) {
        if (track == null) {
            EmptyPlayerState(
                onOpenLibrary = { navController.navigate(Screen.Library.route) },
            )
            return@Box
        }

        // Layer 0: blurred art background
        BlurredArtBackground(artworkUri = track.artworkPath)

        // Layer 1: grain overlay
        GrainOverlay()

        // Layer 2: UI
        Column(modifier = Modifier.fillMaxSize()) {

            TopNav(onBack = { navController.popBackStack() })

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 420.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    VinylArtwork(
                        artworkUri = track.artworkPath,
                        isPlaying = uiState.isPlaying,
                    )

                    Spacer(Modifier.height(36.dp))

                    TrackInfoRow(
                        track = track,
                        isFavourite = track.isFavorite,
                        onFavouriteToggle = viewModel::toggleFavorite,
                    )

                    Spacer(Modifier.height(14.dp))

                    QualityBadgeRow(track = track)

                    Spacer(Modifier.height(32.dp))

                    ScrubberSection(
                        elapsedMs = uiState.position,
                        durationMs = uiState.duration,
                        onSeek = viewModel::seekTo,
                    )

                    Spacer(Modifier.height(36.dp))

                    ControlsRow(
                        isPlaying = uiState.isPlaying,
                        isShuffle = uiState.shuffleEnabled,
                        repeatMode = uiState.repeatMode,
                        onPlayPause = viewModel::playPause,
                        onNext = viewModel::next,
                        onPrevious = viewModel::previous,
                        onShuffle = viewModel::toggleShuffle,
                        onRepeat = viewModel::cycleRepeat,
                    )

                    Spacer(Modifier.height(28.dp))

                    SecondaryRow(
                        speed = uiState.speed,
                        volume = uiState.volume,
                        onSpeedCycle = viewModel::cycleSpeed,
                        onVolumeChange = viewModel::adjustVolume,
                    )
                }
            }

            BottomNav(
                activeTab = BottomNavTab.PLAYER,
                onLibrary = { navController.navigate(Screen.Library.route) },
                onQueue = { navController.navigate(Screen.Queue.route) },
                onPlayer = {},
            )
        }
    }
}

@Composable
private fun SpeedChip(
    speed: Float,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (interactionSource.collectIsPressedAsState().value) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 800f),
        label = "speed_chip_scale",
    )
    
    Text(
        text = "${speed}x",
        style = SylphyType.CodeSmall,
        color = FgPrimary,
        modifier = Modifier
            .scale(scale)
            .border(Layout.borderThin, BorderDefault, ChipCorner)
            .clip(RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    )
}

@Composable
private fun VolumeIndicator(volume: Float) {
    Column(
        modifier = Modifier
            .background(BgBase)
            .border(Layout.borderThin, BorderDefault, ChipCorner)
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("VOL ${(volume * 100).toInt()}", style = SylphyType.Code, color = FgPrimary)
        Spacer(Modifier.height(Spacing.sm))
        Box(Modifier.size(width = 160.dp, height = Spacing.px1).background(FgMuted)) {
            Box(Modifier.fillMaxWidth(volume.coerceIn(0f, 1f)).height(Spacing.px1).background(FgPrimary))
        }
    }
}

@Composable
private fun EmptyPlayerState(
    onOpenLibrary: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(Layout.albumArtSize)
                .graphicsLayer {
                    alpha = 0.15f
                },
            contentAlignment = Alignment.Center,
        ) {
            CDDisc(
                artworkPath = null,
                isPlaying = false,
                discSize = Layout.albumArtSize,
            )
        }

        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = "No track selected",
            style = SylphyType.Heading,
            color = FgMuted,
        )

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = "Select music from Library",
            style = SylphyType.Body,
            color = FgMuted.copy(alpha = 0.6f),
            modifier = Modifier
                .clickable(onClick = onOpenLibrary)
                .padding(Spacing.xs),
        )
    }
}
