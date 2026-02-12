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

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    private var positionUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Callback pour la fin de la playlist
    var onPlaylistEnded: (() -> Unit)? = null
    var onTrackChanged: ((Long) -> Unit)? = null

    init {
        Log.d(TAG, "MediaControllerManager created, initializing...")
        initialize()
    }

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

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "onIsPlayingChanged: $isPlaying")
                _isPlaying.value = isPlaying
                if (isPlaying) startPositionUpdates() else stopPositionUpdates()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                updatePlaybackState()

                // FIN DE PISTE - Passage automatique au suivant
                if (playbackState == Player.STATE_ENDED) {
                    Log.d(TAG, "Playback ended - playing next")
                    skipToNext()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d(TAG, "onMediaItemTransition: ${mediaItem?.mediaId}")
                _currentMediaItem.value = mediaItem
                updatePlaybackState()

                // Notifier le changement de track
                mediaItem?.mediaId?.toLongOrNull()?.let { trackId ->
                    onTrackChanged?.invoke(trackId)
                }
            }
        })

        _currentMediaItem.value = mediaController?.currentMediaItem
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                delay(POSITION_UPDATE_INTERVAL_MS)
                updatePlaybackState()
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun updatePlaybackState() {
        mediaController?.let { controller ->
            _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
            _duration.value = controller.duration.coerceAtLeast(0L)
        }
    }

    /**
     * Play a single track
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

                val mediaItem = MediaItem.fromUri(uri)
                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()
                _currentMediaItem.value = mediaItem
                Log.d(TAG, "Playback started")
            } catch (e: Exception) {
                Log.e(TAG, "Error playing track", e)
            }
        }
    }

    /**
     * Set playlist for navigation
     */
    fun setPlaylist(filePaths: List<String>, startIndex: Int = 0) {
        Log.d(TAG, "setPlaylist called with ${filePaths.size} tracks, startIndex: $startIndex")

        try {
            val mediaItems = filePaths.map { filePath ->
                val uri = if (filePath.startsWith("content://")) {
                    Uri.parse(filePath)
                } else {
                    Uri.fromFile(File(filePath))
                }
                MediaItem.fromUri(uri)
            }

            mediaController?.let { controller ->
                controller.setMediaItems(mediaItems, startIndex, 0)
                controller.prepare()
                controller.play()
                Log.d(TAG, "Playlist set successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting playlist", e)
        }
    }

    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun skipToNext() {
        Log.d(TAG, "skipToNext called")
        mediaController?.let { controller ->
            if (controller.hasNextMediaItem()) {
                controller.seekToNextMediaItem()
            } else {
                // Fin de playlist
                onPlaylistEnded?.invoke()
            }
        }
    }

    fun skipToPrevious() {
        Log.d(TAG, "skipToPrevious called")
        mediaController?.let { controller ->
            if (controller.currentPosition > 3000) {
                controller.seekTo(0)
            } else if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
            }
        }
    }

    fun release() {
        Log.d(TAG, "Releasing MediaController")
        stopPositionUpdates()
        scope.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
    }
}