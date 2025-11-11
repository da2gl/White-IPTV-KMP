package com.simplevideo.whiteiptv.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation routes for WhiteIPTV app
 *
 * Using Kotlin Serialization for type-safe navigation
 *
 * TODO: Add routes for other features as they are implemented
 * TODO: Add route parameters (e.g., channelId, playlistId) when needed
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
    sealed class Main : Route() {
        /**
         * Home screen
         */
        @Serializable
        data object Home : Main()

        /**
         * Favorites screen
         */
        @Serializable
        data object Favorites : Main()

        /**
         * Categories screen
         */
        @Serializable
        data object Categories : Main()

        /**
         * Settings screen
         */
        @Serializable
        data object Settings : Main()
    }
}
