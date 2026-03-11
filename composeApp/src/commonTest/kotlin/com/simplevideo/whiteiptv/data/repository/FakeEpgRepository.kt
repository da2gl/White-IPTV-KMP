package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.domain.model.EpgProgram
import com.simplevideo.whiteiptv.domain.repository.EpgRepository

class FakeEpgRepository : EpgRepository {

    var currentProgram: EpgProgram? = null
    var nextProgram: EpgProgram? = null

    var loadEpgCalled = false
        private set
    var lastLoadPlaylistId: Long? = null
        private set
    var lastLoadXmltvUrl: String? = null
        private set
    var lastLoadTvgShiftHours: Int? = null
        private set

    var loadEpgError: Throwable? = null

    override suspend fun loadEpg(playlistId: Long, xmltvUrl: String, tvgShiftHours: Int) {
        loadEpgError?.let { throw it }
        loadEpgCalled = true
        lastLoadPlaylistId = playlistId
        lastLoadXmltvUrl = xmltvUrl
        lastLoadTvgShiftHours = tvgShiftHours
    }

    override suspend fun getCurrentProgram(tvgId: String): EpgProgram? = currentProgram

    override suspend fun getNextProgram(tvgId: String): EpgProgram? = nextProgram

    override suspend fun clearEpg() {
        currentProgram = null
        nextProgram = null
    }

    override suspend fun hasEpgData(): Boolean = currentProgram != null || nextProgram != null
}
