package com.simplevideo.whiteiptv.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Upsert
    suspend fun upsertWatchHistory(entry: WatchHistoryEntity)

    /**
     * Returns recently watched channels sorted by most recent, limited to [limit].
     * Joins with channels table to return full ChannelEntity data.
     */
    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN watch_history wh ON c.id = wh.channelId
        ORDER BY wh.lastWatchedAt DESC
        LIMIT :limit
        """,
    )
    fun getRecentlyWatchedChannels(limit: Int): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM watch_history WHERE channelId = :channelId LIMIT 1")
    suspend fun getWatchHistoryForChannel(channelId: Long): WatchHistoryEntity?

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}
