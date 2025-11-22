package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.ChannelRepository

/**
 * Use case for toggling the favorite status of a channel
 * Delegates to repository to flip the isFavorite flag
 */
class ToggleFavoriteUseCase(
    private val channelRepository: ChannelRepository,
) {
    suspend operator fun invoke(channelId: Long) = channelRepository.toggleFavoriteStatus(channelId)
}
