package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.repository.FakeChannelRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearFavoritesUseCaseTest {

    private lateinit var fakeChannelRepository: FakeChannelRepository
    private lateinit var clearFavoritesUseCase: ClearFavoritesUseCase

    @BeforeTest
    fun setUp() {
        fakeChannelRepository = FakeChannelRepository()
        clearFavoritesUseCase = ClearFavoritesUseCase(fakeChannelRepository)
    }

    @Test
    fun `invoke calls clearAllFavorites on repository`() = runTest {
        clearFavoritesUseCase()

        assertTrue(fakeChannelRepository.methodCalls.contains("clearAllFavorites"))
    }

    @Test
    fun `invoke clears all favorite flags`() = runTest {
        fakeChannelRepository.setChannels(
            listOf(
                createChannel(id = 1, isFavorite = true),
                createChannel(id = 2, isFavorite = true),
                createChannel(id = 3, isFavorite = false),
            ),
        )

        clearFavoritesUseCase()

        val channels = fakeChannelRepository.getAllChannels().first()
        assertTrue(channels.none { it.isFavorite })
    }

    @Test
    fun `invoke succeeds when no favorites exist`() = runTest {
        fakeChannelRepository.setChannels(
            listOf(
                createChannel(id = 1, isFavorite = false),
                createChannel(id = 2, isFavorite = false),
            ),
        )

        clearFavoritesUseCase()

        val channels = fakeChannelRepository.getAllChannels().first()
        assertEquals(2, channels.size)
        assertTrue(channels.none { it.isFavorite })
    }

    @Test
    fun `invoke succeeds with empty channel list`() = runTest {
        clearFavoritesUseCase()

        val channels = fakeChannelRepository.getAllChannels().first()
        assertTrue(channels.isEmpty())
    }

    private fun createChannel(id: Long, isFavorite: Boolean = false) = ChannelEntity(
        id = id,
        playlistId = 1,
        name = "Channel $id",
        url = "https://example.com/stream$id.m3u8",
        isFavorite = isFavorite,
    )
}
