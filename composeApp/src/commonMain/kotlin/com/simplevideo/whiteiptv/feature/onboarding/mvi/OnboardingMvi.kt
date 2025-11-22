package com.simplevideo.whiteiptv.feature.onboarding.mvi

data class OnboardingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val playlistUrl: String = "",
    val playlistFileName: String? = null,
    val isValidUrl: Boolean = false,
)

sealed interface OnboardingEvent {
    data class EnterPlaylistUrl(val url: String) : OnboardingEvent
    data object ChooseFile : OnboardingEvent
    data class FileSelected(val fileName: String, val fileUri: String) : OnboardingEvent
    data object ImportPlaylist : OnboardingEvent
    data object UseDemoPlaylist : OnboardingEvent
}

sealed interface OnboardingAction {
    data object NavigateToMain : OnboardingAction
    data object ShowFilePicker : OnboardingAction
}
