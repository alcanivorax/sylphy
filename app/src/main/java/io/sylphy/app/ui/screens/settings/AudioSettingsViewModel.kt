package io.sylphy.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sylphy.app.audio.SleepTimerController
import io.sylphy.app.audio.SleepTimerState
import io.sylphy.app.data.local.datastore.SettingsDataStore
import io.sylphy.app.data.model.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val EqPresets = mapOf(
    "flat" to List(10) { 0f },
    "bass" to listOf(6f, 5f, 3f, 1f, 0f, 0f, 0f, -1f, -1f, -1f),
    "vocal" to listOf(-2f, -1f, 0f, 2f, 4f, 4f, 3f, 1f, 0f, -1f),
    "pop" to listOf(3f, 2f, 0f, -1f, -1f, 1f, 3f, 4f, 3f, 2f),
)

data class AudioSettingsUiState(
    val settings: Settings = Settings(),
    val timer: SleepTimerState = SleepTimerState(),
)

@HiltViewModel
class AudioSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val sleepTimerController: SleepTimerController,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AudioSettingsUiState())
    val uiState: StateFlow<AudioSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
        viewModelScope.launch {
            sleepTimerController.state.collect { timer ->
                _uiState.update { it.copy(timer = timer) }
            }
        }
    }

    fun setEqEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setEqEnabled(enabled) }
    }

    fun setEqBand(index: Int, value: Float) {
        val bands = _uiState.value.settings.eqBands.toMutableList()
        if (index !in bands.indices) return
        bands[index] = value.coerceIn(-12f, 12f)
        viewModelScope.launch {
            settingsDataStore.setEqPreset("custom")
            settingsDataStore.setEqBands(bands)
        }
    }

    fun setPreset(name: String) {
        val bands = EqPresets[name] ?: return
        viewModelScope.launch {
            settingsDataStore.setEqPreset(name)
            settingsDataStore.setEqBands(bands)
        }
    }

    fun setTimer(durationMs: Long) {
        viewModelScope.launch { sleepTimerController.setForDuration(durationMs) }
    }

    fun setEndOfTrackTimer() {
        viewModelScope.launch { sleepTimerController.setEndOfTrack() }
    }

    fun cancelTimer() {
        viewModelScope.launch { sleepTimerController.clear() }
    }
}
