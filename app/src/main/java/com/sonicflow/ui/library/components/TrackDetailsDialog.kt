package com.sonicflow.ui.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sonicflow.domain.model.Track
import com.sonicflow.ui.theme.TextSecondary
import com.sonicflow.ui.theme.VioletPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackDetailsDialog(
    track: Track,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Audiotrack,
                contentDescription = null,
                tint = VioletPrimary
            )
        },
        title = {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Artiste
                DetailRow(
                    icon = Icons.Default.Person,
                    label = "Artist",
                    value = track.artist
                )

                // Album
                DetailRow(
                    icon = Icons.Default.Album,
                    label = "Album",
                    value = track.album ?: "Unknown Album"
                )

                // Durée
                DetailRow(
                    icon = Icons.Default.Schedule,
                    label = "Duration",
                    value = formatDuration(track.duration)
                )

                // Date d'ajout
                DetailRow(
                    icon = Icons.Default.Event,  // Changé de Info à Event
                    label = "Added",
                    value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date(track.dateAdded))
                )

                // Chemin du fichier (optionnel)
                DetailRow(
                    icon = Icons.Default.Folder,
                    label = "File",
                    value = track.filePath.substringAfterLast("/")
                        .take(30) + "...",
                    isFilePath = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioletPrimary
                )
            ) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isFilePath: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = VioletPrimary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = if (isFilePath)
                    MaterialTheme.typography.bodySmall
                else
                    MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isFilePath) 1 else 2
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