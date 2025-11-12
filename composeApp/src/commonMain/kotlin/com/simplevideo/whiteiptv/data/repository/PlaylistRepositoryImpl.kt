package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Implementation of PlaylistRepository
 * Provides simple CRUD operations delegating to DAO
 * All business logic is in UseCases
 */
class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
) : PlaylistRepository {

    override suspend fun hasPlaylist(): Boolean {
        return playlistDao.getPlaylistCount() > 0
    }

    override suspend fun getPlaylistById(id: Long): PlaylistEntity? {
        return playlistDao.getPlaylistById(id)
    }

    override suspend fun getPlaylistByUrl(url: String): PlaylistEntity? {
        return playlistDao.getPlaylistByUrl(url)
    }

    override fun getPlaylists(): Flow<List<PlaylistEntity>> {
        return playlistDao.getPlaylists()
    }

    override suspend fun insertPlaylist(playlist: PlaylistEntity): Long {
        return playlistDao.insertPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: PlaylistEntity) {
        playlistDao.updatePlaylist(playlist)
    }

    override suspend fun deletePlaylist(id: Long) {
        playlistDao.deletePlaylist(id)
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
