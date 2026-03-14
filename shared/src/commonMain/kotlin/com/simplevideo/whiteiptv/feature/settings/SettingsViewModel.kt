package com.simplevideo.whiteiptv.feature.settings

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.data.local.SettingsPreferences
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import com.simplevideo.whiteiptv.domain.repository.ThemeRepository
import com.simplevideo.whiteiptv.domain.usecase.ClearFavoritesUseCase
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsAction
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsEvent
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsState
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val settingsPreferences: SettingsPreferences,
    private val clearFavoritesUseCase: ClearFavoritesUseCase,
) : BaseViewModel<SettingsState, SettingsAction, SettingsEvent>(
    initialState = SettingsState(),
) {

    init {
        viewState = viewState.copy(
            themeMode = themeRepository.themeMode.value,
            accentColor = settingsPreferences.getAccentColor(),
            channelViewMode = settingsPreferences.getChannelViewMode(),
            autoUpdateEnabled = settingsPreferences.getAutoUpdateEnabled(),
            appVersion = APP_VERSION,
        )
    }

    override fun obtainEvent(viewEvent: SettingsEvent) {
        when (viewEvent) {
            is SettingsEvent.OnThemeModeChanged -> {
                themeRepository.setThemeMode(viewEvent.mode)
                viewState = viewState.copy(themeMode = viewEvent.mode)
            }

            is SettingsEvent.OnAccentColorChanged -> {
                settingsPreferences.setAccentColor(viewEvent.color)
                viewState = viewState.copy(accentColor = viewEvent.color)
            }

            is SettingsEvent.OnChannelViewModeChanged -> {
                settingsPreferences.setChannelViewMode(viewEvent.mode)
                viewState = viewState.copy(channelViewMode = viewEvent.mode)
            }

            is SettingsEvent.OnAutoUpdateChanged -> {
                settingsPreferences.setAutoUpdateEnabled(viewEvent.enabled)
                viewState = viewState.copy(autoUpdateEnabled = viewEvent.enabled)
            }

            is SettingsEvent.OnClearCacheClick -> {
                viewAction = SettingsAction.ShowCacheCleared
            }

            is SettingsEvent.OnClearFavoritesClick -> {
                viewState = viewState.copy(showClearFavoritesDialog = true)
            }

            is SettingsEvent.OnClearFavoritesConfirm -> {
                viewState = viewState.copy(showClearFavoritesDialog = false)
                viewModelScope.launch {
                    runCatching { clearFavoritesUseCase() }
                    viewAction = SettingsAction.ShowFavoritesCleared
                }
            }

            is SettingsEvent.OnResetClick -> {
                viewState = viewState.copy(showResetDialog = true)
            }

            is SettingsEvent.OnResetConfirm -> {
                viewState = viewState.copy(showResetDialog = false)
                settingsPreferences.resetAll()
                themeRepository.setThemeMode(ThemeMode.System)
                viewState = SettingsState(
                    appVersion = APP_VERSION,
                )
                viewAction = SettingsAction.ShowSettingsReset
            }

            is SettingsEvent.OnDismissDialog -> {
                viewState = viewState.copy(
                    showClearFavoritesDialog = false,
                    showResetDialog = false,
                )
            }

            is SettingsEvent.OnContactSupportClick -> {
                viewAction = SettingsAction.OpenEmail(SUPPORT_EMAIL)
            }

            is SettingsEvent.OnPrivacyPolicyClick -> {
                viewAction = SettingsAction.OpenUrl(PRIVACY_POLICY_URL)
            }
        }
    }

    companion object {
        private const val APP_VERSION = "1.0"
        private const val SUPPORT_EMAIL = "mailto:support@simplevideo.com"
        private const val PRIVACY_POLICY_URL = "https://simplevideo.com/privacy"
    }
}
