package io.sylphy.app.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sylphy.app.audio.SleepTimerController
import io.sylphy.app.data.local.datastore.SettingsDataStore
import io.sylphy.app.data.model.PlaybackState
import io.sylphy.app.data.model.PlayerUiState
import io.sylphy.app.data.model.RepeatMode
import io.sylphy.app.domain.repository.TrackRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val player: Player,
    private val trackRepository: TrackRepository,
    private val settingsDataStore: SettingsDataStore,
    private val sleepTimerController: SleepTimerController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            syncPlaybackState()
        }

        override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
            loadActiveTrack()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _uiState.update { it.copy(shuffleEnabled = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _uiState.update { it.copy(repeatMode = repeatMode.toRepeatMode()) }
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            _uiState.update { it.copy(speed = playbackParameters.speed) }
        }
    }

    init {
        player.addListener(listener)
        syncPlaybackState()
        loadActiveTrack()
        startProgressPolling()
        observeSettings()
        observeSleepTimer()
    }

    fun playPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    fun previous() {
        if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem() else player.seekTo(0L)
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0L))
        syncPlaybackState()
    }

    fun toggleShuffle() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun cycleRepeat() {
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun cycleSpeed() {
        val next = when {
            _uiState.value.speed < 1.0f -> 1.0f
            _uiState.value.speed < 1.25f -> 1.25f
            _uiState.value.speed < 1.5f -> 1.5f
            else -> 0.75f
        }
        player.setPlaybackSpeed(next)
        _uiState.update { it.copy(speed = next) }
        viewModelScope.launch { settingsDataStore.setPlaybackSpeed(next) }
    }

    fun adjustVolume(delta: Float) {
        val next = (player.volume + delta).coerceIn(0f, 1f)
        player.volume = next
        _uiState.update { it.copy(volume = next) }
    }

    fun setCrossfadeDuration(ms: Int) {
        viewModelScope.launch { settingsDataStore.setCrossfadeDuration(ms) }
    }

    private fun startProgressPolling() {
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    syncPlaybackState()
                }
                delay(100)
            }
        }
    }

    private fun syncPlaybackState() {
        _uiState.update {
            it.copy(
                isPlaying = player.isPlaying,
                playbackState = player.playbackState.toPlaybackState(),
                position = player.currentPosition.coerceAtLeast(0L),
                duration = player.duration.takeIf { duration -> duration != C.TIME_UNSET } ?: 0L,
                buffered = player.bufferedPosition.coerceAtLeast(0L),
                speed = player.playbackParameters.speed,
                shuffleEnabled = player.shuffleModeEnabled,
                repeatMode = player.repeatMode.toRepeatMode(),
                volume = player.volume,
            )
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsDataStore.settings.collect { settings ->
                if (player.playbackParameters.speed != settings.playbackSpeed) {
                    player.setPlaybackSpeed(settings.playbackSpeed)
                }
                _uiState.update {
                    it.copy(
                        speed = settings.playbackSpeed,
                        crossfadeDurationMs = settings.crossfadeDurationMs,
                    )
                }
            }
        }
    }

    private fun observeSleepTimer() {
        viewModelScope.launch {
            sleepTimerController.state.collect { timer ->
                _uiState.update { it.copy(sleepTimerRemainingMs = timer.remainingMs) }
            }
        }
    }

    private fun loadActiveTrack() {
        val id = player.currentMediaItem?.mediaId
        if (id.isNullOrBlank()) {
            _uiState.update { it.copy(activeTrack = null) }
            return
        }
        viewModelScope.launch {
            runCatching { trackRepository.getTrackById(id) }
                .onSuccess { track -> _uiState.update { it.copy(activeTrack = track) } }
                .onFailure { Timber.e(it, "Failed to load active track $id") }
        }
    }

    override fun onCleared() {
        player.removeListener(listener)
        super.onCleared()
    }
}

private fun Int.toPlaybackState(): PlaybackState = when (this) {
    Player.STATE_BUFFERING -> PlaybackState.BUFFERING
    Player.STATE_READY -> PlaybackState.READY
    Player.STATE_ENDED -> PlaybackState.ENDED
    else -> PlaybackState.IDLE
}

private fun Int.toRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
    else -> RepeatMode.OFF
}
