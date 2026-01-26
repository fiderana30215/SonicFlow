package com.sonicflow.domain.model

/**
 * Domain model representing a playlist
 */
data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList()
)
