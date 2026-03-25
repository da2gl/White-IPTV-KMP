package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake ChannelRepository that tracks method calls for verifying UseCase routing logic.
 * Stores channels in memory and performs simple LIKE-style filtering for search methods.
 */
class FakeChannelRepository : ChannelRepository {

    private val channels = MutableStateFlow<List<ChannelEntity>>(emptyList())
    private val groups = MutableStateFlow<List<ChannelGroupEntity>>(emptyList())
    private val crossRefs = mutableListOf<ChannelGroupCrossRef>()

    /** Tracks which methods were called with what arguments */
    val methodCalls = mutableListOf<String>()

    fun setChannels(channelList: List<ChannelEntity>) {
        channels.value = channelList
    }

    fun setGroups(groupList: List<ChannelGroupEntity>) {
        groups.value = groupList
    }

    fun addCrossRefs(refs: List<ChannelGroupCrossRef>) {
        crossRefs.addAll(refs)
    }

    // Channels
    override fun getAllChannels(): Flow<List<ChannelEntity>> {
        methodCalls.add("getAllChannels")
        return channels
    }

    override fun getChannelsByPlaylistId(playlistId: Long): Flow<List<ChannelEntity>> {
        methodCalls.add("getChannelsByPlaylistId($playlistId)")
        return channels.map { list -> list.filter { it.playlistId == playlistId } }
    }

    override fun getChannelsByGroupId(groupId: Long): Flow<List<ChannelEntity>> {
        methodCalls.add("getChannelsByGroupId($groupId)")
        val channelIds = crossRefs.filter { it.groupId == groupId }.map { it.channelId }.toSet()
        return channels.map { list -> list.filter { it.id in channelIds } }
    }

    override suspend fun getChannelsList(playlistId: Long): List<ChannelEntity> {
        methodCalls.add("getChannelsList($playlistId)")
        return channels.value.filter { it.playlistId == playlistId }
    }

    override suspend fun insertChannels(channelList: List<ChannelEntity>): List<Long> {
        methodCalls.add("insertChannels")
        channels.value = channels.value + channelList
        return channelList.map { it.id }
    }

    override suspend fun deleteChannelsByPlaylistId(playlistId: Long) {
        methodCalls.add("deleteChannelsByPlaylistId($playlistId)")
        channels.value = channels.value.filter { it.playlistId != playlistId }
    }

    // Favorites
    override fun getFavoriteChannels(): Flow<List<ChannelEntity>> {
        methodCalls.add("getFavoriteChannels")
        return channels.map { list -> list.filter { it.isFavorite } }
    }

    override fun getFavoriteChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>> {
        methodCalls.add("getFavoriteChannelsByPlaylist($playlistId)")
        return channels.map { list -> list.filter { it.isFavorite && it.playlistId == playlistId } }
    }

    override suspend fun toggleFavoriteStatus(channelId: Long) {
        methodCalls.add("toggleFavoriteStatus($channelId)")
        channels.value = channels.value.map {
            if (it.id == channelId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    override suspend fun clearAllFavorites() {
        methodCalls.add("clearAllFavorites")
        channels.value = channels.value.map { it.copy(isFavorite = false) }
    }

    // Search
    override fun searchChannels(query: String): Flow<List<ChannelEntity>> {
        methodCalls.add("searchChannels($query)")
        return channels.map { list ->
            list.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    override fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>> {
        methodCalls.add("searchChannelsByPlaylistId($query, $playlistId)")
        return channels.map { list ->
            list.filter { it.name.contains(query, ignoreCase = true) && it.playlistId == playlistId }
        }
    }

    override fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>> {
        methodCalls.add("searchChannelsByGroupId($query, $groupId)")
        val channelIds = crossRefs.filter { it.groupId == groupId }.map { it.channelId }.toSet()
        return channels.map { list ->
            list.filter { it.name.contains(query, ignoreCase = true) && it.id in channelIds }
        }
    }

    override fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>> {
        methodCalls.add("searchFavoriteChannels($query)")
        return channels.map { list ->
            list.filter { it.isFavorite && it.name.contains(query, ignoreCase = true) }
        }
    }

    override fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>> {
        methodCalls.add("searchFavoriteChannelsByPlaylist($query, $playlistId)")
        return channels.map { list ->
            list.filter {
                it.isFavorite && it.playlistId == playlistId && it.name.contains(query, ignoreCase = true)
            }
        }
    }

    // Groups
    override fun getAllGroups(): Flow<List<ChannelGroupEntity>> {
        methodCalls.add("getAllGroups")
        return groups
    }

    override fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>> {
        methodCalls.add("getTopGroups($limit)")
        return groups.map { list -> list.sortedByDescending { it.channelCount }.take(limit) }
    }

    override fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>> {
        methodCalls.add("getTopGroupsByPlaylist($playlistId, $limit)")
        return groups.map { list ->
            list.filter { it.playlistId == playlistId }.sortedByDescending { it.channelCount }.take(limit)
        }
    }

    override suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity> {
        methodCalls.add("getRandomChannelsByGroupId($groupId, $limit)")
        val channelIds = crossRefs.filter { it.groupId == groupId }.map { it.channelId }.toSet()
        return channels.value.filter { it.id in channelIds }.take(limit)
    }

    override suspend fun getRandomChannelsForGroups(
        groupIds: List<Long>,
        limitPerGroup: Int,
    ): Map<Long, List<ChannelEntity>> {
        methodCalls.add("getRandomChannelsForGroups")
        return groupIds.associateWith { groupId ->
            val channelIds = crossRefs.filter { it.groupId == groupId }.map { it.channelId }.toSet()
            channels.value.filter { it.id in channelIds }.take(limitPerGroup)
        }
    }

    override suspend fun insertGroups(groupList: List<ChannelGroupEntity>): List<Long> {
        methodCalls.add("insertGroups")
        groups.value = groups.value + groupList
        return groupList.map { it.id }
    }

    override suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>) {
        methodCalls.add("insertChannelGroupCrossRefs")
        crossRefs.addAll(refs)
    }

    // Paged channels
    override suspend fun getChannelsPaged(limit: Int, offset: Int): List<ChannelEntity> {
        methodCalls.add("getChannelsPaged($limit, $offset)")
        return channels.value.sortedBy { it.name }.drop(offset).take(limit)
    }

    override suspend fun getChannelsCount(): Int = channels.value.size

    override suspend fun getChannelsByPlaylistIdPaged(
        playlistId: Long,
        limit: Int,
        offset: Int,
    ): List<ChannelEntity> {
        methodCalls.add("getChannelsByPlaylistIdPaged($playlistId, $limit, $offset)")
        return channels.value.filter { it.playlistId == playlistId }.sortedBy { it.name }.drop(offset).take(limit)
    }

    override suspend fun getChannelsByPlaylistIdCount(playlistId: Long): Int =
        channels.value.count { it.playlistId == playlistId }

    override suspend fun getChannelsByGroupIdPaged(groupId: Long, limit: Int, offset: Int): List<ChannelEntity> {
        methodCalls.add("getChannelsByGroupIdPaged($groupId, $limit, $offset)")
        val channelIds = crossRefs.filter { it.groupId == groupId }.map { it.channelId }.toSet()
        return channels.value.filter { it.id in channelIds }.sortedBy { it.name }.drop(offset).take(limit)
    }

    override suspend fun getChannelsByGroupIdCount(groupId: Long): Int {
        val channelIds = crossRefs.filter { it.groupId == groupId }.map { it.channelId }.toSet()
        return channels.value.count { it.id in channelIds }
    }

    override suspend fun searchChannelsPaged(query: String, limit: Int, offset: Int): List<ChannelEntity> {
        methodCalls.add("searchChannelsPaged($query, $limit, $offset)")
        return channels.value.filter { it.name.contains(query, ignoreCase = true) }
            .sortedBy { it.name }.drop(offset).take(limit)
    }

    override suspend fun searchChannelsCount(query: String): Int =
        channels.value.count { it.name.contains(query, ignoreCase = true) }

    override suspend fun searchChannelsByPlaylistIdPaged(
        query: String,
        playlistId: Long,
        limit: Int,
        offset: Int,
    ): List<ChannelEntity> {
        methodCalls.add("searchChannelsByPlaylistIdPaged($query, $playlistId, $limit, $offset)")
        return channels.value
            .filter { it.name.contains(query, ignoreCase = true) && it.playlistId == playlistId }
            .sortedBy { it.name }.drop(offset).take(limit)
    }

    override suspend fun searchChannelsByPlaylistIdCount(query: String, playlistId: Long): Int =
        channels.value.count { it.name.contains(query, ignoreCase = true) && it.playlistId == playlistId }

    override suspend fun searchChannelsByGroupIdPaged(
        query: String,
        groupId: Long,
        limit: Int,
        offset: Int,
    ): List<ChannelEntity> {
        methodCalls.add("searchChannelsByGroupIdPaged($query, $groupId, $limit, $offset)")
        val channelIds = crossRefs.filter { it.groupId == groupId }.map { it.channelId }.toSet()
        return channels.value
            .filter { it.name.contains(query, ignoreCase = true) && it.id in channelIds }
            .sortedBy { it.name }.drop(offset).take(limit)
    }

    override suspend fun searchChannelsByGroupIdCount(query: String, groupId: Long): Int {
        val channelIds = crossRefs.filter { it.groupId == groupId }.map { it.channelId }.toSet()
        return channels.value.count { it.name.contains(query, ignoreCase = true) && it.id in channelIds }
    }
}
