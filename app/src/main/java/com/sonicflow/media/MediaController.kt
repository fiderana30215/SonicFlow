package com.sonicflow.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for MediaController to control playback
 */
@Singleton
class MediaControllerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    /**
     * Initialize MediaController
     */
    fun initialize() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
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
                _isPlaying.value = isPlaying
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }
        })
    }
    
    /**
     * Update playback state
     */
    private fun updatePlaybackState() {
        mediaController?.let { controller ->
            _currentPosition.value = controller.currentPosition
            _duration.value = controller.duration
        }
    }
    
    /**
     * Play a track
     */
    fun playTrack(uri: String) {
        mediaController?.let { controller ->
            val mediaItem = MediaItem.fromUri(uri)
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
    }
    
    /**
     * Play/pause toggle
     */
    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
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
        MediaController.releaseFuture(controllerFuture ?: return)
        mediaController = null
        controllerFuture = null
    }
}
