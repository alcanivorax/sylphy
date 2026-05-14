package io.sylphy.app.core.extension

import io.sylphy.app.data.local.db.entity.TrackEntity
import io.sylphy.app.data.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()
private val floatListType = object : TypeToken<List<Float>>() {}.type

fun TrackEntity.toTrack(): Track = Track(
    id            = id,
    contentUri    = contentUri,
    title         = title,
    artist        = artist,
    album         = album,
    albumArtist   = albumArtist,
    genre         = genre,
    year          = year,
    trackNumber   = trackNumber,
    discNumber    = discNumber,
    durationMs    = durationMs,
    fileSize      = fileSize,
    mimeType      = mimeType,
    sampleRate    = sampleRate,
    bitRate       = bitRate,
    artworkPath   = artworkPath,
    waveformData  = waveformJson?.let { gson.fromJson(it, floatListType) },
    playCount     = playCount,
    lastPlayedAt  = lastPlayedAt,
    addedAt       = addedAt,
    isAvailable   = isAvailable,
    isFavorite    = isFavorite,
)

fun Track.toEntity(): TrackEntity = TrackEntity(
    id            = id,
    contentUri    = contentUri,
    title         = title,
    artist        = artist,
    album         = album,
    albumArtist   = albumArtist,
    genre         = genre,
    year          = year,
    trackNumber   = trackNumber,
    discNumber    = discNumber,
    durationMs    = durationMs,
    fileSize      = fileSize,
    mimeType      = mimeType,
    sampleRate    = sampleRate,
    bitRate       = bitRate,
    artworkPath   = artworkPath,
    waveformJson  = waveformData?.let { gson.toJson(it) },
    playCount     = playCount,
    lastPlayedAt  = lastPlayedAt,
    addedAt       = addedAt,
    isAvailable   = isAvailable,
    isFavorite    = isFavorite,
)
