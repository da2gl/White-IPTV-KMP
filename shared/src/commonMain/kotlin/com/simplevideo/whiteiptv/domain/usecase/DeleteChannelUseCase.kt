package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.ChannelRepository

/**
 * Use case for deleting a single channel by ID
 */
class DeleteChannelUseCase(
    private val channelRepository: ChannelRepository,
) {
    suspend operator fun invoke(channelId: Long) = channelRepository.deleteChannel(channelId)
}
