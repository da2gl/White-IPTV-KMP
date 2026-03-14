package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.repository.FakeChannelRepository
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetFavoritesUseCaseTest {

    private lateinit var fakeRepository: FakeChannelRepository
    private lateinit var useCase: GetFavoritesUseCase

    private val testChannels = listOf(
        createChannel(1L, "CNN News", playlistId = 1L, isFavorite = true),
        createChannel(2L, "BBC World", playlistId = 1L, isFavorite = true),
        createChannel(3L, "ESPN Sports", playlistId = 2L, isFavorite = false),
        createChannel(4L, "Discovery Channel", playlistId = 2L, isFavorite = true),
        createChannel(5L, "CNN International", playlistId = 1L, isFavorite = true),
        createChannel(6L, "National Geographic", playlistId = 2L, isFavorite = false),
    )

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeChannelRepository()
        useCase = GetFavoritesUseCase(fakeRepository)
        fakeRepository.setChannels(testChannels)
    }

    // --- Empty query routing (should call non-search methods) ---

    @Test
    fun `empty query with All selection calls getFavoriteChannels`() = runTest {
        useCase(selection = PlaylistSelection.All, query = "").first()

        assertTrue(fakeRepository.methodCalls.contains("getFavoriteChannels"))
        assertTrue(fakeRepository.methodCalls.none { it.startsWith("search") })
    }

    @Test
    fun `empty query with Selected playlist calls getFavoriteChannelsByPlaylist`() = runTest {
        useCase(selection = PlaylistSelection.Selected(1L), query = "").first()

        assertTrue(fakeRepository.methodCalls.contains("getFavoriteChannelsByPlaylist(1)"))
        assertTrue(fakeRepository.methodCalls.none { it.startsWith("search") })
    }

    @Test
    fun `default parameters call getFavoriteChannels`() = runTest {
        useCase().first()

        assertTrue(fakeRepository.methodCalls.contains("getFavoriteChannels"))
    }

    // --- Non-empty query routing (should call search methods) ---

    @Test
    fun `non-empty query with All selection calls searchFavoriteChannels`() = runTest {
        useCase(selection = PlaylistSelection.All, query = "CNN").first()

        assertTrue(fakeRepository.methodCalls.contains("searchFavoriteChannels(CNN)"))
        assertTrue(fakeRepository.methodCalls.none { it == "getFavoriteChannels" })
    }

    @Test
    fun `non-empty query with Selected playlist calls searchFavoriteChannelsByPlaylist`() = runTest {
        useCase(selection = PlaylistSelection.Selected(1L), query = "CNN").first()

        assertTrue(fakeRepository.methodCalls.contains("searchFavoriteChannelsByPlaylist(CNN, 1)"))
    }

    // --- Query trimming ---

    @Test
    fun `whitespace-only query is treated as empty`() = runTest {
        useCase(selection = PlaylistSelection.All, query = "   ").first()

        assertTrue(fakeRepository.methodCalls.contains("getFavoriteChannels"))
        assertTrue(fakeRepository.methodCalls.none { it.startsWith("search") })
    }

    @Test
    fun `query with surrounding whitespace is trimmed`() = runTest {
        useCase(selection = PlaylistSelection.All, query = "  CNN  ").first()

        assertTrue(fakeRepository.methodCalls.contains("searchFavoriteChannels(CNN)"))
    }

    // --- Result correctness ---

    @Test
    fun `search returns matching favorite channels for All selection`() = runTest {
        val result = useCase(selection = PlaylistSelection.All, query = "CNN").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.name.contains("CNN", ignoreCase = true) })
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `search returns matching favorites filtered by playlist`() = runTest {
        val result = useCase(selection = PlaylistSelection.Selected(1L), query = "CNN").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.playlistId == 1L })
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `search does not return non-favorite channels`() = runTest {
        val result = useCase(selection = PlaylistSelection.All, query = "ESPN").first()

        assertTrue(result.isEmpty(), "ESPN is not a favorite, should not appear in results")
    }

    @Test
    fun `search with no matches returns empty list`() = runTest {
        val result = useCase(selection = PlaylistSelection.All, query = "NonExistent").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `empty query returns all favorites in scope`() = runTest {
        val result = useCase(selection = PlaylistSelection.All, query = "").first()

        assertEquals(4, result.size)
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `empty query with Selected returns favorites for that playlist only`() = runTest {
        val result = useCase(selection = PlaylistSelection.Selected(2L), query = "").first()

        assertEquals(1, result.size)
        assertEquals("Discovery Channel", result[0].name)
        assertTrue(result.all { it.playlistId == 2L && it.isFavorite })
    }

    @Test
    fun `search is case insensitive`() = runTest {
        val result = useCase(selection = PlaylistSelection.All, query = "cnn").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.name.contains("CNN", ignoreCase = true) })
    }

    private fun createChannel(
        id: Long,
        name: String,
        playlistId: Long = 1L,
        isFavorite: Boolean = false,
    ): ChannelEntity = ChannelEntity(
        id = id,
        playlistId = playlistId,
        name = name,
        url = "http://example.com/stream/$id",
        isFavorite = isFavorite,
    )
}
