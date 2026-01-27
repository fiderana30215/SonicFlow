package com.sonicflow.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for storing waveform amplitude data for tracks
 */
@Entity(
    tableName = "waveforms",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trackId", unique = true)]
)
data class WaveformEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackId: Long,
    val amplitudes: String, // JSON array of amplitude values
    val sampleRate: Int,
    val generatedAt: Long = System.currentTimeMillis()
)
