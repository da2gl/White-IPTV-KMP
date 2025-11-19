package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

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
}
