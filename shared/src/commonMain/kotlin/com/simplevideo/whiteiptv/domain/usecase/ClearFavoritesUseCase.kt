package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.ChannelRepository

/**
 * Clears the favorite flag from all channels.
 */
class ClearFavoritesUseCase(private val channelRepository: ChannelRepository) {
    suspend operator fun invoke() {
        channelRepository.clearAllFavorites()
    }
}
