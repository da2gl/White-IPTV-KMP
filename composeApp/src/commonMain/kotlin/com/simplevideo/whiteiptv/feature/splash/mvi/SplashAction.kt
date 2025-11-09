package com.simplevideo.whiteiptv.feature.splash.mvi

import com.simplevideo.whiteiptv.navigation.Route

/**
 * One-time actions for Splash screen
 */
sealed interface SplashAction {
    /**
     * Navigate to the determined initial route
     */
    data class Navigate(val route: Route) : SplashAction
}
