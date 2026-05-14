package io.sylphy.app.data.model

data class PlayerUiState(
    val activeTrack: Track? = null,
    val isPlaying: Boolean = false,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val position: Long = 0L,
    val duration: Long = 0L,
    val buffered: Long = 0L,
    val speed: Float = 1.0f,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
)

enum class PlaybackState { IDLE, BUFFERING, READY, ENDED }
enum class RepeatMode    { OFF, ONE, ALL }
