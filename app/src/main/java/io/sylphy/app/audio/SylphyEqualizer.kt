package io.sylphy.app.audio

import android.media.audiofx.Equalizer
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SylphyEqualizer @Inject constructor() {
    private var eq: Equalizer? = null

    fun attach(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        runCatching {
            eq?.release()
            eq = Equalizer(0, audioSessionId).apply { enabled = true }
        }.onFailure { Timber.w(it, "Equalizer attach failed") }
    }

    fun setEnabled(enabled: Boolean) {
        runCatching { eq?.enabled = enabled }
    }

    fun setBands(gainsDb: List<Float>) {
        val equalizer = eq ?: return
        runCatching {
            val bands = equalizer.numberOfBands.toInt().coerceAtMost(gainsDb.size)
            repeat(bands) { index ->
                equalizer.setBandLevel(index.toShort(), (gainsDb[index].coerceIn(-12f, 12f) * 100).toInt().toShort())
            }
        }.onFailure { Timber.w(it, "Equalizer band update failed") }
    }

    fun release() {
        eq?.release()
        eq = null
    }
}
