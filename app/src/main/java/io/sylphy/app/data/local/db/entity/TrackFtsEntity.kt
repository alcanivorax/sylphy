package io.sylphy.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4
@Entity(tableName = "tracks_fts")
data class TrackFtsEntity(
    @PrimaryKey(autoGenerate = true)
    val rowid: Int = 0,
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
)
