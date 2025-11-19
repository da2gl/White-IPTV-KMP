package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
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

    override suspend fun importPlaylistData(
        playlist: PlaylistEntity,
        groups: List<ChannelGroupEntity>,
        channels: List<ChannelEntity>,
        crossRefsProvider: (channelIds: List<Long>, groupIds: List<Long>) -> List<ChannelGroupCrossRef>,
    ): Long {
        // Insert playlist first to get the ID
        val playlistId = playlistDao.insertPlaylist(playlist)

        // Update entities with actual playlistId
        val groupsWithId = groups.map { it.copy(playlistId = playlistId) }
        val channelsWithId = channels.map { it.copy(playlistId = playlistId) }

        // Get group IDs after upsert (for cross-refs)
        val groupIds = if (groupsWithId.isNotEmpty()) {
            playlistDao.upsertGroups(groupsWithId)
        } else {
            emptyList()
        }

        // Insert channels and get their IDs
        val channelIds = playlistDao.insertChannels(channelsWithId)

        // Create cross-refs with actual IDs
        val crossRefs = crossRefsProvider(channelIds, groupIds)

        // Insert remaining data in transaction for atomicity
        if (crossRefs.isNotEmpty()) {
            playlistDao.insertChannelGroupCrossRefs(crossRefs)
        }

        return playlistId
    }

    override suspend fun updatePlaylistData(
        playlist: PlaylistEntity,
        groups: List<ChannelGroupEntity>,
        channels: List<ChannelEntity>,
        crossRefsProvider: (channelIds: List<Long>, groupIds: List<Long>) -> List<ChannelGroupCrossRef>,
    ) {
        // Update playlist metadata
        playlistDao.updatePlaylist(playlist)

        // Delete old channels (cascade will delete cross-refs)
        playlistDao.deleteChannelsByPlaylistId(playlist.id)

        // Ensure entities have correct playlistId
        val groupsWithId = groups.map { it.copy(playlistId = playlist.id) }
        val channelsWithId = channels.map { it.copy(playlistId = playlist.id) }

        // Get group IDs after upsert
        val groupIds = if (groupsWithId.isNotEmpty()) {
            playlistDao.upsertGroups(groupsWithId)
        } else {
            emptyList()
        }

        // Insert channels and get their IDs
        val channelIds = playlistDao.insertChannels(channelsWithId)

        // Create and insert cross-refs with actual IDs
        val crossRefs = crossRefsProvider(channelIds, groupIds)
        if (crossRefs.isNotEmpty()) {
            playlistDao.insertChannelGroupCrossRefs(crossRefs)
        }
    }
}
