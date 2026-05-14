package io.sylphy.app.core.di

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sylphy.app.service.SylphyPlaybackService
import kotlinx.coroutines.guava.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaControllerProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private var mediaController: MediaController? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null

    fun connect() {
        val token = SessionToken(
            context,
            ComponentName(context, SylphyPlaybackService::class.java),
        )
        controllerFuture = MediaController.Builder(context, token).buildAsync()
        controllerFuture?.addListener(
            {
                runCatching { mediaController = controllerFuture?.get() }
                    .onFailure { Timber.e(it, "MediaController connection failed") }
            },
            { command -> command.run() },
        )
    }

    fun getController(): MediaController? = mediaController

    fun disconnect() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
    }
}
