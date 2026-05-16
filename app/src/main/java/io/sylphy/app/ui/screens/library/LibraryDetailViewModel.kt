package io.sylphy.app.ui.screens.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sylphy.app.core.extension.toAlbum
import io.sylphy.app.core.extension.toArtist
import io.sylphy.app.core.extension.toPlaylist
import io.sylphy.app.core.extension.toTrack
import io.sylphy.app.data.local.db.dao.AlbumDao
import io.sylphy.app.data.local.db.dao.ArtistDao
import io.sylphy.app.data.local.db.dao.PlaylistDao
import io.sylphy.app.data.model.Album
import io.sylphy.app.data.model.Artist
import io.sylphy.app.data.model.Playlist
import io.sylphy.app.data.model.Track
import io.sylphy.app.domain.usecase.PlayTrackUseCase
import io.sylphy.app.domain.repository.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class LibraryDetailUiState(
    val album: Album? = null,
    val artist: Artist? = null,
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val confirmDeletePlaylist: Boolean = false,
)

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val playlistDao: PlaylistDao,
    private val trackRepository: TrackRepository,
    private val playTrackUseCase: PlayTrackUseCase,
) : ViewModel() {
    private val albumId: String? = savedStateHandle["albumId"]
    private val artistId: String? = savedStateHandle["artistId"]
    private val playlistId: String? = savedStateHandle["playlistId"]

    private val _uiState = MutableStateFlow(LibraryDetailUiState())
    val uiState: StateFlow<LibraryDetailUiState> = _uiState.asStateFlow()

    init {
        albumId?.let { observeAlbum(it) }
        artistId?.let { observeArtist(it) }
        playlistId?.let { observePlaylist(it) }
    }

    private fun observeAlbum(id: String) {
        albumDao.observeAlbumById(id)
            .combine(trackRepository.getAllTracks()) { album, tracks ->
                val albumTracks = if (album == null) emptyList() else {
                    tracks.filter { it.album == album.title && (it.albumArtist ?: it.artist) == album.artist }
                        .sortedWith(compareBy<Track> { it.discNumber ?: 0 }.thenBy { it.trackNumber ?: 0 }.thenBy { it.title })
                }
                LibraryDetailUiState(album = album?.toAlbum(albumTracks), tracks = albumTracks)
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    private fun observeArtist(id: String) {
        artistDao.observeArtistById(id)
            .combine(trackRepository.getAllTracks()) { artist, tracks ->
                val artistTracks = if (artist == null) emptyList() else {
                    tracks.filter { (it.albumArtist ?: it.artist) == artist.name }
                        .sortedWith(compareBy<Track> { it.album }.thenBy { it.trackNumber ?: 0 }.thenBy { it.title })
                }
                LibraryDetailUiState(
                    artist = artist?.toArtist(),
                    tracks = artistTracks,
                )
            }
            .combine(albumDao.getAllAlbums()) { state, albums ->
                state.copy(albums = state.artist?.let { artist ->
                    albums.filter { it.artist == artist.name }.map { it.toAlbum() }
                } ?: emptyList())
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    private fun observePlaylist(id: String) {
        playlistDao.observePlaylistById(id)
            .combine(playlistDao.getTracksForPlaylist(id)) { playlist, tracks ->
                val mapped = tracks.map { it.toTrack() }
                LibraryDetailUiState(playlist = playlist?.toPlaylist(mapped), tracks = mapped)
            }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun playTrack(track: Track, queue: List<Track> = _uiState.value.tracks) {
        viewModelScope.launch {
            runCatching {
                playTrackUseCase(track, queue)
                trackRepository.incrementPlayCount(track.id, System.currentTimeMillis())
            }.onFailure { Timber.e(it, "Failed to play detail track") }
        }
    }

    fun playAll(shuffle: Boolean = false) {
        val queue = if (shuffle) _uiState.value.tracks.shuffled() else _uiState.value.tracks
        queue.firstOrNull()?.let { playTrack(it, queue) }
    }

    fun removeFromPlaylist(track: Track) {
        val id = playlistId ?: return
        viewModelScope.launch {
            playlistDao.removeTrackFromPlaylist(id, track.id)
            updatePlaylistStats(id)
        }
    }

    fun movePlaylistTrack(from: Int, to: Int) {
        val id = playlistId ?: return
        val tracks = _uiState.value.tracks.toMutableList()
        if (from !in tracks.indices || to !in tracks.indices) return
        val moved = tracks.removeAt(from)
        tracks.add(to, moved)
        viewModelScope.launch {
            tracks.forEachIndexed { index, track ->
                playlistDao.updateTrackPosition(id, track.id, index)
            }
            updatePlaylistStats(id)
        }
    }

    fun deletePlaylistOrConfirm(onDeleted: () -> Unit) {
        val id = playlistId ?: return
        if (!_uiState.value.confirmDeletePlaylist) {
            _uiState.update { it.copy(confirmDeletePlaylist = true) }
            return
        }
        viewModelScope.launch {
            playlistDao.deletePlaylist(id)
            onDeleted()
        }
    }

    private suspend fun updatePlaylistStats(id: String) {
        val tracks = _uiState.value.tracks
        playlistDao.updateStats(id, tracks.size, tracks.sumOf { it.durationMs }, System.currentTimeMillis())
    }
}
