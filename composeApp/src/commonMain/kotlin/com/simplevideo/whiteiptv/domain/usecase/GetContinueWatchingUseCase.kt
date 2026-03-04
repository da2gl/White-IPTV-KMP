package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository
import com.simplevideo.whiteiptv.feature.home.mvi.ContinueWatchingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Fetches continue watching items for the home screen.
 * Returns the last 10 watched channels sorted by recency.
 */
class GetContinueWatchingUseCase(
    private val watchHistoryRepository: WatchHistoryRepository,
) {
    operator fun invoke(): Flow<List<ContinueWatchingItem>> =
        watchHistoryRepository.getRecentlyWatchedChannels(limit = RECENT_LIMIT)
            .map { channels ->
                channels.map { channel ->
                    ContinueWatchingItem(
                        channel = channel,
                        progress = 0f,
                        timeLeft = "",
                    )
                }
            }

    companion object {
        private const val RECENT_LIMIT = 10
    }
}
