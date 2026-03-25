package com.simplevideo.whiteiptv.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Query result combining a channel with its associated group ID.
 * Used by batch queries that fetch channels for multiple groups at once.
 */
data class ChannelWithGroupId(
    @Embedded val channel: ChannelEntity,
    @ColumnInfo(name = "crossRefGroupId") val groupId: Long,
)
