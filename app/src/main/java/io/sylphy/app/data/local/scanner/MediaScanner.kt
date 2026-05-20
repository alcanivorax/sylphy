package io.sylphy.app.data.local.scanner

import android.content.ContentUris
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.local.db.entity.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
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
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
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
        val cursor = runCatching {
            resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC",
            )
        }.onFailure { e ->
            Timber.e(e, "MediaStore query failed")
        }.getOrNull()

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
            val nameCol        = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleCol       = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistCol      = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumCol       = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val durationCol    = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val sizeCol        = it.getColumnIndex(MediaStore.Audio.Media.SIZE)
            val mimeCol        = it.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            val dateCol        = it.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while (it.moveToNext()) {
                runCatching {
                    val mediaId = it.getLong(idCol)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mediaId,
                    ).toString()

                    TrackEntity(
                        id          = mediaId.toString(),
                        contentUri  = uri,
                        title       = it.getStringOrNull(titleCol)?.takeIf { t -> t.isNotBlank() }
                                        ?: it.getStringOrNull(nameCol)?.takeIf { n -> n.isNotBlank() }
                                        ?: "Unknown",
                        artist      = it.getStringOrNull(artistCol)?.takeIf { a -> a != "<unknown>" }
                                        ?: "Unknown Artist",
                        album       = it.getStringOrNull(albumCol)?.takeIf { a -> a != "<unknown>" }
                                        ?: "Unknown Album",
                        durationMs  = it.getLongOrNull(durationCol) ?: 0L,
                        fileSize    = it.getLongOrNull(sizeCol)?.takeIf { s -> s > 0 },
                        mimeType    = it.getStringOrNull(mimeCol),
                        addedAt     = (it.getLongOrNull(dateCol) ?: 0L) * 1000L,
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
    }.flowOn(Dispatchers.IO)

    private fun Cursor.getStringOrNull(columnIndex: Int): String? =
        if (columnIndex >= 0 && !isNull(columnIndex)) getString(columnIndex) else null

    private fun Cursor.getIntOrNull(columnIndex: Int): Int? =
        if (columnIndex >= 0 && !isNull(columnIndex)) getInt(columnIndex) else null

    private fun Cursor.getLongOrNull(columnIndex: Int): Long? =
        if (columnIndex >= 0 && !isNull(columnIndex)) getLong(columnIndex) else null
}
