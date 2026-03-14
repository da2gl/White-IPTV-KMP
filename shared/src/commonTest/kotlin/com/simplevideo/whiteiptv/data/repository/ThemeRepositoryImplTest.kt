package com.simplevideo.whiteiptv.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.simplevideo.whiteiptv.data.local.ThemePreferences
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeRepositoryImplTest {

    private fun createDataStore(scope: kotlinx.coroutines.CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            scope = scope,
            produceFile = { "test_theme_repo_${kotlin.random.Random.nextInt()}.preferences_pb".toPath() },
        )

    @Test
    fun `initial themeMode is System when no preference stored`() = runTest {
        val dataStore = createDataStore(this)
        val themePreferences = ThemePreferences(dataStore)
        val repository = ThemeRepositoryImpl(themePreferences, scope = backgroundScope)
        assertEquals(ThemeMode.System, repository.themeMode.value)
    }

    @Test
    fun `setThemeMode updates StateFlow value`() = runTest {
        val dataStore = createDataStore(this)
        val themePreferences = ThemePreferences(dataStore)
        val repository = ThemeRepositoryImpl(themePreferences, scope = backgroundScope)
        repository.setThemeMode(ThemeMode.Light)
        assertEquals(ThemeMode.Light, repository.themeMode.value)
    }

    @Test
    fun `setThemeMode persists to preferences`() = runTest {
        val dataStore = createDataStore(this)
        val themePreferences = ThemePreferences(dataStore)
        val repository = ThemeRepositoryImpl(themePreferences, scope = backgroundScope)
        repository.setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode updates both StateFlow and preferences`() = runTest {
        val dataStore = createDataStore(this)
        val themePreferences = ThemePreferences(dataStore)
        val repository = ThemeRepositoryImpl(themePreferences, scope = backgroundScope)
        repository.setThemeMode(ThemeMode.Light)

        assertEquals(ThemeMode.Light, repository.themeMode.value)
        assertEquals(ThemeMode.Light, themePreferences.getThemeMode())
    }

    @Test
    fun `multiple setThemeMode calls update to latest value`() = runTest {
        val dataStore = createDataStore(this)
        val themePreferences = ThemePreferences(dataStore)
        val repository = ThemeRepositoryImpl(themePreferences, scope = backgroundScope)
        repository.setThemeMode(ThemeMode.Light)
        repository.setThemeMode(ThemeMode.Dark)
        repository.setThemeMode(ThemeMode.System)

        assertEquals(ThemeMode.System, repository.themeMode.value)
        assertEquals(ThemeMode.System, themePreferences.getThemeMode())
    }

    @Test
    fun `setThemeMode to same value is idempotent`() = runTest {
        val dataStore = createDataStore(this)
        val themePreferences = ThemePreferences(dataStore)
        val repository = ThemeRepositoryImpl(themePreferences, scope = backgroundScope)
        repository.setThemeMode(ThemeMode.Dark)
        repository.setThemeMode(ThemeMode.Dark)

        assertEquals(ThemeMode.Dark, repository.themeMode.value)
        assertEquals(ThemeMode.Dark, themePreferences.getThemeMode())
    }

    @Test
    fun `initial themeMode reflects previously stored preference`() = runTest {
        val dataStore = createDataStore(this)
        val themePreferences = ThemePreferences(dataStore)
        themePreferences.setThemeMode(ThemeMode.Dark)
        advanceUntilIdle()

        // Create repository with a child scope that shares the test scheduler
        // but has its own SupervisorJob so cancellation doesn't kill the test
        val repoJob = kotlinx.coroutines.SupervisorJob(coroutineContext[kotlinx.coroutines.Job])
        val repoScope = kotlinx.coroutines.CoroutineScope(coroutineContext + repoJob)
        val repository = ThemeRepositoryImpl(themePreferences, scope = repoScope)
        advanceUntilIdle()
        assertEquals(ThemeMode.Dark, repository.themeMode.value)
        repoJob.cancel()
        advanceUntilIdle()
    }
}
