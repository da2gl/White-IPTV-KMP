package com.simplevideo.whiteiptv.data.repository

import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.data.local.EpgDao
import com.simplevideo.whiteiptv.data.mapper.EpgProgramMapper
import com.simplevideo.whiteiptv.data.parser.epg.XmltvParser
import com.simplevideo.whiteiptv.domain.model.EpgProgram
import com.simplevideo.whiteiptv.domain.repository.EpgRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class EpgRepositoryImpl(
    private val httpClient: HttpClient,
    private val epgDao: EpgDao,
    private val xmltvParser: XmltvParser,
    private val epgProgramMapper: EpgProgramMapper,
) : EpgRepository {

    override suspend fun loadEpg(playlistId: Long, xmltvUrl: String, tvgShiftHours: Int) {
        val content = httpClient.get(xmltvUrl).bodyAsText()

        val programs = xmltvParser.parse(content, tvgShiftHours)

        epgDao.deleteByPlaylistChannels(playlistId)

        programs.chunked(BATCH_SIZE).forEach { batch ->
            epgDao.insertPrograms(batch)
        }

        val cutoff = Clock.System.now().toEpochMilliseconds() - PURGE_THRESHOLD_MS
        epgDao.deleteOlderThan(cutoff)

        Logger.withTag(TAG).d { "Loaded ${programs.size} EPG programs for playlist $playlistId" }
    }

    override suspend fun getCurrentProgram(tvgId: String): EpgProgram? {
        val now = Clock.System.now().toEpochMilliseconds()
        return epgDao.getCurrentProgram(tvgId, now)?.let { epgProgramMapper.toDomain(it) }
    }

    override suspend fun getNextProgram(tvgId: String): EpgProgram? {
        val now = Clock.System.now().toEpochMilliseconds()
        return epgDao.getNextProgram(tvgId, now)?.let { epgProgramMapper.toDomain(it) }
    }

    override suspend fun clearEpg() {
        epgDao.deleteAll()
    }

    override suspend fun hasEpgData(): Boolean {
        return epgDao.getProgramCount() > 0
    }

    companion object {
        private const val TAG = "EpgRepository"
        private const val BATCH_SIZE = 500
        private const val PURGE_THRESHOLD_MS = 24 * 60 * 60 * 1000L
    }
}
