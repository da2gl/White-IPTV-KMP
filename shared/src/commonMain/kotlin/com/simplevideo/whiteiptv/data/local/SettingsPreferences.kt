package com.simplevideo.whiteiptv.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Manages non-theme settings persistence using Jetpack DataStore.
 * Theme settings are handled by [ThemePreferences].
 */
class SettingsPreferences(private val dataStore: DataStore<Preferences>) {

    val autoUpdateEnabledFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[AUTO_UPDATE_KEY] ?: false }
        .distinctUntilChanged()

    suspend fun getAccentColor(): AccentColor {
        val prefs = dataStore.data.first()
        val name = prefs[ACCENT_COLOR_KEY] ?: AccentColor.Teal.name
        return runCatching { AccentColor.valueOf(name) }.getOrDefault(AccentColor.Teal)
    }

    suspend fun setAccentColor(color: AccentColor) {
        dataStore.edit { prefs -> prefs[ACCENT_COLOR_KEY] = color.name }
    }

    suspend fun getChannelViewMode(): ChannelViewMode {
        val prefs = dataStore.data.first()
        val name = prefs[CHANNEL_VIEW_MODE_KEY] ?: ChannelViewMode.List.name
        return runCatching { ChannelViewMode.valueOf(name) }.getOrDefault(ChannelViewMode.List)
    }

    suspend fun setChannelViewMode(mode: ChannelViewMode) {
        dataStore.edit { prefs -> prefs[CHANNEL_VIEW_MODE_KEY] = mode.name }
    }

    suspend fun getAutoUpdateEnabled(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[AUTO_UPDATE_KEY] ?: false
    }

    suspend fun setAutoUpdateEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[AUTO_UPDATE_KEY] = enabled }
    }

    suspend fun resetAll() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
        private val CHANNEL_VIEW_MODE_KEY = stringPreferencesKey("channel_view_mode")
        private val AUTO_UPDATE_KEY = booleanPreferencesKey("auto_update_playlists")
    }
}
