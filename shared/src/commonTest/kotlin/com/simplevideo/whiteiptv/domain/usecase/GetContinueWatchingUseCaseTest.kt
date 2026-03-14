package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetContinueWatchingUseCaseTest {

    private lateinit var fakeRepository: FakeWatchHistoryRepository
    private lateinit var useCase: GetContinueWatchingUseCase

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeWatchHistoryRepository()
        useCase = GetContinueWatchingUseCase(fakeRepository)
    }

    @Test
    fun `invoke returns empty list when no watch history`() = runTest {
        val result = useCase().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke maps channels to ContinueWatchingItems`() = runTest {
        val channel = createChannel(1L, "Test Channel")
        fakeRepository.addChannel(channel)
        fakeRepository.addHistoryEntry(
            WatchHistoryEntity(channelId = 1L, playlistId = 10L, lastWatchedAt = 1000L),
        )

        val result = useCase().first()
        assertEquals(1, result.size)
        assertEquals(channel, result[0].channel)
    }

    @Test
    fun `invoke sets progress to zero for live IPTV`() = runTest {
        val channel = createChannel(1L, "Live Channel")
        fakeRepository.addChannel(channel)
        fakeRepository.addHistoryEntry(
            WatchHistoryEntity(channelId = 1L, playlistId = 10L, lastWatchedAt = 1000L),
        )

        val result = useCase().first()
        assertEquals(0f, result[0].progress)
    }

    @Test
    fun `invoke sets timeLeft to empty string`() = runTest {
        val channel = createChannel(1L, "Live Channel")
        fakeRepository.addChannel(channel)
        fakeRepository.addHistoryEntry(
            WatchHistoryEntity(channelId = 1L, playlistId = 10L, lastWatchedAt = 1000L),
        )

        val result = useCase().first()
        assertEquals("", result[0].timeLeft)
    }

    @Test
    fun `invoke returns channels sorted by recency`() = runTest {
        val channel1 = createChannel(1L, "Old Channel")
        val channel2 = createChannel(2L, "Recent Channel")
        val channel3 = createChannel(3L, "Most Recent")
        fakeRepository.addChannel(channel1)
        fakeRepository.addChannel(channel2)
        fakeRepository.addChannel(channel3)

        fakeRepository.addHistoryEntry(
            WatchHistoryEntity(channelId = 1L, playlistId = 10L, lastWatchedAt = 100L),
        )
        fakeRepository.addHistoryEntry(
            WatchHistoryEntity(channelId = 2L, playlistId = 10L, lastWatchedAt = 200L),
        )
        fakeRepository.addHistoryEntry(
            WatchHistoryEntity(channelId = 3L, playlistId = 10L, lastWatchedAt = 300L),
        )

        val result = useCase().first()
        assertEquals(3, result.size)
        assertEquals(3L, result[0].channel.id)
        assertEquals(2L, result[1].channel.id)
        assertEquals(1L, result[2].channel.id)
    }

    @Test
    fun `invoke limits results to 10 channels`() = runTest {
        repeat(15) { i ->
            val channel = createChannel(i.toLong() + 1, "Channel ${i + 1}")
            fakeRepository.addChannel(channel)
            fakeRepository.addHistoryEntry(
                WatchHistoryEntity(
                    channelId = channel.id,
                    playlistId = 10L,
                    lastWatchedAt = i.toLong() * 100,
                ),
            )
        }

        val result = useCase().first()
        assertEquals(10, result.size)
    }

    @Test
    fun `invoke limits to 10 most recent channels`() = runTest {
        repeat(15) { i ->
            val channel = createChannel(i.toLong() + 1, "Channel ${i + 1}")
            fakeRepository.addChannel(channel)
            fakeRepository.addHistoryEntry(
                WatchHistoryEntity(
                    channelId = channel.id,
                    playlistId = 10L,
                    lastWatchedAt = i.toLong() * 100,
                ),
            )
        }

        val result = useCase().first()
        assertEquals(15L, result[0].channel.id, "Most recent channel should be first")
        assertEquals(6L, result[9].channel.id, "10th result should be channel 6 (oldest of top 10)")
    }

    @Test
    fun `invoke preserves channel metadata in mapping`() = runTest {
        val channel = ChannelEntity(
            id = 5L,
            playlistId = 10L,
            name = "HD Channel",
            url = "http://example.com/stream/5",
            logoUrl = "http://example.com/logo.png",
            tvgId = "hd.channel",
            isFavorite = true,
        )
        fakeRepository.addChannel(channel)
        fakeRepository.addHistoryEntry(
            WatchHistoryEntity(channelId = 5L, playlistId = 10L, lastWatchedAt = 1000L),
        )

        val result = useCase().first()
        assertEquals(1, result.size)
        val item = result[0]
        assertEquals("HD Channel", item.channel.name)
        assertEquals("http://example.com/logo.png", item.channel.logoUrl)
        assertEquals("hd.channel", item.channel.tvgId)
        assertTrue(item.channel.isFavorite)
    }

    private fun createChannel(id: Long, name: String, playlistId: Long = 10L): ChannelEntity =
        ChannelEntity(
            id = id,
            playlistId = playlistId,
            name = name,
            url = "http://example.com/stream/$id",
        )
}
