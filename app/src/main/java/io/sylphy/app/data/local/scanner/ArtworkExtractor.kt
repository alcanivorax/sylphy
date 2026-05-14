package io.sylphy.app.data.local.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtworkExtractor @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val cacheDir: File
        get() = File(context.cacheDir, "artwork").also { it.mkdirs() }

    suspend fun extractAndCache(trackId: String, contentUri: String): String? =
        withContext(Dispatchers.IO) {
            val outFile = File(cacheDir, "$trackId.jpg")
            if (outFile.exists()) return@withContext outFile.absolutePath

            val retriever = MediaMetadataRetriever()
            try {
                runCatching {
                    retriever.setDataSource(context, Uri.parse(contentUri))
                    val bytes = retriever.embeddedPicture ?: return@withContext null
                    outFile.writeBytes(bytes)
                    outFile.absolutePath
                }.onFailure { e ->
                    Timber.w(e, "Artwork extraction failed: $trackId")
                }.getOrNull()
            } finally {
                retriever.release()
            }
        }

    suspend fun extractArtwork(trackId: String, contentUri: String): String? =
        extractAndCache(trackId, contentUri)

    fun getCachedPath(trackId: String): String? {
        val file = File(cacheDir, "$trackId.jpg")
        return if (file.exists()) file.absolutePath else null
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
