package com.simplevideo.whiteiptv.data.mapper

import com.simplevideo.whiteiptv.data.local.model.EpgProgramEntity
import com.simplevideo.whiteiptv.domain.model.EpgProgram

class EpgProgramMapper {
    fun toDomain(entity: EpgProgramEntity): EpgProgram = EpgProgram(
        title = entity.title,
        description = entity.description,
        startTimeMs = entity.startTime,
        endTimeMs = entity.endTime,
        category = entity.category,
        iconUrl = entity.iconUrl,
    )
}
