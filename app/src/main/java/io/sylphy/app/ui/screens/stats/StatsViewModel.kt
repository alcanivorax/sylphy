package io.sylphy.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sylphy.app.core.extension.toTrack
import io.sylphy.app.data.local.db.dao.DayListening
import io.sylphy.app.data.local.db.dao.SessionDao
import io.sylphy.app.data.local.db.dao.TrackWithStats
import io.sylphy.app.data.local.db.entity.TrackEntity
import io.sylphy.app.data.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopTrackItem(val track: Track, val sessionCount: Int, val listenedMs: Long)

data class StatsUiState(
    val heatmapData: List<DayListening> = emptyList(),
    val topTracks: List<TopTrackItem> = emptyList(),
    val weeklyMinutes: Long = 0L,
    val monthlyTracks: Int = 0,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val sessionDao: SessionDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadStats() }
    }

    private suspend fun loadStats() {
        val now = System.currentTimeMillis()
        val week = now - 7L * 24 * 3600 * 1000
        val month = now - 30L * 24 * 3600 * 1000
        val weeks12 = now - 84L * 24 * 3600 * 1000

        _uiState.update {
            it.copy(
                heatmapData = sessionDao.getListeningByDay(weeks12),
                topTracks = sessionDao.getTopTracks(month, 10).map { item -> item.toTopTrackItem() },
                weeklyMinutes = (sessionDao.getTotalListeningMs(week) ?: 0L) / 60_000L,
                monthlyTracks = sessionDao.getTopTracks(month, 1000).size,
            )
        }
    }
}

private fun TrackWithStats.toTopTrackItem(): TopTrackItem =
    TopTrackItem(
        track = TrackEntity(
            id = id,
            contentUri = contentUri,
            title = title,
            artist = artist,
            album = album,
            albumArtist = albumArtist,
            genre = genre,
            year = year,
            trackNumber = trackNumber,
            discNumber = discNumber,
            durationMs = durationMs,
            fileSize = fileSize,
            mimeType = mimeType,
            sampleRate = sampleRate,
            bitRate = bitRate,
            artworkPath = artworkPath,
            waveformJson = waveformJson,
            playCount = playCount,
            lastPlayedAt = lastPlayedAt,
            addedAt = addedAt,
            isAvailable = isAvailable,
            isFavorite = isFavorite,
        ).toTrack(),
        sessionCount = sessionCount,
        listenedMs = listenedMs ?: 0L,
    )
