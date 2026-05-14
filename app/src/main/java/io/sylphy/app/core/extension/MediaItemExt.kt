package io.sylphy.app.core.extension

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import io.sylphy.app.data.model.Track

fun Track.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id)
        .setUri(contentUri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setAlbumArtist(albumArtist)
                .setTrackNumber(trackNumber)
                .setRecordingYear(year)
                .setArtworkUri(artworkPath?.let { Uri.parse(it) })
                .build()
        )
        .build()

fun MediaItem.toTrackId(): String = mediaId
