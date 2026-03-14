package com.simplevideo.whiteiptv.domain.model

/**
 * Channel group from M3U playlist (group-title attribute)
 *
 * Represents a dynamic category backed by ChannelGroupEntity in database.
 * Used for filtering channels in UI dropdown.
 */
data class ChannelGroup(
    val id: String,
    val displayName: String,
    val icon: String? = null,
    val channelCount: Int = 0,
    val playlistId: Long? = null,
)
