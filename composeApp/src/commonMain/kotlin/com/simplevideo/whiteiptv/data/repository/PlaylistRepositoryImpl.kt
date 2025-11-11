package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.data.parser.playlist.M3uParser
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val httpClient: HttpClient
) : PlaylistRepository {
    override suspend fun hasPlaylist(): Boolean {
        return playlistDao.getPlaylistCount() > 0
    }

    override suspend fun importPlaylistFromUrl(url: String) {
        val m3uString = httpClient.get(url).bodyAsText()

        // Parse using enhanced M3uParser with full IPTV tags support
        val (header, channels) = M3uParser.parse(m3uString)

        // Create playlist entity with header metadata
        val playlistEntity = PlaylistEntity(
            name = url.substringAfterLast('/').substringBeforeLast('.'), // Extract name from URL
            url = url,
            icon = null, // TODO: User can set later
            // EPG settings
            urlTvg = header.urlTvg,
            tvgShift = header.tvgShift,
            // Network settings
            userAgent = header.userAgent,
            refreshInterval = header.refresh,
            // Metadata
            channelCount = channels.size,
            lastUpdate = Clock.System.now().toEpochMilliseconds(),
            createdAt = Clock.System.now().toEpochMilliseconds(),
        )

        val playlistId = playlistDao.insertPlaylist(playlistEntity)

        // Map parsed channels to database entities
        val channelEntities = channels.map { channel ->
            // Build extended metadata JSON if needed
            val extendedMetadata = buildExtendedMetadata(channel)

            ChannelEntity(
                playlistId = playlistId,
                // Basic
                name = channel.title,
                url = channel.url,
                logoUrl = channel.tvgLogo,
                // TVG tags for EPG
                tvgId = channel.tvgId,
                tvgName = channel.tvgName,
                tvgChno = channel.tvgChno,
                tvgLanguage = channel.tvgLanguage,
                tvgCountry = channel.tvgCountry,
                // Grouping
                groupTitle = channel.groupTitle,
                isFavorite = false, // User preference, not from M3U
                // Catchup
                catchupDays = channel.catchup?.days,
                catchupType = channel.catchup?.type?.name?.lowercase(),
                catchupSource = channel.catchup?.source,
                // Network
                userAgent = channel.vlcOpts["http-user-agent"],
                referer = channel.vlcOpts["http-referrer"],
                // Extended metadata (less common fields)
                extendedMetadata = extendedMetadata,
            )
        }

        playlistDao.insertChannels(channelEntities)
    }

    /**
     * Build JSON string for extended metadata (optional fields)
     * Stores: description, vlcOpts, kodiProps, provider, etc.
     */
    private fun buildExtendedMetadata(channel: com.simplevideo.whiteiptv.data.parser.playlist.model.Channel): String? {
        // TODO: Use kotlinx.serialization to build JSON
        // For now, return null - can be implemented later
        return null
    }
}
