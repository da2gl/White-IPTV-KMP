package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Implementation of ChannelRepository
 * Provides channel operations delegating to DAO
 * All business logic is in UseCases
 */
class ChannelRepositoryImpl(
    private val playlistDao: PlaylistDao,
) : ChannelRepository {

    // Channels
    override fun getAllChannels(): Flow<List<ChannelEntity>> =
        playlistDao.getAllChannels()

    override fun getChannelsByPlaylistId(playlistId: Long): Flow<List<ChannelEntity>> =
        playlistDao.getChannelsByPlaylistId(playlistId)

    override fun getChannelsByGroupId(groupId: Long): Flow<List<ChannelEntity>> =
        playlistDao.getChannelsByGroupId(groupId)

    override suspend fun getChannelsList(playlistId: Long): List<ChannelEntity> =
        playlistDao.getChannelsByPlaylistId(playlistId).firstOrNull() ?: emptyList()

    override suspend fun insertChannels(channels: List<ChannelEntity>): List<Long> =
        playlistDao.insertChannels(channels)

    override suspend fun deleteChannelsByPlaylistId(playlistId: Long) {
        playlistDao.deleteChannelsByPlaylistId(playlistId)
    }

    // Favorites
    override fun getFavoriteChannels(): Flow<List<ChannelEntity>> =
        playlistDao.getFavoriteChannels()

    override fun getFavoriteChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>> =
        playlistDao.getFavoriteChannelsByPlaylist(playlistId)

    override suspend fun toggleFavoriteStatus(channelId: Long) {
        playlistDao.toggleFavoriteStatus(channelId)
    }

    override suspend fun clearAllFavorites() {
        playlistDao.clearAllFavorites()
    }

    // Search
    override fun searchChannels(query: String): Flow<List<ChannelEntity>> =
        playlistDao.searchChannels(query)

    override fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
        playlistDao.searchChannelsByPlaylistId(query, playlistId)

    override fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>> =
        playlistDao.searchChannelsByGroupId(query, groupId)

    override fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>> =
        playlistDao.searchFavoriteChannels(query)

    override fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
        playlistDao.searchFavoriteChannelsByPlaylist(query, playlistId)

    // Paged channels
    override suspend fun getChannelsPaged(limit: Int, offset: Int): List<ChannelEntity> =
        playlistDao.getChannelsPaged(limit, offset)

    override suspend fun getChannelsCount(): Int =
        playlistDao.getChannelsCount()

    override suspend fun getChannelsByPlaylistIdPaged(playlistId: Long, limit: Int, offset: Int): List<ChannelEntity> =
        playlistDao.getChannelsByPlaylistIdPaged(playlistId, limit, offset)

    override suspend fun getChannelsByPlaylistIdCount(playlistId: Long): Int =
        playlistDao.getChannelsByPlaylistIdCount(playlistId)

    override suspend fun getChannelsByGroupIdPaged(groupId: Long, limit: Int, offset: Int): List<ChannelEntity> =
        playlistDao.getChannelsByGroupIdPaged(groupId, limit, offset)

    override suspend fun getChannelsByGroupIdCount(groupId: Long): Int =
        playlistDao.getChannelsByGroupIdCount(groupId)

    override suspend fun searchChannelsPaged(query: String, limit: Int, offset: Int): List<ChannelEntity> =
        playlistDao.searchChannelsPaged(query, limit, offset)

    override suspend fun searchChannelsCount(query: String): Int =
        playlistDao.searchChannelsCount(query)

    override suspend fun searchChannelsByPlaylistIdPaged(
        query: String,
        playlistId: Long,
        limit: Int,
        offset: Int,
    ): List<ChannelEntity> =
        playlistDao.searchChannelsByPlaylistIdPaged(query, playlistId, limit, offset)

    override suspend fun searchChannelsByPlaylistIdCount(query: String, playlistId: Long): Int =
        playlistDao.searchChannelsByPlaylistIdCount(query, playlistId)

    override suspend fun searchChannelsByGroupIdPaged(
        query: String,
        groupId: Long,
        limit: Int,
        offset: Int,
    ): List<ChannelEntity> =
        playlistDao.searchChannelsByGroupIdPaged(query, groupId, limit, offset)

    override suspend fun searchChannelsByGroupIdCount(query: String, groupId: Long): Int =
        playlistDao.searchChannelsByGroupIdCount(query, groupId)

    // Groups
    override fun getAllGroups(): Flow<List<ChannelGroupEntity>> =
        playlistDao.getAllGroups()

    override fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>> =
        playlistDao.getTopGroups(limit)

    override fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>> =
        playlistDao.getTopGroupsByPlaylist(playlistId, limit)

    override suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity> =
        playlistDao.getRandomChannelsByGroupId(groupId, limit)

    override suspend fun insertGroups(groups: List<ChannelGroupEntity>): List<Long> =
        playlistDao.upsertGroups(groups)

    override suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>) {
        playlistDao.insertChannelGroupCrossRefs(refs)
    }
}
