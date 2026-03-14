package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository

/**
 * Records a watch event when a user plays, pauses, or stops a channel.
 * Called from PlayerViewModel on playback state changes and periodic updates.
 */
class RecordWatchEventUseCase(
    private val watchHistoryRepository: WatchHistoryRepository,
) {
    suspend operator fun invoke(channelId: Long, playlistId: Long, durationMs: Long = 0) {
        watchHistoryRepository.recordWatchEvent(channelId, playlistId, durationMs)
    }
}
