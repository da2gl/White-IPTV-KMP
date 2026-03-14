package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.WatchHistoryDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class WatchHistoryRepositoryImpl(
    private val watchHistoryDao: WatchHistoryDao,
) : WatchHistoryRepository {

    override fun getRecentlyWatchedChannels(limit: Int): Flow<List<ChannelEntity>> =
        watchHistoryDao.getRecentlyWatchedChannels(limit)

    override suspend fun recordWatchEvent(channelId: Long, playlistId: Long, durationMs: Long) {
        val entry = WatchHistoryEntity(
            channelId = channelId,
            playlistId = playlistId,
            lastWatchedAt = System.now().toEpochMilliseconds(),
            watchDurationMs = durationMs,
        )
        watchHistoryDao.upsertWatchHistory(entry)
    }

    override suspend fun getWatchHistoryForChannel(channelId: Long): WatchHistoryEntity? =
        watchHistoryDao.getWatchHistoryForChannel(channelId)
}
