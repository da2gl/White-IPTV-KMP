package com.simplevideo.whiteiptv.data.parser.playlist.model

/**
 * IPTV Channel with comprehensive metadata support
 *
 * Supports all popular IPTV M3U tags as of 2025
 */
data class Channel(
    // ═══════════════════════════════════════════════════════════════════
    // Basic Information
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Channel title/name
     */
    val title: String,

    /**
     * Stream URL
     */
    val url: String,

    /**
     * Track duration in seconds (-1 for live streams)
     */
    val duration: Int = -1,

    // ═══════════════════════════════════════════════════════════════════
    // TVG (TV Guide) Tags
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Channel ID for EPG/XMLTV matching
     */
    val tvgId: String? = null,

    /**
     * Channel name for EPG matching
     */
    val tvgName: String? = null,

    /**
     * Channel logo URL or path
     */
    val tvgLogo: String? = null,

    /**
     * Channel number (for ordering)
     */
    val tvgChno: String? = null,

    /**
     * Channel language (e.g., "en", "ru", "es")
     */
    val tvgLanguage: String? = null,

    /**
     * Channel country code (e.g., "US", "RU", "FR")
     */
    val tvgCountry: String? = null,

    /**
     * Content type: "live", "series", "movie"
     */
    val tvgType: String? = null,

    /**
     * Time shift for this specific channel (hours)
     */
    val tvgShift: Int? = null,

    /**
     * Recording availability indicator
     */
    val tvgRec: Int? = null,

    /**
     * Alternative stream URL
     */
    val tvgUrl: String? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Grouping and Organization
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Channel group/category (e.g., "Sports", "News", "Movies")
     */
    val groupTitle: String? = null,

    /**
     * Channel description
     */
    val description: String? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Catchup TV
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Catchup TV configuration
     * Enables time-shifted viewing of past broadcasts
     */
    val catchup: CatchupConfig? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Media Properties
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Audio track information
     */
    val audioTrack: String? = null,

    /**
     * Subtitles URL
     */
    val subtitles: String? = null,

    /**
     * Aspect ratio (e.g., "16:9", "4:3")
     */
    val aspectRatio: String? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Parental Control and Metadata
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Parental control code
     */
    val parentCode: String? = null,

    /**
     * Whether content is censored
     */
    val censored: Boolean? = null,

    /**
     * Content provider name
     */
    val provider: String? = null,

    /**
     * Provider type
     */
    val providerType: String? = null,

    // ═══════════════════════════════════════════════════════════════════
    // Player-Specific Options
    // ═══════════════════════════════════════════════════════════════════

    /**
     * VLC player options (#EXTVLCOPT)
     * Common keys:
     * - http-user-agent
     * - http-referrer
     * - http-origin
     * - network-caching
     * - demux
     */
    val vlcOpts: Map<String, String> = emptyMap(),

    /**
     * Kodi player properties (#KODIPROP)
     * Common keys:
     * - inputstream
     * - inputstream.adaptive.manifest_type
     * - inputstream.adaptive.license_type
     * - inputstream.adaptive.license_key
     * - inputstream.adaptive.stream_headers
     */
    val kodiProps: Map<String, String> = emptyMap(),

    // ═══════════════════════════════════════════════════════════════════
    // Additional Metadata
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Additional unknown attributes
     * Preserves forward compatibility with new tags
     * Maps attribute name to value
     */
    val additionalMetadata: Map<String, String> = emptyMap(),
)
