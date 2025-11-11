package com.simplevideo.whiteiptv.feature.onboarding.mvi

sealed interface OnboardingAction {
    data object NavigateToMain : OnboardingAction
    data class ShowError(val message: String) : OnboardingAction
    data class ShowSuccess(val message: String) : OnboardingAction
    data object ShowFilePicker : OnboardingAction
}
