package com.simplevideo.whiteiptv.data.local

import com.russhwolf.settings.Settings
import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode

/**
 * Manages non-theme settings persistence using multiplatform-settings.
 * Theme settings are handled by [ThemePreferences].
 */
class SettingsPreferences(private val settings: Settings) {

    fun getAccentColor(): AccentColor {
        val name = settings.getString(KEY_ACCENT_COLOR, AccentColor.Teal.name)
        return runCatching { AccentColor.valueOf(name) }.getOrDefault(AccentColor.Teal)
    }

    fun setAccentColor(color: AccentColor) {
        settings.putString(KEY_ACCENT_COLOR, color.name)
    }

    fun getChannelViewMode(): ChannelViewMode {
        val name = settings.getString(KEY_CHANNEL_VIEW_MODE, ChannelViewMode.List.name)
        return runCatching { ChannelViewMode.valueOf(name) }.getOrDefault(ChannelViewMode.List)
    }

    fun setChannelViewMode(mode: ChannelViewMode) {
        settings.putString(KEY_CHANNEL_VIEW_MODE, mode.name)
    }

    fun getAutoUpdateEnabled(): Boolean {
        return settings.getBoolean(KEY_AUTO_UPDATE, false)
    }

    fun setAutoUpdateEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_AUTO_UPDATE, enabled)
    }

    fun resetAll() {
        settings.clear()
    }

    companion object {
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_CHANNEL_VIEW_MODE = "channel_view_mode"
        private const val KEY_AUTO_UPDATE = "auto_update_playlists"
    }
}
