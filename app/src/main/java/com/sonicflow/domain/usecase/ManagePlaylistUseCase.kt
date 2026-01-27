package com.sonicflow.domain.usecase

import com.sonicflow.data.repository.PlaylistRepository
import com.sonicflow.domain.model.Playlist
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing playlists
 */
class ManagePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    
    /**
     * Get all playlists
     */
    fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistRepository.getAllPlaylists()
    }
    
    /**
     * Get playlist by ID
     */
    fun getPlaylistById(playlistId: Long): Flow<Playlist?> {
        return playlistRepository.getPlaylistById(playlistId)
    }
    
    /**
     * Create a new playlist
     */
    suspend fun createPlaylist(name: String, description: String? = null): Long {
        return playlistRepository.createPlaylist(name, description)
    }
    
    /**
     * Update a playlist
     */
    suspend fun updatePlaylist(playlist: Playlist) {
        playlistRepository.updatePlaylist(playlist)
    }
    
    /**
     * Delete a playlist
     */
    suspend fun deletePlaylist(playlistId: Long) {
        playlistRepository.deletePlaylist(playlistId)
    }
    
    /**
     * Add track to playlist
     */
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        playlistRepository.addTrackToPlaylist(playlistId, trackId)
    }
    
    /**
     * Remove track from playlist
     */
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
    }
}
