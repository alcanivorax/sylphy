package io.sylphy.app.service

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.sylphy.app.domain.repository.TrackRepository
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.sqrt

@HiltWorker
class WaveformScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val trackRepository: TrackRepository,
) : CoroutineWorker(context, params) {

    companion object {
        const val WAVEFORM_POINTS = 200
        const val BATCH_LIMIT = 10
    }

    override suspend fun doWork(): Result {
        return runCatching {
            val tracks = trackRepository.getTracksWithoutWaveform(BATCH_LIMIT)
            if (tracks.isEmpty()) return Result.success()

            tracks.forEach { track ->
                runCatching {
                    val waveform = generateWaveform(track.contentUri)
                    if (waveform != null) {
                        val json = Gson().toJson(waveform)
                        trackRepository.updateWaveform(track.id, json)
                    }
                }.onFailure { Timber.e(it, "Waveform gen failed for track ${track.id}") }
            }

            Result.success()
        }.getOrElse { e ->
            Timber.e(e, "WaveformScanWorker failed")
            Result.retry()
        }
    }

    private fun generateWaveform(contentUri: String): List<Float>? {
        val extractor = MediaExtractor()
        return runCatching {
            extractor.setDataSource(applicationContext, android.net.Uri.parse(contentUri), null)

            val trackIndex = (0 until extractor.trackCount).firstOrNull { i ->
                extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)
                    ?.startsWith("audio/") == true
            } ?: return null

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val samples = mutableListOf<Float>()
            val bufferInfo = MediaCodec.BufferInfo()
            var sawEos = false

            while (!sawEos) {
                val inIndex = codec.dequeueInputBuffer(10_000)
                if (inIndex >= 0) {
                    val inputBuffer: ByteBuffer = codec.getInputBuffer(inIndex) ?: break
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        sawEos = true
                    } else {
                        codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }

                val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outIndex >= 0) {
                    val outputBuffer: ByteBuffer = codec.getOutputBuffer(outIndex) ?: continue
                    val chunk = ShortArray(bufferInfo.size / 2)
                    outputBuffer.asShortBuffer().get(chunk)

                    if (chunk.isNotEmpty()) {
                        val rms = sqrt(chunk.map { it.toDouble() * it }.average()).toFloat()
                        samples.add(rms)
                    }

                    codec.releaseOutputBuffer(outIndex, false)
                }
            }

            codec.stop()
            codec.release()

            if (samples.isEmpty()) return null

            // Downsample to WAVEFORM_POINTS values normalised to 0..1
            val step = (samples.size / WAVEFORM_POINTS).coerceAtLeast(1)
            val downsampled = (0 until WAVEFORM_POINTS).map { i ->
                val start = i * step
                val end = minOf(start + step, samples.size)
                if (start >= samples.size) 0f
                else samples.subList(start, end).average().toFloat()
            }

            val max = downsampled.maxOrNull()?.takeIf { it > 0f } ?: return null
            downsampled.map { (it / max).coerceIn(0f, 1f) }
        }.onFailure {
            Timber.e(it, "Failed to decode waveform from $contentUri")
        }.getOrNull().also {
            extractor.release()
        }
    }
}
