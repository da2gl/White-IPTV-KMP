package com.simplevideo.whiteiptv.feature.onboarding

/**
 * One-time UI events (side effects) for Onboarding
 *
 * These are consumed by the UI layer and should be handled once
 *
 * TODO: Add analytics events if needed
 */
sealed interface OnboardingAction {
    /**
     * Navigate to main app (after successful import)
     */
    data object NavigateToMain : OnboardingAction

    /**
     * Show an error message to the user
     */
    data class ShowError(val message: String) : OnboardingAction

    /**
     * Show a success message
     */
    data class ShowSuccess(val message: String) : OnboardingAction

    /**
     * Show file picker for playlist file selection
     */
    data object ShowFilePicker : OnboardingAction
}
