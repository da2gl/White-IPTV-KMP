package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for IPTV playlist data access
 * Provides CRUD operations for playlists and channels
 */
interface PlaylistRepository {
    suspend fun hasPlaylist(): Boolean
    suspend fun getPlaylistById(id: Long): PlaylistEntity?
    suspend fun getPlaylistByUrl(url: String): PlaylistEntity?
    fun getPlaylists(): Flow<List<PlaylistEntity>>
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    suspend fun deletePlaylist(id: Long)

    fun getChannels(playlistId: Long): Flow<List<ChannelEntity>>
    suspend fun getChannelsList(playlistId: Long): List<ChannelEntity>
    suspend fun insertChannels(channels: List<ChannelEntity>)
    suspend fun deleteChannelsByPlaylistId(playlistId: Long)
}
