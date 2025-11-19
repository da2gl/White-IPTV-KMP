package com.simplevideo.whiteiptv.domain.model

/**
 * Channel category for UI filtering
 *
 * Supports both special (hardcoded) and dynamic (from M3U) categories
 */
sealed interface ChannelCategory {
    val displayName: String
    val id: String

    /**
     * Special category: All channels
     */
    data object All : ChannelCategory {
        override val displayName = "All"
        override val id = "_all"
    }

    /**
     * Special category: Favorite channels
     */
    data object Favorites : ChannelCategory {
        override val displayName = "Favorites"
        override val id = "_favorites"
    }

    /**
     * Dynamic category from M3U playlist (group-title attribute)
     * Backed by ChannelGroupEntity in database
     */
    data class Group(
        override val id: String,
        override val displayName: String,
        val icon: String? = null,
        val channelCount: Int = 0,
        val playlistId: Long? = null,
    ) : ChannelCategory
}
