package com.sonicflow.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.sonicflow.data.local.db.dao.TrackDao
import com.sonicflow.data.local.db.entity.TrackEntity
import com.sonicflow.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing track data
 */
@Singleton
class TrackRepository @Inject constructor(
    private val trackDao: TrackDao,
    @ApplicationContext private val context: Context
) {
    
    /**
     * Get all tracks from database
     */
    fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Get a specific track by ID
     */
    fun getTrackById(trackId: Long): Flow<Track?> {
        return trackDao.getTrackById(trackId).map { it?.toDomain() }
    }
    
    /**
     * Search tracks
     */
    fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Scan and load tracks from device using MediaStore
     */
    suspend fun scanAndLoadTracks() {
        val tracks = mutableListOf<TrackEntity>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn)
                val duration = cursor.getLong(durationColumn)
                val filePath = cursor.getString(dataColumn) ?: ""
                val albumId = cursor.getLong(albumIdColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val dateModified = cursor.getLong(dateModifiedColumn)
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeTypeColumn)
                
                // Build album art URI
                val albumArtUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    albumId
                ).toString()
                
                tracks.add(
                    TrackEntity(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        filePath = filePath,
                        albumArtUri = albumArtUri,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        size = size,
                        mimeType = mimeType
                    )
                )
            }
        }
        
        trackDao.insertTracks(tracks)
    }
    
    /**
     * Convert TrackEntity to domain Track
     */
    private fun TrackEntity.toDomain() = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        filePath = filePath,
        albumArtUri = albumArtUri,
        dateAdded = dateAdded,
        dateModified = dateModified,
        size = size,
        mimeType = mimeType
    )
}
