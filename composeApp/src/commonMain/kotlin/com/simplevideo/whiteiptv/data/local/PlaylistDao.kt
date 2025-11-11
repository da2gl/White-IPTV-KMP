package com.simplevideo.whiteiptv.data.local

import androidx.room.*
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Upsert
    suspend fun upsertPlaylist(playlist: PlaylistEntity): Long

    @Upsert
    suspend fun upsertChannels(channels: List<ChannelEntity>)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE url = :url LIMIT 1")
    suspend fun getPlaylistByUrl(url: String): PlaylistEntity?

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    fun getChannels(playlistId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylistId(playlistId: Long)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)
}
