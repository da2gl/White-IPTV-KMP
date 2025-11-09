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
     * Onboarding screen - playlist import
     */
    @Serializable
    data object Onboarding : Route()

    /**
     * Main app screen with bottom navigation
     *
     * TODO: Implement main app navigation
     */
    @Serializable
    data object Main : Route()

    /**
     * Home screen
     *
     * TODO: Implement when creating home feature
     */
    @Serializable
    data object Home : Route()

    /**
     * All channels screen
     *
     * TODO: Implement when creating channels feature
     */
    @Serializable
    data object AllChannels : Route()

    /**
     * Favorites screen
     *
     * TODO: Implement when creating favorites feature
     */
    @Serializable
    data object Favorites : Route()

    /**
     * Settings screen
     *
     * TODO: Implement when creating settings feature
     */
    @Serializable
    data object Settings : Route()
}
