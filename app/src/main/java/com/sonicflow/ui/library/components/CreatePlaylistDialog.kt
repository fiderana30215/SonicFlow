package com.sonicflow.ui.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sonicflow.domain.model.Track
import com.sonicflow.ui.theme.TextSecondary
import com.sonicflow.ui.theme.VioletPrimary

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreatePlaylist: (String, String?) -> Unit,
    trackToAdd: Track? = null
) {
    var playlistName by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

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
            Text(
                text = if (trackToAdd != null) "Add to new playlist" else "Create new playlist"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist name") },
                    placeholder = { Text("My Playlist") },
                    singleLine = true,
                    isError = playlistName.isNotBlank() && playlistName.length < 3,
                    supportingText = {
                        if (playlistName.isNotBlank() && playlistName.length < 3) {
                            Text("Name must be at least 3 characters")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletPrimary,
                        focusedLabelColor = VioletPrimary,
                        cursorColor = VioletPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = playlistDescription,
                    onValueChange = { playlistDescription = it },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("My favorite songs") },
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletPrimary,
                        focusedLabelColor = VioletPrimary,
                        cursorColor = VioletPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (trackToAdd != null) {
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
                                    text = trackToAdd.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = trackToAdd.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isCreating = true
                    onCreatePlaylist(playlistName, playlistDescription.takeIf { it.isNotBlank() })
                },
                enabled = playlistName.length >= 3 && !isCreating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioletPrimary
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}