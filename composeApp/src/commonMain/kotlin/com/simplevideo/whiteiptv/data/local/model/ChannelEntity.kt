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
        Index(value = ["groupTitle"]),
        Index(value = ["isFavorite"]),
    ],
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Parent playlist ID (foreign key)
     */
    val playlistId: Long,

    // ═══════════════════════════════════════════════════════════════════
    // Basic Information
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Channel name/title
     */
    val name: String,

    /**
     * Stream URL
     */
    val url: String,

    /**
     * Channel logo URL (from tvg-logo attribute)
     */
    val logoUrl: String? = null,

    // ═══════════════════════════════════════════════════════════════════
    // TVG (TV Guide) Tags - For EPG Integration
    // ═══════════════════════════════════════════════════════════════════

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

    /**
     * Channel number (from tvg-chno attribute)
     * Used for sorting and displaying channel number in UI
     */
    val tvgChno: String? = null,

    /**
     * Channel language (from tvg-language/tvg-lang attribute)
     * Examples: "en", "ru", "es", "fr"
     * Used for filtering channels by language
     */
    val tvgLanguage: String? = null,

    /**
     * Channel country code (from tvg-country attribute)
     * Examples: "US", "RU", "GB", "FR"
     * Used for filtering channels by country
     */
    val tvgCountry: String? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Organization and Grouping
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Channel group/category (from group-title attribute)
     * Examples: "Sports", "News", "Movies", "Entertainment"
     * Used for organizing channels into categories in UI
     */
    val groupTitle: String? = null,

    /**
     * Whether channel is marked as favorite by user
     * NOT from M3U - local user preference
     */
    val isFavorite: Boolean = false,

    // ═══════════════════════════════════════════════════════════════════
    // Catchup TV (Time-Shifted Viewing)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Number of days available for catchup/archive (from catchup-days attribute)
     * If null or 0 - no catchup available
     * If > 0 - shows "Archive available" badge in UI
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
     * Used by player to build catchup stream URL
     */
    val catchupSource: String? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Network Settings (Channel-Specific)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Channel-specific HTTP User-Agent (from #EXTVLCOPT:http-user-agent)
     * Overrides playlist-level userAgent if present
     * Some channels require different User-Agent than others
     */
    val userAgent: String? = null,

    /**
     * HTTP Referer header (from #EXTVLCOPT:http-referrer)
     * Required by some providers for stream access
     */
    val referer: String? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Extended Metadata (Optional JSON)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Additional metadata as JSON string
     * Stores less common attributes like:
     * - vlcOpts (VLC options)
     * - kodiProps (Kodi properties)
     * - description, provider, aspectRatio, etc.
     *
     * Example: {"description": "...", "vlcOpts": {...}}
     *
     * Use kotlinx.serialization to parse when needed
     */
    val extendedMetadata: String? = null,
)
