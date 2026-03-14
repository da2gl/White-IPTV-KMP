package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.domain.model.EpgProgram

interface EpgRepository {
    suspend fun loadEpg(playlistId: Long, xmltvUrl: String, tvgShiftHours: Int = 0)
    suspend fun getCurrentProgram(tvgId: String): EpgProgram?
    suspend fun getNextProgram(tvgId: String): EpgProgram?
    suspend fun clearEpg()
    suspend fun hasEpgData(): Boolean
}
