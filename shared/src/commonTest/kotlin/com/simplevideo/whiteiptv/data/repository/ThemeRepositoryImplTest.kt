package com.simplevideo.whiteiptv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.simplevideo.whiteiptv.data.local.ThemePreferences
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeRepositoryImplTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var themePreferences: ThemePreferences
    private lateinit var repository: ThemeRepositoryImpl

    @BeforeTest
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { "test_theme_repo_${kotlin.random.Random.nextInt()}.preferences_pb".toPath() },
        )
        themePreferences = ThemePreferences(dataStore)
    }

    @Test
    fun `initial themeMode is System when no preference stored`() = runTest {
        repository = ThemeRepositoryImpl(themePreferences)
        assertEquals(ThemeMode.System, repository.themeMode.value)
    }

    @Test
    fun `setThemeMode updates StateFlow value`() = runTest {
        repository = ThemeRepositoryImpl(themePreferences)
        repository.setThemeMode(ThemeMode.Light)
        assertEquals(ThemeMode.Light, repository.themeMode.value)
    }

    @Test
    fun `setThemeMode persists to preferences`() = runTest {
        repository = ThemeRepositoryImpl(themePreferences)
        repository.setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode updates both StateFlow and preferences`() = runTest {
        repository = ThemeRepositoryImpl(themePreferences)
        repository.setThemeMode(ThemeMode.Light)

        assertEquals(ThemeMode.Light, repository.themeMode.value)
        assertEquals(ThemeMode.Light, themePreferences.getThemeMode())
    }

    @Test
    fun `multiple setThemeMode calls update to latest value`() = runTest {
        repository = ThemeRepositoryImpl(themePreferences)
        repository.setThemeMode(ThemeMode.Light)
        repository.setThemeMode(ThemeMode.Dark)
        repository.setThemeMode(ThemeMode.System)

        assertEquals(ThemeMode.System, repository.themeMode.value)
        assertEquals(ThemeMode.System, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode to same value is idempotent`() = runTest {
        repository = ThemeRepositoryImpl(themePreferences)
        repository.setThemeMode(ThemeMode.Dark)
        repository.setThemeMode(ThemeMode.Dark)

        assertEquals(ThemeMode.Dark, repository.themeMode.value)
        assertEquals(ThemeMode.Dark, themePreferences.getThemeMode())
    }

    @Test
    fun `initial themeMode reflects previously stored preference`() = runTest {
        themePreferences.setThemeMode(ThemeMode.Dark)
        repository = ThemeRepositoryImpl(themePreferences)
        advanceUntilIdle()
        assertEquals(ThemeMode.Dark, repository.themeMode.value)
    }
}
