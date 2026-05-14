package io.sylphy.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.sylphy.app.data.local.db.dao.AlbumDao
import io.sylphy.app.data.local.db.dao.ArtistDao
import io.sylphy.app.data.local.db.dao.PlaylistDao
import io.sylphy.app.data.local.db.dao.QueueDao
import io.sylphy.app.data.local.db.dao.SessionDao
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.local.db.entity.AlbumEntity
import io.sylphy.app.data.local.db.entity.ArtistEntity
import io.sylphy.app.data.local.db.entity.ListeningSessionEntity
import io.sylphy.app.data.local.db.entity.PlaylistEntity
import io.sylphy.app.data.local.db.entity.PlaylistTrackEntity
import io.sylphy.app.data.local.db.entity.QueueSnapshotEntity
import io.sylphy.app.data.local.db.entity.TrackEntity

class WaveformConverter {
    private val gson = Gson()
    private val type = object : TypeToken<List<Float>>() {}.type

    @TypeConverter
    fun fromJson(json: String?): List<Float>? =
        json?.let { gson.fromJson(it, type) }

    @TypeConverter
    fun toJson(data: List<Float>?): String? =
        data?.let { gson.toJson(it) }
}

@Database(
    entities = [
        TrackEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        ListeningSessionEntity::class,
        QueueSnapshotEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(WaveformConverter::class)
abstract class SylphyDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun sessionDao(): SessionDao
    abstract fun queueDao(): QueueDao
}
