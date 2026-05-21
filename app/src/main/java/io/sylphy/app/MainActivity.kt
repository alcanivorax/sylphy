package io.sylphy.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import io.sylphy.app.core.di.MediaControllerProvider
import io.sylphy.app.data.local.datastore.SettingsDataStore
import io.sylphy.app.data.model.ThemeMode
import io.sylphy.app.ui.navigation.SylphyNavGraph
import io.sylphy.app.ui.theme.SylphyTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var mediaControllerProvider: MediaControllerProvider
    @Inject lateinit var settingsDataStore: SettingsDataStore
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeMs = 0L

    private val shakeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values.getOrNull(0) ?: return
            val y = event.values.getOrNull(1) ?: return
            val z = event.values.getOrNull(2) ?: return
            val magnitude = kotlin.math.sqrt(x * x + y * y + z * z)
            val now = System.currentTimeMillis()
            if (magnitude > 15f && now - lastShakeMs > 2000L) {
                lastShakeMs = now
                mediaControllerProvider.getController()?.let { controller ->
                    controller.shuffleModeEnabled = !controller.shuffleModeEnabled
                    vibrate()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mediaControllerProvider.connect()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        setContent {
            val settings by settingsDataStore.settings.collectAsState(initial = null)
            SylphyTheme(mode = settings?.themeMode ?: ThemeMode.MONOCHROME_DARK) {
                SylphyNavGraph(themeMode = settings?.themeMode ?: ThemeMode.MONOCHROME_DARK)
            }
        }
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(shakeListener)
        mediaControllerProvider.disconnect()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager?.registerListener(shakeListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        sensorManager?.unregisterListener(shakeListener)
        super.onPause()
    }

    private fun vibrate() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
