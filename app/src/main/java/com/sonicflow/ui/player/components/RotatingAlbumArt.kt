package com.sonicflow.ui.player.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sonicflow.ui.theme.VioletPrimary

/**
 * Rotating album art component that rotates during playback
 * 
 * @param albumArtUri URI of the album artwork
 * @param isPlaying Whether the music is currently playing
 * @param modifier Modifier for the component
 */
@Composable
fun RotatingAlbumArt(
    albumArtUri: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    // Track cumulative rotation to preserve position when paused
    var currentRotation by remember { mutableStateOf(0f) }
    
    // Animate rotation continuously when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val startRotation = currentRotation
            val startTime = withFrameNanos { it }
            
            while (true) {
                withFrameNanos { frameTime ->
                    val elapsed = (frameTime - startTime) / 1_000_000f // Convert to milliseconds
                    currentRotation = startRotation + (elapsed / 25000f) * 360f
                }
            }
        }
    }
    
    Card(
        modifier = modifier
            .size(220.dp)
            .rotate(currentRotation),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    color = VioletPrimary,
                    shape = CircleShape
                )
        ) {
            AsyncImage(
                model = albumArtUri,
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
