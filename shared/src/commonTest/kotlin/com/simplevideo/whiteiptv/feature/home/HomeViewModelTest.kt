package com.simplevideo.whiteiptv.feature.home

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import com.simplevideo.whiteiptv.data.mapper.ChannelGroupMapper
import com.simplevideo.whiteiptv.data.mapper.ChannelMapper
import com.simplevideo.whiteiptv.data.mapper.PlaylistMapper
import com.simplevideo.whiteiptv.data.repository.FakePlaylistRepository
import com.simplevideo.whiteiptv.data.repository.StubChannelRepository
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository
import com.simplevideo.whiteiptv.domain.usecase.DeletePlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetChannelsUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetContinueWatchingUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetFavoritesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetHomeCategoriesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPlaylistsUseCase
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.RenamePlaylistUseCase
import com.simplevideo.whiteiptv.feature.home.mvi.HomeAction
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
import com.simplevideo.whiteiptv.platform.FileReader
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakePlaylistRepository: FakePlaylistRepository
    private lateinit var currentPlaylistRepository: CurrentPlaylistRepository
    private lateinit var stubChannelRepository: ChannelRepository
    private lateinit var stubWatchHistoryRepository: WatchHistoryRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePlaylistRepository = FakePlaylistRepository()
        currentPlaylistRepository = CurrentPlaylistRepository()
        stubChannelRepository = StubChannelRepository()
        stubWatchHistoryRepository = object : WatchHistoryRepository {
            override fun getRecentlyWatchedChannels(limit: Int): Flow<List<ChannelEntity>> = flowOf(emptyList())
            override suspend fun recordWatchEvent(channelId: Long, playlistId: Long, durationMs: Long) = Unit
            override suspend fun getWatchHistoryForChannel(channelId: Long): WatchHistoryEntity? = null
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        playlists: List<PlaylistEntity> = emptyList(),
    ): HomeViewModel {
        playlists.forEach { fakePlaylistRepository.addPlaylist(it) }

        return HomeViewModel(
            getPlaylists = GetPlaylistsUseCase(fakePlaylistRepository),
            getContinueWatching = GetContinueWatchingUseCase(stubWatchHistoryRepository),
            getFavorites = GetFavoritesUseCase(stubChannelRepository),
            getHomeCategories = GetHomeCategoriesUseCase(stubChannelRepository, ChannelGroupMapper()),
            getChannels = GetChannelsUseCase(stubChannelRepository),
            currentPlaylistRepository = currentPlaylistRepository,
            renamePlaylistUseCase = RenamePlaylistUseCase(fakePlaylistRepository),
            deletePlaylistUseCase = DeletePlaylistUseCase(fakePlaylistRepository),
            importPlaylistUseCase = ImportPlaylistUseCase(
                playlistRepository = fakePlaylistRepository,
                channelRepository = stubChannelRepository,
                httpClient = HttpClient(),
                fileReader = object : FileReader {
                    override suspend fun readFile(uri: String) = ""
                },
                channelMapper = ChannelMapper(),
                playlistMapper = PlaylistMapper(),
            ),
        )
    }

    // --- Playlist Settings Bottom Sheet ---

    @Test
    fun `OnPlaylistSettingsClick sets showPlaylistSettings to true`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsClick)

        assertTrue(viewModel.viewStates().value.showPlaylistSettings)
    }

    @Test
    fun `OnPlaylistSettingsDismiss sets showPlaylistSettings to false`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsClick)
        viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsDismiss)

        assertFalse(viewModel.viewStates().value.showPlaylistSettings)
    }

    // --- Rename Flow ---

    @Test
    fun `OnRenameClick closes bottom sheet and opens rename dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsClick)
        viewModel.obtainEvent(HomeEvent.OnRenameClick)

        val state = viewModel.viewStates().value
        assertFalse(state.showPlaylistSettings)
        assertTrue(state.showRenameDialog)
    }

    @Test
    fun `OnRenameDialogDismiss closes rename dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnRenameClick)
        viewModel.obtainEvent(HomeEvent.OnRenameDialogDismiss)

        assertFalse(viewModel.viewStates().value.showRenameDialog)
    }

    @Test
    fun `OnRenameConfirm with valid name closes dialog and renames`() = runTest {
        val playlist = PlaylistEntity(id = 1, name = "Old Name", url = "https://example.com/p.m3u")
        val viewModel = createViewModel(playlists = listOf(playlist))
        advanceUntilIdle()

        currentPlaylistRepository.select(PlaylistSelection.Selected(1))
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnRenameConfirm("New Name"))
        advanceUntilIdle()

        assertFalse(viewModel.viewStates().value.showRenameDialog)
        assertNull(viewModel.viewStates().value.playlistManagementError)
        assertEquals("New Name", fakePlaylistRepository.getPlaylistById(1)?.name)
    }

    @Test
    fun `OnRenameConfirm with blank name shows error`() = runTest {
        val playlist = PlaylistEntity(id = 1, name = "Old Name", url = "https://example.com/p.m3u")
        val viewModel = createViewModel(playlists = listOf(playlist))
        advanceUntilIdle()

        currentPlaylistRepository.select(PlaylistSelection.Selected(1))
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnRenameConfirm(""))
        advanceUntilIdle()

        assertFalse(viewModel.viewStates().value.showRenameDialog)
        assertNotNull(viewModel.viewStates().value.playlistManagementError)
    }

    @Test
    fun `OnRenameConfirm with All selection is no-op`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnRenameConfirm("New Name"))
        advanceUntilIdle()

        assertFalse(viewModel.viewStates().value.showRenameDialog)
        assertNull(viewModel.viewStates().value.playlistManagementError)
    }

    // --- Delete Flow ---

    @Test
    fun `OnDeleteClick closes bottom sheet and opens delete confirmation`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsClick)
        viewModel.obtainEvent(HomeEvent.OnDeleteClick)

        val state = viewModel.viewStates().value
        assertFalse(state.showPlaylistSettings)
        assertTrue(state.showDeleteConfirmation)
    }

    @Test
    fun `OnDeleteDialogDismiss closes delete confirmation`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnDeleteClick)
        viewModel.obtainEvent(HomeEvent.OnDeleteDialogDismiss)

        assertFalse(viewModel.viewStates().value.showDeleteConfirmation)
    }

    @Test
    fun `OnDeleteConfirm last playlist emits NavigateToOnboarding`() = runTest {
        val playlist = PlaylistEntity(id = 1, name = "Only Playlist", url = "https://example.com/p.m3u")
        val viewModel = createViewModel(playlists = listOf(playlist))
        advanceUntilIdle()

        currentPlaylistRepository.select(PlaylistSelection.Selected(1))
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnDeleteConfirm)
        advanceUntilIdle()

        assertFalse(viewModel.viewStates().value.showDeleteConfirmation)
        val action = viewModel.viewActions().first()
        assertIs<HomeAction.NavigateToOnboarding>(action)
    }

    @Test
    fun `OnDeleteConfirm non-last playlist resets selection to All`() = runTest {
        val playlist1 = PlaylistEntity(id = 1, name = "Playlist 1", url = "https://example.com/p1.m3u")
        val playlist2 = PlaylistEntity(id = 2, name = "Playlist 2", url = "https://example.com/p2.m3u")
        val viewModel = createViewModel(playlists = listOf(playlist1, playlist2))
        advanceUntilIdle()

        currentPlaylistRepository.select(PlaylistSelection.Selected(1))
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnDeleteConfirm)
        advanceUntilIdle()

        assertFalse(viewModel.viewStates().value.showDeleteConfirmation)
        assertEquals(PlaylistSelection.All, currentPlaylistRepository.selection.value)
    }

    @Test
    fun `OnDeleteConfirm with All selection is no-op`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnDeleteConfirm)
        advanceUntilIdle()

        assertFalse(viewModel.viewStates().value.showDeleteConfirmation)
    }

    // --- View URL Flow ---

    @Test
    fun `OnViewUrlClick closes bottom sheet and opens URL dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsClick)
        viewModel.obtainEvent(HomeEvent.OnViewUrlClick)

        val state = viewModel.viewStates().value
        assertFalse(state.showPlaylistSettings)
        assertTrue(state.showViewUrlDialog)
    }

    @Test
    fun `OnViewUrlDialogDismiss closes URL dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnViewUrlClick)
        viewModel.obtainEvent(HomeEvent.OnViewUrlDialogDismiss)

        assertFalse(viewModel.viewStates().value.showViewUrlDialog)
    }

    // --- Error Dismiss ---

    @Test
    fun `OnPlaylistManagementErrorDismiss clears error`() = runTest {
        val playlist = PlaylistEntity(id = 1, name = "Old", url = "https://example.com/p.m3u")
        val viewModel = createViewModel(playlists = listOf(playlist))
        advanceUntilIdle()

        currentPlaylistRepository.select(PlaylistSelection.Selected(1))
        advanceUntilIdle()

        // Trigger an error via blank rename
        viewModel.obtainEvent(HomeEvent.OnRenameConfirm(""))
        advanceUntilIdle()
        assertNotNull(viewModel.viewStates().value.playlistManagementError)

        viewModel.obtainEvent(HomeEvent.OnPlaylistManagementErrorDismiss)

        assertNull(viewModel.viewStates().value.playlistManagementError)
    }

    // --- Init: playlists loaded ---

    @Test
    fun `init loads playlists into state`() = runTest {
        val playlist = PlaylistEntity(id = 1, name = "Test", url = "https://example.com/p.m3u")
        val viewModel = createViewModel(playlists = listOf(playlist))
        advanceUntilIdle()

        val state = viewModel.viewStates().value
        assertEquals(1, state.playlists.size)
        assertEquals("Test", state.playlists[0].name)
        assertFalse(state.isLoading)
    }

    // --- Add Playlist ---

    @Test
    fun `OnAddPlaylistClick emits NavigateToOnboarding action`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnAddPlaylistClick)

        val action = viewModel.viewActions().first()
        assertIs<HomeAction.NavigateToOnboarding>(action)
    }

    // --- Navigation Events ---

    @Test
    fun `OnChannelClick emits NavigateToPlayer action`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnChannelClick(42L))

        val action = viewModel.viewActions().first()
        assertIs<HomeAction.NavigateToPlayer>(action)
        assertEquals(42L, (action as HomeAction.NavigateToPlayer).channelId)
    }

    @Test
    fun `OnFavoritesViewAllClick emits NavigateToFavorites action`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnFavoritesViewAllClick)

        val action = viewModel.viewActions().first()
        assertIs<HomeAction.NavigateToFavorites>(action)
    }

    @Test
    fun `OnGroupViewAllClick emits NavigateToChannels action`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnGroupViewAllClick("group-1"))

        val action = viewModel.viewActions().first()
        assertIs<HomeAction.NavigateToChannels>(action)
        assertEquals("group-1", (action as HomeAction.NavigateToChannels).groupId)
    }

    // --- Search ---

    @Test
    fun `OnToggleSearch activates search mode`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnToggleSearch)

        assertTrue(viewModel.viewStates().value.isSearchActive)
    }

    @Test
    fun `OnToggleSearch twice deactivates search and clears query`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnToggleSearch)
        viewModel.obtainEvent(HomeEvent.OnSearchQueryChanged("test"))
        viewModel.obtainEvent(HomeEvent.OnToggleSearch)

        val state = viewModel.viewStates().value
        assertFalse(state.isSearchActive)
        assertEquals("", state.searchQuery)
    }

    // --- Playlist Selection ---

    @Test
    fun `OnPlaylistSelected updates selection in repository`() = runTest {
        val playlist = PlaylistEntity(id = 1, name = "Test", url = "https://example.com/p.m3u")
        val viewModel = createViewModel(playlists = listOf(playlist))
        advanceUntilIdle()

        viewModel.obtainEvent(HomeEvent.OnPlaylistSelected(PlaylistSelection.Selected(1)))
        advanceUntilIdle()

        assertEquals(PlaylistSelection.Selected(1), currentPlaylistRepository.selection.value)
    }
}
