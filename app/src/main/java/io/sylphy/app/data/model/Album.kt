package io.sylphy.app.data.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtist: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    val artworkPath: String? = null,
    val trackCount: Int = 0,
    val durationMs: Long = 0L,
    val addedAt: Long,
    val tracks: List<Track> = emptyList(),
)
