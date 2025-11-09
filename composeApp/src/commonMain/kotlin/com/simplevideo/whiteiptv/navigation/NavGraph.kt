package com.simplevideo.whiteiptv.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simplevideo.whiteiptv.feature.onboarding.OnboardingScreen

/**
 * Main navigation graph for WhiteIPTV app
 *
 * Sets up the navigation structure with onboarding as the initial destination
 *
 * TODO: Add main app navigation when features are implemented
 * TODO: Implement navigation animations/transitions
 * TODO: Handle deep links if needed
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.Onboarding,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // Onboarding screen
        composable<Route.Onboarding> {
            OnboardingScreen(
                onNavigateToMain = {
                    navController.navigate(Route.Main) {
                        popUpTo(Route.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        // Main app screen
        composable<Route.Main> {
            // TODO: Implement main app screen with bottom navigation
            // This will contain Home, AllChannels, Favorites, Settings tabs
        }
    }
}
