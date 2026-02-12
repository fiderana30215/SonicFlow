package com.sonicflow.ui.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sonicflow.domain.model.Track
import com.sonicflow.ui.theme.TextSecondary
import com.sonicflow.ui.theme.VioletPrimary

@Composable
fun SelectTracksDialog(
    tracks: List<Track>,
    playlistName: String,
    onDismiss: () -> Unit,
    onAddTracks: (List<Track>) -> Unit
) {
    var selectedTracks by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }

    // Filtrer les tracks
    val filteredTracks = if (searchQuery.isBlank()) {
        tracks
    } else {
        tracks.filter { track ->
            track.title.contains(searchQuery, ignoreCase = true) ||
                    track.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.PlaylistAdd,
                contentDescription = null,
                tint = VioletPrimary
            )
        },
        title = {
            Text(text = "Add tracks to playlist")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Playlist info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = VioletPrimary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = VioletPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = playlistName,
                                style = MaterialTheme.typography.titleSmall,
                                color = VioletPrimary
                            )
                            Text(
                                text = "${selectedTracks.size} tracks selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tracks...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletPrimary,
                        focusedLabelColor = VioletPrimary
                    )
                )

                // Tracks list
                if (filteredTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tracks found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = filteredTracks,
                            key = { it.id }
                        ) { track ->
                            TrackSelectionItem(
                                track = track,
                                isSelected = selectedTracks.contains(track.id),
                                onSelect = { isSelected ->
                                    selectedTracks = if (isSelected) {
                                        selectedTracks + track.id
                                    } else {
                                        selectedTracks - track.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tracksToAdd = tracks.filter { selectedTracks.contains(it.id) }
                    onAddTracks(tracksToAdd)
                    onDismiss()
                },
                enabled = selectedTracks.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioletPrimary
                )
            ) {
                Text("Add ${selectedTracks.size} tracks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TrackSelectionItem(
    track: Track,
    isSelected: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(!isSelected) },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected)
            VioletPrimary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelect,
                colors = CheckboxDefaults.colors(
                    checkedColor = VioletPrimary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Track icon
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (isSelected) VioletPrimary else TextSecondary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    color = if (isSelected) VioletPrimary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = TextSecondary
                )
            }

            // Duration
            Text(
                text = formatDuration(track.duration),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

private fun formatDuration(duration: Long): String {
    val totalSeconds = duration / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}