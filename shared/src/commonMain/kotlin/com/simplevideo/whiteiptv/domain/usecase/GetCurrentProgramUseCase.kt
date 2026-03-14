package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.model.EpgProgram
import com.simplevideo.whiteiptv.domain.repository.EpgRepository

/**
 * Returns current and next EPG program for the given tvgId.
 * Returns nulls if no EPG data is available or tvgId is blank.
 */
class GetCurrentProgramUseCase(
    private val epgRepository: EpgRepository,
) {
    suspend operator fun invoke(tvgId: String?): Pair<EpgProgram?, EpgProgram?> {
        if (tvgId.isNullOrBlank()) return null to null
        val current = epgRepository.getCurrentProgram(tvgId)
        val next = epgRepository.getNextProgram(tvgId)
        return current to next
    }
}
