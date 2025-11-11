package com.simplevideo.whiteiptv.feature.onboarding.mvi

sealed interface OnboardingAction {
    data object NavigateToMain : OnboardingAction
    data object ShowFilePicker : OnboardingAction
}
