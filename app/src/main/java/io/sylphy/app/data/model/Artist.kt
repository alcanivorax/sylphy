package io.sylphy.app.data.model

data class Artist(
    val id: String,
    val name: String,
    val artworkPath: String? = null,
    val albumCount: Int = 0,
    val trackCount: Int = 0,
)
