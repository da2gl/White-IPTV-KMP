package com.simplevideo.whiteiptv.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simplevideo.whiteiptv.feature.main.MainScreen
import com.simplevideo.whiteiptv.feature.onboarding.OnboardingScreen
import com.simplevideo.whiteiptv.feature.splash.SplashScreen
import com.simplevideo.whiteiptv.feature.favorites.FavoritesScreen
import org.koin.compose.koinInject

/**
 * Main navigation graph for WhiteIPTV app
 *
 * Sets up the navigation structure with splash as the initial destination
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.Splash,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // Splash screen
        composable<Route.Splash> {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                },
            )
        }

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
            MainScreen()
        }

        // Favorites screen
        composable<Route.Favorites> {
            FavoritesScreen(
                viewModel = koinInject()
            )
        }
    }
}
