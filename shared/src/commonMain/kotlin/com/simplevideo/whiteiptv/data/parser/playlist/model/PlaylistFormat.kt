package com.simplevideo.whiteiptv.data.parser.playlist.model

/**
 * Supported IPTV playlist formats
 */
enum class PlaylistFormat {
    /**
     * M3U format - ANSI encoding, basic format
     */
    M3U,

    /**
     * M3U8 format - UTF-8 encoding, modern standard
     */
    M3U8,

    /**
     * XSPF format - XML Shareable Playlist Format
     */
    XSPF,

    /**
     * JSON format - Advanced IPTV services
     */
    JSON,

    /**
     * Unknown or unsupported format
     */
    UNKNOWN
}
