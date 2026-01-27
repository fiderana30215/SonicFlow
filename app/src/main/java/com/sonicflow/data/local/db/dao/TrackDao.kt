package com.sonicflow.data.local.db.dao

import androidx.room.*
import com.sonicflow.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Track operations
 */
@Dao
interface TrackDao {
    
    /**
     * Get all tracks ordered by title
     */
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>
    
    /**
     * Get a specific track by ID
     */
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    fun getTrackById(trackId: Long): Flow<TrackEntity?>
    
    /**
     * Search tracks by title, artist, or album
     */
    @Query("""
        SELECT * FROM tracks 
        WHERE title LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%' 
        OR album LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchTracks(query: String): Flow<List<TrackEntity>>
    
    /**
     * Get tracks by artist
     */
    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY title ASC")
    fun getTracksByArtist(artist: String): Flow<List<TrackEntity>>
    
    /**
     * Get tracks by album
     */
    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY title ASC")
    fun getTracksByAlbum(album: String): Flow<List<TrackEntity>>
    
    /**
     * Insert tracks (replace on conflict)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)
    
    /**
     * Insert a single track
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)
    
    /**
     * Delete a track
     */
    @Delete
    suspend fun deleteTrack(track: TrackEntity)
    
    /**
     * Delete all tracks
     */
    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()
    
    /**
     * Get track count
     */
    @Query("SELECT COUNT(*) FROM tracks")
    fun getTrackCount(): Flow<Int>
}
