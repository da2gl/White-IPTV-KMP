package com.simplevideo.whiteiptv.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelFtsEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelWithGroups
import com.simplevideo.whiteiptv.data.local.model.GroupWithChannels
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
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

    @Query("SELECT * FROM playlists")
    suspend fun getPlaylistsList(): List<PlaylistEntity>

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

    // Search (FTS4 full-text search with phrase-prefix matching)
    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN channels_fts fts ON c.id = fts.rowid
        WHERE channels_fts MATCH '"' || :query || '*'
        ORDER BY c.name ASC
        """,
    )
    fun searchChannels(query: String): Flow<List<ChannelEntity>>

    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN channels_fts fts ON c.id = fts.rowid
        WHERE channels_fts MATCH '"' || :query || '*' AND c.playlistId = :playlistId
        ORDER BY c.name ASC
        """,
    )
    fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>>

    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN channels_fts fts ON c.id = fts.rowid
        INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
        WHERE channels_fts MATCH '"' || :query || '*' AND cgr.groupId = :groupId
        ORDER BY c.name ASC
        """,
    )
    fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>>

    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN channels_fts fts ON c.id = fts.rowid
        WHERE channels_fts MATCH '"' || :query || '*' AND c.isFavorite = 1
        ORDER BY c.name ASC
        """,
    )
    fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>>

    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN channels_fts fts ON c.id = fts.rowid
        WHERE channels_fts MATCH '"' || :query || '*' AND c.isFavorite = 1 AND c.playlistId = :playlistId
        ORDER BY c.name ASC
        """,
    )
    fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>>

    @Query(
        """
        UPDATE channels
        SET isFavorite = NOT isFavorite
        WHERE id = :channelId
        """,
    )
    suspend fun toggleFavoriteStatus(channelId: Long)

    @Query("UPDATE channels SET isFavorite = 0 WHERE isFavorite = 1")
    suspend fun clearAllFavorites()

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylistId(playlistId: Long)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Upsert
    suspend fun upsertGroups(groups: List<ChannelGroupEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChannelGroupCrossRefs(refs: List<ChannelGroupCrossRef>)

    @Insert
    suspend fun insertChannelFts(ftsEntities: List<ChannelFtsEntity>)

    @Query("DELETE FROM channels_fts")
    suspend fun deleteAllChannelFts()

    @Query(
        """
        DELETE FROM channels_fts WHERE rowid IN (
            SELECT id FROM channels WHERE playlistId = :playlistId
        )
        """,
    )
    suspend fun deleteChannelFtsByPlaylistId(playlistId: Long)

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

    // Paged channel queries
    @Query("SELECT * FROM channels ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getChannelsPaged(limit: Int, offset: Int): List<ChannelEntity>

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun getChannelsCount(): Int

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getChannelsByPlaylistIdPaged(playlistId: Long, limit: Int, offset: Int): List<ChannelEntity>

    @Query("SELECT COUNT(*) FROM channels WHERE playlistId = :playlistId")
    suspend fun getChannelsByPlaylistIdCount(playlistId: Long): Int

    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
        WHERE cgr.groupId = :groupId
        ORDER BY c.name ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun getChannelsByGroupIdPaged(groupId: Long, limit: Int, offset: Int): List<ChannelEntity>

    @Query(
        """
        SELECT COUNT(*) FROM channels c
        INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
        WHERE cgr.groupId = :groupId
        """,
    )
    suspend fun getChannelsByGroupIdCount(groupId: Long): Int

    @Query(
        """
        SELECT * FROM channels
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun searchChannelsPaged(query: String, limit: Int, offset: Int): List<ChannelEntity>

    @Query("SELECT COUNT(*) FROM channels WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
    suspend fun searchChannelsCount(query: String): Int

    @Query(
        """
        SELECT * FROM channels
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE AND playlistId = :playlistId
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun searchChannelsByPlaylistIdPaged(
        query: String,
        playlistId: Long,
        limit: Int,
        offset: Int,
    ): List<ChannelEntity>

    @Query(
        """
        SELECT COUNT(*) FROM channels
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE AND playlistId = :playlistId
        """,
    )
    suspend fun searchChannelsByPlaylistIdCount(query: String, playlistId: Long): Int

    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
        WHERE cgr.groupId = :groupId AND c.name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY c.name ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun searchChannelsByGroupIdPaged(
        query: String,
        groupId: Long,
        limit: Int,
        offset: Int,
    ): List<ChannelEntity>

    @Query(
        """
        SELECT COUNT(*) FROM channels c
        INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
        WHERE cgr.groupId = :groupId AND c.name LIKE '%' || :query || '%' COLLATE NOCASE
        """,
    )
    suspend fun searchChannelsByGroupIdCount(query: String, groupId: Long): Int

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

        val ftsEntities = channels.map { channel ->
            ChannelFtsEntity(name = channel.name)
        }
        if (ftsEntities.isNotEmpty()) {
            insertChannelFts(ftsEntities)
        }

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

        deleteChannelFtsByPlaylistId(playlist.id)
        deleteChannelsByPlaylistId(playlist.id)

        if (groups.isNotEmpty()) {
            upsertGroups(groups)
        }

        insertChannels(channels)

        val ftsEntities = channels.map { channel ->
            ChannelFtsEntity(name = channel.name)
        }
        if (ftsEntities.isNotEmpty()) {
            insertChannelFts(ftsEntities)
        }

        if (crossRefs.isNotEmpty()) {
            insertChannelGroupCrossRefs(crossRefs)
        }
    }
}
