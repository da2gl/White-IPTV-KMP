package com.simplevideo.whiteiptv.data.parser.playlist.model

/**
 * IPTV Channel with comprehensive metadata support
 * Supports all popular IPTV M3U tags as of 2025
 */
data class Channel(
    val title: String,
    val url: String,
    val duration: Int = -1,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val tvgLogo: String? = null,
    val tvgChno: String? = null,
    val tvgLanguage: String? = null,
    val tvgCountry: String? = null,
    val tvgType: String? = null,
    val tvgShift: Int? = null,
    val tvgRec: Int? = null,
    val tvgUrl: String? = null,
    val groupTitles: List<String> = emptyList(),
    val description: String? = null,

    /**
     * Catchup TV configuration
     * Enables time-shifted viewing of past broadcasts
     */
    val catchup: CatchupConfig? = null,

    val audioTrack: String? = null,
    val subtitles: String? = null,
    val aspectRatio: String? = null,
    val parentCode: String? = null,
    val censored: Boolean? = null,
    val provider: String? = null,
    val providerType: String? = null,

    /**
     * VLC player options (#EXTVLCOPT)
     * Common keys: http-user-agent, http-referrer, http-origin, network-caching, demux
     */
    val vlcOpts: Map<String, String> = emptyMap(),

    /**
     * Kodi player properties (#KODIPROP)
     * Common keys: inputstream, inputstream.adaptive.manifest_type, license_type, license_key
     */
    val kodiProps: Map<String, String> = emptyMap(),

    /**
     * Additional unknown attributes
     * Preserves forward compatibility with new tags
     */
    val additionalMetadata: Map<String, String> = emptyMap(),
)
