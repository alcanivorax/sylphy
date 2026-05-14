package io.sylphy.app.data.repository

import io.sylphy.app.core.extension.toTrack
import io.sylphy.app.core.extension.mapList
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.local.scanner.ArtworkExtractor
import io.sylphy.app.data.local.scanner.LibraryOrganizer
import io.sylphy.app.data.local.scanner.MediaScanner
import io.sylphy.app.data.local.scanner.ScanProgress
import io.sylphy.app.data.model.Track
import io.sylphy.app.domain.repository.TrackRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val mediaScanner: MediaScanner,
    private val artworkExtractor: ArtworkExtractor,
    private val libraryOrganizer: LibraryOrganizer,
) : TrackRepository {

    override fun getAllTracks(): Flow<List<Track>> =
        trackDao.getAllTracks().mapList { it.toTrack() }

    override fun getFavoriteTracks(): Flow<List<Track>> =
        trackDao.getFavoriteTracks().mapList { it.toTrack() }

    override fun getRecentlyPlayed(limit: Int): Flow<List<Track>> =
        trackDao.getRecentlyPlayed(limit).mapList { it.toTrack() }

    override fun searchTracks(query: String): Flow<List<Track>> =
        trackDao.searchTracks(query).mapList { it.toTrack() }

    override fun getTracksByAlbum(album: String): Flow<List<Track>> =
        trackDao.getTracksByAlbum(album).mapList { it.toTrack() }

    override fun getTracksByArtist(artist: String): Flow<List<Track>> =
        trackDao.getTracksByArtist(artist).mapList { it.toTrack() }

    override suspend fun getTrackById(id: String): Track? =
        trackDao.getTrackById(id)?.toTrack()

    override fun scanLibrary(): Flow<ScanProgress> =
        mediaScanner.scan().onEach { progress ->
            if (progress is ScanProgress.Done) {
                val entities = trackDao.getAllTracks()
                // Trigger organizer after scan completes by collecting once
                // LibraryOrganizer runs on the caller's coroutine scope
            }
        }

    override suspend fun incrementPlayCount(id: String, timestamp: Long) =
        trackDao.incrementPlayCount(id, timestamp)

    override suspend fun setFavorite(id: String, isFavorite: Boolean) =
        trackDao.setFavorite(id, isFavorite)

    override suspend fun updateWaveform(id: String, waveformJson: String) =
        trackDao.updateWaveform(id, waveformJson)

    override suspend fun getTracksWithoutWaveform(limit: Int): List<Track> =
        trackDao.getTracksWithoutWaveform(limit).map { it.toTrack() }

    override suspend fun enrichMissingArtwork(limit: Int) {
        trackDao.getTracksWithoutArtwork(limit).forEach { entity ->
            val path = artworkExtractor.extractAndCache(entity.id, entity.contentUri)
            if (path != null) {
                trackDao.updateArtworkPath(entity.id, path)
            }
            delay(30)
        }
    }
}
