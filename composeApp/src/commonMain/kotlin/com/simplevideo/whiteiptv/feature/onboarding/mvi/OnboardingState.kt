package com.simplevideo.whiteiptv.feature.onboarding.mvi

/**
 * UI State for Onboarding screen
 *
 * Simple onboarding with playlist import
 *
 * TODO: Add validation states for playlist URL format
 * TODO: Add support for different playlist formats (M3U, M3U8)
 */
data class OnboardingState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // Playlist import
    val playlistUrl: String = "",
    val playlistFileName: String? = null,
    val isValidUrl: Boolean = false,
)
