package com.sonicflow.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for MediaController to control playback
 */
@Singleton
class MediaControllerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "MediaControllerManager"
        private const val POSITION_UPDATE_INTERVAL_MS = 1000L
    }
    
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private var positionUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    init {
        Log.d(TAG, "MediaControllerManager created, initializing...")
        initialize()
    }
    
    /**
     * Initialize MediaController
     */
    private fun initialize() {
        Log.d(TAG, "Initializing MediaController...")
        
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                try {
                    mediaController = controllerFuture?.get()
                    Log.d(TAG, "MediaController connected successfully")
                    setupPlayerListener()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to connect MediaController", e)
                }
            },
            MoreExecutors.directExecutor()
        )
    }
    
    /**
     * Setup player event listener
     */
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "onIsPlayingChanged: $isPlaying")
                _isPlaying.value = isPlaying
                
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                updatePlaybackState()
            }
        })
    }
    
    /**
     * Start periodic position updates
     */
    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }
    
    /**
     * Stop periodic position updates
     */
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    /**
     * Update playback state
     */
    private fun updatePlaybackState() {
        mediaController?.let { controller ->
            _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
            _duration.value = controller.duration.coerceAtLeast(0L)
        }
    }
    
    /**
     * Play a track
     */
    fun playTrack(filePath: String) {
        Log.d(TAG, "playTrack called with: $filePath")
        
        mediaController?.let { controller ->
            try {
                val uri = if (filePath.startsWith("content://")) {
                    Uri.parse(filePath)
                } else {
                    Uri.fromFile(File(filePath))
                }
                
                Log.d(TAG, "Playing URI: $uri")
                
                val mediaItem = MediaItem.fromUri(uri)
                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()
                
                Log.d(TAG, "Playback started")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing track", e)
            }
        } ?: run {
            Log.e(TAG, "MediaController is null, cannot play track")
        }
    }
    
    /**
     * Play/pause toggle
     */
    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
                Log.d(TAG, "Paused")
            } else {
                controller.play()
                Log.d(TAG, "Playing")
            }
        }
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        Log.d(TAG, "seekTo: $position")
        mediaController?.seekTo(position)
    }
    
    /**
     * Skip to next
     */
    fun skipToNext() {
        mediaController?.seekToNext()
    }
    
    /**
     * Skip to previous
     */
    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    /**
     * Release resources
     */
    fun release() {
        Log.d(TAG, "Releasing MediaController")
        stopPositionUpdates()
        scope.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
    }
}
