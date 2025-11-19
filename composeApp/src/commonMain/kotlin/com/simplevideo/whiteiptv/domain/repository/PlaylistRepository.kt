package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for IPTV playlist data access
 * Provides CRUD operations for playlists only
 */
interface PlaylistRepository {
    suspend fun hasPlaylist(): Boolean
    suspend fun getPlaylistById(id: Long): PlaylistEntity?
    suspend fun getPlaylistByUrl(url: String): PlaylistEntity?
    fun getPlaylists(): Flow<List<PlaylistEntity>>
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    suspend fun deletePlaylist(id: Long)

    /**
     * Import playlist with all related data in a single transaction
     * Ensures atomicity: either all data is saved or nothing
     *
     * @return Pair of (playlistId, channelIds) for cross-ref creation
     */
    suspend fun importPlaylistData(
        playlist: PlaylistEntity,
        groups: List<ChannelGroupEntity>,
        channels: List<ChannelEntity>,
        crossRefsProvider: (channelIds: List<Long>, groupIds: List<Long>) -> List<ChannelGroupCrossRef>,
    ): Long

    /**
     * Update existing playlist with new data in a single transaction
     */
    suspend fun updatePlaylistData(
        playlist: PlaylistEntity,
        groups: List<ChannelGroupEntity>,
        channels: List<ChannelEntity>,
        crossRefsProvider: (channelIds: List<Long>, groupIds: List<Long>) -> List<ChannelGroupCrossRef>,
    )
}
