package io.sylphy.app.data.model

data class Settings(
    val themeMode: ThemeMode = ThemeMode.MONOCHROME_DARK,
    val crossfadeDurationMs: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val gaplessEnabled: Boolean = true,
    val eqEnabled: Boolean = false,
    val eqPreset: String = "flat",
    val eqBands: List<Float> = List(10) { 0f },
    val sleepTimerEnabled: Boolean = false,
    val sleepTimerEndTime: Long? = null,
    val ambientModeEnabled: Boolean = true,
)
