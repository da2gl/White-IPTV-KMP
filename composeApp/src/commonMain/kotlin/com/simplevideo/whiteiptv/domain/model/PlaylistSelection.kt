package com.simplevideo.whiteiptv.domain.model

/**
 * Represents playlist selection state for filtering channels
 *
 * Used to explicitly express "all playlists" vs "specific playlist" states
 * instead of using nullable Long
 */
sealed interface PlaylistSelection {
    /** Show channels from all playlists */
    data object All : PlaylistSelection

    /** Show channels from specific playlist */
    data class Selected(val id: Long) : PlaylistSelection
}
