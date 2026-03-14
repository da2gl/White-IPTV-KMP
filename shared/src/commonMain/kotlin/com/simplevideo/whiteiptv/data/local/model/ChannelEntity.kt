package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * IPTV Channel entity with comprehensive metadata
 *
 * Stores channel information with top IPTV M3U tags for UI and functionality
 */
@Entity(
    tableName = "channels",
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
        Index(value = ["tvgId"]),
        Index(value = ["isFavorite"]),
    ],
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long,
    val name: String,
    val url: String,
    val logoUrl: String? = null,

    /**
     * Channel ID for EPG/XMLTV matching (from tvg-id attribute)
     * CRITICAL for linking channel with TV program guide
     */
    val tvgId: String? = null,

    /**
     * Channel name for EPG matching (from tvg-name attribute)
     * Used as fallback if tvgId matching fails
     */
    val tvgName: String? = null,

    val tvgChno: String? = null,
    val tvgLanguage: String? = null,
    val tvgCountry: String? = null,

    /**
     * Whether channel is marked as favorite by user
     * NOT from M3U - local user preference
     */
    val isFavorite: Boolean = false,

    /**
     * Number of days available for catchup/archive (from catchup-days attribute)
     * If null or 0 - no catchup available
     */
    val catchupDays: Int? = null,

    /**
     * Catchup type (from catchup-type attribute)
     * Values: "default", "append", "shift", "flussonic", "xtream", "fs"
     * Used by player to construct catchup URL
     */
    val catchupType: String? = null,

    /**
     * Catchup URL template (from catchup-source attribute)
     * Contains placeholders like ${start}, ${duration}, etc.
     */
    val catchupSource: String? = null,

    /**
     * Channel-specific HTTP User-Agent (from #EXTVLCOPT:http-user-agent)
     * Overrides playlist-level userAgent if present
     */
    val userAgent: String? = null,

    val referer: String? = null,

    /**
     * Additional metadata as JSON string
     * Stores less common attributes: vlcOpts, kodiProps, description, provider, etc.
     * Use kotlinx.serialization to parse when needed
     */
    val extendedMetadata: String? = null,
)
