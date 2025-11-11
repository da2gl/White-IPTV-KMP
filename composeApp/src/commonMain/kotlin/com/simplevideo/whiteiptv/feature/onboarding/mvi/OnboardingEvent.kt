package com.simplevideo.whiteiptv.feature.onboarding.mvi

sealed interface OnboardingEvent {
    data class EnterPlaylistUrl(val url: String) : OnboardingEvent

    data object ChooseFile : OnboardingEvent
    data class FileSelected(val fileName: String, val fileUri: String) : OnboardingEvent

    data object ImportPlaylist : OnboardingEvent
    data object UseDemoPlaylist : OnboardingEvent
}
