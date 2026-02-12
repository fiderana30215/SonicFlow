package com.sonicflow.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.domain.model.Track
import com.sonicflow.domain.usecase.GetTracksUseCase
import com.sonicflow.media.MediaControllerManager
import com.sonicflow.media.WaveformExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val mediaControllerManager: MediaControllerManager,
    private val waveformExtractor: WaveformExtractor
) : ViewModel() {

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _waveformData = MutableStateFlow<List<Int>>(emptyList())
    val waveformData: StateFlow<List<Int>> = _waveformData.asStateFlow()

    private var allTracks: List<Track> = emptyList()
    private var currentPlaylistId: Long? = null

    init {
        observeMediaControllerStates()

        viewModelScope.launch {
            getTracksUseCase().collect { tracks ->
                allTracks = tracks
            }
        }

        // Callback pour mettre à jour le track quand il change
        mediaControllerManager.onTrackChanged = { trackId ->
            viewModelScope.launch {
                loadTrackById(trackId)
            }
        }
    }

    private fun observeMediaControllerStates() {
        viewModelScope.launch {
            mediaControllerManager.isPlaying.collect { playing ->
                _isPlaying.value = playing
            }
        }

        viewModelScope.launch {
            mediaControllerManager.currentPosition.collect { position ->
                _currentPosition.value = position
            }
        }

        viewModelScope.launch {
            mediaControllerManager.duration.collect { duration ->
                _duration.value = duration
            }
        }
    }

    fun loadTrack(trackId: Long) {
        viewModelScope.launch {
            // Charger le track
            loadTrackById(trackId)

            // Configurer la playlist pour next/previous
            val trackFilePaths = allTracks.map { it.filePath }
            val startIndex = allTracks.indexOfFirst { it.id == trackId }.coerceAtLeast(0)
            mediaControllerManager.setPlaylist(trackFilePaths, startIndex)
        }
    }

    private suspend fun loadTrackById(trackId: Long) {
        try {
            val track = getTracksUseCase(trackId).firstOrNull()
            _currentTrack.value = track
            track?.let {
                _duration.value = it.duration
                loadWaveform(it.filePath)
            }
        } catch (e: Exception) {
            println("Error loading track: $e")
        }
    }

    fun togglePlayPause() = mediaControllerManager.togglePlayPause()
    fun seekTo(position: Long) = mediaControllerManager.seekTo(position)
    fun skipToNext() = mediaControllerManager.skipToNext()
    fun skipToPrevious() = mediaControllerManager.skipToPrevious()

    private fun loadWaveform(filePath: String) {
        viewModelScope.launch {
            try {
                _waveformData.value = emptyList()
                val waveform = waveformExtractor.extractWaveform(filePath, 80)
                _waveformData.value = if (waveform.isNotEmpty()) waveform else generateFallbackWaveform(80)
            } catch (e: Exception) {
                _waveformData.value = generateFallbackWaveform(80)
            }
        }
    }

    private fun generateFallbackWaveform(samplesCount: Int): List<Int> {
        return List(samplesCount) { index ->
            val sinValue = (Math.sin(index * 0.2) * 0.5 + 0.5) * 70
            val randomVariation = (Math.random() * 30).toInt()
            (sinValue.toInt() + randomVariation).coerceIn(20, 95)
        }
    }
}