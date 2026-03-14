package com.simplevideo.whiteiptv.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Manages theme preference persistence using Jetpack DataStore.
 */
class ThemePreferences(private val dataStore: DataStore<Preferences>) {

    val themeModeFlow: Flow<ThemeMode> = dataStore.data
        .map { prefs -> mapToThemeMode(prefs[THEME_MODE_KEY]) }
        .distinctUntilChanged()

    suspend fun getThemeMode(): ThemeMode {
        val prefs = dataStore.data.first()
        return mapToThemeMode(prefs[THEME_MODE_KEY])
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        val value = when (mode) {
            ThemeMode.System -> VALUE_SYSTEM
            ThemeMode.Light -> VALUE_LIGHT
            ThemeMode.Dark -> VALUE_DARK
        }
        dataStore.edit { prefs -> prefs[THEME_MODE_KEY] = value }
    }

    private fun mapToThemeMode(value: String?): ThemeMode = when (value) {
        VALUE_LIGHT -> ThemeMode.Light
        VALUE_DARK -> ThemeMode.Dark
        else -> ThemeMode.System
    }

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private const val VALUE_SYSTEM = "system"
        private const val VALUE_LIGHT = "light"
        private const val VALUE_DARK = "dark"
    }
}
