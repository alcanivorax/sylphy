package io.sylphy.app.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import io.sylphy.app.audio.SleepTimerController
import io.sylphy.app.audio.SylphyEqualizer
import io.sylphy.app.core.extension.toMediaItem
import io.sylphy.app.core.extension.toTrack
import io.sylphy.app.data.local.datastore.SettingsDataStore
import io.sylphy.app.data.local.db.dao.QueueDao
import io.sylphy.app.data.local.db.dao.SessionDao
import io.sylphy.app.data.local.db.dao.TrackDao
import io.sylphy.app.data.local.db.entity.ListeningSessionEntity
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
    @Inject lateinit var sessionDao: SessionDao
    @Inject lateinit var settingsDataStore: SettingsDataStore
    @Inject lateinit var equalizer: SylphyEqualizer
    @Inject lateinit var sleepTimerController: SleepTimerController

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type
    private var currentSessionId: String? = null
    private var currentSessionStartedAt: Long = 0L
    private var currentTrackId: String? = null
    private var crossfadeMs: Int = 0
    private var fadingForTrackId: String? = null
    private val queueListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            saveQueueSnapshot()
            recordTransition(mediaItem)
            player.volume = 1f
            fadingForTrackId = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player).build()
        player.addListener(queueListener)
        equalizer.attach(player.audioSessionId)
        restoreQueueSnapshot()
        observeAudioSettings()
        startCrossfadePolling()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        saveQueueSnapshot(blocking = true)
        closeCurrentSession(blocking = true)
        player.removeListener(queueListener)
        equalizer.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun observeAudioSettings() {
        serviceScope.launch {
            settingsDataStore.settings.collect { settings ->
                crossfadeMs = settings.crossfadeDurationMs
                player.setPlaybackSpeed(settings.playbackSpeed)
                equalizer.setEnabled(settings.eqEnabled)
                equalizer.setBands(settings.eqBands)
            }
        }
    }

    private fun startCrossfadePolling() {
        serviceScope.launch {
            while (true) {
                val duration = player.duration
                val remaining = duration - player.currentPosition
                val trackId = player.currentMediaItem?.mediaId
                if (crossfadeMs > 0 && player.isPlaying && player.hasNextMediaItem() && duration > 0 && remaining in 1..crossfadeMs && fadingForTrackId != trackId) {
                    fadingForTrackId = trackId
                    launch {
                        val start = System.currentTimeMillis()
                        while (player.isPlaying && player.currentMediaItem?.mediaId == trackId) {
                            val elapsed = System.currentTimeMillis() - start
                            player.volume = (1f - (elapsed / crossfadeMs.toFloat())).coerceIn(0f, 1f)
                            if (elapsed >= crossfadeMs) {
                                player.seekToNextMediaItem()
                                player.volume = 1f
                                break
                            }
                            kotlinx.coroutines.delay(100)
                        }
                    }
                }
                kotlinx.coroutines.delay(250)
            }
        }
    }

    private fun recordTransition(mediaItem: MediaItem?) {
        val nextTrackId = mediaItem?.mediaId?.takeIf { it.isNotBlank() }
        serviceScope.launch(Dispatchers.IO) {
            closeCurrentSessionNow()
            if (nextTrackId != null) {
                val now = System.currentTimeMillis()
                val id = UUID.randomUUID().toString()
                currentSessionId = id
                currentSessionStartedAt = now
                currentTrackId = nextTrackId
                sessionDao.insertSession(ListeningSessionEntity(id = id, trackId = nextTrackId, startedAt = now))
            }
        }
    }

    private fun closeCurrentSession(blocking: Boolean = false) {
        val sessionId = currentSessionId ?: return
        val startedAt = currentSessionStartedAt
        val duration = (System.currentTimeMillis() - startedAt).coerceAtLeast(0L)
        val close: suspend () -> Unit = {
            sessionDao.closeSession(sessionId, System.currentTimeMillis(), duration, duration > 30_000L)
            currentSessionId = null
            currentTrackId = null
        }
        if (blocking) runBlocking(Dispatchers.IO) { close() } else serviceScope.launch(Dispatchers.IO) { close() }
    }

    private suspend fun closeCurrentSessionNow() {
        val sessionId = currentSessionId ?: return
        val startedAt = currentSessionStartedAt
        val duration = (System.currentTimeMillis() - startedAt).coerceAtLeast(0L)
        sessionDao.closeSession(sessionId, System.currentTimeMillis(), duration, duration > 30_000L)
        currentSessionId = null
        currentTrackId = null
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
