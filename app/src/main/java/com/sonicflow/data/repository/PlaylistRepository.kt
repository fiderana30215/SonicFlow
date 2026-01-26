package com.sonicflow.data.repository

import com.sonicflow.data.local.db.dao.PlaylistDao
import com.sonicflow.data.local.db.dao.TrackDao
import com.sonicflow.data.local.db.entity.PlaylistEntity
import com.sonicflow.data.local.db.entity.PlaylistTrackCrossRef
import com.sonicflow.domain.model.Playlist
import com.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing playlist data
 */
@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val trackDao: TrackDao
) {
    
    /**
     * Get all playlists
     */
    fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Get a specific playlist by ID with its tracks
     */
    fun getPlaylistById(playlistId: Long): Flow<Playlist?> {
        return combine(
            playlistDao.getPlaylistById(playlistId),
            playlistDao.getTracksInPlaylist(playlistId)
        ) { playlist, tracks ->
            playlist?.toDomain(tracks.map { it.toDomainTrack() })
        }
    }
    
    /**
     * Create a new playlist
     */
    suspend fun createPlaylist(name: String, description: String?): Long {
        val playlist = PlaylistEntity(
            name = name,
            description = description,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return playlistDao.insertPlaylist(playlist)
    }
    
    /**
     * Update an existing playlist
     */
    suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                description = playlist.description,
                createdAt = playlist.createdAt,
                updatedAt = System.currentTimeMillis(),
                trackCount = playlist.trackCount
            )
        )
    }
    
    /**
     * Delete a playlist
     */
    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.getPlaylistById(playlistId).collect { playlist ->
            playlist?.let { playlistDao.deletePlaylist(it) }
        }
    }
    
    /**
     * Add track to playlist
     */
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val crossRef = PlaylistTrackCrossRef(
            playlistId = playlistId,
            trackId = trackId,
            addedAt = System.currentTimeMillis()
        )
        playlistDao.addTrackToPlaylist(crossRef)
        playlistDao.updatePlaylistTrackCount(playlistId)
    }
    
    /**
     * Remove track from playlist
     */
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        val crossRef = PlaylistTrackCrossRef(
            playlistId = playlistId,
            trackId = trackId
        )
        playlistDao.removeTrackFromPlaylist(crossRef)
        playlistDao.updatePlaylistTrackCount(playlistId)
    }
    
    /**
     * Convert PlaylistEntity to domain Playlist
     */
    private fun PlaylistEntity.toDomain(tracks: List<Track> = emptyList()) = Playlist(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        trackCount = trackCount,
        tracks = tracks
    )
    
    /**
     * Convert TrackEntity to domain Track
     */
    private fun com.sonicflow.data.local.db.entity.TrackEntity.toDomainTrack() = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        filePath = filePath,
        albumArtUri = albumArtUri,
        dateAdded = dateAdded,
        dateModified = dateModified,
        size = size,
        mimeType = mimeType
    )
}
