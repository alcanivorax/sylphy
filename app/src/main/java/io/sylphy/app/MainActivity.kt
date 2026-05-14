package io.sylphy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import dagger.hilt.android.AndroidEntryPoint
import io.sylphy.app.core.di.MediaControllerProvider
import io.sylphy.app.ui.navigation.SylphyNavGraph
import io.sylphy.app.ui.theme.SylphyTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var mediaControllerProvider: MediaControllerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mediaControllerProvider.connect()
        setContent {
            SylphyTheme {
                SylphyNavGraph()
            }
        }
    }

    override fun onDestroy() {
        mediaControllerProvider.disconnect()
        super.onDestroy()
    }
}
