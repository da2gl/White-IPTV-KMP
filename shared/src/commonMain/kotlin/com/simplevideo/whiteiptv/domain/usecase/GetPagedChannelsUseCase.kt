package com.simplevideo.whiteiptv.domain.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.model.ChannelsFilter
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving paged channels with flexible filtering.
 * Uses Room PagingSource for automatic invalidation on data changes.
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

    private fun createPagingSource(filter: ChannelsFilter, query: String): PagingSource<Int, ChannelEntity> {
        if (query.isEmpty()) {
            return when (filter) {
                is ChannelsFilter.All -> channelRepository.getChannelsPaged()
                is ChannelsFilter.ByPlaylist -> channelRepository.getChannelsByPlaylistIdPaged(filter.playlistId)
                is ChannelsFilter.ByGroup -> channelRepository.getChannelsByGroupIdPaged(filter.groupId)
            }
        }
        return when (filter) {
            is ChannelsFilter.All -> channelRepository.searchChannelsPaged(query)
            is ChannelsFilter.ByPlaylist ->
                channelRepository.searchChannelsByPlaylistIdPaged(query, filter.playlistId)
            is ChannelsFilter.ByGroup ->
                channelRepository.searchChannelsByGroupIdPaged(query, filter.groupId)
        }
    }
}
