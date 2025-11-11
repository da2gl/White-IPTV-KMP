package com.simplevideo.whiteiptv.data.local.model

import kotlinx.serialization.Serializable

/**
 * Extended channel metadata that is stored as JSON
 * Contains less common fields that don't need separate columns
 */
@Serializable
data class ExtendedChannelMetadata(
    /**
     * Channel description/synopsis
     */
    val description: String? = null,

    /**
     * Content provider name
     */
    val provider: String? = null,

    /**
     * VLC player options (all http-* and other options)
     * Example: {"http-reconnect": "true", "network-caching": "1000"}
     */
    val vlcOpts: Map<String, String> = emptyMap(),

    /**
     * Kodi player properties
     * Example: {"inputstream": "inputstream.adaptive"}
     */
    val kodiProps: Map<String, String> = emptyMap(),

    /**
     * Additional attributes not covered by standard fields
     * For future extensibility
     */
    val additionalAttributes: Map<String, String> = emptyMap(),
)
