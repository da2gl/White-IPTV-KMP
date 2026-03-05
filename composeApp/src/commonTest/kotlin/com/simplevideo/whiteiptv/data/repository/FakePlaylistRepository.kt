package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePlaylistRepository : PlaylistRepository {
    private val playlists = mutableMapOf<Long, PlaylistEntity>()
    private val _flow = MutableStateFlow(0)
    private var nextId = 1L

    var deletePlaylistCalled = false
        private set
    var lastDeletedPlaylistId: Long? = null
        private set

    fun addPlaylist(playlist: PlaylistEntity): PlaylistEntity {
        val entity = if (playlist.id == 0L) {
            playlist.copy(id = nextId++)
        } else {
            if (playlist.id >= nextId) nextId = playlist.id + 1
            playlist
        }
        playlists[entity.id] = entity
        _flow.value++
        return entity
    }

    override suspend fun hasPlaylist(): Boolean = playlists.isNotEmpty()

    override suspend fun getPlaylistById(id: Long): PlaylistEntity? = playlists[id]

    override suspend fun getPlaylistByUrl(url: String): PlaylistEntity? =
        playlists.values.find { it.url == url }

    override fun getPlaylists(): Flow<List<PlaylistEntity>> =
        _flow.map { playlists.values.toList() }

    override suspend fun insertPlaylist(playlist: PlaylistEntity): Long {
        val entity = playlist.copy(id = nextId++)
        playlists[entity.id] = entity
        _flow.value++
        return entity.id
    }

    override suspend fun updatePlaylist(playlist: PlaylistEntity) {
        playlists[playlist.id] = playlist
        _flow.value++
    }

    override suspend fun deletePlaylist(id: Long) {
        deletePlaylistCalled = true
        lastDeletedPlaylistId = id
        playlists.remove(id)
        _flow.value++
    }

    override suspend fun importPlaylistData(
        playlist: PlaylistEntity,
        groups: List<ChannelGroupEntity>,
        channels: List<ChannelEntity>,
        crossRefsProvider: (channelIds: List<Long>, groupIds: List<Long>) -> List<ChannelGroupCrossRef>,
    ): Long {
        val id = insertPlaylist(playlist)
        return id
    }

    override suspend fun updatePlaylistData(
        playlist: PlaylistEntity,
        groups: List<ChannelGroupEntity>,
        channels: List<ChannelEntity>,
        crossRefsProvider: (channelIds: List<Long>, groupIds: List<Long>) -> List<ChannelGroupCrossRef>,
    ) {
        updatePlaylist(playlist)
    }
}
