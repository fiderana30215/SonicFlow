package com.sonicflow.ui.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.domain.model.Track
import com.sonicflow.domain.usecase.GetTracksUseCase
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
 * ViewModel for managing library state and operations
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "LibraryViewModel"
    }

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected tab state
    private val _selectedTab = MutableStateFlow(LibraryTab.TRACKS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    // Sort order state
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state for UI feedback
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Permission required event
    private val _permissionRequired = MutableSharedFlow<Unit>()
    val permissionRequired = _permissionRequired.asSharedFlow()

    // Scan result event
    private val _scanResult = MutableSharedFlow<ScanResult>()
    val scanResult = _scanResult.asSharedFlow()

    // All tracks from repository
    private val allTracks = getTracksUseCase()

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

                // Emit success result
                _scanResult.emit(ScanResult.Success)

            } catch (e: SecurityException) {
                // Permission denied - need to request permission
                Log.e(TAG, "Permission denied for media access", e)
                _errorMessage.value = "Permission required to access media files"
                _permissionRequired.emit(Unit)
                _scanResult.emit(ScanResult.PermissionDenied)

            } catch (e: IllegalStateException) {
                // MediaStore not available or cursor issue
                Log.e(TAG, "MediaStore error", e)
                _errorMessage.value = "Unable to access media library"
                _scanResult.emit(ScanResult.Error("MediaStore error: ${e.message}"))

            } catch (e: Exception) {
                // Generic error
                Log.e(TAG, "Error scanning library", e)
                _errorMessage.value = "Error scanning library: ${e.localizedMessage}"
                _scanResult.emit(ScanResult.Error(e.localizedMessage ?: "Unknown error"))

            } finally {
                _isLoading.value = false
            }
        }
    }
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
 * Library tabs
 */
enum class LibraryTab {
    TRACKS,
    ALBUMS,
    SMART_LISTS
}

/**
 * Sort order options
 */
enum class SortOrder {
    TITLE,
    ARTIST,
    DATE_ADDED
}