package io.sylphy.app.data.local.scanner

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class ScanProgress {
    object Idle : ScanProgress()
    data class Scanning(val progress: Float, val found: Int) : ScanProgress()
    data class Done(val total: Int) : ScanProgress()
    data class Error(val message: String) : ScanProgress()
}

@Singleton
class MediaScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
) {

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ARTIST,
        MediaStore.Audio.Media.GENRE,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.DISC_NUMBER,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.MIME_TYPE,
        MediaStore.Audio.Media.DATE_ADDED,
    )

    private val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0" +
            " AND ${MediaStore.Audio.Media.DURATION} > 10000"

    fun scan(): Flow<ScanProgress> = flow {
        emit(ScanProgress.Scanning(0f, 0))

        val resolver: ContentResolver = context.contentResolver
        val cursor = resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC",
        )

        if (cursor == null) {
            emit(ScanProgress.Error("MediaStore query returned null"))
            return@flow
        }

        val total = cursor.count
        if (total == 0) {
            cursor.close()
            emit(ScanProgress.Done(0))
            return@flow
        }

        val batch = mutableListOf<TrackEntity>()
        var processed = 0

        cursor.use {
            val idCol          = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dataCol        = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val titleCol       = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol      = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol       = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumArtistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
            val genreCol       = it.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)
            val yearCol        = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackCol       = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val discCol        = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISC_NUMBER)
            val durationCol    = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol        = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol        = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateCol        = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (it.moveToNext()) {
                runCatching {
                    val mediaId = it.getLong(idCol)
                    val uri     = "${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/$mediaId"

                    TrackEntity(
                        id          = mediaId.toString(),
                        contentUri  = uri,
                        title       = it.getString(titleCol)?.takeIf { t -> t.isNotBlank() }
                                        ?: it.getString(dataCol)?.substringAfterLast('/')
                                        ?: "Unknown",
                        artist      = it.getString(artistCol)?.takeIf { a -> a != "<unknown>" }
                                        ?: "Unknown Artist",
                        album       = it.getString(albumCol)?.takeIf { a -> a != "<unknown>" }
                                        ?: "Unknown Album",
                        albumArtist = it.getString(albumArtistCol),
                        genre       = it.getString(genreCol),
                        year        = it.getInt(yearCol).takeIf { y -> y > 0 },
                        trackNumber = it.getInt(trackCol).takeIf { n -> n > 0 },
                        discNumber  = it.getInt(discCol).takeIf { n -> n > 0 },
                        durationMs  = it.getLong(durationCol),
                        fileSize    = it.getLong(sizeCol).takeIf { s -> s > 0 },
                        mimeType    = it.getString(mimeCol),
                        addedAt     = it.getLong(dateCol) * 1000L,
                    )
                }.onSuccess { entity ->
                    batch.add(entity)
                }.onFailure { e ->
                    Timber.e(e, "Failed to read track at cursor position ${it.position}")
                }

                processed++

                if (batch.size >= 100) {
                    trackDao.insertTracks(batch.toList())
                    batch.clear()
                }

                emit(ScanProgress.Scanning(processed.toFloat() / total, processed))
            }
        }

        if (batch.isNotEmpty()) {
            trackDao.insertTracks(batch)
        }

        Timber.d("Scan complete: $total tracks found")
        emit(ScanProgress.Done(total))
    }
}
