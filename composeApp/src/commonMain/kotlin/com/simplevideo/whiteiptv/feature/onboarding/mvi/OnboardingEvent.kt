package com.simplevideo.whiteiptv.feature.onboarding.mvi

/**
 * User events/intents for Onboarding
 *
 * Represents all possible user interactions in the onboarding screen
 *
 * TODO: Add validation events when implementing URL format validation
 */
sealed interface OnboardingEvent {
    // Playlist URL input
    data class EnterPlaylistUrl(val url: String) : OnboardingEvent

    // File picker
    data object ChooseFile : OnboardingEvent
    data class FileSelected(val fileName: String, val fileUri: String) : OnboardingEvent

    // Import actions
    data object ImportPlaylist : OnboardingEvent
    data object UseDemoPlaylist : OnboardingEvent

    // Error handling
    data object DismissError : OnboardingEvent
}
