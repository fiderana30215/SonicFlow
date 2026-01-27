package com.sonicflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sonicflow.ui.auth.SignInScreen
import com.sonicflow.ui.auth.SignUpScreen
import com.sonicflow.ui.library.LibraryScreen
import com.sonicflow.ui.player.PlayerScreen
import com.sonicflow.ui.playlist.PlaylistDetailScreen
import com.sonicflow.ui.playlist.PlaylistScreen

/**
 * Navigation graph for SonicFlow app
 */
@Composable
fun SonicFlowNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Library.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.SignIn.route) {
            SignInScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToLibrary = {
                    navController.navigate(Screen.Library.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToSignIn = {
                    navController.popBackStack()
                },
                onNavigateToLibrary = {
                    navController.navigate(Screen.Library.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main screens
        composable(Screen.Library.route) {
            LibraryScreen(
                onNavigateToPlayer = { trackId ->
                    navController.navigate(Screen.Player.createRoute(trackId))
                },
                onNavigateToPlaylist = {
                    navController.navigate(Screen.Playlist.route)
                }
            )
        }
        
        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("trackId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getLong("trackId") ?: 0L
            PlayerScreen(
                trackId = trackId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Playlist.route) {
            PlaylistScreen(
                onNavigateToPlaylistDetail = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            PlaylistDetailScreen(
                playlistId = playlistId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
