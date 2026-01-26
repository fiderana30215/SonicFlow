package com.sonicflow.ui.playlist

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.domain.model.Track
import com.sonicflow.ui.library.components.TrackItem
import com.sonicflow.ui.theme.TextSecondary
import com.sonicflow.ui.theme.VioletPrimary
import org.burnoutcrew.reorderable.*

/**
 * Playlist detail screen showing tracks in a playlist
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var trackToRemove by remember { mutableStateOf<Track?>(null) }
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistById(playlistId)
    }

    LaunchedEffect(selectedPlaylist) {
        selectedPlaylist?.let {
            tracks = it.tracks
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedPlaylist?.name ?: "Playlist") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is PlaylistUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VioletPrimary
                    )
                }
                is PlaylistUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (uiState as PlaylistUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPlaylistById(playlistId) }) {
                            Text("Retry")
                        }
                    }
                }
                is PlaylistUiState.Success -> {
                    selectedPlaylist?.let { playlist ->
                        Column(modifier = Modifier.fillMaxSize()) {
                            PlaylistHeader(
                                name = playlist.name,
                                description = playlist.description,
                                trackCount = playlist.trackCount
                            )
                            
                            if (tracks.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No tracks in this playlist",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Add tracks from your library",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            } else {
                                val state = rememberReorderableLazyListState(
                                    onMove = { from, to ->
                                        tracks = tracks.toMutableList().apply {
                                            add(to.index, removeAt(from.index))
                                        }
                                    }
                                )
                                
                                LazyColumn(
                                    state = state.listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .reorderable(state)
                                ) {
                                    itemsIndexed(
                                        items = tracks,
                                        key = { _, track -> track.id }
                                    ) { index, track ->
                                        ReorderableItem(state, key = track.id) { isDragging ->
                                            val elevation by animateDpAsState(
                                                if (isDragging) 8.dp else 0.dp,
                                                label = "elevation"
                                            )
                                            
                                            PlaylistTrackItem(
                                                track = track,
                                                index = index + 1,
                                                onRemove = { trackToRemove = track },
                                                modifier = Modifier.shadow(elevation)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    trackToRemove?.let { track ->
        AlertDialog(
            onDismissRequest = { trackToRemove = null },
            title = { Text("Remove Track") },
            text = { Text("Remove \"${track.title}\" from this playlist?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeTrackFromPlaylist(playlistId, track.id)
                        trackToRemove = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PlaylistHeader(
    name: String,
    description: String?,
    trackCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    color = VioletPrimary.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.padding(20.dp),
                        tint = VioletPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "$trackCount ${if (trackCount == 1) "track" else "tracks"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PlaylistTrackItem(
    track: Track,
    index: Int,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.width(32.dp)
            )
            
            if (track.albumArtUri != null) {
                coil.compose.AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            VioletPrimary.copy(alpha = 0.1f),
                            MaterialTheme.shapes.small
                        )
                )
            } else {
                Surface(
                    modifier = Modifier.size(48.dp),
                    color = VioletPrimary.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        modifier = Modifier.padding(10.dp),
                        tint = VioletPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${track.artist} • ${track.getFormattedDuration()}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextSecondary
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Remove from playlist",
                    tint = TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reorder",
                tint = TextSecondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
