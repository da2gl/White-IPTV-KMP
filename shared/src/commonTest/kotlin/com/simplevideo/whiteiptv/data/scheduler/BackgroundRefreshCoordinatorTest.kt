package com.simplevideo.whiteiptv.data.scheduler

import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.data.repository.FakePlaylistRepository
import com.simplevideo.whiteiptv.domain.model.PlaylistSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackgroundRefreshCoordinatorTest {

    private val fakePlaylistRepository = FakePlaylistRepository()
    private val refreshInvocations = mutableListOf<PlaylistSource>()
    private val failForUrls = mutableSetOf<String>()

    private val fakeRefreshPlaylist: suspend (PlaylistSource) -> Unit = { source ->
        refreshInvocations.add(source)
        if (source is PlaylistSource.Url && source.url in failForUrls) {
            throw RuntimeException("Simulated refresh failure for ${source.url}")
        }
    }

    private val coordinator = BackgroundRefreshCoordinator(
        playlistRepository = fakePlaylistRepository,
        refreshPlaylist = fakeRefreshPlaylist,
    )

    @Test
    fun `refreshes all URL-based playlists`() = runTest {
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "https://example.com/a.m3u"))
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "http://example.com/b.m3u"))

        coordinator.refreshAllPlaylists()

        assertEquals(2, refreshInvocations.size)
        val urls = refreshInvocations.map { (it as PlaylistSource.Url).url }
        assertTrue("https://example.com/a.m3u" in urls)
        assertTrue("http://example.com/b.m3u" in urls)
    }

    @Test
    fun `skips local file playlists`() = runTest {
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "https://example.com/remote.m3u"))
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "file://local-playlist.m3u"))

        coordinator.refreshAllPlaylists()

        assertEquals(1, refreshInvocations.size)
        assertEquals(
            "https://example.com/remote.m3u",
            (refreshInvocations[0] as PlaylistSource.Url).url,
        )
    }

    @Test
    fun `handles individual refresh errors gracefully`() = runTest {
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "https://fail.com/playlist.m3u"))
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "https://success.com/playlist.m3u"))
        failForUrls.add("https://fail.com/playlist.m3u")

        coordinator.refreshAllPlaylists()

        assertEquals(2, refreshInvocations.size)
    }

    @Test
    fun `no-op when no playlists exist`() = runTest {
        coordinator.refreshAllPlaylists()

        assertEquals(0, refreshInvocations.size)
    }

    @Test
    fun `no-op when all playlists are local files`() = runTest {
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "file://local1.m3u"))
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "file://local2.m3u"))

        coordinator.refreshAllPlaylists()

        assertEquals(0, refreshInvocations.size)
    }

    @Test
    fun `calculateIntervalSeconds uses minimum across playlists`() = runTest {
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "https://a.com/p.m3u", refreshInterval = 7200))
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "https://b.com/p.m3u", refreshInterval = 3600))

        val interval = coordinator.calculateIntervalSeconds()

        assertEquals(3600L, interval)
    }

    @Test
    fun `calculateIntervalSeconds clamps to minimum`() = runTest {
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "https://a.com/p.m3u", refreshInterval = 10))

        val interval = coordinator.calculateIntervalSeconds()

        assertEquals(BackgroundRefreshCoordinator.MIN_REFRESH_INTERVAL_SECONDS, interval)
    }

    @Test
    fun `calculateIntervalSeconds uses default when no intervals specified`() = runTest {
        fakePlaylistRepository.addPlaylist(makePlaylist(url = "https://a.com/p.m3u"))

        val interval = coordinator.calculateIntervalSeconds()

        assertEquals(BackgroundRefreshCoordinator.DEFAULT_REFRESH_INTERVAL_SECONDS, interval)
    }

    private fun makePlaylist(
        url: String,
        name: String = "Test Playlist",
        refreshInterval: Int? = null,
    ): PlaylistEntity = PlaylistEntity(
        name = name,
        url = url,
        refreshInterval = refreshInterval,
    )
}
