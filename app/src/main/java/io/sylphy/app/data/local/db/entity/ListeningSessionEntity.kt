package io.sylphy.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listening_sessions")
data class ListeningSessionEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationListenedMs: Long = 0L,
    val completed: Boolean = false,
)

@Entity(tableName = "queue_snapshots")
data class QueueSnapshotEntity(
    @PrimaryKey val id: String,
    val trackIdsJson: String,
    val currentIndex: Int = 0,
    val savedAt: Long,
)
