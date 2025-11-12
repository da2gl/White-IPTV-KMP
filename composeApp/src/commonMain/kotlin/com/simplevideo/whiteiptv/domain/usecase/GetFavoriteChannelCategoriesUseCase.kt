package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetFavoriteChannelCategoriesUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    operator fun invoke(): Flow<List<String>> {
        return playlistRepository.getFavoriteChannels().map { channels ->
            channels
                .mapNotNull { it.groupTitle }
                .distinct()
                .sorted()
        }
    }
}
