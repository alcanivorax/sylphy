package io.sylphy.app.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.sylphy.app.core.util.toMmSs
import io.sylphy.app.ui.components.player.AlbumArtwork
import io.sylphy.app.ui.components.player.ProgressRing
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavController,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val track = uiState.activeTrack
    val scope = rememberCoroutineScope()
    var settingsOpen by remember { mutableStateOf(false) }
    var volumeVisible by remember { mutableStateOf(false) }
    var volumeJob by remember { mutableStateOf<Job?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.size == 2 && event.changes.all { it.pressed }) {
                            val dy = event.changes.map { it.position.y - it.previousPosition.y }.average().toFloat()
                            viewModel.adjustVolume((-dy / 400f).coerceIn(-0.08f, 0.08f))
                            volumeVisible = true
                            volumeJob?.cancel()
                            volumeJob = scope.launch {
                                delay(1500)
                                volumeVisible = false
                            }
                        }
                    }
                }
            },
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

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
                .padding(top = Spacing.lg, bottom = Spacing.md),
        ) {
            val widthBoundArtSize = if (maxWidth < Layout.albumArtSize + Spacing.xxxl) {
                maxWidth - Spacing.xxl
            } else {
                Layout.albumArtSize
            }
            val heightBoundArtSize = (maxHeight - 380.dp).coerceIn(168.dp, Layout.albumArtSize)
            val artSize = minOf(widthBoundArtSize, heightBoundArtSize)
            val progress by animateFloatAsState(
                targetValue = if (uiState.duration > 0L) {
                    (uiState.position.toFloat() / uiState.duration).coerceIn(0f, 1f)
                } else {
                    0f
                },
                animationSpec = tween(Duration.Normal, easing = SylphyEasing.Standard),
                label = "player_progress",
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { settingsOpen = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Audio settings", tint = FgPrimary)
                    }
                }
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

                Spacer(Modifier.weight(1f))
                TickerTape(track = track)
            }
        }
        AnimatedVisibility(
            visible = volumeVisible,
            enter = fadeIn(tween(Duration.Fast)),
            exit = fadeOut(tween(Duration.Normal)),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = Spacing.xxl),
        ) {
            VolumeIndicator(uiState.volume)
        }
        if (settingsOpen) {
            AudioSettingsSheet(
                crossfadeMs = uiState.crossfadeDurationMs,
                onDismiss = { settingsOpen = false },
                onCrossfade = viewModel::setCrossfadeDuration,
                onEq = {
                    settingsOpen = false
                    navController.navigate(Screen.Eq.route)
                },
                onSleep = {
                    settingsOpen = false
                    navController.navigate(Screen.SleepTimer.route)
                },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioSettingsSheet(
    crossfadeMs: Int,
    onDismiss: () -> Unit,
    onCrossfade: (Int) -> Unit,
    onEq: () -> Unit,
    onSleep: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = BgBase) {
        Column(Modifier.padding(Spacing.lg)) {
            Text("Audio settings", style = SylphyType.Heading, color = FgPrimary)
            Spacer(Modifier.height(Spacing.md))
            SettingsRow("Crossfade", "${crossfadeMs / 1000}s") {
                val next = when (crossfadeMs) {
                    0 -> 3000
                    3000 -> 6000
                    6000 -> 12000
                    else -> 0
                }
                onCrossfade(next)
            }
            SettingsRow("EQ", ">", onEq)
            SettingsRow("Sleep timer", ">", onSleep)
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = SylphyType.Body, color = FgPrimary, modifier = Modifier.weight(1f))
        Text(value, style = SylphyType.Code, color = FgMuted)
    }
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
