package com.simplevideo.whiteiptv.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation routes for WhiteIPTV app
 *
 * Using Kotlin Serialization for type-safe navigation
 */

/**
 * Root level routes
 */
@Serializable
sealed class Route {
    /**
     * Splash screen - initial loading and route determination
     */
    @Serializable
    data object Splash : Route()

    /**
     * Onboarding screen - playlist import
     */
    @Serializable
    data object Onboarding : Route()

    /**
     * Main app screen with bottom navigation
     */
    @Serializable
    data object Main : Route()

    /**
     * Main screen tabs
     */
    @Serializable
    sealed class MainTab {
        /**
         * Home screen
         */
        @Serializable
        data object Home : MainTab()

        /**
         * Favorites screen
         */
        @Serializable
        data object Favorites : MainTab()

        /**
         * Channels screen
         */
        @Serializable
        data object Channels : MainTab()

        /**
         * Settings screen
         */
        @Serializable
        data object Settings : MainTab()
    }
}
