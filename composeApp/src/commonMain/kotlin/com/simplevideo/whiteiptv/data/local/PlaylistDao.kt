package com.simplevideo.whiteiptv.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("SELECT * FROM playlists")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    fun getChannels(playlistId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
}
