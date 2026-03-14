package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 virtual table for fast full-text search on channel names.
 * Linked to [ChannelEntity] as a content table -- stores only the search index,
 * not the actual data.
 */
@Fts4(contentEntity = ChannelEntity::class)
@Entity(tableName = "channels_fts")
data class ChannelFtsEntity(
    val name: String,
)
