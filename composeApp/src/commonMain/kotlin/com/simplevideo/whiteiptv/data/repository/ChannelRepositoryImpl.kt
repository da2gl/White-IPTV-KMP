package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
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

    override suspend fun insertChannels(channels: List<ChannelEntity>) {
        playlistDao.insertChannels(channels)
    }

    override suspend fun deleteChannelsByPlaylistId(playlistId: Long) {
        playlistDao.deleteChannelsByPlaylistId(playlistId)
    }

    override fun getFavoriteChannels(): Flow<List<ChannelEntity>> {
        return playlistDao.getFavoriteChannels()
    }

    override suspend fun toggleFavoriteStatus(channelId: Long) {
        playlistDao.toggleFavoriteStatus(channelId)
    }
}
