package com.simplevideo.whiteiptv.domain.model

/**
 * Represents different sources for playlist import
 */
sealed interface PlaylistSource {
    /**
     * Import from HTTP/HTTPS URL
     */
    data class Url(val url: String) : PlaylistSource

    /**
     * Import from local file
     */
    data class LocalFile(val uri: String, val fileName: String) : PlaylistSource
}
