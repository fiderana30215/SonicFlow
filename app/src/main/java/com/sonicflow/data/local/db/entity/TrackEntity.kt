package com.sonicflow.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an audio track
 */
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val album: String?,
    val duration: Long,
    val filePath: String,
    val albumArtUri: String?,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val mimeType: String?
)
