package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "epg_programs",
    indices = [
        Index(value = ["channelTvgId", "startTime"]),
        Index(value = ["channelTvgId", "endTime"]),
    ],
)
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelTvgId: String,
    val title: String,
    val description: String? = null,
    val startTime: Long,
    val endTime: Long,
    val category: String? = null,
    val iconUrl: String? = null,
)
