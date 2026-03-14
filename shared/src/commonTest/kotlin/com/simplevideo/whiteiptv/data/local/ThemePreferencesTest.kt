package com.simplevideo.whiteiptv.data.local

import com.russhwolf.settings.MapSettings
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemePreferencesTest {

    private lateinit var settings: MapSettings
    private lateinit var themePreferences: ThemePreferences

    @BeforeTest
    fun setUp() {
        settings = MapSettings()
        themePreferences = ThemePreferences(settings)
    }

    @Test
    fun `default theme mode is System`() {
        assertEquals(ThemeMode.System, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode Light persists and reads back`() {
        themePreferences.setThemeMode(ThemeMode.Light)
        assertEquals(ThemeMode.Light, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode Dark persists and reads back`() {
        themePreferences.setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode System persists and reads back`() {
        themePreferences.setThemeMode(ThemeMode.Dark)
        themePreferences.setThemeMode(ThemeMode.System)
        assertEquals(ThemeMode.System, themePreferences.getThemeMode())
    }

    @Test
    fun `switching between all modes preserves last set value`() {
        themePreferences.setThemeMode(ThemeMode.Light)
        themePreferences.setThemeMode(ThemeMode.Dark)
        themePreferences.setThemeMode(ThemeMode.Light)
        assertEquals(ThemeMode.Light, themePreferences.getThemeMode())
    }

    @Test
    fun `unknown stored value falls back to System`() {
        settings.putString("theme_mode", "invalid_value")
        assertEquals(ThemeMode.System, themePreferences.getThemeMode())
    }

    @Test
    fun `empty stored value falls back to System`() {
        settings.putString("theme_mode", "")
        assertEquals(ThemeMode.System, themePreferences.getThemeMode())
    }

    @Test
    fun `new ThemePreferences instance reads previously persisted value`() {
        themePreferences.setThemeMode(ThemeMode.Dark)

        val newPreferences = ThemePreferences(settings)
        assertEquals(ThemeMode.Dark, newPreferences.getThemeMode())
    }
}
