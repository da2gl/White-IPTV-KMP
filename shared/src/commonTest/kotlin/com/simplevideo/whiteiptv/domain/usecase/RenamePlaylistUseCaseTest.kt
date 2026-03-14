package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.data.repository.FakePlaylistRepository
import com.simplevideo.whiteiptv.domain.exception.PlaylistException
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RenamePlaylistUseCaseTest {

    private lateinit var fakeRepository: FakePlaylistRepository
    private lateinit var useCase: RenamePlaylistUseCase

    @BeforeTest
    fun setUp() {
        fakeRepository = FakePlaylistRepository()
        useCase = RenamePlaylistUseCase(fakeRepository)
    }

    @Test
    fun `rename updates playlist name in repository`() = runTest {
        val playlist = fakeRepository.addPlaylist(
            PlaylistEntity(name = "Old Name", url = "https://example.com/playlist.m3u"),
        )

        useCase(playlist.id, "New Name")

        val updated = fakeRepository.getPlaylistById(playlist.id)
        assertEquals("New Name", updated?.name)
    }

    @Test
    fun `rename trims whitespace from name`() = runTest {
        val playlist = fakeRepository.addPlaylist(
            PlaylistEntity(name = "Old Name", url = "https://example.com/playlist.m3u"),
        )

        useCase(playlist.id, "  Trimmed Name  ")

        val updated = fakeRepository.getPlaylistById(playlist.id)
        assertEquals("Trimmed Name", updated?.name)
    }

    @Test
    fun `rename preserves other playlist fields`() = runTest {
        val playlist = fakeRepository.addPlaylist(
            PlaylistEntity(
                name = "Old Name",
                url = "https://example.com/playlist.m3u",
                channelCount = 42,
                userAgent = "TestAgent",
            ),
        )

        useCase(playlist.id, "New Name")

        val updated = fakeRepository.getPlaylistById(playlist.id)!!
        assertEquals("New Name", updated.name)
        assertEquals("https://example.com/playlist.m3u", updated.url)
        assertEquals(42, updated.channelCount)
        assertEquals("TestAgent", updated.userAgent)
    }

    @Test
    fun `rename with blank name throws IllegalArgumentException`() = runTest {
        val playlist = fakeRepository.addPlaylist(
            PlaylistEntity(name = "Old Name", url = "https://example.com/playlist.m3u"),
        )

        assertFailsWith<IllegalArgumentException> {
            useCase(playlist.id, "")
        }
    }

    @Test
    fun `rename with whitespace-only name throws IllegalArgumentException`() = runTest {
        val playlist = fakeRepository.addPlaylist(
            PlaylistEntity(name = "Old Name", url = "https://example.com/playlist.m3u"),
        )

        assertFailsWith<IllegalArgumentException> {
            useCase(playlist.id, "   ")
        }
    }

    @Test
    fun `rename nonexistent playlist throws NotFound`() = runTest {
        assertFailsWith<PlaylistException.NotFound> {
            useCase(999L, "New Name")
        }
    }

    @Test
    fun `rename does not modify original name on validation failure`() = runTest {
        val playlist = fakeRepository.addPlaylist(
            PlaylistEntity(name = "Original", url = "https://example.com/playlist.m3u"),
        )

        runCatching { useCase(playlist.id, "") }

        val unchanged = fakeRepository.getPlaylistById(playlist.id)
        assertEquals("Original", unchanged?.name)
    }
}
