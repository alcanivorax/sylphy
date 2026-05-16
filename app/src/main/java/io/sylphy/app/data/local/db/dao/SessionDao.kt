package io.sylphy.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.sylphy.app.data.local.db.entity.ListeningSessionEntity
import io.sylphy.app.data.local.db.entity.QueueSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ListeningSessionEntity)

    @Query("UPDATE listening_sessions SET endedAt = :endedAt, durationListenedMs = :durationMs, completed = :completed WHERE id = :id")
    suspend fun closeSession(id: String, endedAt: Long, durationMs: Long, completed: Boolean)

    @Query("SELECT * FROM listening_sessions ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 100): Flow<List<ListeningSessionEntity>>

    @Query("SELECT SUM(durationListenedMs) FROM listening_sessions")
    suspend fun getTotalListeningTimeMs(): Long?

    @Query("SELECT SUM(durationListenedMs) FROM listening_sessions WHERE startedAt >= :since")
    suspend fun getTotalListeningMs(since: Long): Long?

    @Query("""
        SELECT (startedAt / 86400000) * 86400000 AS dayStartMs, SUM(durationListenedMs) AS durationMs
        FROM listening_sessions
        WHERE startedAt >= :since
        GROUP BY dayStartMs
        ORDER BY dayStartMs ASC
    """)
    suspend fun getListeningByDay(since: Long): List<DayListening>

    @Query("""
        SELECT tracks.*, COUNT(listening_sessions.id) AS sessionCount, SUM(listening_sessions.durationListenedMs) AS listenedMs
        FROM listening_sessions
        INNER JOIN tracks ON tracks.id = listening_sessions.trackId
        WHERE listening_sessions.startedAt >= :since
        GROUP BY tracks.id
        ORDER BY sessionCount DESC, listenedMs DESC
        LIMIT :limit
    """)
    suspend fun getTopTracks(since: Long, limit: Int): List<TrackWithStats>

    @Query("SELECT trackId, COUNT(*) as plays FROM listening_sessions GROUP BY trackId ORDER BY plays DESC LIMIT :limit")
    suspend fun getTopTrackIds(limit: Int = 10): List<TrackPlayCount>

    @Query("DELETE FROM listening_sessions WHERE startedAt < :before")
    suspend fun deleteBefore(before: Long)
}

data class TrackPlayCount(val trackId: String, val plays: Int)
data class DayListening(val dayStartMs: Long, val durationMs: Long)
data class TrackWithStats(
    val id: String,
    val contentUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String?,
    val genre: String?,
    val year: Int?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val durationMs: Long,
    val fileSize: Long?,
    val mimeType: String?,
    val sampleRate: Int?,
    val bitRate: Int?,
    val artworkPath: String?,
    val waveformJson: String?,
    val playCount: Int,
    val lastPlayedAt: Long?,
    val addedAt: Long,
    val isAvailable: Boolean,
    val isFavorite: Boolean,
    val sessionCount: Int,
    val listenedMs: Long?,
)

@Dao
interface QueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSnapshot(snapshot: QueueSnapshotEntity)

    @Query("SELECT * FROM queue_snapshots ORDER BY savedAt DESC LIMIT 1")
    suspend fun getLatestSnapshot(): QueueSnapshotEntity?

    @Query("DELETE FROM queue_snapshots")
    suspend fun clearSnapshots()
}
