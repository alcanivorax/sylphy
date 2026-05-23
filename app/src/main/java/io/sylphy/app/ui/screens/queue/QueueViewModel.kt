package io.sylphy.app.ui.screens.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sylphy.app.data.model.Track
import io.sylphy.app.domain.repository.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class QueueUiState(
    val tracks: List<Track> = emptyList(),
    val activeIndex: Int = -1,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
) {
    /** The currently playing track, or null if nothing is loaded. */
    val nowPlaying: Track? get() = tracks.getOrNull(activeIndex)

    /** All tracks after the active index. */
    val upNext: List<Track> get() = if (activeIndex >= 0) tracks.drop(activeIndex + 1) else tracks

    /** Number of tracks still to be played (excluding the active one). */
    val remainingCount: Int get() = upNext.size

    /** Sum of durations of all up-next tracks in milliseconds. */
    val upNextDurationMs: Long get() = upNext.sumOf { it.durationMs }
}

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val player: Player,
    private val trackRepository: TrackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onTimelineChanged(
            timeline: androidx.media3.common.Timeline,
            reason: Int,
        ) { refreshQueue() }

        override fun onMediaItemTransition(
            mediaItem: androidx.media3.common.MediaItem?,
            reason: Int,
        ) { refreshQueue() }
    }

    init {
        player.addListener(listener)
        refreshQueue()
        viewModelScope.launch {
            while (true) {
                _uiState.update {
                    it.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.takeIf { duration -> duration > 0L } ?: 0L,
                        shuffleEnabled = player.shuffleModeEnabled,
                    )
                }
                kotlinx.coroutines.delay(500L)
            }
        }
    }

    fun playAt(index: Int) {
        if (index in 0 until player.mediaItemCount) {
            player.seekToDefaultPosition(index)
            player.play()
            refreshQueue()
        }
    }

    fun removeAt(index: Int) {
        if (index in 0 until player.mediaItemCount &&
            index != player.currentMediaItemIndex
        ) {
            player.removeMediaItem(index)
            refreshQueue()
        }
    }

    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex !in 0 until player.mediaItemCount) return
        if (toIndex !in 0 until player.mediaItemCount) return
        if (fromIndex == player.currentMediaItemIndex) return
        player.moveMediaItem(fromIndex, toIndex)
        refreshQueue()
    }

    /**
     * Removes every track after the currently playing item.
     * The active track is preserved. No-op if there is no active track.
     */
    fun clearUpNext() {
        val active = player.currentMediaItemIndex
        if (active < 0) return
        for (i in player.mediaItemCount - 1 downTo active + 1) {
            player.removeMediaItem(i)
        }
        refreshQueue()
    }

    fun toggleShuffle() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
        _uiState.update { it.copy(shuffleEnabled = player.shuffleModeEnabled) }
    }

    private fun refreshQueue() {
        val ids = (0 until player.mediaItemCount)
            .mapNotNull { i ->
                player.getMediaItemAt(i).mediaId.takeIf { it.isNotBlank() }
            }
        val activeIndex = player.currentMediaItemIndex

        viewModelScope.launch {
            runCatching {
                ids.mapNotNull { id -> trackRepository.getTrackById(id) }
            }.onSuccess { tracks ->
                _uiState.update { it.copy(tracks = tracks, activeIndex = activeIndex) }
            }.onFailure {
                Timber.e(it, "Failed to refresh playback queue")
            }
        }
    }

    override fun onCleared() {
        player.removeListener(listener)
        super.onCleared()
    }
}
