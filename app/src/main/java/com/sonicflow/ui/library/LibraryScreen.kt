package com.sonicflow.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sonicflow.ui.library.components.SearchBar
import com.sonicflow.ui.library.components.TrackItem
import com.sonicflow.ui.theme.VioletPrimary

/**
 * Main library screen with tabs, search, and track list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlayer: (Long) -> Unit,
    onNavigateToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            Column {
                // Search bar
                SearchBar(
                    value = searchQuery,
                    onValueChange = { viewModel.searchTracks(it) }
                )
                
                // Tab row
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = VioletPrimary
                ) {
                    Tab(
                        selected = selectedTab == LibraryTab.TRACKS,
                        onClick = { viewModel.selectTab(LibraryTab.TRACKS) },
                        text = { 
                            Text(
                                "Tracks",
                                color = if (selectedTab == LibraryTab.TRACKS) 
                                    VioletPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ) 
                        }
                    )
                    Tab(
                        selected = selectedTab == LibraryTab.ALBUMS,
                        onClick = { viewModel.selectTab(LibraryTab.ALBUMS) },
                        text = { 
                            Text(
                                "Albums",
                                color = if (selectedTab == LibraryTab.ALBUMS) 
                                    VioletPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ) 
                        }
                    )
                    Tab(
                        selected = selectedTab == LibraryTab.SMART_LISTS,
                        onClick = { viewModel.selectTab(LibraryTab.SMART_LISTS) },
                        text = { 
                            Text(
                                "Smart Lists",
                                color = if (selectedTab == LibraryTab.SMART_LISTS) 
                                    VioletPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ) 
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.scanLibrary() },
                containerColor = VioletPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Scan library"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                LibraryTab.TRACKS -> {
                    if (tracks.isEmpty() && !isLoading) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = VioletPrimary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) 
                                    "No tracks found" 
                                else 
                                    "No tracks in library",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the button to scan your library",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = tracks,
                                key = { it.id }
                            ) { track ->
                                TrackItem(
                                    track = track,
                                    onClick = { onNavigateToPlayer(track.id) }
                                )
                            }
                        }
                    }
                }
                LibraryTab.ALBUMS -> {
                    // Placeholder for albums tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Albums view coming soon",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                LibraryTab.SMART_LISTS -> {
                    // Placeholder for smart lists tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Smart Lists view coming soon",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                    color = VioletPrimary
                )
            }
        }
    }
}
