package com.sonicflow.media

import android.content.Intent
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint

/**
 * MediaSessionService for background audio playback with Media3
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    
    companion object {
        private const val TAG = "PlaybackService"
    }
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PlaybackService onCreate")
        
        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this)
            .build()
            .apply {
                // Configure player
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_OFF
            }
        
        Log.d(TAG, "ExoPlayer initialized")
        
        // Create MediaSession
        mediaSession = MediaSession.Builder(this, player!!)
            .build()
        
        Log.d(TAG, "MediaSession created")
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d(TAG, "onGetSession called for controller: ${controllerInfo.packageName}")
        return mediaSession
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved")
        // Stop service when task is removed if not playing
        val player = mediaSession?.player
        if (player?.playWhenReady == false) {
            stopSelf()
        }
    }
    
    override fun onDestroy() {
        Log.d(TAG, "PlaybackService onDestroy")
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player?.release()
        player = null
        super.onDestroy()
    }
}
