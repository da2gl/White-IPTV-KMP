package com.simplevideo.whiteiptv.data.repository

import com.russhwolf.settings.MapSettings
import com.simplevideo.whiteiptv.data.local.ThemePreferences
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeRepositoryImplTest {

    private lateinit var settings: MapSettings
    private lateinit var themePreferences: ThemePreferences
    private lateinit var repository: ThemeRepositoryImpl

    @BeforeTest
    fun setUp() {
        settings = MapSettings()
        themePreferences = ThemePreferences(settings)
        repository = ThemeRepositoryImpl(themePreferences)
    }

    @Test
    fun `initial themeMode is System when no preference stored`() {
        assertEquals(ThemeMode.System, repository.themeMode.value)
    }

    @Test
    fun `initial themeMode reflects previously stored preference`() {
        themePreferences.setThemeMode(ThemeMode.Dark)
        val repo = ThemeRepositoryImpl(themePreferences)
        assertEquals(ThemeMode.Dark, repo.themeMode.value)
    }

    @Test
    fun `setThemeMode updates StateFlow value`() {
        repository.setThemeMode(ThemeMode.Light)
        assertEquals(ThemeMode.Light, repository.themeMode.value)
    }

    @Test
    fun `setThemeMode persists to preferences`() {
        repository.setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode updates both StateFlow and preferences`() {
        repository.setThemeMode(ThemeMode.Light)

        assertEquals(ThemeMode.Light, repository.themeMode.value)
        assertEquals(ThemeMode.Light, themePreferences.getThemeMode())
    }

    @Test
    fun `multiple setThemeMode calls update to latest value`() {
        repository.setThemeMode(ThemeMode.Light)
        repository.setThemeMode(ThemeMode.Dark)
        repository.setThemeMode(ThemeMode.System)

        assertEquals(ThemeMode.System, repository.themeMode.value)
        assertEquals(ThemeMode.System, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode to same value is idempotent`() {
        repository.setThemeMode(ThemeMode.Dark)
        repository.setThemeMode(ThemeMode.Dark)

        assertEquals(ThemeMode.Dark, repository.themeMode.value)
        assertEquals(ThemeMode.Dark, themePreferences.getThemeMode())
    }
}
