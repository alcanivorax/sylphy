package io.sylphy.app.data.local.scanner

import io.sylphy.app.data.local.db.dao.AlbumDao
import io.sylphy.app.data.local.db.dao.ArtistDao
import io.sylphy.app.data.local.db.entity.AlbumEntity
import io.sylphy.app.data.local.db.entity.ArtistEntity
import io.sylphy.app.data.local.db.entity.TrackEntity
import io.sylphy.app.core.util.sha1
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryOrganizer @Inject constructor(
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
) {

    suspend fun organize(tracks: List<TrackEntity>) {
        if (tracks.isEmpty()) return
        organizeAlbums(tracks)
        organizeArtists(tracks)
        Timber.d("Organized ${tracks.size} tracks into albums/artists")
    }

    private suspend fun organizeAlbums(tracks: List<TrackEntity>) {
        val albumMap = tracks.groupBy { it.album }

        val albums = albumMap.map { (albumTitle, albumTracks) ->
            val first = albumTracks.first()
            AlbumEntity(
                id          = "${albumTitle}_${first.albumArtist ?: first.artist}".sha1(),
                title       = albumTitle,
                artist      = first.artist,
                albumArtist = first.albumArtist,
                year        = first.year,
                genre       = first.genre,
                artworkPath = albumTracks.firstOrNull { it.artworkPath != null }?.artworkPath,
                trackCount  = albumTracks.size,
                durationMs  = albumTracks.sumOf { it.durationMs },
                addedAt     = albumTracks.minOf { it.addedAt },
            )
        }

        albumDao.insertAlbums(albums)

        val activeIds = albums.map { it.id }
        albumDao.removeStale(activeIds)
    }

    private suspend fun organizeArtists(tracks: List<TrackEntity>) {
        val artistMap = tracks.groupBy { it.albumArtist ?: it.artist }

        val artists = artistMap.map { (artistName, artistTracks) ->
            val albumCount = artistTracks.map { it.album }.distinct().size
            ArtistEntity(
                id         = artistName.sha1(),
                name       = artistName,
                albumCount = albumCount,
                trackCount = artistTracks.size,
            )
        }

        artistDao.insertArtists(artists)

        val activeIds = artists.map { it.id }
        artistDao.removeStale(activeIds)
    }
}
