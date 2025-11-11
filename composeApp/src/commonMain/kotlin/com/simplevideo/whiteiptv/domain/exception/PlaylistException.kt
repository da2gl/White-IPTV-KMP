package com.simplevideo.whiteiptv.domain.exception

/**
 * Base exception for playlist-related errors
 */
sealed class PlaylistException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * Network error when downloading playlist
     * Examples: no internet, timeout, DNS error
     */
    class NetworkError(message: String = "Network error occurred", cause: Throwable? = null) :
        PlaylistException(message, cause)

    /**
     * Invalid playlist URL
     */
    class InvalidUrl(url: String) :
        PlaylistException("Invalid playlist URL: $url")

    /**
     * Playlist parsing error
     * Examples: invalid M3U format, corrupted data
     */
    class ParseError(message: String = "Failed to parse playlist", cause: Throwable? = null) :
        PlaylistException(message, cause)

    /**
     * Empty playlist (no channels found)
     */
    class EmptyPlaylist :
        PlaylistException("Playlist is empty or contains no valid channels")

    /**
     * Database error when saving playlist
     */
    class DatabaseError(message: String = "Database error occurred", cause: Throwable? = null) :
        PlaylistException(message, cause)

    /**
     * Playlist not found in database
     */
    class NotFound(playlistId: Long) :
        PlaylistException("Playlist not found: $playlistId")

    /**
     * Unknown error
     */
    class Unknown(message: String = "Unknown error occurred", cause: Throwable? = null) :
        PlaylistException(message, cause)
}
