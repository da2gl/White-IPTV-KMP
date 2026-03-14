package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Channel with all its associated groups
 *
 * Used for UI when displaying channel details with all categories it belongs to.
 * Room automatically performs the JOIN query through the junction table.
 *
 * Example:
 * Channel: "Discovery Science"
 * Groups: ["Education", "Science", "Documentary"]
 */
data class ChannelWithGroups(
    @Embedded
    val channel: ChannelEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ChannelGroupCrossRef::class,
            parentColumn = "channelId",
            entityColumn = "groupId",
        ),
    )
    val groups: List<ChannelGroupEntity>,
)
