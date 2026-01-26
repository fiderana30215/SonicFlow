package com.sonicflow.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playbackDataStore: DataStore<Preferences> by preferencesDataStore(name = "playback_preferences")

/**
 * DataStore-based preferences for playback state
 */
@Singleton
class PlaybackPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val CURRENT_TRACK_ID = longPreferencesKey("current_track_id")
        val PLAYBACK_POSITION = longPreferencesKey("playback_position")
        val IS_PLAYING = booleanPreferencesKey("is_playing")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
    }

    /**
     * Current track ID flow
     */
    val currentTrackId: Flow<Long?> = context.playbackDataStore.data.map { preferences ->
        preferences[PreferencesKeys.CURRENT_TRACK_ID]
    }

    /**
     * Playback position flow
     */
    val playbackPosition: Flow<Long> = context.playbackDataStore.data.map { preferences ->
        preferences[PreferencesKeys.PLAYBACK_POSITION] ?: 0L
    }

    /**
     * Is playing flow
     */
    val isPlaying: Flow<Boolean> = context.playbackDataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_PLAYING] ?: false
    }

    /**
     * Repeat mode flow (0: off, 1: all, 2: one)
     */
    val repeatMode: Flow<Int> = context.playbackDataStore.data.map { preferences ->
        preferences[PreferencesKeys.REPEAT_MODE] ?: 0
    }

    /**
     * Shuffle mode flow
     */
    val shuffleMode: Flow<Boolean> = context.playbackDataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHUFFLE_MODE] ?: false
    }

    /**
     * Playback speed flow
     */
    val playbackSpeed: Flow<Float> = context.playbackDataStore.data.map { preferences ->
        preferences[PreferencesKeys.PLAYBACK_SPEED] ?: 1.0f
    }

    /**
     * Save current track ID
     */
    suspend fun saveCurrentTrackId(trackId: Long?) {
        context.playbackDataStore.edit { preferences ->
            if (trackId != null) {
                preferences[PreferencesKeys.CURRENT_TRACK_ID] = trackId
            } else {
                preferences.remove(PreferencesKeys.CURRENT_TRACK_ID)
            }
        }
    }

    /**
     * Save playback position
     */
    suspend fun savePlaybackPosition(position: Long) {
        context.playbackDataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYBACK_POSITION] = position
        }
    }

    /**
     * Save playing state
     */
    suspend fun saveIsPlaying(isPlaying: Boolean) {
        context.playbackDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PLAYING] = isPlaying
        }
    }

    /**
     * Save repeat mode
     */
    suspend fun saveRepeatMode(mode: Int) {
        context.playbackDataStore.edit { preferences ->
            preferences[PreferencesKeys.REPEAT_MODE] = mode
        }
    }

    /**
     * Save shuffle mode
     */
    suspend fun saveShuffleMode(enabled: Boolean) {
        context.playbackDataStore.edit { preferences ->
            preferences[PreferencesKeys.SHUFFLE_MODE] = enabled
        }
    }

    /**
     * Save playback speed
     */
    suspend fun savePlaybackSpeed(speed: Float) {
        context.playbackDataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYBACK_SPEED] = speed
        }
    }
}
