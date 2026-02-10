package com.sonicflow.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.domain.model.Track
import com.sonicflow.domain.usecase.GetTracksUseCase
import com.sonicflow.media.MediaControllerManager
import com.sonicflow.media.WaveformExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    
    init {
        // Observe MediaController states
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
            val track = getTracksUseCase(trackId).first()
            _currentTrack.value = track
            track?.let {
                _duration.value = it.duration
                // Play track
                mediaControllerManager.playTrack(it.filePath)
                // Load waveform
                loadWaveform(it.filePath)
            }
        }
    }
    
    fun togglePlayPause() {
        mediaControllerManager.togglePlayPause()
    }
    
    fun seekTo(position: Long) {
        mediaControllerManager.seekTo(position)
    }
    
    fun skipToNext() {
        mediaControllerManager.skipToNext()
    }
    
    fun skipToPrevious() {
        mediaControllerManager.skipToPrevious()
    }
    
    private fun loadWaveform(filePath: String) {
        viewModelScope.launch {
            val waveform = waveformExtractor.extractWaveform(filePath, samplesCount = 150)
            _waveformData.value = waveform
        }
    }
}
