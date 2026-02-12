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
import kotlinx.coroutines.delay
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

    // Track ID actuel pour éviter les chargements multiples
    private var currentTrackId: Long? = null

    init {
        observeMediaControllerStates()
    }

    private fun observeMediaControllerStates() {
        viewModelScope.launch {
            mediaControllerManager.isPlaying
                .catch { e -> println("Error observing isPlaying: $e") }
                .collect { playing ->
                    _isPlaying.value = playing
                }
        }

        viewModelScope.launch {
            mediaControllerManager.currentPosition
                .catch { e -> println("Error observing currentPosition: $e") }
                .collect { position ->
                    _currentPosition.value = position
                }
        }

        viewModelScope.launch {
            mediaControllerManager.duration
                .catch { e -> println("Error observing duration: $e") }
                .collect { duration ->
                    _duration.value = duration
                }
        }

        // Observer les changements de track (pour next/previous)
        viewModelScope.launch {
            mediaControllerManager.currentMediaItem
                .catch { e -> println("Error observing currentMediaItem: $e") }
                .collect { mediaItem ->
                    mediaItem?.let {
                        // Extraire l'ID du track depuis les métadonnées
                        val trackId = it.mediaId.toLongOrNull()
                        if (trackId != null && trackId != currentTrackId) {
                            loadTrack(trackId)
                        }
                    }
                }
        }
    }

    fun loadTrack(trackId: Long) {
        // Éviter de recharger le même track
        if (currentTrackId == trackId) {
            return
        }
        currentTrackId = trackId

        viewModelScope.launch {
            try {
                val track = getTracksUseCase(trackId).firstOrNull()
                _currentTrack.value = track

                track?.let {
                    _duration.value = it.duration

                    // Jouer le track
                    mediaControllerManager.playTrack(it.filePath)

                    // Charger le waveform
                    loadWaveform(it.filePath)
                }
            } catch (e: Exception) {
                println("Error loading track: $e")
            }
        }
    }

    fun togglePlayPause() {
        try {
            mediaControllerManager.togglePlayPause()
        } catch (e: Exception) {
            println("Error toggling play/pause: $e")
        }
    }

    fun seekTo(position: Long) {
        try {
            mediaControllerManager.seekTo(position)
        } catch (e: Exception) {
            println("Error seeking: $e")
        }
    }

    fun skipToNext() {
        try {
            mediaControllerManager.skipToNext()
        } catch (e: Exception) {
            println("Error skipping to next: $e")
        }
    }

    fun skipToPrevious() {
        try {
            mediaControllerManager.skipToPrevious()
        } catch (e: Exception) {
            println("Error skipping to previous: $e")
        }
    }

    private fun loadWaveform(filePath: String) {
        viewModelScope.launch {
            try {
                // Réinitialiser le waveform
                _waveformData.value = emptyList()

                // Ajouter un petit délai pour l'UI
                delay(100)

                val waveform = waveformExtractor.extractWaveform(
                    filePath = filePath,
                    samplesCount = 80 // Réduire pour les performances
                )

                if (waveform.isNotEmpty()) {
                    _waveformData.value = waveform
                } else {
                    // Générer des données de fallback
                    _waveformData.value = generateFallbackWaveform(80)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    private fun normalizeWaveform(amplitudes: List<Int>): List<Int> {
        if (amplitudes.isEmpty()) return emptyList()

        val maxAmplitude = amplitudes.maxOrNull() ?: 1
        return if (maxAmplitude > 0) {
            amplitudes.map { amplitude ->
                // Normaliser entre 10 et 100 pour avoir des barres visibles
                val normalized = ((amplitude.toFloat() / maxAmplitude) * 90) + 10
                normalized.toInt().coerceIn(10, 100)
            }
        } else {
            amplitudes
        }
    }

    private fun generateDefaultWaveform(): List<Int> {
        // Générer des données de test réalistes
        return List(100) { index ->
            val sinValue = Math.sin(index * 0.3).toFloat()
            val randomFactor = 0.7f + (Math.random().toFloat() * 0.6f)
            ((sinValue * 0.5f + 0.5f) * 80 * randomFactor + 20).toInt()
        }
    }

    fun reset() {
        currentTrackId = null
        _currentTrack.value = null
        _waveformData.value = emptyList()
        _currentPosition.value = 0L
        _duration.value = 0L
        _isPlaying.value = false
    }
}