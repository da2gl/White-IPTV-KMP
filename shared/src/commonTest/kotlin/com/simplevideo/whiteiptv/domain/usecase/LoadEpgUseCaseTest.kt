package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.data.repository.FakeEpgRepository
import com.simplevideo.whiteiptv.data.repository.FakePlaylistRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoadEpgUseCaseTest {

    private lateinit var fakePlaylistRepository: FakePlaylistRepository
    private lateinit var fakeEpgRepository: FakeEpgRepository
    private lateinit var useCase: LoadEpgUseCase

    @BeforeTest
    fun setUp() {
        fakePlaylistRepository = FakePlaylistRepository()
        fakeEpgRepository = FakeEpgRepository()
        useCase = LoadEpgUseCase(fakePlaylistRepository, fakeEpgRepository)
    }

    @Test
    fun `does nothing when playlist not found`() = runTest {
        useCase(playlistId = 999L)

        assertFalse(fakeEpgRepository.loadEpgCalled)
    }

    @Test
    fun `does nothing when playlist has no urlTvg`() = runTest {
        fakePlaylistRepository.addPlaylist(
            PlaylistEntity(
                id = 1L,
                name = "Test Playlist",
                url = "http://example.com/playlist.m3u",
                urlTvg = null,
            ),
        )

        useCase(playlistId = 1L)

        assertFalse(fakeEpgRepository.loadEpgCalled)
    }

    @Test
    fun `loads EPG when playlist has urlTvg`() = runTest {
        fakePlaylistRepository.addPlaylist(
            PlaylistEntity(
                id = 1L,
                name = "Test Playlist",
                url = "http://example.com/playlist.m3u",
                urlTvg = "http://example.com/epg.xml",
                tvgShift = 2,
            ),
        )

        useCase(playlistId = 1L)

        assertTrue(fakeEpgRepository.loadEpgCalled)
        assertEquals(1L, fakeEpgRepository.lastLoadPlaylistId)
        assertEquals("http://example.com/epg.xml", fakeEpgRepository.lastLoadXmltvUrl)
        assertEquals(2, fakeEpgRepository.lastLoadTvgShiftHours)
    }

    @Test
    fun `uses zero shift when playlist tvgShift is null`() = runTest {
        fakePlaylistRepository.addPlaylist(
            PlaylistEntity(
                id = 1L,
                name = "Test Playlist",
                url = "http://example.com/playlist.m3u",
                urlTvg = "http://example.com/epg.xml",
                tvgShift = null,
            ),
        )

        useCase(playlistId = 1L)

        assertTrue(fakeEpgRepository.loadEpgCalled)
        assertEquals(0, fakeEpgRepository.lastLoadTvgShiftHours)
    }

    @Test
    fun `catches and does not throw on repository error`() = runTest {
        fakePlaylistRepository.addPlaylist(
            PlaylistEntity(
                id = 1L,
                name = "Test Playlist",
                url = "http://example.com/playlist.m3u",
                urlTvg = "http://example.com/epg.xml",
            ),
        )
        fakeEpgRepository.loadEpgError = RuntimeException("Network failure")

        // Should not throw
        useCase(playlistId = 1L)
    }
}
