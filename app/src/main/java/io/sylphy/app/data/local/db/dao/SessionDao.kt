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

    @Query("SELECT trackId, COUNT(*) as plays FROM listening_sessions GROUP BY trackId ORDER BY plays DESC LIMIT :limit")
    suspend fun getTopTrackIds(limit: Int = 10): List<TrackPlayCount>

    @Query("DELETE FROM listening_sessions WHERE startedAt < :before")
    suspend fun deleteBefore(before: Long)
}

data class TrackPlayCount(val trackId: String, val plays: Int)

@Dao
interface QueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSnapshot(snapshot: QueueSnapshotEntity)

    @Query("SELECT * FROM queue_snapshots ORDER BY savedAt DESC LIMIT 1")
    suspend fun getLatestSnapshot(): QueueSnapshotEntity?

    @Query("DELETE FROM queue_snapshots")
    suspend fun clearSnapshots()
}
