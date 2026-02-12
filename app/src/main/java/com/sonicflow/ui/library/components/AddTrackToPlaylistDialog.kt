package com.sonicflow.ui.library.components

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
import com.sonicflow.domain.model.Playlist
import com.sonicflow.domain.model.Track
import com.sonicflow.ui.theme.TextSecondary
import com.sonicflow.ui.theme.VioletPrimary
import androidx.compose.foundation.clickable

@Composable
fun AddTrackToPlaylistDialog(
    playlists: List<Playlist>,
    track: Track,
    onDismiss: () -> Unit,
    onAddToPlaylist: (Playlist, Track) -> Unit
) {
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

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
            Text(text = "Add to playlist")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Track info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = VioletPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Playlists list
                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No playlists available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    Text(
                        text = "Select a playlist:",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = playlists,
                            key = { it.id }
                        ) { playlist ->
                            PlaylistSelectionItem(
                                playlist = playlist,
                                isSelected = selectedPlaylist?.id == playlist.id,
                                onClick = { selectedPlaylist = playlist }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPlaylist?.let {
                        onAddToPlaylist(it, track)
                        onDismiss()
                    }
                },
                enabled = selectedPlaylist != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioletPrimary
                )
            ) {
                Text("Add")
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
private fun PlaylistSelectionItem(
    playlist: Playlist,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected)
            VioletPrimary.copy(alpha = 0.2f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlaylistPlay,
                contentDescription = null,
                tint = if (isSelected) VioletPrimary else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) VioletPrimary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${playlist.trackCount ?: 0} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = VioletPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}