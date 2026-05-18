package io.sylphy.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sylphy.app.core.extension.toAlbum
import io.sylphy.app.core.extension.toArtist
import io.sylphy.app.core.extension.toPlaylist
import io.sylphy.app.data.local.db.dao.AlbumDao
import io.sylphy.app.data.local.db.dao.ArtistDao
import io.sylphy.app.data.local.db.dao.PlaylistDao
import io.sylphy.app.data.local.db.entity.PlaylistEntity
import io.sylphy.app.data.local.db.entity.PlaylistTrackEntity
import io.sylphy.app.data.local.scanner.ScanProgress
import io.sylphy.app.data.model.Album
import io.sylphy.app.data.model.Artist
import io.sylphy.app.data.model.Playlist
import io.sylphy.app.data.model.Track
import io.sylphy.app.domain.usecase.PlayTrackUseCase
import io.sylphy.app.domain.usecase.ScanLibraryUseCase
import io.sylphy.app.domain.repository.TrackRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

enum class LibraryTab { Songs, Albums, Artists, Playlists }

data class SearchResults(
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
)

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val recentlyPlayed: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.Songs,
    val searchQuery: String = "",
    val searchResults: SearchResults? = null,
    val scanStatus: ScanProgress = ScanProgress.Idle,
    val isLoading: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val scanLibraryUseCase: ScanLibraryUseCase,
    private val playTrackUseCase: PlayTrackUseCase,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val playlistDao: PlaylistDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()
    private val searchQuery = MutableStateFlow("")
    private var scanJob: Job? = null

    init {
        observeLibrary()
        observeSearch()
    }

    private fun observeLibrary() {
        combine(
            trackRepository.getAllTracks(),
            trackRepository.getRecentlyPlayed(12),
            albumDao.getAllAlbums(),
            artistDao.getAllArtists(),
            playlistDao.getAllPlaylists(),
        ) { tracks, recent, albums, artists, playlists ->
            LibraryUiState(
                tracks = tracks,
                recentlyPlayed = recent,
                albums = albums.map { it.toAlbum() },
                artists = artists.map { it.toArtist() },
                playlists = playlists.map { it.toPlaylist() },
                selectedTab = _uiState.value.selectedTab,
                searchQuery = _uiState.value.searchQuery,
                searchResults = _uiState.value.searchResults,
                scanStatus = _uiState.value.scanStatus,
            )
        }
            .onEach { state -> _uiState.value = state }
            .catch { e -> Timber.e(e, "Failed to load tracks") }
            .launchIn(viewModelScope)
    }

    private fun observeSearch() {
        searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .filter { it.length >= 2 || it.isEmpty() }
            .flatMapLatest { query ->
                if (query.length < 2) {
                    flowOf<SearchResults?>(null)
                } else {
                    combine(
                        trackRepository.searchTracks(query),
                        flow { emit(albumDao.searchAlbums(query).map { it.toAlbum() }) },
                        flow { emit(artistDao.searchArtists(query).map { it.toArtist() }) },
                    ) { tracks, albums, artists ->
                        SearchResults(tracks = tracks, albums = albums, artists = artists) as SearchResults?
                    }
                }
            }
            .onEach { results -> _uiState.update { it.copy(searchResults = results) } }
            .catch { e -> Timber.e(e, "Search failed") }
            .launchIn(viewModelScope)
    }

    fun scanLibrary() {
        if (scanJob?.isActive == true) return
        scanJob = viewModelScope.launch {
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
                .collect {}
        }
    }

    fun selectTab(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query, searchResults = if (query.length < 2) null else it.searchResults) }
    }

    fun playTrack(track: Track, queue: List<Track> = emptyList()) {
        viewModelScope.launch {
            runCatching {
                playTrackUseCase(track, queue)
                trackRepository.incrementPlayCount(track.id, System.currentTimeMillis())
            }.onFailure { Timber.e(it, "Failed to play track ${track.id}") }
        }
    }

    fun playTracks(tracks: List<Track>, shuffled: Boolean = false) {
        val queue = if (shuffled) tracks.shuffled() else tracks
        queue.firstOrNull()?.let { playTrack(it, queue) }
    }

    fun createPlaylist(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            playlistDao.insertPlaylist(
                PlaylistEntity(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
    }

    fun addToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val position = playlistDao.nextPosition(playlistId)
            playlistDao.insertPlaylistTracks(
                listOf(
                    PlaylistTrackEntity(
                        playlistId = playlistId,
                        trackId = trackId,
                        position = position,
                        addedAt = System.currentTimeMillis(),
                    ),
                ),
            )
            refreshPlaylistStats(playlistId)
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            trackRepository.setFavorite(track.id, !track.isFavorite)
        }
    }

    private suspend fun refreshPlaylistStats(playlistId: String) {
        val tracks = playlistDao.getTracksForPlaylist(playlistId).first()
        playlistDao.updateStats(
            id = playlistId,
            count = tracks.size,
            durationMs = tracks.sumOf { it.durationMs },
            updatedAt = System.currentTimeMillis(),
        )
    }
}
