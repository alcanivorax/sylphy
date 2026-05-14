package io.sylphy.app.domain.repository

import io.sylphy.app.data.local.scanner.ScanProgress
import io.sylphy.app.data.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getAllTracks(): Flow<List<Track>>
    fun getFavoriteTracks(): Flow<List<Track>>
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<Track>>
    fun searchTracks(query: String): Flow<List<Track>>
    fun getTracksByAlbum(album: String): Flow<List<Track>>
    fun getTracksByArtist(artist: String): Flow<List<Track>>
    suspend fun getTrackById(id: String): Track?
    fun scanLibrary(): Flow<ScanProgress>
    suspend fun incrementPlayCount(id: String, timestamp: Long)
    suspend fun setFavorite(id: String, isFavorite: Boolean)
    suspend fun updateWaveform(id: String, waveformJson: String)
    suspend fun getTracksWithoutWaveform(limit: Int = 50): List<Track>
    suspend fun enrichMissingArtwork(limit: Int = 100)
}
