package com.simplevideo.whiteiptv.data.mapper

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ExtendedChannelMetadata
import com.simplevideo.whiteiptv.data.parser.playlist.model.Channel
import kotlinx.serialization.json.Json

/**
 * Maps parsed Channel from M3U to database ChannelEntity
 */
class ChannelMapper {

    fun toEntity(
        playlistId: Long,
        channel: Channel,
        isFavorite: Boolean = false,
    ): ChannelEntity {
        return ChannelEntity(
            playlistId = playlistId,
            name = channel.title,
            url = channel.url,
            logoUrl = channel.tvgLogo,
            tvgId = channel.tvgId,
            tvgName = channel.tvgName,
            tvgChno = channel.tvgChno,
            tvgLanguage = channel.tvgLanguage,
            tvgCountry = channel.tvgCountry,
            groupTitle = channel.groupTitle,
            isFavorite = isFavorite,
            catchupDays = channel.catchup?.days,
            catchupType = channel.catchup?.type?.name?.lowercase(),
            catchupSource = channel.catchup?.source,
            userAgent = channel.vlcOpts["http-user-agent"],
            referer = channel.vlcOpts["http-referrer"],
            extendedMetadata = buildExtendedMetadata(channel),
        )
    }

    fun toEntityList(
        playlistId: Long,
        channels: List<Channel>,
        favoritesTvgIds: Set<String?> = emptySet(),
        favoritesUrls: Set<String> = emptySet(),
    ): List<ChannelEntity> {
        return channels.map { channel ->
            val wasFavorite = channel.tvgId in favoritesTvgIds || channel.url in favoritesUrls
            toEntity(playlistId, channel, wasFavorite)
        }
    }

    /**
     * Build JSON string for extended metadata (optional fields)
     * Stores: description, vlcOpts, kodiProps, provider, etc.
     */
    private fun buildExtendedMetadata(channel: Channel): String? {
        // Early exit if no extended metadata
        if (channel.vlcOpts.isEmpty() &&
            channel.kodiProps.isEmpty() &&
            channel.additionalMetadata.isEmpty()
        ) {
            return null
        }

        // Filter out vlcOpts that are already stored in separate columns
        val extraVlcOpts = channel.vlcOpts
            .filterKeys { it != "http-user-agent" && it != "http-referrer" }

        // Double check after filtering
        if (extraVlcOpts.isEmpty() &&
            channel.kodiProps.isEmpty() &&
            channel.additionalMetadata.isEmpty()
        ) {
            return null
        }

        // Create and serialize metadata
        return try {
            val metadata = ExtendedChannelMetadata(
                description = null,
                provider = null,
                vlcOpts = extraVlcOpts,
                kodiProps = channel.kodiProps,
                additionalAttributes = channel.additionalMetadata,
            )
            Json.encodeToString(metadata)
        } catch (_: Exception) {
            // If serialization fails, return null
            null
        }
    }
}
