package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for IPTV channel data access
 * Provides operations for channels and favorites
 */
interface ChannelRepository {
    fun getAllChannels(): Flow<List<ChannelEntity>>
    fun getChannels(playlistId: Long): Flow<List<ChannelEntity>>
    suspend fun getChannelsList(playlistId: Long): List<ChannelEntity>
    suspend fun insertChannels(channels: List<ChannelEntity>)
    suspend fun deleteChannelsByPlaylistId(playlistId: Long)

    fun getFavoriteChannels(): Flow<List<ChannelEntity>>
    suspend fun toggleFavoriteStatus(channelId: Long)
}
