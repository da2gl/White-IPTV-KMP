package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StubChannelRepository : ChannelRepository {
    override fun getAllChannels(): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun getChannelsByPlaylistId(playlistId: Long): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun getChannelsByGroupId(groupId: Long): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override suspend fun getChannelsList(playlistId: Long): List<ChannelEntity> = emptyList()
    override suspend fun insertChannels(channels: List<ChannelEntity>): List<Long> = emptyList()
    override suspend fun deleteChannelsByPlaylistId(playlistId: Long) {}
    override fun getFavoriteChannels(): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun getFavoriteChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override suspend fun toggleFavoriteStatus(channelId: Long) {}
    override fun searchChannels(query: String): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
        flowOf(emptyList())
    override fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>> =
        flowOf(emptyList())
    override fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
        flowOf(emptyList())
    override fun getAllGroups(): Flow<List<ChannelGroupEntity>> = flowOf(emptyList())
    override fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>> = flowOf(emptyList())
    override fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>> =
        flowOf(emptyList())
    override suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity> = emptyList()
    override suspend fun insertGroups(groups: List<ChannelGroupEntity>): List<Long> = emptyList()
    override suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>) {}
}
