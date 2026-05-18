package io.sylphy.app.data.repository

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sylphy.app.core.extension.toTrack
import io.sylphy.app.core.extension.mapList
import io.sylphy.app.core.extension.toFtsEntity
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.local.scanner.ArtworkExtractor
import io.sylphy.app.data.local.scanner.LibraryOrganizer
import io.sylphy.app.data.local.scanner.MediaScanner
import io.sylphy.app.data.local.scanner.MetadataReader
import io.sylphy.app.data.local.scanner.ScanProgress
import io.sylphy.app.data.model.Track
import io.sylphy.app.domain.repository.TrackRepository
import io.sylphy.app.service.WaveformScanWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val mediaScanner: MediaScanner,
    private val artworkExtractor: ArtworkExtractor,
    private val libraryOrganizer: LibraryOrganizer,
    private val metadataReader: MetadataReader,
) : TrackRepository {

    override fun getAllTracks(): Flow<List<Track>> =
        trackDao.getAllTracks().mapList { it.toTrack() }

    override fun getFavoriteTracks(): Flow<List<Track>> =
        trackDao.getFavoriteTracks().mapList { it.toTrack() }

    override fun getRecentlyPlayed(limit: Int): Flow<List<Track>> =
        trackDao.getRecentlyPlayed(limit).mapList { it.toTrack() }

    override fun searchTracks(query: String): Flow<List<Track>> {
        val ftsQuery = query.toFtsPrefixQuery()
        return if (ftsQuery.isBlank()) {
            flowOf(emptyList())
        } else {
            trackDao.searchTracks(ftsQuery).mapList { it.toTrack() }
        }
    }

    override fun getTracksByAlbum(album: String): Flow<List<Track>> =
        trackDao.getTracksByAlbum(album).mapList { it.toTrack() }

    override fun getTracksByArtist(artist: String): Flow<List<Track>> =
        trackDao.getTracksByArtist(artist).mapList { it.toTrack() }

    override suspend fun getTrackById(id: String): Track? =
        trackDao.getTrackById(id)?.toTrack()

    override fun scanLibrary(): Flow<ScanProgress> =
        mediaScanner.scan().onEach { progress ->
            if (progress is ScanProgress.Done) {
                val entities = trackDao.getAllTrackEntities()
                rebuildSearchIndex(entities)
                metadataReader.enrichBatch(entities)
                val enriched = trackDao.getAllTrackEntities()
                rebuildSearchIndex(enriched)
                libraryOrganizer.organize(enriched)
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "waveform_scan",
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<WaveformScanWorker>().build(),
                )
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
        libraryOrganizer.organize(trackDao.getAllTrackEntities())
    }

    private suspend fun rebuildSearchIndex(entities: List<io.sylphy.app.data.local.db.entity.TrackEntity>) {
        trackDao.clearSearchIndex()
        if (entities.isNotEmpty()) {
            trackDao.insertSearchRows(entities.map { it.toFtsEntity() })
        }
    }

    private fun String.toFtsPrefixQuery(): String {
        val tokens = trim()
            .split(Regex("\\s+"))
            .map { token -> token.filter { it.isLetterOrDigit() } }
            .filter { it.isNotBlank() }

        return tokens.joinToString(" ") { "$it*" }
    }
}
