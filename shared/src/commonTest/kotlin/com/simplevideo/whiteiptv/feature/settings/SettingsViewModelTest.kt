package com.simplevideo.whiteiptv.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.simplevideo.whiteiptv.AppConfig
import com.simplevideo.whiteiptv.data.local.SettingsPreferences
import com.simplevideo.whiteiptv.data.local.ThemePreferences
import com.simplevideo.whiteiptv.data.repository.FakeChannelRepository
import com.simplevideo.whiteiptv.data.repository.FakePlaylistRepository
import com.simplevideo.whiteiptv.data.repository.ThemeRepositoryImpl
import com.simplevideo.whiteiptv.data.scheduler.BackgroundRefreshCoordinator
import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import com.simplevideo.whiteiptv.domain.usecase.ClearFavoritesUseCase
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsAction
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsEvent
import com.simplevideo.whiteiptv.platform.BackgroundScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repositoryJob: kotlinx.coroutines.CompletableJob

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var themePreferences: ThemePreferences
    private lateinit var themeRepository: ThemeRepositoryImpl
    private lateinit var settingsPreferences: SettingsPreferences
    private lateinit var fakeChannelRepository: FakeChannelRepository
    private lateinit var clearFavoritesUseCase: ClearFavoritesUseCase
    private lateinit var fakeBackgroundScheduler: FakeBackgroundScheduler
    private lateinit var refreshCoordinator: BackgroundRefreshCoordinator

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dataStore = PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = null,
            migrations = emptyList(),
            scope = CoroutineScope(testDispatcher + SupervisorJob()),
            produceFile = {
                "test_settings_vm_${kotlin.random.Random.nextInt()}.preferences_pb".toPath()
            },
        )
        themePreferences = ThemePreferences(dataStore)
        repositoryJob = SupervisorJob()
        themeRepository = ThemeRepositoryImpl(
            themePreferences,
            scope = CoroutineScope(repositoryJob + testDispatcher),
        )
        settingsPreferences = SettingsPreferences(dataStore)
        fakeChannelRepository = FakeChannelRepository()
        clearFavoritesUseCase = ClearFavoritesUseCase(fakeChannelRepository)
        fakeBackgroundScheduler = FakeBackgroundScheduler()
        refreshCoordinator = BackgroundRefreshCoordinator(
            playlistRepository = FakePlaylistRepository(),
            refreshPlaylist = {},
        )
    }

    @AfterTest
    fun tearDown() {
        repositoryJob.cancel()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            themeRepository = themeRepository,
            settingsPreferences = settingsPreferences,
            clearFavoritesUseCase = clearFavoritesUseCase,
            backgroundScheduler = fakeBackgroundScheduler,
            refreshCoordinator = refreshCoordinator,
        )
    }

    private class FakeBackgroundScheduler : BackgroundScheduler {
        var scheduled = false
        var lastInterval: Long? = null

        override fun schedule(intervalSeconds: Long) {
            scheduled = true
            lastInterval = intervalSeconds
        }

        override fun cancel() {
            scheduled = false
        }

        override fun isScheduled(): Boolean = scheduled
    }

    // --- Init ---

    @Test
    fun `init loads default state when no preferences stored`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.viewStates().value
        assertEquals(ThemeMode.System, state.themeMode)
        assertEquals(AccentColor.Teal, state.accentColor)
        assertEquals(ChannelViewMode.List, state.channelViewMode)
        assertFalse(state.autoUpdateEnabled)
        assertEquals(AppConfig.VERSION_NAME, state.appVersion)
        assertFalse(state.showClearFavoritesDialog)
    }

    @Test
    fun `init loads previously stored preferences`() = runTest {
        themeRepository.setThemeMode(ThemeMode.Dark)
        settingsPreferences.setAccentColor(AccentColor.Red)
        settingsPreferences.setChannelViewMode(ChannelViewMode.Grid)
        settingsPreferences.setAutoUpdateEnabled(true)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.viewStates().value
        assertEquals(ThemeMode.Dark, state.themeMode)
        assertEquals(AccentColor.Red, state.accentColor)
        assertEquals(ChannelViewMode.Grid, state.channelViewMode)
        assertTrue(state.autoUpdateEnabled)
    }

    // --- Theme Mode ---

    @Test
    fun `OnThemeModeChanged updates state and persists`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnThemeModeChanged(ThemeMode.Dark))
        advanceUntilIdle()

        assertEquals(ThemeMode.Dark, viewModel.viewStates().value.themeMode)
        assertEquals(ThemeMode.Dark, themeRepository.themeMode.value)
    }

    @Test
    fun `OnThemeModeChanged to Light updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnThemeModeChanged(ThemeMode.Light))
        advanceUntilIdle()

        assertEquals(ThemeMode.Light, viewModel.viewStates().value.themeMode)
        assertEquals(ThemeMode.Light, themeRepository.themeMode.value)
    }

    @Test
    fun `OnThemeModeChanged to System updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.OnThemeModeChanged(ThemeMode.Dark))
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnThemeModeChanged(ThemeMode.System))
        advanceUntilIdle()

        assertEquals(ThemeMode.System, viewModel.viewStates().value.themeMode)
        assertEquals(ThemeMode.System, themeRepository.themeMode.value)
    }

    // --- Accent Color ---

    @Test
    fun `OnAccentColorChanged updates state and persists`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnAccentColorChanged(AccentColor.Blue))
        advanceUntilIdle()

        assertEquals(AccentColor.Blue, viewModel.viewStates().value.accentColor)
        assertEquals(AccentColor.Blue, settingsPreferences.getAccentColor())
    }

    @Test
    fun `OnAccentColorChanged to Red updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnAccentColorChanged(AccentColor.Red))
        advanceUntilIdle()

        assertEquals(AccentColor.Red, viewModel.viewStates().value.accentColor)
        assertEquals(AccentColor.Red, settingsPreferences.getAccentColor())
    }

    // --- Channel View Mode ---

    @Test
    fun `OnChannelViewModeChanged updates state and persists`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnChannelViewModeChanged(ChannelViewMode.Grid))
        advanceUntilIdle()

        assertEquals(ChannelViewMode.Grid, viewModel.viewStates().value.channelViewMode)
        assertEquals(ChannelViewMode.Grid, settingsPreferences.getChannelViewMode())
    }

    @Test
    fun `OnChannelViewModeChanged back to List updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.OnChannelViewModeChanged(ChannelViewMode.Grid))
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnChannelViewModeChanged(ChannelViewMode.List))
        advanceUntilIdle()

        assertEquals(ChannelViewMode.List, viewModel.viewStates().value.channelViewMode)
        assertEquals(ChannelViewMode.List, settingsPreferences.getChannelViewMode())
    }

    // --- Auto Update ---

    @Test
    fun `OnAutoUpdateChanged true updates state and persists`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnAutoUpdateChanged(true))
        advanceUntilIdle()

        assertTrue(viewModel.viewStates().value.autoUpdateEnabled)
        assertTrue(settingsPreferences.getAutoUpdateEnabled())
    }

    @Test
    fun `OnAutoUpdateChanged false after true updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.OnAutoUpdateChanged(true))
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnAutoUpdateChanged(false))
        advanceUntilIdle()

        assertFalse(viewModel.viewStates().value.autoUpdateEnabled)
        assertFalse(settingsPreferences.getAutoUpdateEnabled())
    }

    // --- Notifications ---

    @Test
    fun `OnNotificationsChanged updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnNotificationsChanged(true))

        assertTrue(viewModel.viewStates().value.notificationsEnabled)
    }

    @Test
    fun `OnNotificationsChanged false after true updates state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.OnNotificationsChanged(true))

        viewModel.obtainEvent(SettingsEvent.OnNotificationsChanged(false))

        assertFalse(viewModel.viewStates().value.notificationsEnabled)
    }

    // --- Clear Cache ---

    @Test
    fun `OnClearCacheClick emits ShowCacheCleared action`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnClearCacheClick)

        val action = viewModel.viewActions().first()
        assertIs<SettingsAction.ShowCacheCleared>(action)
    }

    // --- Clear Favorites ---

    @Test
    fun `OnClearFavoritesClick shows confirmation dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnClearFavoritesClick)

        assertTrue(viewModel.viewStates().value.showClearFavoritesDialog)
    }

    @Test
    fun `OnClearFavoritesConfirm closes dialog and clears favorites`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.OnClearFavoritesClick)

        viewModel.obtainEvent(SettingsEvent.OnClearFavoritesConfirm)
        advanceUntilIdle()

        assertFalse(viewModel.viewStates().value.showClearFavoritesDialog)
        assertTrue(fakeChannelRepository.methodCalls.contains("clearAllFavorites"))
        val action = viewModel.viewActions().first()
        assertIs<SettingsAction.ShowFavoritesCleared>(action)
    }

    // --- Dismiss Dialog ---

    @Test
    fun `OnDismissDialog closes clear favorites dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.obtainEvent(SettingsEvent.OnClearFavoritesClick)
        assertTrue(viewModel.viewStates().value.showClearFavoritesDialog)

        viewModel.obtainEvent(SettingsEvent.OnDismissDialog)

        assertFalse(viewModel.viewStates().value.showClearFavoritesDialog)
    }

    // --- Privacy Policy ---

    @Test
    fun `OnPrivacyPolicyClick emits OpenUrl action`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnPrivacyPolicyClick)

        val action = viewModel.viewActions().first()
        assertIs<SettingsAction.OpenUrl>(action)
        assertEquals("https://simplevideo.com/privacy", (action as SettingsAction.OpenUrl).url)
    }

    // --- Terms of Service ---

    @Test
    fun `OnTermsOfServiceClick emits OpenUrl action`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnTermsOfServiceClick)

        val action = viewModel.viewActions().first()
        assertIs<SettingsAction.OpenUrl>(action)
        assertEquals("https://simplevideo.com/terms", (action as SettingsAction.OpenUrl).url)
    }

    // --- Edge Cases ---

    @Test
    fun `multiple rapid theme switches preserve last value`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(SettingsEvent.OnThemeModeChanged(ThemeMode.Light))
        viewModel.obtainEvent(SettingsEvent.OnThemeModeChanged(ThemeMode.Dark))
        viewModel.obtainEvent(SettingsEvent.OnThemeModeChanged(ThemeMode.Light))
        advanceUntilIdle()

        assertEquals(ThemeMode.Light, viewModel.viewStates().value.themeMode)
        assertEquals(ThemeMode.Light, themeRepository.themeMode.value)
    }
}
