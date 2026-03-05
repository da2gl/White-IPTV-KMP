package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FakeWatchHistoryRepository : WatchHistoryRepository {
    private val historyEntries = mutableMapOf<Long, WatchHistoryEntity>()
    private val channels = mutableMapOf<Long, ChannelEntity>()
    private val _flow = MutableStateFlow(0)

    var recordCallCount = 0
        private set
    var lastRecordedChannelId: Long? = null
        private set
    var lastRecordedPlaylistId: Long? = null
        private set
    var lastRecordedDurationMs: Long? = null
        private set

    fun addChannel(channel: ChannelEntity) {
        channels[channel.id] = channel
    }

    fun addHistoryEntry(entry: WatchHistoryEntity) {
        historyEntries[entry.channelId] = entry
        _flow.value++
    }

    override fun getRecentlyWatchedChannels(limit: Int): Flow<List<ChannelEntity>> =
        _flow.map {
            historyEntries.values
                .sortedByDescending { it.lastWatchedAt }
                .take(limit)
                .mapNotNull { channels[it.channelId] }
        }

    override suspend fun recordWatchEvent(channelId: Long, playlistId: Long, durationMs: Long) {
        recordCallCount++
        lastRecordedChannelId = channelId
        lastRecordedPlaylistId = playlistId
        lastRecordedDurationMs = durationMs

        val entry = WatchHistoryEntity(
            channelId = channelId,
            playlistId = playlistId,
            lastWatchedAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            watchDurationMs = durationMs,
        )
        historyEntries[channelId] = entry
        _flow.value++
    }

    override suspend fun getWatchHistoryForChannel(channelId: Long): WatchHistoryEntity? =
        historyEntries[channelId]
}
