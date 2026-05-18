package io.sylphy.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.sylphy.app.data.local.db.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE id = :id")
    suspend fun getArtistById(id: String): ArtistEntity?

    @Query("SELECT * FROM artists WHERE id = :id")
    fun observeArtistById(id: String): Flow<ArtistEntity?>

    @Query("SELECT * FROM artists WHERE name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 20")
    suspend fun searchArtists(query: String): List<ArtistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Query("UPDATE artists SET albumCount = :albumCount, trackCount = :trackCount WHERE id = :id")
    suspend fun updateStats(id: String, albumCount: Int, trackCount: Int)

    @Query("DELETE FROM artists WHERE id NOT IN (:activeIds)")
    suspend fun removeStale(activeIds: List<String>)

    @Query("DELETE FROM artists")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM artists")
    suspend fun getArtistCount(): Int
}
