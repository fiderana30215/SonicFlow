package com.sonicflow.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.domain.model.Track
import com.sonicflow.domain.usecase.GetTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
     * Scan library for new tracks
     */
    fun scanLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getTracksUseCase.scanTracks()
            } catch (e: SecurityException) {
                // Handle permission exception
                // Optional: emit an event to inform the UI that permission is needed
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
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
