package com.simplevideo.whiteiptv.feature.onboarding.mvi

data class OnboardingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val playlistUrl: String = "",
    val playlistFileName: String? = null,
    val isValidUrl: Boolean = false,
)
