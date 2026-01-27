package com.sonicflow.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonicflow.data.local.db.dao.PlaylistDao
import com.sonicflow.data.local.db.dao.TrackDao
import com.sonicflow.data.local.db.entity.PlaylistEntity
import com.sonicflow.data.local.db.entity.PlaylistTrackCrossRef
import com.sonicflow.data.local.db.entity.TrackEntity
import com.sonicflow.data.local.db.entity.WaveformEntity

/**
 * Room database for SonicFlow application
 */
@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        WaveformEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SonicFlowDatabase : RoomDatabase() {
    
    /**
     * Provides access to TrackDao
     */
    abstract fun trackDao(): TrackDao
    
    /**
     * Provides access to PlaylistDao
     */
    abstract fun playlistDao(): PlaylistDao
}
