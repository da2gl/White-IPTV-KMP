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
