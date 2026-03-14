package com.simplevideo.whiteiptv.data.local.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity

/**
 * Custom PagingSource for channels that uses Room LIMIT/OFFSET queries.
 * Required because room-paging artifact is not KMP-compatible.
 */
class ChannelPagingSource(
    private val queryExecutor: suspend (limit: Int, offset: Int) -> List<ChannelEntity>,
    private val countExecutor: suspend () -> Int,
) : PagingSource<Int, ChannelEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChannelEntity> {
        val page = params.key ?: 0
        val pageSize = params.loadSize
        val offset = page * pageSize

        return try {
            val totalCount = countExecutor()
            val items = queryExecutor(pageSize, offset)

            LoadResult.Page(
                data = items,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (offset + items.size < totalCount) page + 1 else null,
            )
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ChannelEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
