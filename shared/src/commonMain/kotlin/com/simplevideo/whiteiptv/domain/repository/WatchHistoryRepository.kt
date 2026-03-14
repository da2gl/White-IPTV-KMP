package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

interface WatchHistoryRepository {
    fun getRecentlyWatchedChannels(limit: Int): Flow<List<ChannelEntity>>
    suspend fun recordWatchEvent(channelId: Long, playlistId: Long, durationMs: Long)
    suspend fun getWatchHistoryForChannel(channelId: Long): WatchHistoryEntity?
}
