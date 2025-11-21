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

    override fun getAllChannels(): Flow<List<ChannelEntity>> {
        return playlistDao.getAllChannels()
    }

    override fun getChannels(playlistId: Long): Flow<List<ChannelEntity>> {
        return playlistDao.getChannels(playlistId)
    }

    override suspend fun getChannelsList(playlistId: Long): List<ChannelEntity> {
        return playlistDao.getChannels(playlistId).firstOrNull() ?: emptyList()
    }

    override suspend fun insertChannels(channels: List<ChannelEntity>): List<Long> {
        return playlistDao.insertChannels(channels)
    }

    override suspend fun deleteChannelsByPlaylistId(playlistId: Long) {
        playlistDao.deleteChannelsByPlaylistId(playlistId)
    }

    override fun getFavoriteChannels(): Flow<List<ChannelEntity>> {
        return playlistDao.getFavoriteChannels()
    }

    override fun getFavoriteChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>> {
        return playlistDao.getFavoriteChannelsByPlaylist(playlistId)
    }

    override suspend fun toggleFavoriteStatus(channelId: Long) {
        playlistDao.toggleFavoriteStatus(channelId)
    }

    override suspend fun insertGroups(groups: List<ChannelGroupEntity>): List<Long> {
        return playlistDao.upsertGroups(groups)
    }

    override suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>) {
        playlistDao.insertChannelGroupCrossRefs(refs)
    }

    override fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>> {
        return playlistDao.getTopGroups(limit)
    }

    override fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>> {
        return playlistDao.getTopGroupsByPlaylist(playlistId, limit)
    }

    override fun getAllGroups(): Flow<List<ChannelGroupEntity>> {
        return playlistDao.getAllGroups()
    }

    override fun getChannelsByGroupId(groupId: Long): Flow<List<ChannelEntity>> {
        return playlistDao.getChannelsByGroupId(groupId)
    }

    override suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity> {
        return playlistDao.getRandomChannelsByGroupId(groupId, limit)
    }
}
