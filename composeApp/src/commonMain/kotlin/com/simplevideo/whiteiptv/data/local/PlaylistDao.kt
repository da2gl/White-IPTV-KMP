package com.simplevideo.whiteiptv.data.local

import androidx.room.*
import com.simplevideo.whiteiptv.data.local.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert
    suspend fun insertChannels(channels: List<ChannelEntity>): List<Long>

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

    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :channelId")
    suspend fun getChannelById(channelId: Long): ChannelEntity?

    @Query(
        """
        SELECT * FROM channels
        WHERE playlistId = :playlistId AND id > :currentChannelId
        ORDER BY id ASC
        LIMIT 1
        """,
    )
    suspend fun getNextChannel(playlistId: Long, currentChannelId: Long): ChannelEntity?

    @Query(
        """
        SELECT * FROM channels
        WHERE playlistId = :playlistId AND id < :currentChannelId
        ORDER BY id DESC
        LIMIT 1
        """,
    )
    suspend fun getPreviousChannel(playlistId: Long, currentChannelId: Long): ChannelEntity?

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    fun getChannelsByPlaylistId(playlistId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int

    @Query("SELECT * FROM channels WHERE isFavorite = 1")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 AND playlistId = :playlistId")
    fun getFavoriteChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>>

    @Query(
        """
        UPDATE channels
        SET isFavorite = NOT isFavorite
        WHERE id = :channelId
        """,
    )
    suspend fun toggleFavoriteStatus(channelId: Long)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylistId(playlistId: Long)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Upsert
    suspend fun upsertGroups(groups: List<ChannelGroupEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>)

    @Query("SELECT * FROM channel_groups WHERE playlistId = :playlistId ORDER BY displayOrder ASC")
    fun getGroupsByPlaylist(playlistId: Long): Flow<List<ChannelGroupEntity>>

    @Query("SELECT * FROM channel_groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<ChannelGroupEntity>>

    @Query("SELECT * FROM channel_groups ORDER BY channelCount DESC LIMIT :limit")
    fun getTopGroups(limit: Int): Flow<List<ChannelGroupEntity>>

    @Query(
        """
            SELECT * FROM channel_groups
            WHERE playlistId = :playlistId
            ORDER BY channelCount
            DESC LIMIT :limit
            """,
    )
    fun getTopGroupsByPlaylist(playlistId: Long, limit: Int): Flow<List<ChannelGroupEntity>>

    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
        WHERE cgr.groupId = :groupId
        ORDER BY RANDOM()
        LIMIT :limit
        """,
    )
    suspend fun getRandomChannelsByGroupId(groupId: Long, limit: Int): List<ChannelEntity>

    @Transaction
    @Query("SELECT * FROM channels WHERE id = :channelId")
    suspend fun getChannelWithGroups(channelId: Long): ChannelWithGroups?

    @Transaction
    @Query("SELECT * FROM channel_groups WHERE id = :groupId")
    fun getGroupWithChannels(groupId: Long): Flow<GroupWithChannels?>

    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
        WHERE cgr.groupId = :groupId
        ORDER BY c.name ASC
    """,
    )
    fun getChannelsByGroupId(groupId: Long): Flow<List<ChannelEntity>>

    @Query(
        """
        SELECT cg.* FROM channel_groups cg
        INNER JOIN channel_group_cross_ref cgr ON cg.id = cgr.groupId
        WHERE cgr.channelId = :channelId
        ORDER BY cg.name ASC
    """,
    )
    suspend fun getGroupsForChannel(channelId: Long): List<ChannelGroupEntity>

    /**
     * Import playlist with all related data in a single transaction
     */
    @Transaction
    suspend fun importPlaylistWithData(
        playlist: PlaylistEntity,
        groups: List<ChannelGroupEntity>,
        channels: List<ChannelEntity>,
        crossRefs: List<ChannelGroupCrossRef>,
    ): Long {
        val playlistId = insertPlaylist(playlist)

        if (groups.isNotEmpty()) {
            upsertGroups(groups)
        }

        insertChannels(channels)

        if (crossRefs.isNotEmpty()) {
            insertChannelGroupCrossRefs(crossRefs)
        }

        return playlistId
    }

    /**
     * Update playlist with all related data in a single transaction
     */
    @Transaction
    suspend fun updatePlaylistWithData(
        playlist: PlaylistEntity,
        groups: List<ChannelGroupEntity>,
        channels: List<ChannelEntity>,
        crossRefs: List<ChannelGroupCrossRef>,
    ) {
        updatePlaylist(playlist)
        deleteChannelsByPlaylistId(playlist.id)

        if (groups.isNotEmpty()) {
            upsertGroups(groups)
        }

        insertChannels(channels)

        if (crossRefs.isNotEmpty()) {
            insertChannelGroupCrossRefs(crossRefs)
        }
    }
}
