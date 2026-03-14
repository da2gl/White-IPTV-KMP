package com.simplevideo.whiteiptv.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simplevideo.whiteiptv.data.local.model.EpgProgramEntity

@Dao
interface EpgDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgProgramEntity>)

    @Query(
        """
        SELECT * FROM epg_programs
        WHERE channelTvgId = :tvgId
          AND startTime <= :timeMs
          AND endTime > :timeMs
        LIMIT 1
        """,
    )
    suspend fun getCurrentProgram(tvgId: String, timeMs: Long): EpgProgramEntity?

    @Query(
        """
        SELECT * FROM epg_programs
        WHERE channelTvgId = :tvgId
          AND startTime > :timeMs
        ORDER BY startTime ASC
        LIMIT 1
        """,
    )
    suspend fun getNextProgram(tvgId: String, timeMs: Long): EpgProgramEntity?

    @Query("DELETE FROM epg_programs WHERE endTime < :timeMs")
    suspend fun deleteOlderThan(timeMs: Long)

    @Query(
        "DELETE FROM epg_programs WHERE channelTvgId IN (SELECT tvgId FROM channels WHERE playlistId = :playlistId)",
    )
    suspend fun deleteByPlaylistChannels(playlistId: Long)

    @Query("DELETE FROM epg_programs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM epg_programs")
    suspend fun getProgramCount(): Int
}
