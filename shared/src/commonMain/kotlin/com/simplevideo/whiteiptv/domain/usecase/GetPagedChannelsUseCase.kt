package com.simplevideo.whiteiptv.domain.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.paging.ChannelPagingSource
import com.simplevideo.whiteiptv.domain.model.ChannelsFilter
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving paged channels with flexible filtering.
 * Returns Flow<PagingData> for incremental loading in LazyVerticalGrid.
 */
class GetPagedChannelsUseCase(
    private val channelRepository: ChannelRepository,
) {
    companion object {
        private const val PAGE_SIZE = 50
        private const val PREFETCH_DISTANCE = 25
    }

    operator fun invoke(
        filter: ChannelsFilter = ChannelsFilter.All,
        query: String = "",
    ): Flow<PagingData<ChannelEntity>> {
        val trimmedQuery = query.trim()
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
            ),
            pagingSourceFactory = { createPagingSource(filter, trimmedQuery) },
        ).flow
    }

    private fun createPagingSource(filter: ChannelsFilter, query: String): ChannelPagingSource {
        if (query.isEmpty()) {
            return when (filter) {
                is ChannelsFilter.All -> ChannelPagingSource(
                    queryExecutor = { limit, offset -> channelRepository.getChannelsPaged(limit, offset) },
                    countExecutor = { channelRepository.getChannelsCount() },
                )
                is ChannelsFilter.ByPlaylist -> ChannelPagingSource(
                    queryExecutor = { limit, offset ->
                        channelRepository.getChannelsByPlaylistIdPaged(filter.playlistId, limit, offset)
                    },
                    countExecutor = { channelRepository.getChannelsByPlaylistIdCount(filter.playlistId) },
                )
                is ChannelsFilter.ByGroup -> ChannelPagingSource(
                    queryExecutor = { limit, offset ->
                        channelRepository.getChannelsByGroupIdPaged(filter.groupId, limit, offset)
                    },
                    countExecutor = { channelRepository.getChannelsByGroupIdCount(filter.groupId) },
                )
            }
        }
        return when (filter) {
            is ChannelsFilter.All -> ChannelPagingSource(
                queryExecutor = { limit, offset -> channelRepository.searchChannelsPaged(query, limit, offset) },
                countExecutor = { channelRepository.searchChannelsCount(query) },
            )
            is ChannelsFilter.ByPlaylist -> ChannelPagingSource(
                queryExecutor = { limit, offset ->
                    channelRepository.searchChannelsByPlaylistIdPaged(query, filter.playlistId, limit, offset)
                },
                countExecutor = { channelRepository.searchChannelsByPlaylistIdCount(query, filter.playlistId) },
            )
            is ChannelsFilter.ByGroup -> ChannelPagingSource(
                queryExecutor = { limit, offset ->
                    channelRepository.searchChannelsByGroupIdPaged(query, filter.groupId, limit, offset)
                },
                countExecutor = { channelRepository.searchChannelsByGroupIdCount(query, filter.groupId) },
            )
        }
    }
}
