package com.simplevideo.whiteiptv.feature.settings

import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsAction
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsEvent
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsState

class SettingsViewModel : BaseViewModel<SettingsState, SettingsAction, SettingsEvent>(
    initialState = SettingsState()
) {
    override fun obtainEvent(viewEvent: SettingsEvent) {
        when (viewEvent) {
            SettingsEvent.OnBackClicked -> viewAction = SettingsAction.NavigateBack

            // Appearance
            SettingsEvent.OnThemeClicked -> { /* TODO: Implement theme selection */ }
            SettingsEvent.OnAccentColorClicked -> { /* TODO: Implement accent color selection */ }
            SettingsEvent.OnChannelViewClicked -> { /* TODO: Implement channel view selection */ }

            // Playback
            SettingsEvent.OnDefaultPlayerClicked -> { /* TODO: Implement default player selection */ }
            SettingsEvent.OnPreferredQualityClicked -> { /* TODO: Implement preferred quality selection */ }

            // App Behavior
            SettingsEvent.OnDefaultPlaylistClicked -> { /* TODO: Implement default playlist selection */ }
            SettingsEvent.OnLanguageClicked -> { /* TODO: Implement language selection */ }
            is SettingsEvent.OnAutoUpdatePlaylistsToggled -> {
                viewState = viewState.copy(autoUpdatePlaylists = viewEvent.isEnabled)
            }

            // Data & Storage
            SettingsEvent.OnClearCacheClicked -> { /* TODO: Implement clear cache */ }
            SettingsEvent.OnClearFavoritesClicked -> { /* TODO: Implement clear favorites */ }
            SettingsEvent.OnResetToDefaultsClicked -> { /* TODO: Implement reset to defaults */ }

            // About
            SettingsEvent.OnContactSupportClicked -> { /* TODO: Implement contact support */ }
            SettingsEvent.OnPrivacyPolicyClicked -> { /* TODO: Implement privacy policy */ }
        }
    }
}
