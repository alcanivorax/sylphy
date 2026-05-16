package io.sylphy.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sylphy.app.data.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sylphy_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val gson = Gson()
    private val floatListType = object : TypeToken<List<Float>>() {}.type

    private object Keys {
        val CROSSFADE_MS       = intPreferencesKey("crossfade_ms")
        val PLAYBACK_SPEED     = floatPreferencesKey("playback_speed")
        val GAPLESS_ENABLED    = booleanPreferencesKey("gapless_enabled")
        val EQ_ENABLED         = booleanPreferencesKey("eq_enabled")
        val EQ_PRESET          = stringPreferencesKey("eq_preset")
        val EQ_BANDS           = stringPreferencesKey("eq_bands")
        val SLEEP_TIMER_ON     = booleanPreferencesKey("sleep_timer_enabled")
        val SLEEP_TIMER_END    = longPreferencesKey("sleep_timer_end_time")
        val AMBIENT_MODE       = booleanPreferencesKey("ambient_mode_enabled")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            crossfadeDurationMs  = prefs[Keys.CROSSFADE_MS]    ?: 0,
            playbackSpeed        = prefs[Keys.PLAYBACK_SPEED]  ?: 1.0f,
            gaplessEnabled       = prefs[Keys.GAPLESS_ENABLED] ?: true,
            eqEnabled            = prefs[Keys.EQ_ENABLED]      ?: false,
            eqPreset             = prefs[Keys.EQ_PRESET]       ?: "flat",
            eqBands              = prefs[Keys.EQ_BANDS]?.let { gson.fromJson(it, floatListType) } ?: List(10) { 0f },
            sleepTimerEnabled    = prefs[Keys.SLEEP_TIMER_ON]  ?: false,
            sleepTimerEndTime    = prefs[Keys.SLEEP_TIMER_END],
            ambientModeEnabled   = prefs[Keys.AMBIENT_MODE]    ?: true,
        )
    }

    suspend fun setCrossfadeDuration(ms: Int) {
        context.dataStore.edit { it[Keys.CROSSFADE_MS] = ms }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = speed }
    }

    suspend fun setGapless(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GAPLESS_ENABLED] = enabled }
    }

    suspend fun setEqEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.EQ_ENABLED] = enabled }
    }

    suspend fun setEqPreset(preset: String) {
        context.dataStore.edit { it[Keys.EQ_PRESET] = preset }
    }

    suspend fun setEqBands(bands: List<Float>) {
        context.dataStore.edit { it[Keys.EQ_BANDS] = gson.toJson(bands) }
    }

    suspend fun setSleepTimer(enabled: Boolean, endTime: Long? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SLEEP_TIMER_ON] = enabled
            if (endTime != null) prefs[Keys.SLEEP_TIMER_END] = endTime
            else prefs.remove(Keys.SLEEP_TIMER_END)
        }
    }

    suspend fun setAmbientMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AMBIENT_MODE] = enabled }
    }
}
