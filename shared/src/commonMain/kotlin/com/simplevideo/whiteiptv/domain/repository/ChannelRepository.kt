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
    suspend fun clearAllFavorites()

    // Search
    fun searchChannels(query: String): Flow<List<ChannelEntity>>
    fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>>
    fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>>
    fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>>
    fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>>

    // Paged channels
    suspend fun getChannelsPaged(limit: Int, offset: Int): List<ChannelEntity>
    suspend fun getChannelsCount(): Int
    suspend fun getChannelsByPlaylistIdPaged(playlistId: Long, limit: Int, offset: Int): List<ChannelEntity>
    suspend fun getChannelsByPlaylistIdCount(playlistId: Long): Int
    suspend fun getChannelsByGroupIdPaged(groupId: Long, limit: Int, offset: Int): List<ChannelEntity>
    suspend fun getChannelsByGroupIdCount(groupId: Long): Int
    suspend fun searchChannelsPaged(query: String, limit: Int, offset: Int): List<ChannelEntity>
    suspend fun searchChannelsCount(query: String): Int
    suspend fun searchChannelsByPlaylistIdPaged(
        query: String,
        playlistId: Long,
        limit: Int,
        offset: Int,
    ): List<ChannelEntity>
    suspend fun searchChannelsByPlaylistIdCount(query: String, playlistId: Long): Int
    suspend fun searchChannelsByGroupIdPaged(query: String, groupId: Long, limit: Int, offset: Int): List<ChannelEntity>
    suspend fun searchChannelsByGroupIdCount(query: String, groupId: Long): Int

    // Groups
    fun getAllGroups(): Flow<List<ChannelGroupEntity>>
    fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>>
    fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>>
    fun getTopValidGroups(limit: Int): Flow<List<ChannelGroupEntity>>
    fun getTopValidGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>>
    suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity>
    suspend fun getRandomChannelsForGroups(groupIds: List<Long>, limitPerGroup: Int): Map<Long, List<ChannelEntity>>
    suspend fun insertGroups(groups: List<ChannelGroupEntity>): List<Long>
    suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>)
}
