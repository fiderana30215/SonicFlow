package com.sonicflow.ui.navigation

/**
 * Sealed class representing all screens in the app
 */
sealed class Screen(val route: String) {
    
    // Auth screens
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    
    // Main screens
    object Library : Screen("library")
    object Player : Screen("player/{trackId}") {
        fun createRoute(trackId: Long) = "player/$trackId"
    }
    object Playlist : Screen("playlist")
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
}
