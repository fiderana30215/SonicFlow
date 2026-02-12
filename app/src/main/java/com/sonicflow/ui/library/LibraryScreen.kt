package com.sonicflow.ui.library

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sonicflow.domain.model.Track
import com.sonicflow.ui.library.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlayer: (Long) -> Unit,
    onNavigateToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()

    // États pour la création de playlist
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackForPlaylist by remember { mutableStateOf<Track?>(null) }

    // États pour l'ajout aux playlists existantes
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectedTrackForAddToPlaylist by remember { mutableStateOf<Track?>(null) }

    // États pour les détails et partage
    var showTrackDetails by remember { mutableStateOf(false) }
    var selectedTrackForDetails by remember { mutableStateOf<Track?>(null) }

    // Gestion des messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            println("Error: $it")
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            println("Success: $it")
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                SearchBar(
                    value = searchQuery,
                    onValueChange = { viewModel.searchTracks(it) }
                )

                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    LibraryTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            text = {
                                Text(
                                    text = tab.title,
                                    color = if (selectedTab == tab)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                LibraryTab.TRACKS -> {
                    FloatingActionButton(
                        onClick = { viewModel.scanLibrary() },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Scan library"
                        )
                    }
                }
                LibraryTab.PLAYLISTS -> {
                    FloatingActionButton(
                        onClick = {
                            selectedTrackForPlaylist = null
                            showCreatePlaylistDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Create playlist"
                        )
                    }
                }
                else -> {}
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
                        EmptyLibraryState(isSearching = searchQuery.isNotEmpty())
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
                                    onClick = { onNavigateToPlayer(track.id) },
                                    onAddToPlaylist = {
                                        selectedTrackForAddToPlaylist = track
                                        showAddToPlaylistDialog = true
                                    },
                                    onShare = {
                                        shareTrack(context, track)
                                    },
                                    onDetails = {
                                        selectedTrackForDetails = track
                                        showTrackDetails = true
                                    }
                                )
                            }
                        }
                    }
                }

                LibraryTab.ALBUMS -> {
                    ComingSoonView(featureName = "Albums")
                }

                LibraryTab.PLAYLISTS -> {
                    if (playlists.isEmpty() && !isLoading) {
                        EmptyPlaylistsState(
                            onCreatePlaylist = {
                                selectedTrackForPlaylist = null
                                showCreatePlaylistDialog = true
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                items = playlists,
                                key = { it.id }
                            ) { playlist ->
                                PlaylistItem(
                                    playlist = playlist,
                                    onClick = { onNavigateToPlaylist() },
                                    onDelete = { viewModel.deletePlaylist(playlist.id) },
                                    onAddTrack = {
                                        // TODO: Implémenter la sélection multiple de tracks
                                        println("Add track to playlist: ${playlist.name}")
                                    }
                                )
                            }
                        }
                    }
                }

                LibraryTab.SMART_LISTS -> {
                    ComingSoonView(featureName = "Smart Lists")
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Dialogue de création de playlist
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = {
                showCreatePlaylistDialog = false
                selectedTrackForPlaylist = null
            },
            onCreatePlaylist = { name, description ->
                viewModel.createPlaylist(
                    name = name,
                    description = description,
                    track = selectedTrackForPlaylist
                )
                showCreatePlaylistDialog = false
                selectedTrackForPlaylist = null
            },
            trackToAdd = selectedTrackForPlaylist
        )
    }

    // Dialogue d'ajout aux playlists existantes
    if (showAddToPlaylistDialog && selectedTrackForAddToPlaylist != null) {
        AddTrackToPlaylistDialog(
            playlists = playlists,
            track = selectedTrackForAddToPlaylist!!,
            onDismiss = {
                showAddToPlaylistDialog = false
                selectedTrackForAddToPlaylist = null
            },
            onAddToPlaylist = { playlist, track ->
                viewModel.addTrackToPlaylist(playlist.id, track)
                showAddToPlaylistDialog = false
                selectedTrackForAddToPlaylist = null
            }
        )
    }

    // Dialogue des détails du track
    if (showTrackDetails && selectedTrackForDetails != null) {
        TrackDetailsDialog(
            track = selectedTrackForDetails!!,
            onDismiss = {
                showTrackDetails = false
                selectedTrackForDetails = null
            }
        )
    }
}

@Composable
fun EmptyLibraryState(isSearching: Boolean) {
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
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearching) "No tracks found" else "No tracks in library",
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
}

@Composable
fun EmptyPlaylistsState(
    onCreatePlaylist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlaylistAdd,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No playlists yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first playlist",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreatePlaylist,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Playlist")
        }
    }
}

@Composable
fun ComingSoonView(featureName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Construction,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = featureName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Coming soon!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

private fun shareTrack(context: android.content.Context, track: Track) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, track.title)
        putExtra(Intent.EXTRA_SUBJECT, "${track.title} - ${track.artist}")
        putExtra(Intent.EXTRA_TEXT, "Check out this song: ${track.title} by ${track.artist}")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
}