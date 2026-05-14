package io.sylphy.app.ui.screens.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.sylphy.app.ui.components.player.AlbumArtwork
import io.sylphy.app.ui.components.player.ProgressRing
import io.sylphy.app.ui.components.player.SylphySeekBar
import io.sylphy.app.ui.components.player.TickerTape
import io.sylphy.app.ui.components.player.TrackInfoSection
import io.sylphy.app.ui.components.player.TransportControls
import io.sylphy.app.ui.theme.BgBase
import io.sylphy.app.ui.theme.BorderDefault
import io.sylphy.app.ui.theme.ChipCorner
import io.sylphy.app.ui.theme.Duration
import io.sylphy.app.ui.theme.FgMuted
import io.sylphy.app.ui.theme.FgPrimary
import io.sylphy.app.ui.theme.Layout
import io.sylphy.app.ui.theme.Spacing
import io.sylphy.app.ui.theme.SylphyEasing
import io.sylphy.app.ui.theme.SylphyType

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val track = uiState.activeTrack

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase),
    ) {
        if (track == null) {
            Text(
                text = "\u2014",
                style = SylphyType.DisplayLarge,
                color = FgMuted,
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
                .padding(top = Spacing.lg, bottom = Spacing.md),
        ) {
            val artSize = if (maxWidth < Layout.albumArtSize + Spacing.xxxl) {
                maxWidth - Spacing.xxl
            } else {
                Layout.albumArtSize
            }
            val progress by animateFloatAsState(
                targetValue = if (uiState.duration > 0L) {
                    (uiState.position.toFloat() / uiState.duration).coerceIn(0f, 1f)
                } else {
                    0f
                },
                animationSpec = tween(Duration.Normal, easing = SylphyEasing.Standard),
                label = "player_progress",
            )

            Column(modifier = Modifier.fillMaxSize()) {
                TrackInfoSection(track = track)

                Spacer(Modifier.height(Spacing.lg))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(artSize + Spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    ProgressRing(size = artSize + Spacing.md, progress = progress)
                    AlbumArtwork(artworkPath = track.artworkPath, size = artSize)
                }

                Spacer(Modifier.height(Spacing.lg))

                SylphySeekBar(
                    positionMs = uiState.position,
                    durationMs = uiState.duration,
                    waveformData = track.waveformData,
                    onSeek = viewModel::seekTo,
                )

                Spacer(Modifier.height(Spacing.lg))

                TransportControls(
                    isPlaying = uiState.isPlaying,
                    shuffleEnabled = uiState.shuffleEnabled,
                    repeatMode = uiState.repeatMode,
                    onPlayPause = viewModel::playPause,
                    onNext = viewModel::next,
                    onPrevious = viewModel::previous,
                    onToggleShuffle = viewModel::toggleShuffle,
                    onCycleRepeat = viewModel::cycleRepeat,
                )

                Spacer(Modifier.height(Spacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    SpeedChip(speed = uiState.speed, onClick = viewModel::cycleSpeed)
                }

                Spacer(Modifier.weight(1f))
                TickerTape(track = track)
            }
        }
    }
}

@Composable
private fun SpeedChip(
    speed: Float,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = "${speed}x",
        style = SylphyType.CodeSmall,
        color = FgPrimary,
        modifier = Modifier
            .border(Layout.borderThin, BorderDefault, ChipCorner)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    )
}
