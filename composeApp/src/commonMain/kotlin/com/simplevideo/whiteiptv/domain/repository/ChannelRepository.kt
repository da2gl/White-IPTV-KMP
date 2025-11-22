package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for IPTV channel data access
 * Provides operations for channels and favorites
 */
interface ChannelRepository {
    // Channels
    fun getAllChannels(): Flow<List<ChannelEntity>>
    fun getChannelsByPlaylistId(playlistId: Long): Flow<List<ChannelEntity>>
    fun getChannelsByGroupId(groupId: Long): Flow<List<ChannelEntity>>
    suspend fun getChannelsList(playlistId: Long): List<ChannelEntity>
    suspend fun insertChannels(channels: List<ChannelEntity>): List<Long>
    suspend fun deleteChannelsByPlaylistId(playlistId: Long)

    // Favorites
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>
    fun getFavoriteChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>>
    suspend fun toggleFavoriteStatus(channelId: Long)

    // Groups
    fun getAllGroups(): Flow<List<ChannelGroupEntity>>
    fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>>
    fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>>
    suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity>
    suspend fun insertGroups(groups: List<ChannelGroupEntity>): List<Long>
    suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>)
}
