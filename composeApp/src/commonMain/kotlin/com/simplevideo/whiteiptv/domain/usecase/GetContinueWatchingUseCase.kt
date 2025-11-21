package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.feature.home.mvi.ContinueWatchingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fetches continue watching items for the home screen
 *
 * TODO: Implement actual watch history tracking from repository
 * Currently returns empty Flow as placeholder
 */
class GetContinueWatchingUseCase {
    operator fun invoke(): Flow<List<ContinueWatchingItem>> = flowOf(emptyList())
}
