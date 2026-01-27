package com.sonicflow.ui.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sonicflow.domain.model.Track
import com.sonicflow.ui.theme.TextSecondary
import com.sonicflow.ui.theme.VioletPrimary

/**
 * Reusable track list item component
 */
@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
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
            // Album art or music icon
            if (track.albumArtUri != null) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = VioletPrimary.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        modifier = Modifier.padding(12.dp),
                        tint = VioletPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
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
            
            // More options menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = TextSecondary
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to playlist") },
                        onClick = {
                            showMenu = false
                            // TODO: Implement add to playlist
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            showMenu = false
                            // TODO: Implement share
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Details") },
                        onClick = {
                            showMenu = false
                            // TODO: Implement show details
                        }
                    )
                }
            }
        }
    }
}
