package io.sylphy.app.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import io.sylphy.app.core.extension.toMediaItem
import io.sylphy.app.core.extension.toTrack
import io.sylphy.app.data.local.db.dao.QueueDao
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.local.db.entity.QueueSnapshotEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class SylphyPlaybackService : MediaSessionService() {

    @Inject lateinit var player: ExoPlayer
    @Inject lateinit var queueDao: QueueDao
    @Inject lateinit var trackDao: TrackDao

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type
    private val queueListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            saveQueueSnapshot()
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player).build()
        player.addListener(queueListener)
        restoreQueueSnapshot()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        saveQueueSnapshot(blocking = true)
        player.removeListener(queueListener)
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun restoreQueueSnapshot() {
        serviceScope.launch(Dispatchers.IO) {
            val snapshot = queueDao.getLatestSnapshot() ?: return@launch
            val ids = runCatching {
                gson.fromJson<List<String>>(snapshot.trackIdsJson, stringListType)
            }.getOrDefault(emptyList())
            if (ids.isEmpty()) return@launch

            val tracksById = trackDao.getAllTrackEntities().associateBy { it.id }
            val mediaItems = ids.mapNotNull { tracksById[it]?.toTrack()?.toMediaItem() }
            if (mediaItems.isEmpty()) return@launch

            launch(Dispatchers.Main) {
                player.setMediaItems(mediaItems, snapshot.currentIndex.coerceIn(mediaItems.indices), 0L)
                player.prepare()
            }
        }
    }

    private fun saveQueueSnapshot(blocking: Boolean = false) {
        val count = player.mediaItemCount
        if (count == 0) return
        val ids = (0 until count).mapNotNull { index ->
            player.getMediaItemAt(index).mediaId.takeIf { it.isNotBlank() }
        }
        if (ids.isEmpty()) return
        val currentIndex = player.currentMediaItemIndex.coerceAtLeast(0)

        val save: suspend () -> Unit = {
            queueDao.clearSnapshots()
            queueDao.saveSnapshot(
                QueueSnapshotEntity(
                    id = UUID.randomUUID().toString(),
                    trackIdsJson = gson.toJson(ids),
                    currentIndex = currentIndex,
                    savedAt = System.currentTimeMillis(),
                ),
            )
        }
        if (blocking) {
            runBlocking(Dispatchers.IO) { save() }
        } else {
            serviceScope.launch(Dispatchers.IO) { save() }
        }
    }
}
