package io.sylphy.app.data.local.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.local.db.entity.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataReader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val artworkExtractor: ArtworkExtractor,
) {

    suspend fun enrichTrack(entity: TrackEntity): TrackEntity = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        runCatching {
            retriever.setDataSource(context, Uri.parse(entity.contentUri))
            entity.copy(
                title       = retriever.extract(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: entity.title,
                artist      = retriever.extract(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: entity.artist,
                album       = retriever.extract(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: entity.album,
                albumArtist = retriever.extract(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: entity.albumArtist,
                genre       = retriever.extract(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: entity.genre,
                year        = retriever.extract(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull() ?: entity.year,
                trackNumber = retriever.extract(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                    ?.split("/")
                    ?.firstOrNull()
                    ?.toIntOrNull()
                    ?: entity.trackNumber,
                bitRate    = retriever.extract(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
                    ?: entity.bitRate,
                sampleRate = retriever.extract(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()
                    ?: entity.sampleRate,
                artworkPath = artworkExtractor.extractArtwork(entity.id, entity.contentUri) ?: entity.artworkPath,
            )
        }.onFailure {
            Timber.e(it, "MetadataReader failed for track ${entity.id}")
        }.getOrDefault(entity).also {
            runCatching { retriever.release() }
        }
    }

    suspend fun enrichBatch(entities: List<TrackEntity>) {
        entities.forEach { entity ->
            val enriched = enrichTrack(entity)
            if (enriched != entity) {
                trackDao.updateTrack(enriched)
            }
            delay(30)
        }
    }

    private fun MediaMetadataRetriever.extract(key: Int): String? =
        extractMetadata(key)?.takeIf { it.isNotBlank() && it != "<unknown>" }
}
