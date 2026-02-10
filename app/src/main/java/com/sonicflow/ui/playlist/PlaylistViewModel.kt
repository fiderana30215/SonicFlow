package com.sonicflow.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.domain.model.Playlist
import com.sonicflow.domain.usecase.ManagePlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing playlists
 */
@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val managePlaylistUseCase: ManagePlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    init {
        loadPlaylists()
    }

    /**
     * Load all playlists
     */
    fun loadPlaylists() {
        viewModelScope.launch {
            try {
                _uiState.value = PlaylistUiState.Loading
                managePlaylistUseCase.getAllPlaylists()
                    .catch { e ->
                        _uiState.value = PlaylistUiState.Error(e.message ?: "Failed to load playlists")
                    }
                    .collect { playlistList ->
                        _playlists.value = playlistList
                        _uiState.value = PlaylistUiState.Success
                    }
            } catch (e: Exception) {
                _uiState.value = PlaylistUiState.Error(e.message ?: "Failed to load playlists")
            }
        }
    }

    /**
     * Load a specific playlist by ID
     */
    fun loadPlaylistById(playlistId: Long) {
        viewModelScope.launch {
            try {
                managePlaylistUseCase.getPlaylistById(playlistId)
                    .catch { e ->
                        _uiState.value = PlaylistUiState.Error(e.message ?: "Failed to load playlist")
                    }
                    .collect { playlist ->
                        _selectedPlaylist.value = playlist
                        if (playlist != null) {
                            _uiState.value = PlaylistUiState.Success
                        } else {
                            _uiState.value = PlaylistUiState.Error("Playlist not found")
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = PlaylistUiState.Error(e.message ?: "Failed to load playlist")
            }
        }
    }

    /**
     * Create a new playlist
     */
    fun createPlaylist(name: String, description: String?) {
        viewModelScope.launch {
            try {
                _uiState.value = PlaylistUiState.Loading
                val playlistId = managePlaylistUseCase.createPlaylist(name, description)
                _uiState.value = PlaylistUiState.PlaylistCreated
                // Reload playlists to show the newly created one
                loadPlaylists()
            } catch (e: Exception) {
                _uiState.value = PlaylistUiState.Error(e.message ?: "Failed to create playlist")
            }
        }
    }

    /**
     * Delete a playlist
     */
    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            try {
                managePlaylistUseCase.deletePlaylist(id)
            } catch (e: Exception) {
                _uiState.value = PlaylistUiState.Error(e.message ?: "Failed to delete playlist")
            }
        }
    }

    /**
     * Add a track to a playlist
     */
    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                managePlaylistUseCase.addTrackToPlaylist(playlistId, trackId)
            } catch (e: Exception) {
                _uiState.value = PlaylistUiState.Error(e.message ?: "Failed to add track to playlist")
            }
        }
    }

    /**
     * Remove a track from a playlist
     */
    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                managePlaylistUseCase.removeTrackFromPlaylist(playlistId, trackId)
            } catch (e: Exception) {
                _uiState.value = PlaylistUiState.Error(e.message ?: "Failed to remove track from playlist")
            }
        }
    }
}

/**
 * UI state for playlist screens
 */
sealed class PlaylistUiState {
    object Loading : PlaylistUiState()
    object Success : PlaylistUiState()
    data class Error(val message: String) : PlaylistUiState()
    object PlaylistCreated : PlaylistUiState()
}
