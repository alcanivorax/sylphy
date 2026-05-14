package io.sylphy.app.data.local.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.local.db.entity.TrackEntity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataReader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val artworkExtractor: ArtworkExtractor,
) {

    suspend fun enrichTrack(entity: TrackEntity): TrackEntity {
        val retriever = MediaMetadataRetriever()
        return runCatching {
            retriever.setDataSource(context, android.net.Uri.parse(entity.contentUri))

            val sampleRate = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                ?.toIntOrNull()

            val bitRate = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toIntOrNull()
                ?.div(1000)

            val albumArtist = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                ?.takeIf { it.isNotBlank() }
                ?: entity.albumArtist

            val genre = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                ?.takeIf { it.isNotBlank() }
                ?: entity.genre

            val artworkPath = artworkExtractor.extractArtwork(entity.id, entity.contentUri)
                ?: entity.artworkPath

            entity.copy(
                sampleRate  = sampleRate ?: entity.sampleRate,
                bitRate     = bitRate ?: entity.bitRate,
                albumArtist = albumArtist,
                genre       = genre,
                artworkPath = artworkPath,
            )
        }.onFailure {
            Timber.e(it, "MetadataReader failed for track ${entity.id}")
        }.getOrDefault(entity).also {
            retriever.release()
        }
    }

    suspend fun enrichBatch(entities: List<TrackEntity>) {
        entities.forEach { entity ->
            val enriched = enrichTrack(entity)
            if (enriched != entity) {
                trackDao.updateTrack(enriched)
            }
        }
    }
}
