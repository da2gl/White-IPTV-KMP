package com.simplevideo.whiteiptv.data.mapper

import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.data.parser.playlist.model.PlaylistHeader
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps parsed PlaylistHeader from M3U to database PlaylistEntity
 */
@OptIn(ExperimentalTime::class)
class PlaylistMapper {

    fun toEntity(
        url: String,
        header: PlaylistHeader,
        channelCount: Int,
    ): PlaylistEntity {
        return PlaylistEntity(
            name = extractNameFromUrl(url),
            url = url,
            icon = null,
            urlTvg = header.urlTvg,
            tvgShift = header.tvgShift,
            userAgent = header.userAgent,
            refreshInterval = header.refresh,
            channelCount = channelCount,
            lastUpdate = Clock.System.now().toEpochMilliseconds(),
            createdAt = Clock.System.now().toEpochMilliseconds(),
        )
    }

    fun updateEntity(
        existing: PlaylistEntity,
        header: PlaylistHeader,
        channelCount: Int,
    ): PlaylistEntity {
        return existing.copy(
            urlTvg = header.urlTvg,
            tvgShift = header.tvgShift,
            userAgent = header.userAgent,
            refreshInterval = header.refresh,
            channelCount = channelCount,
            lastUpdate = Clock.System.now().toEpochMilliseconds(),
        )
    }

    /**
     * Extract playlist name from URL
     * Example: "https://example.com/playlist.m3u" -> "playlist"
     */
    private fun extractNameFromUrl(url: String): String {
        return url.substringAfterLast('/').substringBeforeLast('.')
    }
}
