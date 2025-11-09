package com.simplevideo.whiteiptv.domain.repository

interface PlaylistRepository {
    suspend fun hasPlaylist(): Boolean
    suspend fun importPlaylistFromUrl(url: String)
}
