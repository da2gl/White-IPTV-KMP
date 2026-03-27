package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.ChannelRepository

/**
 * Use case for renaming a single channel
 */
class RenameChannelUseCase(
    private val channelRepository: ChannelRepository,
) {
    suspend operator fun invoke(channelId: Long, newName: String) =
        channelRepository.renameChannel(channelId, newName)
}
