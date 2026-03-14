package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records channel watch events for "Continue Watching" feature.
 * One row per channel — stores the most recent watch session.
 */
@Entity(
    tableName = "watch_history",
    foreignKeys = [
        ForeignKey(
            entity = ChannelEntity::class,
            parentColumns = ["id"],
            childColumns = ["channelId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["lastWatchedAt"]),
    ],
)
data class WatchHistoryEntity(
    @PrimaryKey
    val channelId: Long,
    val playlistId: Long,
    val lastWatchedAt: Long,
    val watchDurationMs: Long = 0,
)
