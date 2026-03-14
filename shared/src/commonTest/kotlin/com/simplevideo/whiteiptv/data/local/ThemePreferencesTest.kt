package com.simplevideo.whiteiptv.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemePreferencesTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var themePreferences: ThemePreferences

    @BeforeTest
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { "test_theme_${kotlin.random.Random.nextInt()}.preferences_pb".toPath() },
        )
        themePreferences = ThemePreferences(dataStore)
    }

    @Test
    fun `default theme mode is System`() = runTest {
        assertEquals(ThemeMode.System, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode Light persists and reads back`() = runTest {
        themePreferences.setThemeMode(ThemeMode.Light)
        assertEquals(ThemeMode.Light, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode Dark persists and reads back`() = runTest {
        themePreferences.setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode System persists and reads back`() = runTest {
        themePreferences.setThemeMode(ThemeMode.Dark)
        themePreferences.setThemeMode(ThemeMode.System)
        assertEquals(ThemeMode.System, themePreferences.getThemeMode())
    }

    @Test
    fun `switching between all modes preserves last set value`() = runTest {
        themePreferences.setThemeMode(ThemeMode.Light)
        themePreferences.setThemeMode(ThemeMode.Dark)
        themePreferences.setThemeMode(ThemeMode.Light)
        assertEquals(ThemeMode.Light, themePreferences.getThemeMode())
    }

    @Test
    fun `themeModeFlow emits updates reactively`() = runTest {
        assertEquals(ThemeMode.System, themePreferences.themeModeFlow.first())
        themePreferences.setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, themePreferences.themeModeFlow.first())
    }

    @Test
    fun `new ThemePreferences instance reads previously persisted value`() = runTest {
        themePreferences.setThemeMode(ThemeMode.Dark)

        val newPreferences = ThemePreferences(dataStore)
        assertEquals(ThemeMode.Dark, newPreferences.getThemeMode())
    }
}
