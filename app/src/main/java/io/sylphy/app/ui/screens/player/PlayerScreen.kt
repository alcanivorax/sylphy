package io.sylphy.app.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.ui.components.player.AmbientBackgroundGlow
import io.sylphy.app.ui.components.player.CDDisc
import io.sylphy.app.ui.components.player.SylphySeekBar
import io.sylphy.app.ui.components.player.TickerTape
import io.sylphy.app.ui.components.player.TrackInfoSection
import io.sylphy.app.ui.components.player.TransportControls
import io.sylphy.app.ui.components.shared.EmptyState
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
import io.sylphy.app.ui.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    navController: NavController,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val track = uiState.activeTrack
    val scope = rememberCoroutineScope()
    var volumeVisible by remember { mutableStateOf(false) }
    var volumeJob by remember { mutableStateOf<Job?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase),
    ) {
        if (track == null) {
            EmptyState(
                title = "Nothing playing",
                description = "Open Library to choose a track.",
                action = "Open Library" to { navController.navigate(Screen.Library.route) },
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }

        // Ambient background glow from album art
        AmbientBackgroundGlow(
            artworkPath = track.artworkPath,
            intensity = 0.12f,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
                .padding(top = Spacing.lg, bottom = Spacing.md)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.changes.size == 2 && event.changes.all { it.pressed }) {
                                val dy = event.changes.map { it.position.y - it.previousPosition.y }.average().toFloat()
                                if (dy != 0f) {
                                    viewModel.adjustVolume((-dy / 400f).coerceIn(-0.08f, 0.08f))
                                    volumeVisible = true
                                    volumeJob?.cancel()
                                    volumeJob = scope.launch {
                                        delay(1500)
                                        volumeVisible = false
                                    }
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    }
                },
        ) {
            val widthBoundArtSize = if (maxWidth < Layout.albumArtSize + Spacing.xxxl) {
                maxWidth - Spacing.xxl
            } else {
                Layout.albumArtSize
            }
            val heightBoundArtSize = (maxHeight - 380.dp).coerceIn(168.dp, Layout.albumArtSize)
            val artSize = minOf(widthBoundArtSize, heightBoundArtSize)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(Spacing.lg))

                // Track info - above CD for better hierarchy
                AnimatedContent(
                    targetState = track,
                    transitionSpec = {
                        fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) togetherWith
                            fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit))
                    },
                    label = "track_info",
                ) { currentTrack ->
                    TrackInfoSection(track = currentTrack)
                }

                Spacer(Modifier.height(Spacing.xl))

                // CD Disc - the centerpiece
                CDDisc(
                    artworkPath = track.artworkPath,
                    isPlaying = uiState.isPlaying,
                    discSize = artSize,
                )

                Spacer(Modifier.height(Spacing.xl))

                // Progress ring integrated around CD (now part of CDDisc, but we keep seek bar below)
                SylphySeekBar(
                    positionMs = uiState.position,
                    durationMs = uiState.duration,
                    waveformData = track.waveformData,
                    onSeek = viewModel::seekTo,
                )

                Spacer(Modifier.height(Spacing.lg))

                // Transport controls - minimal
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

                // Bottom row: Sleep timer + Speed chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (uiState.sleepTimerRemainingMs > 0L) {
                        Text(
                            text = "■ ${uiState.sleepTimerRemainingMs.toMmSs()}",
                            style = SylphyType.CodeSmall,
                            color = FgMuted,
                            modifier = Modifier
                                .clickable { navController.navigate(Screen.SleepTimer.route) }
                                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                        )
                    }
                    SpeedChip(speed = uiState.speed, onClick = viewModel::cycleSpeed)
                }

                Spacer(Modifier.height(Spacing.xl))
                
                // Ticker tape - secondary metadata, kept but minimal
                TickerTape(track = track)
                
                Spacer(Modifier.height(Spacing.xxl))
            }
        }
        
        // Volume indicator overlay
        AnimatedVisibility(
            visible = volumeVisible,
            enter = fadeIn(tween(Duration.Fast)),
            exit = fadeOut(tween(Duration.Normal)),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = Spacing.xxl),
        ) {
            VolumeIndicator(uiState.volume)
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
