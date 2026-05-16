package io.sylphy.app.domain.usecase

import androidx.media3.common.Player
import io.sylphy.app.core.extension.toMediaItem
import io.sylphy.app.data.local.scanner.ScanProgress
import io.sylphy.app.data.model.Track
import io.sylphy.app.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanLibraryUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
) {
    operator fun invoke(): Flow<ScanProgress> =
        trackRepository.scanLibrary()
}

class PlayTrackUseCase @Inject constructor(
    private val player: Player,
) {
    operator fun invoke(track: Track, queue: List<Track> = emptyList()) {
        val mediaItems = if (queue.isEmpty()) {
            listOf(track.toMediaItem())
        } else {
            queue.map { it.toMediaItem() }
        }

        val startIndex = if (queue.isEmpty()) 0 else queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()
    }
}
