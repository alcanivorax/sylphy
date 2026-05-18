package io.sylphy.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.sylphy.app.data.local.db.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: String): AlbumEntity?

    @Query("SELECT * FROM albums WHERE id = :id")
    fun observeAlbumById(id: String): Flow<AlbumEntity?>

    @Query("SELECT * FROM albums WHERE artist = :artist ORDER BY year DESC")
    fun getAlbumsByArtist(artist: String): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY title ASC LIMIT 20")
    suspend fun searchAlbums(query: String): List<AlbumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Query("UPDATE albums SET trackCount = :count, durationMs = :durationMs WHERE id = :id")
    suspend fun updateStats(id: String, count: Int, durationMs: Long)

    @Query("DELETE FROM albums WHERE id NOT IN (:activeIds)")
    suspend fun removeStale(activeIds: List<String>)

    @Query("DELETE FROM albums")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getAlbumCount(): Int
}
