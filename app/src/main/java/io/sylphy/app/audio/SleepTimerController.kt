package io.sylphy.app.audio

import androidx.media3.common.Player
import io.sylphy.app.data.local.datastore.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class SleepTimerState(
    val active: Boolean = false,
    val endAtMs: Long? = null,
    val remainingMs: Long = 0L,
)

@Singleton
class SleepTimerController @Inject constructor(
    private val player: Player,
    private val settingsDataStore: SettingsDataStore,
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()
    private var job: Job? = null

    init {
        scope.launch {
            settingsDataStore.settings.collect { settings ->
                val endAt = settings.sleepTimerEndTime
                if (settings.sleepTimerEnabled && endAt != null && endAt > System.currentTimeMillis()) {
                    startUntil(endAt)
                } else if (!settings.sleepTimerEnabled) {
                    cancel()
                }
            }
        }
    }

    suspend fun setForDuration(durationMs: Long) {
        settingsDataStore.setSleepTimer(true, System.currentTimeMillis() + durationMs)
    }

    suspend fun setEndOfTrack() {
        val duration = player.duration.takeIf { it > 0 } ?: return
        settingsDataStore.setSleepTimer(true, System.currentTimeMillis() + (duration - player.currentPosition).coerceAtLeast(0L))
    }

    suspend fun clear() {
        settingsDataStore.setSleepTimer(false, null)
        cancel()
    }

    private fun startUntil(endAtMs: Long) {
        if (_state.value.endAtMs == endAtMs && job?.isActive == true) return
        job?.cancel()
        job = scope.launch {
            var fading = false
            while (true) {
                val remaining = (endAtMs - System.currentTimeMillis()).coerceAtLeast(0L)
                _state.update { it.copy(active = true, endAtMs = endAtMs, remainingMs = remaining) }
                if (remaining <= 0L) {
                    player.pause()
                    player.volume = 1f
                    settingsDataStore.setSleepTimer(false, null)
                    break
                }
                if (remaining <= 30_000L) {
                    fading = true
                    player.volume = (remaining / 30_000f).coerceIn(0f, 1f)
                } else if (!fading) {
                    player.volume = 1f
                }
                delay(1000)
            }
        }
    }

    private fun cancel() {
        job?.cancel()
        job = null
        player.volume = 1f
        _state.value = SleepTimerState()
    }
}
