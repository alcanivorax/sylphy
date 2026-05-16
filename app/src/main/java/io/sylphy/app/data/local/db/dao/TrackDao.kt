package io.sylphy.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.sylphy.app.data.local.db.entity.TrackEntity
import io.sylphy.app.data.local.db.entity.TrackFtsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY discNumber ASC, trackNumber ASC")
    fun getTracksByAlbum(album: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY album ASC, trackNumber ASC")
    fun getTracksByArtist(artist: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<TrackEntity>>

    @Query("SELECT tracks.* FROM tracks JOIN tracks_fts ON tracks.id = tracks_fts.trackId WHERE tracks_fts MATCH :query ORDER BY tracks.title ASC")
    fun searchTracks(query: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    suspend fun getAllTrackEntities(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE waveformJson IS NULL LIMIT :limit")
    suspend fun getTracksWithoutWaveform(limit: Int = 50): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE artworkPath IS NULL LIMIT :limit")
    suspend fun getTracksWithoutArtwork(limit: Int = 100): List<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchRows(rows: List<TrackFtsEntity>)

    @Query("DELETE FROM tracks_fts")
    suspend fun clearSearchIndex()

    @Query("DELETE FROM tracks_fts WHERE trackId = :trackId")
    suspend fun deleteSearchRow(trackId: String)

    @Query("UPDATE tracks SET waveformJson = :waveformJson WHERE id = :id")
    suspend fun updateWaveform(id: String, waveformJson: String)

    @Query("UPDATE tracks SET artworkPath = :artworkPath WHERE id = :id")
    suspend fun updateArtworkPath(id: String, artworkPath: String)

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedAt = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE tracks SET isAvailable = 0 WHERE id NOT IN (:availableIds)")
    suspend fun markUnavailable(availableIds: List<String>)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int
}
