package com.simplevideo.whiteiptv.data.local

import com.russhwolf.settings.MapSettings
import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsPreferencesTest {

    private lateinit var settings: MapSettings
    private lateinit var settingsPreferences: SettingsPreferences

    @BeforeTest
    fun setUp() {
        settings = MapSettings()
        settingsPreferences = SettingsPreferences(settings)
    }

    // --- Accent Color ---

    @Test
    fun `default accent color is Teal`() {
        assertEquals(AccentColor.Teal, settingsPreferences.getAccentColor())
    }

    @Test
    fun `setAccentColor Blue persists and reads back`() {
        settingsPreferences.setAccentColor(AccentColor.Blue)
        assertEquals(AccentColor.Blue, settingsPreferences.getAccentColor())
    }

    @Test
    fun `setAccentColor Red persists and reads back`() {
        settingsPreferences.setAccentColor(AccentColor.Red)
        assertEquals(AccentColor.Red, settingsPreferences.getAccentColor())
    }

    @Test
    fun `invalid stored accent color falls back to Teal`() {
        settings.putString("accent_color", "invalid_value")
        assertEquals(AccentColor.Teal, settingsPreferences.getAccentColor())
    }

    // --- Channel View Mode ---

    @Test
    fun `default channel view mode is List`() {
        assertEquals(ChannelViewMode.List, settingsPreferences.getChannelViewMode())
    }

    @Test
    fun `setChannelViewMode Grid persists and reads back`() {
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        assertEquals(ChannelViewMode.Grid, settingsPreferences.getChannelViewMode())
    }

    @Test
    fun `setChannelViewMode List persists and reads back`() {
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        settingsPreferences.setChannelViewMode(ChannelViewMode.List)
        assertEquals(ChannelViewMode.List, settingsPreferences.getChannelViewMode())
    }

    @Test
    fun `invalid stored channel view mode falls back to List`() {
        settings.putString("channel_view_mode", "invalid_value")
        assertEquals(ChannelViewMode.List, settingsPreferences.getChannelViewMode())
    }

    // --- Auto Update ---

    @Test
    fun `default auto update is false`() {
        assertFalse(settingsPreferences.getAutoUpdateEnabled())
    }

    @Test
    fun `setAutoUpdateEnabled true persists and reads back`() {
        settingsPreferences.setAutoUpdateEnabled(true)
        assertTrue(settingsPreferences.getAutoUpdateEnabled())
    }

    @Test
    fun `setAutoUpdateEnabled false after true persists and reads back`() {
        settingsPreferences.setAutoUpdateEnabled(true)
        settingsPreferences.setAutoUpdateEnabled(false)
        assertFalse(settingsPreferences.getAutoUpdateEnabled())
    }

    // --- Reset All ---

    @Test
    fun `resetAll clears all settings to defaults`() {
        settingsPreferences.setAccentColor(AccentColor.Red)
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        settingsPreferences.setAutoUpdateEnabled(true)

        settingsPreferences.resetAll()

        assertEquals(AccentColor.Teal, settingsPreferences.getAccentColor())
        assertEquals(ChannelViewMode.List, settingsPreferences.getChannelViewMode())
        assertFalse(settingsPreferences.getAutoUpdateEnabled())
    }

    // --- Persistence across instances ---

    @Test
    fun `new SettingsPreferences instance reads previously persisted values`() {
        settingsPreferences.setAccentColor(AccentColor.Blue)
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        settingsPreferences.setAutoUpdateEnabled(true)

        val newPreferences = SettingsPreferences(settings)
        assertEquals(AccentColor.Blue, newPreferences.getAccentColor())
        assertEquals(ChannelViewMode.Grid, newPreferences.getChannelViewMode())
        assertTrue(newPreferences.getAutoUpdateEnabled())
    }
}
