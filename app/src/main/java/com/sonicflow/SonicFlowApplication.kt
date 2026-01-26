package com.sonicflow

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for SonicFlow
 * Initializes Hilt for dependency injection
 */
@HiltAndroidApp
class SonicFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization
    }
}
