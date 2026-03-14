package com.simplevideo.whiteiptv.data.local

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelWithGroups
import com.simplevideo.whiteiptv.data.local.model.GroupWithChannels
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Minimal fake PlaylistDao for unit testing ViewModels that depend on use cases backed by PlaylistDao.
 * Only implements methods needed by GetChannelByIdUseCase and GetAdjacentChannelUseCase.
 */
class FakePlaylistDao : PlaylistDao {

    private val channels = mutableMapOf<Long, ChannelEntity>()

    fun addChannel(channel: ChannelEntity) {
        channels[channel.id] = channel
    }

    override suspend fun getChannelById(channelId: Long): ChannelEntity? = channels[channelId]

    override suspend fun getNextChannel(playlistId: Long, currentChannelId: Long): ChannelEntity? =
        channels.values
            .filter { it.playlistId == playlistId && it.id > currentChannelId }
            .minByOrNull { it.id }

    override suspend fun getPreviousChannel(playlistId: Long, currentChannelId: Long): ChannelEntity? =
        channels.values
            .filter { it.playlistId == playlistId && it.id < currentChannelId }
            .maxByOrNull { it.id }

    // Stub implementations for unused methods

    override suspend fun insertPlaylist(playlist: PlaylistEntity): Long = 0L
    override suspend fun insertChannels(channels: List<ChannelEntity>): List<Long> = emptyList()
    override suspend fun upsertPlaylist(playlist: PlaylistEntity): Long = 0L
    override suspend fun upsertChannels(channels: List<ChannelEntity>) {}
    override suspend fun updatePlaylist(playlist: PlaylistEntity) {}
    override fun getPlaylists(): Flow<List<PlaylistEntity>> = flowOf(emptyList())
    override suspend fun getPlaylistsList(): List<PlaylistEntity> = emptyList()
    override suspend fun getPlaylistById(playlistId: Long): PlaylistEntity? = null
    override suspend fun getPlaylistByUrl(url: String): PlaylistEntity? = null
    override fun getAllChannels(): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun getChannelsByPlaylistId(playlistId: Long): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override suspend fun getPlaylistCount(): Int = 0
    override fun getFavoriteChannels(): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun getFavoriteChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun searchChannels(query: String): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
        flowOf(emptyList())
    override fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>> =
        flowOf(emptyList())
    override fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
        flowOf(emptyList())
    override suspend fun toggleFavoriteStatus(channelId: Long) {}
    override suspend fun clearAllFavorites() {}
    override suspend fun deleteChannelsByPlaylistId(playlistId: Long) {}
    override suspend fun deletePlaylist(playlistId: Long) {}
    override suspend fun upsertGroups(groups: List<ChannelGroupEntity>): List<Long> = emptyList()
    override suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>) {}
    override fun getGroupsByPlaylist(playlistId: Long): Flow<List<ChannelGroupEntity>> = flowOf(emptyList())
    override fun getAllGroups(): Flow<List<ChannelGroupEntity>> = flowOf(emptyList())
    override fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>> = flowOf(emptyList())
    override fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>> =
        flowOf(emptyList())
    override suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity> = emptyList()
    override suspend fun getChannelWithGroups(channelId: Long): ChannelWithGroups? = null
    override fun getGroupWithChannels(groupId: Long): Flow<GroupWithChannels?> = flowOf(null)
    override fun getChannelsByGroupId(groupId: Long): Flow<List<ChannelEntity>> = flowOf(emptyList())
    override suspend fun getGroupsForChannel(channelId: Long): List<ChannelGroupEntity> = emptyList()
}
