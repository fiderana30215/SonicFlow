package com.sonicflow.media

import android.content.Intent
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
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this)
            .build()
            .apply {
                // Configure player
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_OFF
            }
        
        // Create MediaSession
        mediaSession = MediaSession.Builder(this, player!!)
            .build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        // Stop service when task is removed if not playing
        val player = mediaSession?.player
        if (player?.playWhenReady == false) {
            stopSelf()
        }
    }
    
    override fun onDestroy() {
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
