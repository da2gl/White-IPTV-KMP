package com.simplevideo.whiteiptv.data.repository

import androidx.paging.PagingSource
import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf

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

    override suspend fun renameChannel(channelId: Long, newName: String) {
        playlistDao.renameChannel(channelId, newName)
        playlistDao.renameChannelFts(channelId, newName)
    }

    override suspend fun deleteChannel(channelId: Long) {
        playlistDao.deleteChannelFts(channelId)
        playlistDao.deleteChannel(channelId)
    }

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

    // Search — all queries sanitized via FtsQuerySanitizer to prevent FTS MATCH crashes
    override fun searchChannels(query: String): Flow<List<ChannelEntity>> =
        FtsQuerySanitizer.sanitize(query)?.let { playlistDao.searchChannels(it) } ?: flowOf(emptyList())

    override fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
        FtsQuerySanitizer.sanitize(query)?.let { playlistDao.searchChannelsByPlaylistId(it, playlistId) }
            ?: flowOf(emptyList())

    override fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>> =
        FtsQuerySanitizer.sanitize(query)?.let { playlistDao.searchChannelsByGroupId(it, groupId) }
            ?: flowOf(emptyList())

    override fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>> =
        FtsQuerySanitizer.sanitize(query)?.let { playlistDao.searchFavoriteChannels(it) } ?: flowOf(emptyList())

    override fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
        FtsQuerySanitizer.sanitize(query)?.let { playlistDao.searchFavoriteChannelsByPlaylist(it, playlistId) }
            ?: flowOf(emptyList())

    // Paged channels (Room PagingSource — auto-invalidates on table changes)
    override fun getChannelsPaged(): PagingSource<Int, ChannelEntity> =
        playlistDao.getChannelsPaged()

    override fun getChannelsByPlaylistIdPaged(playlistId: Long): PagingSource<Int, ChannelEntity> =
        playlistDao.getChannelsByPlaylistIdPaged(playlistId)

    override fun getChannelsByGroupIdPaged(groupId: Long): PagingSource<Int, ChannelEntity> =
        playlistDao.getChannelsByGroupIdPaged(groupId)

    override fun searchChannelsPaged(query: String): PagingSource<Int, ChannelEntity> =
        playlistDao.searchChannelsPaged(query)

    override fun searchChannelsByPlaylistIdPaged(query: String, playlistId: Long): PagingSource<Int, ChannelEntity> =
        playlistDao.searchChannelsByPlaylistIdPaged(query, playlistId)

    override fun searchChannelsByGroupIdPaged(query: String, groupId: Long): PagingSource<Int, ChannelEntity> =
        playlistDao.searchChannelsByGroupIdPaged(query, groupId)

    // Groups
    override fun getAllGroups(): Flow<List<ChannelGroupEntity>> =
        playlistDao.getAllGroups()

    override fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>> =
        playlistDao.getTopGroups(limit)

    override fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>> =
        playlistDao.getTopGroupsByPlaylist(playlistId, limit)

    override fun getTopValidGroups(limit: Int): Flow<List<ChannelGroupEntity>> =
        playlistDao.getTopValidGroups(limit)

    override fun getTopValidGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>> =
        playlistDao.getTopValidGroupsByPlaylist(playlistId, limit)

    override suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity> =
        playlistDao.getRandomChannelsByGroupId(groupId, limit)

    override suspend fun getRandomChannelsForGroups(
        groupIds: List<Long>,
        limitPerGroup: Int,
    ): Map<Long, List<ChannelEntity>> =
        groupIds.associateWith { groupId ->
            playlistDao.getRandomChannelsByGroupId(groupId, limitPerGroup)
        }

    override suspend fun insertGroups(groups: List<ChannelGroupEntity>): List<Long> =
        playlistDao.upsertGroups(groups)

    override suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>) {
        playlistDao.insertChannelGroupCrossRefs(refs)
    }
}
