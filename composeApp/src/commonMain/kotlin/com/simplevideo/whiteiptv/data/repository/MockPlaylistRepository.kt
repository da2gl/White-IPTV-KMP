package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository

class MockPlaylistRepository : PlaylistRepository {
    override suspend fun hasPlaylist(): Boolean {
        // For now, we'll always return false to force the onboarding screen
        return false
    }

    override suspend fun importPlaylistFromUrl(url: String) {
        // TODO: Implement playlist import from URL
        // This is a mock implementation
    }
}
