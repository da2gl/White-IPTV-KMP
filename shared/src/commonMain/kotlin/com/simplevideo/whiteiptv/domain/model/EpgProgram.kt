package com.simplevideo.whiteiptv.domain.model

data class EpgProgram(
    val title: String,
    val description: String? = null,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val category: String? = null,
    val iconUrl: String? = null,
)
