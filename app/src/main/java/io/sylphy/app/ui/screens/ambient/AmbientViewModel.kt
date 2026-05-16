package io.sylphy.app.ui.screens.ambient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sylphy.app.data.model.Track
import io.sylphy.app.domain.repository.TrackRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class AmbientUiState(
    val time: String = "00:00",
    val cursorVisible: Boolean = true,
    val activeTrack: Track? = null,
    val progress: Float? = null,
)

@HiltViewModel
class AmbientViewModel @Inject constructor(
    private val player: Player,
    private val trackRepository: TrackRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AmbientUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                val now = LocalTime.now()
                _uiState.update { it.copy(time = "%02d:%02d".format(now.hour, now.minute)) }
                delay(1000)
            }
        }
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(cursorVisible = !it.cursorVisible) }
                delay(1000)
            }
        }
        viewModelScope.launch {
            while (true) {
                syncPlayer()
                delay(500)
            }
        }
    }

    private suspend fun syncPlayer() {
        val id = player.currentMediaItem?.mediaId
        val track = id?.let { trackRepository.getTrackById(it) }
        val duration = player.duration.takeIf { it != C.TIME_UNSET && it > 0 }
        _uiState.update {
            it.copy(
                activeTrack = track,
                progress = duration?.let { d -> (player.currentPosition / d.toFloat()).coerceIn(0f, 1f) },
            )
        }
    }
}
