package com.sonicflow.data.local.db.dao

import androidx.room.*
import com.sonicflow.data.local.db.entity.PlaylistEntity
import com.sonicflow.data.local.db.entity.PlaylistTrackCrossRef
import com.sonicflow.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Playlist operations
 */
@Dao
interface PlaylistDao {
    
    /**
     * Get all playlists
     */
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    
    /**
     * Get a specific playlist by ID
     */
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?>
    
    /**
     * Get tracks in a playlist
     */
    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN playlist_track_cross_ref ON tracks.id = playlist_track_cross_ref.trackId
        WHERE playlist_track_cross_ref.playlistId = :playlistId
        ORDER BY playlist_track_cross_ref.position ASC
    """)
    fun getTracksInPlaylist(playlistId: Long): Flow<List<TrackEntity>>
    
    /**
     * Insert a playlist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    
    /**
     * Update a playlist
     */
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    
    /**
     * Delete a playlist
     */
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    
    /**
     * Add track to playlist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)
    
    /**
     * Remove track from playlist
     */
    @Delete
    suspend fun removeTrackFromPlaylist(crossRef: PlaylistTrackCrossRef)
    
    /**
     * Get playlist count
     */
    @Query("SELECT COUNT(*) FROM playlists")
    fun getPlaylistCount(): Flow<Int>
    
    /**
     * Update playlist track count
     */
    @Query("""
        UPDATE playlists 
        SET trackCount = (
            SELECT COUNT(*) FROM playlist_track_cross_ref 
            WHERE playlistId = :playlistId
        )
        WHERE id = :playlistId
    """)
    suspend fun updatePlaylistTrackCount(playlistId: Long)
}
