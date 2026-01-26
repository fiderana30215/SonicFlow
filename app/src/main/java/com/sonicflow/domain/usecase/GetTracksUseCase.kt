package com.sonicflow.domain.usecase

import com.sonicflow.data.repository.TrackRepository
import com.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving tracks from the repository
 */
class GetTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    
    /**
     * Get all tracks
     */
    operator fun invoke(): Flow<List<Track>> {
        return trackRepository.getAllTracks()
    }
    
    /**
     * Get track by ID
     */
    operator fun invoke(trackId: Long): Flow<Track?> {
        return trackRepository.getTrackById(trackId)
    }
    
    /**
     * Search tracks
     */
    fun search(query: String): Flow<List<Track>> {
        return trackRepository.searchTracks(query)
    }
    
    /**
     * Scan and load tracks from device
     */
    suspend fun scanTracks() {
        trackRepository.scanAndLoadTracks()
    }
}
