package io.sylphy.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sylphy.app.data.local.scanner.LibraryOrganizer
import io.sylphy.app.data.local.scanner.ScanProgress
import io.sylphy.app.data.model.Track
import io.sylphy.app.domain.usecase.PlayTrackUseCase
import io.sylphy.app.domain.usecase.ScanLibraryUseCase
import io.sylphy.app.domain.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val scanStatus: ScanProgress = ScanProgress.Idle,
    val isLoading: Boolean = false,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val scanLibraryUseCase: ScanLibraryUseCase,
    private val playTrackUseCase: PlayTrackUseCase,
    private val libraryOrganizer: LibraryOrganizer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeTracks()
    }

    private fun observeTracks() {
        trackRepository.getAllTracks()
            .onEach { tracks -> _uiState.update { it.copy(tracks = tracks) } }
            .catch { e -> Timber.e(e, "Failed to load tracks") }
            .launchIn(viewModelScope)
    }

    fun scanLibrary() {
        viewModelScope.launch {
            scanLibraryUseCase()
                .onEach { progress ->
                    _uiState.update { it.copy(scanStatus = progress) }
                    if (progress is ScanProgress.Done) {
                        launch(Dispatchers.IO) {
                            runCatching { trackRepository.enrichMissingArtwork() }
                                .onFailure { Timber.e(it, "Artwork enrichment failed") }
                        }
                    }
                }
                .catch { e ->
                    Timber.e(e, "Scan failed")
                    _uiState.update { it.copy(scanStatus = ScanProgress.Error(e.message ?: "Unknown error")) }
                }
                .launchIn(this)
        }
    }

    fun playTrack(track: Track, queue: List<Track> = emptyList()) {
        viewModelScope.launch {
            runCatching {
                playTrackUseCase(track, queue)
                trackRepository.incrementPlayCount(track.id, System.currentTimeMillis())
            }.onFailure { Timber.e(it, "Failed to play track ${track.id}") }
        }
    }
}
