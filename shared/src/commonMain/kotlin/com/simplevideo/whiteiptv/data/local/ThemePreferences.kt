package com.simplevideo.whiteiptv.data.local

import com.russhwolf.settings.Settings
import com.simplevideo.whiteiptv.domain.model.ThemeMode

/**
 * Manages theme preference persistence using multiplatform-settings.
 */
class ThemePreferences(private val settings: Settings) {

    fun getThemeMode(): ThemeMode {
        return when (settings.getString(KEY_THEME_MODE, DEFAULT_THEME)) {
            VALUE_LIGHT -> ThemeMode.Light
            VALUE_DARK -> ThemeMode.Dark
            else -> ThemeMode.System
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        val value = when (mode) {
            ThemeMode.System -> VALUE_SYSTEM
            ThemeMode.Light -> VALUE_LIGHT
            ThemeMode.Dark -> VALUE_DARK
        }
        settings.putString(KEY_THEME_MODE, value)
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val VALUE_SYSTEM = "system"
        private const val VALUE_LIGHT = "light"
        private const val VALUE_DARK = "dark"
        private const val DEFAULT_THEME = VALUE_SYSTEM
    }
}
