package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.WatchHistoryDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWatchHistoryDao : WatchHistoryDao {
    private val historyEntries = mutableMapOf<Long, WatchHistoryEntity>()
    private val channels = mutableMapOf<Long, ChannelEntity>()
    private val _flow = MutableStateFlow(0) // trigger reemission on changes

    fun addChannel(channel: ChannelEntity) {
        channels[channel.id] = channel
    }

    override suspend fun upsertWatchHistory(entry: WatchHistoryEntity) {
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

    override suspend fun getWatchHistoryForChannel(channelId: Long): WatchHistoryEntity? =
        historyEntries[channelId]

    override suspend fun clearAllHistory() {
        historyEntries.clear()
        _flow.value++
    }

    fun getStoredEntry(channelId: Long): WatchHistoryEntity? = historyEntries[channelId]

    fun getAllEntries(): List<WatchHistoryEntity> = historyEntries.values.toList()
}
