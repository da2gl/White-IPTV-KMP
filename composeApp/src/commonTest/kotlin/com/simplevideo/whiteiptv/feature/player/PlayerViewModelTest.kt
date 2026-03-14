//package com.simplevideo.whiteiptv.feature.player
//
//import androidx.lifecycle.SavedStateHandle
//import com.simplevideo.whiteiptv.data.local.FakePlaylistDao
//import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
//import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
//import com.simplevideo.whiteiptv.data.repository.FakeEpgRepository
//import com.simplevideo.whiteiptv.data.repository.FakePlaylistRepository
//import com.simplevideo.whiteiptv.domain.model.EpgProgram
//import com.simplevideo.whiteiptv.domain.usecase.FakeWatchHistoryRepository
//import com.simplevideo.whiteiptv.domain.usecase.GetAdjacentChannelUseCase
//import com.simplevideo.whiteiptv.domain.usecase.GetChannelByIdUseCase
//import com.simplevideo.whiteiptv.domain.usecase.GetCurrentProgramUseCase
//import com.simplevideo.whiteiptv.domain.usecase.LoadEpgUseCase
//import com.simplevideo.whiteiptv.domain.usecase.RecordWatchEventUseCase
//import com.simplevideo.whiteiptv.feature.player.mvi.PlayerEvent
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.advanceUntilIdle
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import kotlin.test.AfterTest
//import kotlin.test.BeforeTest
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//import kotlin.test.assertNull
//import kotlin.test.assertTrue
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class PlayerViewModelTest {
//
//    private val testDispatcher = StandardTestDispatcher()
//
//    private lateinit var fakePlaylistDao: FakePlaylistDao
//    private lateinit var fakePlaylistRepository: FakePlaylistRepository
//    private lateinit var fakeEpgRepository: FakeEpgRepository
//    private lateinit var fakeWatchHistoryRepository: FakeWatchHistoryRepository
//
//    private val testChannel = ChannelEntity(
//        id = 1L,
//        playlistId = 10L,
//        name = "Test Channel",
//        url = "http://example.com/stream.m3u8",
//        tvgId = "channel.one",
//    )
//
//    private val testPlaylist = PlaylistEntity(
//        id = 10L,
//        name = "Test Playlist",
//        url = "http://example.com/playlist.m3u",
//        urlTvg = "http://example.com/epg.xml",
//    )
//
//    private val testCurrentProgram = EpgProgram(
//        title = "Morning News",
//        startTimeMs = 1_000_000L,
//        endTimeMs = 2_000_000L,
//    )
//
//    private val testNextProgram = EpgProgram(
//        title = "Afternoon Show",
//        startTimeMs = 2_000_000L,
//        endTimeMs = 3_000_000L,
//    )
//
//    @BeforeTest
//    fun setUp() {
//        Dispatchers.setMain(testDispatcher)
//        fakePlaylistDao = FakePlaylistDao()
//        fakePlaylistRepository = FakePlaylistRepository()
//        fakeEpgRepository = FakeEpgRepository()
//        fakeWatchHistoryRepository = FakeWatchHistoryRepository()
//    }
//
//    @AfterTest
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    private fun createViewModel(
//        channelId: Long = testChannel.id,
//    ): PlayerViewModel {
//        val savedStateHandle = SavedStateHandle(mapOf("channelId" to channelId))
//        return PlayerViewModel(
//            savedStateHandle = savedStateHandle,
//            getChannelById = GetChannelByIdUseCase(fakePlaylistDao),
//            getAdjacentChannel = GetAdjacentChannelUseCase(fakePlaylistDao),
//            recordWatchEvent = RecordWatchEventUseCase(fakeWatchHistoryRepository),
//            loadEpg = LoadEpgUseCase(fakePlaylistRepository, fakeEpgRepository),
//            getCurrentProgram = GetCurrentProgramUseCase(fakeEpgRepository),
//        )
//    }
//
//    // --- EPG Loading ---
//
//    @Test
//    fun `EPG data is loaded when channel loads`() = runTest {
//        fakePlaylistDao.addChannel(testChannel)
//        fakePlaylistRepository.addPlaylist(testPlaylist)
//
//        createViewModel()
//        advanceUntilIdle()
//
//        assertTrue(fakeEpgRepository.loadEpgCalled)
//        assertEquals(10L, fakeEpgRepository.lastLoadPlaylistId)
//    }
//
//    @Test
//    fun `current and next program are set in state after channel loads`() = runTest {
//        fakePlaylistDao.addChannel(testChannel)
//        fakePlaylistRepository.addPlaylist(testPlaylist)
//        fakeEpgRepository.currentProgram = testCurrentProgram
//        fakeEpgRepository.nextProgram = testNextProgram
//
//        val viewModel = createViewModel()
//        advanceUntilIdle()
//
//        val state = viewModel.viewStates().value
//        assertNotNull(state.currentProgram)
//        assertEquals("Morning News", state.currentProgram?.title)
//        assertNotNull(state.nextProgram)
//        assertEquals("Afternoon Show", state.nextProgram?.title)
//    }
//
//    @Test
//    fun `EPG state is null when channel has no tvgId`() = runTest {
//        val channelNoTvg = testChannel.copy(tvgId = null)
//        fakePlaylistDao.addChannel(channelNoTvg)
//        fakePlaylistRepository.addPlaylist(testPlaylist)
//        fakeEpgRepository.currentProgram = testCurrentProgram
//
//        val viewModel = createViewModel()
//        advanceUntilIdle()
//
//        val state = viewModel.viewStates().value
//        assertNull(state.currentProgram)
//        assertNull(state.nextProgram)
//    }
//
//    @Test
//    fun `EPG not loaded when playlist has no urlTvg`() = runTest {
//        fakePlaylistDao.addChannel(testChannel)
//        fakePlaylistRepository.addPlaylist(testPlaylist.copy(urlTvg = null))
//
//        createViewModel()
//        advanceUntilIdle()
//
//        // LoadEpgUseCase short-circuits when no urlTvg
//        assertTrue(!fakeEpgRepository.loadEpgCalled)
//    }
//
//    @Test
//    fun `EPG state cleared when switching channels`() = runTest {
//        val channel2 = ChannelEntity(
//            id = 2L,
//            playlistId = 10L,
//            name = "Channel Two",
//            url = "http://example.com/stream2.m3u8",
//            tvgId = "channel.two",
//        )
//        fakePlaylistDao.addChannel(testChannel)
//        fakePlaylistDao.addChannel(channel2)
//        fakePlaylistRepository.addPlaylist(testPlaylist)
//        fakeEpgRepository.currentProgram = testCurrentProgram
//        fakeEpgRepository.nextProgram = testNextProgram
//
//        val viewModel = createViewModel()
//        advanceUntilIdle()
//
//        // Verify initial EPG state is set
//        assertNotNull(viewModel.viewStates().value.currentProgram)
//
//        // Clear EPG data so the next fetch returns null
//        fakeEpgRepository.currentProgram = null
//        fakeEpgRepository.nextProgram = null
//
//        // Navigate to next channel
//        viewModel.obtainEvent(PlayerEvent.OnNextChannel)
//        advanceUntilIdle()
//
//        val state = viewModel.viewStates().value
//        assertNull(state.currentProgram)
//        assertNull(state.nextProgram)
//    }
//
//    @Test
//    fun `only current program shown when no next program exists`() = runTest {
//        fakePlaylistDao.addChannel(testChannel)
//        fakePlaylistRepository.addPlaylist(testPlaylist)
//        fakeEpgRepository.currentProgram = testCurrentProgram
//        fakeEpgRepository.nextProgram = null
//
//        val viewModel = createViewModel()
//        advanceUntilIdle()
//
//        val state = viewModel.viewStates().value
//        assertNotNull(state.currentProgram)
//        assertEquals("Morning News", state.currentProgram?.title)
//        assertNull(state.nextProgram)
//    }
//
////    @Test
////    fun `channel loads correctly with EPG fields in initial state`() = runTest {
////        fakePlaylistDao.addChannel(testChannel)
////        fakePlaylistRepository.addPlaylist(testPlaylist)
////
////        val viewModel = createViewModel()
////        advanceUntilIdle()
////
////        val state = viewModel.viewStates().value
////        assertNotNull(state.channel)
////        assertEquals("Test Channel", state.channel?.name)
////        assertEquals(false, state.isLoading)
////        assertNull(state.error)
////    }
//
//    @Test
//    fun `EPG load failure does not affect channel state`() = runTest {
//        fakePlaylistDao.addChannel(testChannel)
//        fakePlaylistRepository.addPlaylist(testPlaylist)
//        fakeEpgRepository.loadEpgError = RuntimeException("Network failure")
//
//        val viewModel = createViewModel()
//        advanceUntilIdle()
//
//        val state = viewModel.viewStates().value
//        // Channel should still load fine
//        assertNotNull(state.channel)
//        assertEquals("Test Channel", state.channel?.name)
//        // EPG data should be null due to load error
//        assertNull(state.currentProgram)
//        assertNull(state.nextProgram)
//        // No error shown to user (EPG is non-critical)
//        assertNull(state.error)
//    }
//}
