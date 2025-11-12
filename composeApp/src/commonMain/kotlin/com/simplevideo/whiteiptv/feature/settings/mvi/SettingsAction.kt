package com.simplevideo.whiteiptv.feature.settings.mvi

sealed interface SettingsAction {
    data object NavigateBack : SettingsAction
}
