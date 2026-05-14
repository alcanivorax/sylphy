package io.sylphy.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val artworkPath: String? = null,
    val albumCount: Int = 0,
    val trackCount: Int = 0,
)
