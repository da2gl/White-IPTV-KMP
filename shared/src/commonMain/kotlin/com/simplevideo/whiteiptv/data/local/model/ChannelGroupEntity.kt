package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Channel Group/Category entity (OPTIONAL)
 *
 * Normalized table for channel groups to avoid string duplication
 * and enable efficient group-based queries
 *
 * Example groups: "Sports", "News", "Movies", "Kids", "Music"
 *
 * Benefits:
 * - Faster filtering by group
 * - Consistent group naming
 * - Easy to get channel count per group
 * - Can add group icons/colors
 *
 * Usage:
 * Instead of storing groupTitle string in each Channel,
 * store groupId reference to this table
 */
@Entity(
    tableName = "channel_groups",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["playlistId", "name"], unique = true), // Unique group per playlist
    ],
)
data class ChannelGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Parent playlist ID
     */
    val playlistId: Long,

    /**
     * Group name (from group-title attribute)
     * Examples: "Sports", "News", "Movies", "Entertainment"
     */
    val name: String,

    /**
     * Optional group icon URL
     * Can be set by user or provider
     */
    val icon: String? = null,

    /**
     * Display order (for custom sorting in UI)
     * Lower numbers appear first
     */
    val displayOrder: Int = 0,

    /**
     * Number of channels in this group (cached for performance)
     * Updated when channels are added/removed
     */
    val channelCount: Int = 0,
)
