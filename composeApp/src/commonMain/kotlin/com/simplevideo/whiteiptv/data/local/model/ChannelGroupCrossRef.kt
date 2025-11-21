package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Many-to-Many junction table for channels and groups
 *
 * Allows a single channel to belong to multiple groups.
 * Example: group-title="Education;Science" creates 2 relationships for one channel
 *
 * Benefits:
 * - One channel can appear in multiple categories
 * - Flexible filtering (show all channels in "Science" group)
 * - Normalized storage (no string duplication)
 */
@Entity(
    tableName = "channel_group_cross_ref",
    primaryKeys = ["channelId", "groupId"],
    foreignKeys = [
        ForeignKey(
            entity = ChannelEntity::class,
            parentColumns = ["id"],
            childColumns = ["channelId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ChannelGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["channelId"]),
        Index(value = ["groupId"]),
    ],
)
data class ChannelGroupCrossRef(
    val channelId: Long,
    val groupId: Long,
)
