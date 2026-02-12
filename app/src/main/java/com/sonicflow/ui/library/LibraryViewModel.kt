package com.sonicflow.ui.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.domain.model.Playlist
import com.sonicflow.domain.model.Track
import com.sonicflow.domain.usecase.GetTracksUseCase
import com.sonicflow.domain.usecase.ManagePlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Library tabs for the main library screen
 */
enum class LibraryTab {
    TRACKS,
    ALBUMS,
    PLAYLISTS,
    SMART_LISTS;

    val title: String
        get() = when (this) {
            TRACKS -> "Tracks"
            ALBUMS -> "Albums"
            PLAYLISTS -> "Playlists"
            SMART_LISTS -> "Smart Lists"
        }
}

/**
 * Sort order options for tracks
 */
enum class SortOrder {
    TITLE,
    ARTIST,
    DATE_ADDED
}

/**
 * Scan result sealed class for UI feedback
 */
sealed class ScanResult {
    object Success : ScanResult()
    object PermissionDenied : ScanResult()
    data class Error(val message: String) : ScanResult()
}

/**
 * Playlist creation result
 */
sealed class PlaylistResult {
    object Success : PlaylistResult()
    data class Error(val message: String) : PlaylistResult()
}

/**
 * ViewModel for managing library state and operations
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val managePlaylistUseCase: ManagePlaylistUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "LibraryViewModel"
    }

    // ============= SEARCH & FILTER STATE =============
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ============= TAB STATE =============
    private val _selectedTab = MutableStateFlow(LibraryTab.TRACKS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    // ============= SORT STATE =============
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // ============= LOADING STATE =============
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ============= ERROR STATE =============
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ============= SUCCESS MESSAGE STATE =============
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ============= EVENTS =============
    private val _permissionRequired = MutableSharedFlow<Unit>()
    val permissionRequired = _permissionRequired.asSharedFlow()

    private val _scanResult = MutableSharedFlow<ScanResult>()
    val scanResult = _scanResult.asSharedFlow()

    private val _playlistResult = MutableSharedFlow<PlaylistResult>()
    val playlistResult = _playlistResult.asSharedFlow()

    // ============= DATA SOURCES =============

    // All tracks from repository
    private val allTracks = getTracksUseCase()

    // All playlists from repository
    private val allPlaylists = managePlaylistUseCase.getAllPlaylists()

    // ============= TRACKS STATE =============
    // Tracks state - combines search query and sort order
    val tracks: StateFlow<List<Track>> = combine(
        allTracks,
        _searchQuery,
        _sortOrder
    ) { trackList, query, order ->
        // Filter by search query
        val filteredTracks = if (query.isBlank()) {
            trackList
        } else {
            trackList.filter { track ->
                track.title.contains(query, ignoreCase = true) ||
                        track.artist.contains(query, ignoreCase = true) ||
                        track.album?.contains(query, ignoreCase = true) == true
            }
        }

        // Apply sorting
        when (order) {
            SortOrder.TITLE -> filteredTracks.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> filteredTracks.sortedBy { it.artist.lowercase() }
            SortOrder.DATE_ADDED -> filteredTracks.sortedByDescending { it.dateAdded }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ============= PLAYLISTS STATE =============
    // Playlists state - combines search query
    val playlists: StateFlow<List<Playlist>> = combine(
        allPlaylists,
        _searchQuery
    ) { playlistList, query ->
        if (query.isBlank()) {
            playlistList
        } else {
            playlistList.filter { playlist ->
                playlist.name.contains(query, ignoreCase = true) ||
                        playlist.description?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ============= PUBLIC METHODS =============

    /**
     * Update search query
     */
    fun searchTracks(query: String) {
        _searchQuery.value = query
    }

    /**
     * Change sort order
     */
    fun changeSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    /**
     * Change selected tab
     */
    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Scan library for new tracks
     */
    fun scanLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d(TAG, "Starting library scan...")
                getTracksUseCase.scanTracks()
                Log.d(TAG, "Library scan completed successfully")

                _successMessage.value = "Library scanned successfully"
                _scanResult.emit(ScanResult.Success)

            } catch (e: SecurityException) {
                Log.e(TAG, "Permission denied for media access", e)
                _errorMessage.value = "Permission required to access media files"
                _permissionRequired.emit(Unit)
                _scanResult.emit(ScanResult.PermissionDenied)

            } catch (e: IllegalStateException) {
                Log.e(TAG, "MediaStore error", e)
                _errorMessage.value = "Unable to access media library"
                _scanResult.emit(ScanResult.Error("MediaStore error: ${e.message}"))

            } catch (e: Exception) {
                Log.e(TAG, "Error scanning library", e)
                _errorMessage.value = "Error scanning library: ${e.localizedMessage}"
                _scanResult.emit(ScanResult.Error(e.localizedMessage ?: "Unknown error"))

            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create a new playlist
     */
    fun createPlaylist(name: String, description: String? = null, track: Track? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d(TAG, "Creating playlist: $name")

                // Validate playlist name
                if (name.length < 3) {
                    throw IllegalArgumentException("Playlist name must be at least 3 characters")
                }

                // Create playlist
                val playlistId = managePlaylistUseCase.createPlaylist(name, description)

                Log.d(TAG, "Playlist created successfully with ID: $playlistId")

                // If a track was provided, add it to the playlist
                track?.let {
                    managePlaylistUseCase.addTrackToPlaylist(playlistId, it.id)
                    Log.d(TAG, "Track added to playlist: ${it.title}")
                }

                _successMessage.value = if (track != null) {
                    "Playlist '$name' created and track added"
                } else {
                    "Playlist '$name' created successfully"
                }

                _playlistResult.emit(PlaylistResult.Success)

            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid playlist name", e)
                _errorMessage.value = e.message ?: "Invalid playlist name"
                _playlistResult.emit(PlaylistResult.Error(e.message ?: "Invalid playlist name"))

            } catch (e: Exception) {
                Log.e(TAG, "Error creating playlist", e)
                _errorMessage.value = "Error creating playlist: ${e.localizedMessage}"
                _playlistResult.emit(PlaylistResult.Error(e.localizedMessage ?: "Unknown error"))

            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add track to existing playlist
     */
    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d(TAG, "Adding track to playlist: $playlistId")

                managePlaylistUseCase.addTrackToPlaylist(playlistId, track.id)

                _successMessage.value = "Track added to playlist"
                Log.d(TAG, "Track added successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error adding track to playlist", e)
                _errorMessage.value = "Error adding track: ${e.localizedMessage}"

            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Remove track from playlist
     */
    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d(TAG, "Removing track $trackId from playlist: $playlistId")

                managePlaylistUseCase.removeTrackFromPlaylist(playlistId, trackId)

                _successMessage.value = "Track removed from playlist"
                Log.d(TAG, "Track removed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error removing track from playlist", e)
                _errorMessage.value = "Error removing track: ${e.localizedMessage}"

            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete playlist
     */
    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d(TAG, "Deleting playlist: $playlistId")

                managePlaylistUseCase.deletePlaylist(playlistId)

                _successMessage.value = "Playlist deleted successfully"
                Log.d(TAG, "Playlist deleted successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error deleting playlist", e)
                _errorMessage.value = "Error deleting playlist: ${e.localizedMessage}"

            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update playlist
     */
    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                Log.d(TAG, "Updating playlist: ${playlist.id}")

                managePlaylistUseCase.updatePlaylist(playlist)

                _successMessage.value = "Playlist updated successfully"
                Log.d(TAG, "Playlist updated successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error updating playlist", e)
                _errorMessage.value = "Error updating playlist: ${e.localizedMessage}"

            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get playlist by ID
     */
    fun getPlaylist(playlistId: Long): StateFlow<Playlist?> {
        return managePlaylistUseCase.getPlaylistById(playlistId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    /**
     * Reset all states
     */
    fun reset() {
        _searchQuery.value = ""
        _selectedTab.value = LibraryTab.TRACKS
        _sortOrder.value = SortOrder.TITLE
        _errorMessage.value = null
        _successMessage.value = null
        _isLoading.value = false
    }
}