package com.djoudini.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.djoudini.player.ui.dashboard.DashboardScreen
import com.djoudini.player.ui.epg.EpgScreen
import com.djoudini.player.ui.onboarding.OnboardingScreen
import com.djoudini.player.ui.player.PlayerScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Epg : Screen("epg")
    object Player : Screen("player")
    // Add more screens here like Settings, VOD List etc.
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onLoginSuccess = {
                    // Navigate to dashboard and remove onboarding from backstack
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToLive = { navController.navigate(Screen.Epg.route) },
                onNavigateToVod = { /* Add navigation to VOD list */ },
                onNavigateToSeries = { /* Add navigation to Series list */ },
                onNavigateToSettings = { /* Add navigation to Settings */ }
            )
        }
        
        composable(Screen.Epg.route) {
            EpgScreen(
                onChannelClick = { channel ->
                    // Normally pass channel stream URL here to the player
                    navController.navigate(Screen.Player.route) 
                }
            )
        }
        
        composable(Screen.Player.route) {
            // For now, playing a dummy stream
            PlayerScreen()
        }
    }
}
