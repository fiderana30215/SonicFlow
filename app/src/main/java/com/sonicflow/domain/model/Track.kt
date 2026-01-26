package com.sonicflow.domain.model

/**
 * Domain model representing an audio track
 */
data class Track(
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
) {
    /**
     * Get formatted duration string (MM:SS)
     */
    fun getFormattedDuration(): String {
        val minutes = duration / 1000 / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
