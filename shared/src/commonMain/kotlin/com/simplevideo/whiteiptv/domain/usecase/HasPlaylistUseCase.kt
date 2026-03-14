package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository

/**
 * Use case for checking if any playlist exists
 * Used by SplashScreen to determine navigation destination
 */
class HasPlaylistUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(): Boolean = playlistRepository.hasPlaylist()
}
