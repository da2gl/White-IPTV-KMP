package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WatchHistoryRepositoryImplTest {

    private lateinit var fakeDao: FakeWatchHistoryDao
    private lateinit var repository: WatchHistoryRepositoryImpl

    @BeforeTest
    fun setUp() {
        fakeDao = FakeWatchHistoryDao()
        repository = WatchHistoryRepositoryImpl(fakeDao)
    }

    @Test
    fun `recordWatchEvent creates entry with correct channelId and playlistId`() = runTest {
        repository.recordWatchEvent(channelId = 1L, playlistId = 10L, durationMs = 5000L)

        val entry = fakeDao.getStoredEntry(1L)
        assertNotNull(entry)
        assertEquals(1L, entry.channelId)
        assertEquals(10L, entry.playlistId)
        assertEquals(5000L, entry.watchDurationMs)
    }

    @Test
    fun `recordWatchEvent sets lastWatchedAt to current time`() = runTest {
        val beforeMs = currentTimeMillis()
        repository.recordWatchEvent(channelId = 1L, playlistId = 10L, durationMs = 0L)
        val afterMs = currentTimeMillis()

        val entry = fakeDao.getStoredEntry(1L)
        assertNotNull(entry)
        assertTrue(entry.lastWatchedAt in beforeMs..afterMs)
    }

    @Test
    fun `recordWatchEvent upserts same channel updating timestamp`() = runTest {
        repository.recordWatchEvent(channelId = 1L, playlistId = 10L, durationMs = 1000L)
        val firstEntry = fakeDao.getStoredEntry(1L)

        repository.recordWatchEvent(channelId = 1L, playlistId = 10L, durationMs = 5000L)
        val secondEntry = fakeDao.getStoredEntry(1L)

        assertNotNull(firstEntry)
        assertNotNull(secondEntry)
        assertEquals(5000L, secondEntry.watchDurationMs)
        assertTrue(secondEntry.lastWatchedAt >= firstEntry.lastWatchedAt)
        assertEquals(1, fakeDao.getAllEntries().size, "Should have one entry after upsert")
    }

    @Test
    fun `recordWatchEvent stores multiple different channels`() = runTest {
        repository.recordWatchEvent(channelId = 1L, playlistId = 10L, durationMs = 0L)
        repository.recordWatchEvent(channelId = 2L, playlistId = 10L, durationMs = 0L)
        repository.recordWatchEvent(channelId = 3L, playlistId = 20L, durationMs = 0L)

        assertEquals(3, fakeDao.getAllEntries().size)
    }

    @Test
    fun `getRecentlyWatchedChannels returns channels in recency order`() = runTest {
        val channel1 = createChannel(1L, "Channel 1")
        val channel2 = createChannel(2L, "Channel 2")
        val channel3 = createChannel(3L, "Channel 3")
        fakeDao.addChannel(channel1)
        fakeDao.addChannel(channel2)
        fakeDao.addChannel(channel3)

        repository.recordWatchEvent(channelId = 1L, playlistId = 10L, durationMs = 0L)
        repository.recordWatchEvent(channelId = 2L, playlistId = 10L, durationMs = 0L)
        repository.recordWatchEvent(channelId = 3L, playlistId = 10L, durationMs = 0L)

        val result = repository.getRecentlyWatchedChannels(limit = 10).first()
        assertEquals(3, result.size)
        assertEquals(3L, result[0].id, "Most recent should be first")
        assertEquals(2L, result[1].id)
        assertEquals(1L, result[2].id)
    }

    @Test
    fun `getRecentlyWatchedChannels respects limit`() = runTest {
        repeat(5) { i ->
            val channel = createChannel(i.toLong() + 1, "Channel ${i + 1}")
            fakeDao.addChannel(channel)
            repository.recordWatchEvent(channelId = channel.id, playlistId = 10L, durationMs = 0L)
        }

        val result = repository.getRecentlyWatchedChannels(limit = 3).first()
        assertEquals(3, result.size)
    }

    @Test
    fun `getRecentlyWatchedChannels returns empty when no history`() = runTest {
        val result = repository.getRecentlyWatchedChannels(limit = 10).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getWatchHistoryForChannel returns null for unwatched channel`() = runTest {
        val result = repository.getWatchHistoryForChannel(999L)
        assertNull(result)
    }

    @Test
    fun `getWatchHistoryForChannel returns entry for watched channel`() = runTest {
        repository.recordWatchEvent(channelId = 1L, playlistId = 10L, durationMs = 3000L)

        val result = repository.getWatchHistoryForChannel(1L)
        assertNotNull(result)
        assertEquals(1L, result.channelId)
        assertEquals(3000L, result.watchDurationMs)
    }

    private fun createChannel(id: Long, name: String, playlistId: Long = 10L): ChannelEntity =
        ChannelEntity(
            id = id,
            playlistId = playlistId,
            name = name,
            url = "http://example.com/stream/$id",
        )

    private fun currentTimeMillis(): Long =
        kotlin.time.Clock.System.now().toEpochMilliseconds()
}
