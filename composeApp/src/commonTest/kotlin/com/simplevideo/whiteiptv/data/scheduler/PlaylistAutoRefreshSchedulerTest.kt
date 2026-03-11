package com.simplevideo.whiteiptv.data.scheduler

import com.russhwolf.settings.MapSettings
import com.simplevideo.whiteiptv.data.local.SettingsPreferences
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.data.repository.FakePlaylistRepository
import com.simplevideo.whiteiptv.domain.model.PlaylistSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistAutoRefreshSchedulerTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settings: MapSettings
    private lateinit var settingsPreferences: SettingsPreferences
    private lateinit var fakePlaylistRepository: FakePlaylistRepository
    private lateinit var scheduler: PlaylistAutoRefreshScheduler

    private val refreshInvocations = mutableListOf<PlaylistSource>()
    private val failForUrls = mutableSetOf<String>()

    private val fakeRefreshPlaylist: suspend (PlaylistSource) -> Unit = { source ->
        refreshInvocations.add(source)
        if (source is PlaylistSource.Url && source.url in failForUrls) {
            throw RuntimeException("Simulated refresh failure for ${source.url}")
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settings = MapSettings()
        settingsPreferences = SettingsPreferences(settings)
        fakePlaylistRepository = FakePlaylistRepository()
        refreshInvocations.clear()
        failForUrls.clear()
        scheduler = PlaylistAutoRefreshScheduler(
            settingsPreferences = settingsPreferences,
            playlistRepository = fakePlaylistRepository,
            refreshPlaylist = fakeRefreshPlaylist,
            coroutineContext = testDispatcher,
        )
    }

    @AfterTest
    fun tearDown() {
        scheduler.stop()
        Dispatchers.resetMain()
    }

    // --- Start/Stop based on settings toggle ---

    @Test
    fun `does not refresh when auto-update is disabled`() = runTest {
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://example.com/playlist.m3u"))

        scheduler.start()
        advanceUntilIdle()

        assertEquals(0, refreshInvocations.size)
    }

    @Test
    fun `refreshes playlists when auto-update is enabled`() = runTest {
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://example.com/playlist.m3u"))
        settingsPreferences.setAutoUpdateEnabled(true)

        scheduler.start()
        advanceUntilIdle()

        assertEquals(1, refreshInvocations.size)
        assertEquals(
            "https://example.com/playlist.m3u",
            (refreshInvocations[0] as PlaylistSource.Url).url,
        )
    }

    @Test
    fun `stops refreshing when toggle is disabled after being enabled`() = runTest {
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://example.com/playlist.m3u"))
        settingsPreferences.setAutoUpdateEnabled(true)

        scheduler.start()
        advanceUntilIdle()
        assertEquals(1, refreshInvocations.size)

        // Disable auto-update
        refreshInvocations.clear()
        settingsPreferences.setAutoUpdateEnabled(false)
        advanceUntilIdle()

        // Advance a full default interval — no more refreshes should happen
        advanceTimeBy(PlaylistAutoRefreshScheduler.DEFAULT_REFRESH_INTERVAL_SECONDS * 1000L + 1000L)
        advanceUntilIdle()
        assertEquals(0, refreshInvocations.size)
    }

    @Test
    fun `re-enables refreshing when toggle is re-enabled`() = runTest {
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://example.com/playlist.m3u"))
        settingsPreferences.setAutoUpdateEnabled(true)

        scheduler.start()
        advanceUntilIdle()
        assertEquals(1, refreshInvocations.size)

        // Toggle off then on
        settingsPreferences.setAutoUpdateEnabled(false)
        advanceUntilIdle()
        refreshInvocations.clear()

        settingsPreferences.setAutoUpdateEnabled(true)
        advanceUntilIdle()
        assertEquals(1, refreshInvocations.size)
    }

    // --- URL filtering ---

    @Test
    fun `refreshes only URL-based playlists, skips local file playlists`() = runTest {
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://example.com/remote.m3u"))
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "http://other.com/playlist.m3u"))
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "file://local-playlist.m3u"))

        settingsPreferences.setAutoUpdateEnabled(true)
        scheduler.start()
        advanceUntilIdle()

        assertEquals(2, refreshInvocations.size)
        val urls = refreshInvocations.map { (it as PlaylistSource.Url).url }
        assertTrue("https://example.com/remote.m3u" in urls)
        assertTrue("http://other.com/playlist.m3u" in urls)
    }

    @Test
    fun `no-op when all playlists are local files`() = runTest {
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "file://local1.m3u"))
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "file://local2.m3u"))

        settingsPreferences.setAutoUpdateEnabled(true)
        scheduler.start()
        advanceUntilIdle()

        assertEquals(0, refreshInvocations.size)
    }

    @Test
    fun `no-op when no playlists exist`() = runTest {
        settingsPreferences.setAutoUpdateEnabled(true)
        scheduler.start()
        advanceUntilIdle()

        assertEquals(0, refreshInvocations.size)
    }

    // --- Error handling ---

    @Test
    fun `continues refreshing other playlists when one fails`() = runTest {
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://fail.com/playlist.m3u"))
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://success.com/playlist.m3u"))

        failForUrls.add("https://fail.com/playlist.m3u")
        settingsPreferences.setAutoUpdateEnabled(true)

        scheduler.start()
        advanceUntilIdle()

        assertEquals(2, refreshInvocations.size)
    }

    @Test
    fun `does not crash when all playlists fail to refresh`() = runTest {
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://fail1.com/playlist.m3u"))
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://fail2.com/playlist.m3u"))

        failForUrls.add("https://fail1.com/playlist.m3u")
        failForUrls.add("https://fail2.com/playlist.m3u")
        settingsPreferences.setAutoUpdateEnabled(true)

        scheduler.start()
        advanceUntilIdle()

        assertEquals(2, refreshInvocations.size)
    }

    // --- Interval calculation ---

    @Test
    fun `uses default interval when playlist has no refreshInterval`() = runTest {
        fakePlaylistRepository.addPlaylist(makeUrlPlaylist(url = "https://example.com/playlist.m3u"))
        settingsPreferences.setAutoUpdateEnabled(true)

        scheduler.start()
        advanceUntilIdle()
        refreshInvocations.clear()

        // Advance less than default interval — should not refresh again
        advanceTimeBy(PlaylistAutoRefreshScheduler.DEFAULT_REFRESH_INTERVAL_SECONDS * 1000L - 1000L)
        advanceUntilIdle()
        assertEquals(0, refreshInvocations.size)

        // Advance past the default interval — should refresh
        advanceTimeBy(2000L)
        advanceUntilIdle()
        assertEquals(1, refreshInvocations.size)
    }

    @Test
    fun `clamps minimum interval to 15 minutes`() = runTest {
        fakePlaylistRepository.addPlaylist(
            makeUrlPlaylist(url = "https://example.com/playlist.m3u", refreshInterval = 10),
        )
        settingsPreferences.setAutoUpdateEnabled(true)

        scheduler.start()
        advanceUntilIdle()
        refreshInvocations.clear()

        // Advance 10 seconds — should NOT trigger refresh (clamped to 15 min)
        advanceTimeBy(10_000L)
        advanceUntilIdle()
        assertEquals(0, refreshInvocations.size)

        // Advance to 15 minutes — should refresh
        advanceTimeBy(PlaylistAutoRefreshScheduler.MIN_REFRESH_INTERVAL_SECONDS * 1000L)
        advanceUntilIdle()
        assertEquals(1, refreshInvocations.size)
    }

    @Test
    fun `uses playlist refresh interval when specified and above minimum`() = runTest {
        fakePlaylistRepository.addPlaylist(
            makeUrlPlaylist(url = "https://example.com/playlist.m3u", refreshInterval = 3600),
        )
        settingsPreferences.setAutoUpdateEnabled(true)

        scheduler.start()
        advanceUntilIdle()
        refreshInvocations.clear()

        // Advance less than 1 hour — no refresh
        advanceTimeBy(3599_000L)
        advanceUntilIdle()
        assertEquals(0, refreshInvocations.size)

        // Advance past 1 hour — refresh
        advanceTimeBy(2000L)
        advanceUntilIdle()
        assertEquals(1, refreshInvocations.size)
    }

    @Test
    fun `uses minimum interval from multiple playlists`() = runTest {
        fakePlaylistRepository.addPlaylist(
            makeUrlPlaylist(url = "https://a.com/p.m3u", refreshInterval = 7200), // 2h
        )
        fakePlaylistRepository.addPlaylist(
            makeUrlPlaylist(url = "https://b.com/p.m3u", refreshInterval = 3600), // 1h
        )

        settingsPreferences.setAutoUpdateEnabled(true)
        scheduler.start()
        advanceUntilIdle()
        refreshInvocations.clear()

        // Should use 1h (minimum of the two)
        advanceTimeBy(3600_000L + 1000L)
        advanceUntilIdle()
        assertTrue(refreshInvocations.size >= 1)
    }

    // --- Helpers ---

    private fun makeUrlPlaylist(
        url: String,
        name: String = "Test Playlist",
        refreshInterval: Int? = null,
    ): PlaylistEntity = PlaylistEntity(
        name = name,
        url = url,
        refreshInterval = refreshInterval,
    )
}
