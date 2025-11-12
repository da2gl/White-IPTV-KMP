package com.simplevideo.whiteiptv.feature.settings.mvi

sealed interface SettingsEvent {
    // Appearance
    data object OnThemeClicked : SettingsEvent
    data object OnAccentColorClicked : SettingsEvent
    data object OnChannelViewClicked : SettingsEvent

    // Playback
    data object OnDefaultPlayerClicked : SettingsEvent
    data object OnPreferredQualityClicked : SettingsEvent

    // App Behavior
    data object OnDefaultPlaylistClicked : SettingsEvent
    data object OnLanguageClicked : SettingsEvent
    data class OnAutoUpdatePlaylistsToggled(val isEnabled: Boolean) : SettingsEvent

    // Data & Storage
    data object OnClearCacheClicked : SettingsEvent
    data object OnClearFavoritesClicked : SettingsEvent
    data object OnResetToDefaultsClicked : SettingsEvent

    // About
    data object OnContactSupportClicked : SettingsEvent
    data object OnPrivacyPolicyClicked : SettingsEvent

    // Navigation
    data object OnBackClicked : SettingsEvent
}
