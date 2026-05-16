package io.sylphy.app.data.local.scanner

import io.sylphy.app.data.local.db.dao.AlbumDao
import io.sylphy.app.data.local.db.dao.ArtistDao
import io.sylphy.app.data.local.db.entity.AlbumEntity
import io.sylphy.app.data.local.db.entity.ArtistEntity
import io.sylphy.app.data.local.db.entity.TrackEntity
import io.sylphy.app.core.util.sha1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryOrganizer @Inject constructor(
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
) {

    suspend fun organize(tracks: List<TrackEntity>) = withContext(Dispatchers.IO) {
        if (tracks.isNotEmpty()) {
            organizeAlbums(tracks)
            organizeArtists(tracks)
            Timber.d("Organized ${tracks.size} tracks into albums/artists")
        }
    }

    private suspend fun organizeAlbums(tracks: List<TrackEntity>) {
        val albumMap = tracks.groupBy { track ->
            "${(track.albumArtist ?: track.artist).lowercase()}::${track.album.lowercase()}"
        }

        val albums = albumMap.map { (key, albumTracks) ->
            val first = albumTracks.first()
            AlbumEntity(
                id          = key.sha1(),
                title       = first.album,
                artist      = first.albumArtist ?: first.artist,
                albumArtist = first.albumArtist,
                year        = albumTracks.mapNotNull { it.year }.firstOrNull(),
                genre       = first.genre,
                artworkPath = albumTracks.firstNotNullOfOrNull { it.artworkPath },
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
        val artistMap = tracks.groupBy { (it.albumArtist ?: it.artist).lowercase() }

        val artists = artistMap.map { (_, artistTracks) ->
            val artistName = artistTracks.first().albumArtist ?: artistTracks.first().artist
            val albumCount = artistTracks.map { it.album }.distinct().size
            ArtistEntity(
                id          = artistName.lowercase().sha1(),
                name        = artistName,
                artworkPath = artistTracks.firstNotNullOfOrNull { it.artworkPath },
                albumCount  = albumCount,
                trackCount  = artistTracks.size,
            )
        }

        artistDao.insertArtists(artists)

        val activeIds = artists.map { it.id }
        artistDao.removeStale(activeIds)
    }
}
