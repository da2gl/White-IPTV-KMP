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
    fun getAllChannels(): Flow<List<ChannelEntity>>
    fun getChannels(playlistId: Long): Flow<List<ChannelEntity>>
    suspend fun getChannelsList(playlistId: Long): List<ChannelEntity>
    suspend fun insertChannels(channels: List<ChannelEntity>): List<Long>
    suspend fun deleteChannelsByPlaylistId(playlistId: Long)

    fun getFavoriteChannels(): Flow<List<ChannelEntity>>
    suspend fun toggleFavoriteStatus(channelId: Long)

    suspend fun insertGroups(groups: List<ChannelGroupEntity>): List<Long>
    suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>)

    fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>>
    fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>>
    fun getAllGroups(): Flow<List<ChannelGroupEntity>>
    fun getChannelsByGroupId(groupId: Long): Flow<List<ChannelEntity>>
    suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity>
}
