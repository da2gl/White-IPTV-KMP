package com.simplevideo.whiteiptv.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RecordWatchEventUseCaseTest {

    private lateinit var fakeRepository: FakeWatchHistoryRepository
    private lateinit var useCase: RecordWatchEventUseCase

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeWatchHistoryRepository()
        useCase = RecordWatchEventUseCase(fakeRepository)
    }

    @Test
    fun `invoke delegates to repository with correct parameters`() = runTest {
        useCase(channelId = 42L, playlistId = 10L, durationMs = 5000L)

        assertEquals(1, fakeRepository.recordCallCount)
        assertEquals(42L, fakeRepository.lastRecordedChannelId)
        assertEquals(10L, fakeRepository.lastRecordedPlaylistId)
        assertEquals(5000L, fakeRepository.lastRecordedDurationMs)
    }

    @Test
    fun `invoke with default durationMs passes zero`() = runTest {
        useCase(channelId = 1L, playlistId = 2L)

        assertEquals(0L, fakeRepository.lastRecordedDurationMs)
    }

    @Test
    fun `invoke records entry retrievable from repository`() = runTest {
        useCase(channelId = 7L, playlistId = 3L, durationMs = 10_000L)

        val entry = fakeRepository.getWatchHistoryForChannel(7L)
        assertEquals(7L, entry?.channelId)
        assertEquals(3L, entry?.playlistId)
        assertEquals(10_000L, entry?.watchDurationMs)
    }

    @Test
    fun `multiple invocations record separate entries`() = runTest {
        useCase(channelId = 1L, playlistId = 10L, durationMs = 1000L)
        useCase(channelId = 2L, playlistId = 10L, durationMs = 2000L)
        useCase(channelId = 3L, playlistId = 20L, durationMs = 3000L)

        assertEquals(3, fakeRepository.recordCallCount)
        assertEquals(3L, fakeRepository.lastRecordedChannelId)
    }

    @Test
    fun `repeated invoke for same channel upserts`() = runTest {
        useCase(channelId = 1L, playlistId = 10L, durationMs = 1000L)
        useCase(channelId = 1L, playlistId = 10L, durationMs = 5000L)

        assertEquals(2, fakeRepository.recordCallCount)
        val entry = fakeRepository.getWatchHistoryForChannel(1L)
        assertEquals(5000L, entry?.watchDurationMs)
    }
}
