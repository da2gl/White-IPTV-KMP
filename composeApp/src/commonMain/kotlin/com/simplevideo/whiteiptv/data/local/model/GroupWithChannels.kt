package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Channel group with all its associated channels
 *
 * Used for UI when displaying a category with all channels belonging to it.
 * Room automatically performs the JOIN query through the junction table.
 *
 * Example:
 * Group: "Science"
 * Channels: ["Discovery Science", "National Geographic", "History Channel"]
 */
data class GroupWithChannels(
    @Embedded
    val group: ChannelGroupEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ChannelGroupCrossRef::class,
            parentColumn = "groupId",
            entityColumn = "channelId",
        ),
    )
    val channels: List<ChannelEntity>,
)
