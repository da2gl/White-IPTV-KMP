package com.simplevideo.whiteiptv.domain.repository

import androidx.paging.PagingSource
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
    suspend fun renameChannel(channelId: Long, newName: String)
    suspend fun deleteChannel(channelId: Long)
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

    // Paged channels (Room PagingSource — auto-invalidates on table changes)
    fun getChannelsPaged(): PagingSource<Int, ChannelEntity>
    fun getChannelsByPlaylistIdPaged(playlistId: Long): PagingSource<Int, ChannelEntity>
    fun getChannelsByGroupIdPaged(groupId: Long): PagingSource<Int, ChannelEntity>
    fun searchChannelsPaged(query: String): PagingSource<Int, ChannelEntity>
    fun searchChannelsByPlaylistIdPaged(query: String, playlistId: Long): PagingSource<Int, ChannelEntity>
    fun searchChannelsByGroupIdPaged(query: String, groupId: Long): PagingSource<Int, ChannelEntity>

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
