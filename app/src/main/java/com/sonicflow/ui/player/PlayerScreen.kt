package com.sonicflow.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.ui.player.components.AnimatedWaveformDisk
import com.sonicflow.ui.player.components.AnimatedWaveformView
import com.sonicflow.ui.player.components.PlayerControls
import com.sonicflow.ui.player.components.SeekBar
import com.sonicflow.ui.theme.VioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    trackId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val waveformData by viewModel.waveformData.collectAsState()

    LaunchedEffect(trackId) {
        viewModel.loadTrack(trackId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // FOND TRÈS NOIR
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            // Top bar with back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White // Icône blanche sur fond noir
                )
            }

            // Album Art avec waveform circulaire animé
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedWaveformDisk(
                    albumArtUri = currentTrack?.albumArtUri,
                    isPlaying = isPlaying,
                    waveformData = waveformData,
                    modifier = Modifier
                )
            }

            // Track info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentTrack?.title ?: "Unknown Track",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White, // Texte blanc
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentTrack?.artist ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray, // Gris clair
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            // Waveform visualization animé
            AnimatedWaveformView(
                amplitudes = waveformData,
                progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                isPlaying = isPlaying,
                onSeek = { progress ->
                    viewModel.seekTo((progress * duration).toLong())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Seek bar
            SeekBar(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { position -> viewModel.seekTo(position) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Player controls
            PlayerControls(
                isPlaying = isPlaying,
                onPlayPause = { viewModel.togglePlayPause() },
                onPrevious = { viewModel.skipToPrevious() },
                onNext = { viewModel.skipToNext() },
                onShuffle = { /* TODO: Implement shuffle */ },
                onRepeat = { /* TODO: Implement repeat */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}