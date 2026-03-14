package com.simplevideo.whiteiptv.feature.settings.mvi

import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import com.simplevideo.whiteiptv.domain.model.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val accentColor: AccentColor = AccentColor.Teal,
    val channelViewMode: ChannelViewMode = ChannelViewMode.List,
    val autoUpdateEnabled: Boolean = false,
    val cacheSize: String = "0 B",
    val appVersion: String = "",
    val showClearFavoritesDialog: Boolean = false,
    val showResetDialog: Boolean = false,
)

sealed interface SettingsEvent {
    data class OnThemeModeChanged(val mode: ThemeMode) : SettingsEvent
    data class OnAccentColorChanged(val color: AccentColor) : SettingsEvent
    data class OnChannelViewModeChanged(val mode: ChannelViewMode) : SettingsEvent
    data class OnAutoUpdateChanged(val enabled: Boolean) : SettingsEvent
    data object OnClearCacheClick : SettingsEvent
    data object OnClearFavoritesClick : SettingsEvent
    data object OnClearFavoritesConfirm : SettingsEvent
    data object OnResetClick : SettingsEvent
    data object OnResetConfirm : SettingsEvent
    data object OnDismissDialog : SettingsEvent
    data object OnContactSupportClick : SettingsEvent
    data object OnPrivacyPolicyClick : SettingsEvent
}

sealed interface SettingsAction {
    data object ShowCacheCleared : SettingsAction
    data object ShowFavoritesCleared : SettingsAction
    data object ShowSettingsReset : SettingsAction
    data class OpenUrl(val url: String) : SettingsAction
    data class OpenEmail(val email: String) : SettingsAction
}
