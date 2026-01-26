package com.sonicflow.di

import android.content.Context
import androidx.room.Room
import com.sonicflow.data.local.db.SonicFlowDatabase
import com.sonicflow.data.local.db.dao.PlaylistDao
import com.sonicflow.data.local.db.dao.TrackDao
import com.sonicflow.data.repository.PlaylistRepository
import com.sonicflow.data.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing application-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room database instance
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SonicFlowDatabase {
        return Room.databaseBuilder(
            context,
            SonicFlowDatabase::class.java,
            "sonicflow_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides TrackDao for accessing track data
     */
    @Provides
    @Singleton
    fun provideTrackDao(database: SonicFlowDatabase): TrackDao {
        return database.trackDao()
    }

    /**
     * Provides PlaylistDao for accessing playlist data
     */
    @Provides
    @Singleton
    fun providePlaylistDao(database: SonicFlowDatabase): PlaylistDao {
        return database.playlistDao()
    }

    /**
     * Provides TrackRepository
     */
    @Provides
    @Singleton
    fun provideTrackRepository(
        trackDao: TrackDao,
        @ApplicationContext context: Context
    ): TrackRepository {
        return TrackRepository(trackDao, context)
    }

    /**
     * Provides PlaylistRepository
     */
    @Provides
    @Singleton
    fun providePlaylistRepository(
        playlistDao: PlaylistDao,
        trackDao: TrackDao
    ): PlaylistRepository {
        return PlaylistRepository(playlistDao, trackDao)
    }
}
