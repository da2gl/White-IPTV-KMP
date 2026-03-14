package com.simplevideo.whiteiptv.data.parser.playlist.model

/**
 * Playlist header metadata from #EXTM3U line
 *
 * Contains global settings that apply to all channels in the playlist
 */
data class PlaylistHeader(
    /**
     * URL for EPG/XMLTV data source
     * Can be XML, GZ, or ZIP format
     * Multiple URLs can be comma-separated
     */
    val urlTvg: String? = null,

    /**
     * Global time shift offset for EPG data in hours
     * Can be positive or negative
     */
    val tvgShift: Int? = null,

    /**
     * HTTP User-Agent string to use for requests
     */
    val userAgent: String? = null,

    /**
     * Cache settings for playlist
     */
    val cache: String? = null,

    /**
     * Playlist refresh interval in seconds
     */
    val refresh: Int? = null,

    /**
     * Deinterlace mode
     */
    val deinterlace: String? = null,

    /**
     * Aspect ratio (e.g., "16:9", "4:3")
     */
    val aspectRatio: String? = null,

    /**
     * Additional unknown attributes
     * Preserves forward compatibility with new tags
     */
    val additionalAttributes: Map<String, String> = emptyMap(),
)
