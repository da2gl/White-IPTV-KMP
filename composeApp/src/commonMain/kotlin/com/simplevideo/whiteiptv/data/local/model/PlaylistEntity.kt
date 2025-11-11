package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

/**
 * IPTV Playlist entity with extended metadata
 *
 * Stores playlist information and global settings from M3U header
 */
@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "playlists",
    indices = [Index(value = ["url"], unique = true)],
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // ═══════════════════════════════════════════════════════════════════
    // Basic Information
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Playlist name (user-defined or from source)
     */
    val name: String,

    /**
     * Playlist URL (M3U/M3U8 source)
     */
    val url: String,

    /**
     * Playlist icon/logo URL
     */
    val icon: String? = null,

    // ═══════════════════════════════════════════════════════════════════
    // EPG (Electronic Program Guide) Settings
    // ═══════════════════════════════════════════════════════════════════

    /**
     * EPG/XMLTV source URL (from url-tvg header attribute)
     * Used to fetch TV program guide data
     */
    val urlTvg: String? = null,

    /**
     * Global timezone shift in hours (from tvg-shift header attribute)
     * Applied to all channels in playlist for EPG time correction
     */
    val tvgShift: Int? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Network Settings
    // ═══════════════════════════════════════════════════════════════════

    /**
     * HTTP User-Agent header (from user-agent header attribute)
     * Used for all requests to channels in this playlist
     * Some providers require specific User-Agent
     */
    val userAgent: String? = null,

    /**
     * Playlist refresh interval in seconds (from refresh header attribute)
     * How often to re-download playlist to get updated channel URLs
     */
    val refreshInterval: Int? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Metadata
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Number of channels in playlist (for UI display)
     */
    val channelCount: Int = 0,

    /**
     * Last successful update timestamp (milliseconds)
     */
    val lastUpdate: Long = System.now().toEpochMilliseconds(),

    /**
     * Creation timestamp (milliseconds)
     */
    val createdAt: Long = System.now().toEpochMilliseconds(),
)
