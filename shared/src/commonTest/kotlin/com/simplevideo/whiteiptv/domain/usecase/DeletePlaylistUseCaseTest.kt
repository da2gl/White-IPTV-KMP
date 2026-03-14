package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.data.repository.FakePlaylistRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeletePlaylistUseCaseTest {

    private lateinit var fakeRepository: FakePlaylistRepository
    private lateinit var useCase: DeletePlaylistUseCase

    @BeforeTest
    fun setUp() {
        fakeRepository = FakePlaylistRepository()
        useCase = DeletePlaylistUseCase(fakeRepository)
    }

    @Test
    fun `delete removes playlist from repository`() = runTest {
        val playlist = fakeRepository.addPlaylist(
            PlaylistEntity(name = "Test", url = "https://example.com/playlist.m3u"),
        )

        useCase(playlist.id)

        assertNull(fakeRepository.getPlaylistById(playlist.id))
    }

    @Test
    fun `delete last playlist returns true`() = runTest {
        val playlist = fakeRepository.addPlaylist(
            PlaylistEntity(name = "Only Playlist", url = "https://example.com/playlist.m3u"),
        )

        val wasLast = useCase(playlist.id)

        assertTrue(wasLast)
    }

    @Test
    fun `delete non-last playlist returns false`() = runTest {
        val playlist1 = fakeRepository.addPlaylist(
            PlaylistEntity(name = "Playlist 1", url = "https://example.com/playlist1.m3u"),
        )
        fakeRepository.addPlaylist(
            PlaylistEntity(name = "Playlist 2", url = "https://example.com/playlist2.m3u"),
        )

        val wasLast = useCase(playlist1.id)

        assertFalse(wasLast)
    }

    @Test
    fun `delete calls repository deletePlaylist with correct id`() = runTest {
        val playlist = fakeRepository.addPlaylist(
            PlaylistEntity(name = "Test", url = "https://example.com/playlist.m3u"),
        )

        useCase(playlist.id)

        assertTrue(fakeRepository.deletePlaylistCalled)
        assertEquals(playlist.id, fakeRepository.lastDeletedPlaylistId)
    }

    @Test
    fun `delete nonexistent playlist returns true when no playlists remain`() = runTest {
        val wasLast = useCase(999L)

        assertTrue(wasLast, "Should return true when no playlists exist after delete")
    }

    @Test
    fun `delete nonexistent playlist returns false when other playlists exist`() = runTest {
        fakeRepository.addPlaylist(
            PlaylistEntity(name = "Remaining", url = "https://example.com/remaining.m3u"),
        )

        val wasLast = useCase(999L)

        assertFalse(wasLast, "Should return false when other playlists still exist")
    }
}
