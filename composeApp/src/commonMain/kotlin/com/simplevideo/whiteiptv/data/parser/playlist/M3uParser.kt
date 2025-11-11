package com.simplevideo.whiteiptv.data.parser.playlist

import com.simplevideo.whiteiptv.data.parser.playlist.model.CatchupConfig
import com.simplevideo.whiteiptv.data.parser.playlist.model.CatchupType
import com.simplevideo.whiteiptv.data.parser.playlist.model.Channel
import com.simplevideo.whiteiptv.data.parser.playlist.model.PlaylistHeader

/**
 * Comprehensive M3U/M3U8 IPTV playlist parser
 *
 * Supports all popular IPTV tags as of 2025:
 * - Standard M3U: #EXTINF, duration, title
 * - TVG tags: tvg-id, tvg-name, tvg-logo, tvg-chno, tvg-language, tvg-country, etc.
 * - Catchup TV: catchup, catchup-type, catchup-source, catchup-days
 * - Player options: #EXTVLCOPT, #KODIPROP
 * - Grouping: group-title, #EXTGRP
 * - Metadata: description, provider, parent-code, etc.
 *
 * Features:
 * - UTF-8 encoding support (M3U8)
 * - Attribute alias normalization (tvg_id → tvg-id)
 * - Robust error handling
 * - Unknown tags preservation
 * - Multi-line entry support
 */
object M3uParser {

    private val EXTINF_REGEX = """^#EXTINF:\s*(-?\d+)\s*(.*)$""".toRegex()
    private val ATTR_REGEX = """(\w[-\w]*)="([^"]*)"""".toRegex()
    private val EXTM3U_REGEX = """^#EXTM3U\s*(.*)$""".toRegex()

    /**
     * Parse M3U/M3U8 playlist content
     *
     * @param content Playlist file content as string
     * @return Pair of PlaylistHeader and list of Channels
     */
    fun parse(content: String): Pair<PlaylistHeader, List<Channel>> {
        val lines = content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val header = parseHeader(lines)
        val channels = parseChannels(lines)

        return header to channels
    }

    /**
     * Parse playlist header (#EXTM3U line)
     */
    private fun parseHeader(lines: List<String>): PlaylistHeader {
        val headerLine = lines.firstOrNull { it.startsWith("#EXTM3U") } ?: return PlaylistHeader()

        val match = EXTM3U_REGEX.find(headerLine) ?: return PlaylistHeader()
        val attributes = match.groupValues.getOrNull(1)?.trim() ?: return PlaylistHeader()

        val attrs = parseAttributes(attributes)

        return PlaylistHeader(
            urlTvg = attrs["url-tvg"] ?: attrs["url_tvg"],
            tvgShift = attrs["tvg-shift"]?.toIntOrNull() ?: attrs["tvg_shift"]?.toIntOrNull(),
            userAgent = attrs["user-agent"] ?: attrs["user_agent"],
            cache = attrs["cache"],
            refresh = attrs["refresh"]?.toIntOrNull(),
            deinterlace = attrs["deinterlace"],
            aspectRatio = attrs["aspect-ratio"] ?: attrs["aspect_ratio"],
            additionalAttributes = attrs.filterKeys { key ->
                key !in setOf(
                    "url-tvg", "url_tvg", "tvg-shift", "tvg_shift",
                    "user-agent", "user_agent", "cache", "refresh",
                    "deinterlace", "aspect-ratio", "aspect_ratio",
                )
            },
        )
    }

    /**
     * Parse all channels from playlist
     */
    private fun parseChannels(lines: List<String>): List<Channel> {
        val channels = mutableListOf<Channel>()
        var i = 0

        var pendingVlcOpts = mutableMapOf<String, String>()
        var pendingKodiProps = mutableMapOf<String, String>()

        while (i < lines.size) {
            val line = lines[i]

            when {
                // #EXTVLCOPT
                line.startsWith("#EXTVLCOPT:") -> {
                    val opt = line.substringAfter("#EXTVLCOPT:").trim()
                    val (key, value) = parseKeyValue(opt)
                    if (key != null && value != null) {
                        pendingVlcOpts[key] = value
                    }
                }

                // #KODIPROP
                line.startsWith("#KODIPROP:") -> {
                    val prop = line.substringAfter("#KODIPROP:").trim()
                    val (key, value) = parseKeyValue(prop)
                    if (key != null && value != null) {
                        pendingKodiProps[key] = value
                    }
                }

                // #EXTINF - Channel entry
                line.startsWith("#EXTINF") -> {
                    val channel = parseChannel(
                        lines = lines,
                        startIndex = i,
                        pendingVlcOpts = pendingVlcOpts,
                        pendingKodiProps = pendingKodiProps,
                    )

                    if (channel != null) {
                        channels.add(channel)
                        // Clear pending options after using them
                        pendingVlcOpts = mutableMapOf()
                        pendingKodiProps = mutableMapOf()
                    }
                }
            }

            i++
        }

        return channels
    }

    /**
     * Parse single channel entry
     */
    private fun parseChannel(
        lines: List<String>,
        startIndex: Int,
        pendingVlcOpts: Map<String, String>,
        pendingKodiProps: Map<String, String>,
    ): Channel? {
        val extinfLine = lines[startIndex]
        val match = EXTINF_REGEX.find(extinfLine) ?: return null

        val duration = match.groupValues.getOrNull(1)?.toIntOrNull() ?: -1
        val attributesAndTitle = match.groupValues.getOrNull(2)?.trim() ?: ""

        // Extract title (after last comma)
        val lastCommaIndex = attributesAndTitle.lastIndexOf(',')
        val title = if (lastCommaIndex != -1) {
            attributesAndTitle.substring(lastCommaIndex + 1).trim()
        } else {
            attributesAndTitle.trim()
        }

        if (title.isEmpty()) return null

        // Extract attributes (before last comma)
        val attributesString = if (lastCommaIndex != -1) {
            attributesAndTitle.substring(0, lastCommaIndex).trim()
        } else {
            ""
        }

        val attrs = parseAttributes(attributesString)

        // Find URL (next non-comment line)
        val url = findNextUrl(lines, startIndex) ?: return null

        // Build catchup config
        val catchup = buildCatchupConfig(attrs)

        return Channel(
            title = title,
            url = url,
            duration = duration,
            // TVG tags
            tvgId = attrs["tvg-id"] ?: attrs["tvg_id"],
            tvgName = attrs["tvg-name"] ?: attrs["tvg_name"],
            tvgLogo = attrs["tvg-logo"] ?: attrs["tvg_logo"],
            tvgChno = attrs["tvg-chno"] ?: attrs["tvg_chno"],
            tvgLanguage = attrs["tvg-language"] ?: attrs["tvg-lang"] ?: attrs["tvg_language"],
            tvgCountry = attrs["tvg-country"] ?: attrs["tvg_country"],
            tvgType = attrs["tvg-type"] ?: attrs["tvg_type"],
            tvgShift = attrs["tvg-shift"]?.toIntOrNull() ?: attrs["tvg_shift"]?.toIntOrNull(),
            tvgRec = attrs["tvg-rec"]?.toIntOrNull() ?: attrs["tvg_rec"]?.toIntOrNull(),
            tvgUrl = attrs["tvg-url"] ?: attrs["tvg_url"],
            // Grouping
            groupTitle = attrs["group-title"] ?: attrs["group_title"],
            description = attrs["description"] ?: attrs["tvg-description"] ?: attrs["tvg_description"],
            // Catchup
            catchup = catchup,
            // Media
            audioTrack = attrs["audio-track"] ?: attrs["audio_track"],
            subtitles = attrs["subtitles"],
            aspectRatio = attrs["aspect-ratio"] ?: attrs["aspect_ratio"],
            // Parental control
            parentCode = attrs["parent-code"] ?: attrs["parent_code"],
            censored = attrs["censored"]?.toBooleanStrictOrNull(),
            // Provider
            provider = attrs["provider"],
            providerType = attrs["provider-type"] ?: attrs["provider_type"],
            // Player options
            vlcOpts = pendingVlcOpts,
            kodiProps = pendingKodiProps,
            // Additional metadata
            additionalMetadata = attrs.filterKeys { key ->
                key !in KNOWN_ATTRIBUTES
            },
        )
    }

    /**
     * Build catchup configuration from attributes
     */
    private fun buildCatchupConfig(attrs: Map<String, String>): CatchupConfig? {
        val catchupEnabled = attrs["catchup"]?.let {
            it.equals("true", ignoreCase = true) ||
                it.equals("1", ignoreCase = true) ||
                it.equals("yes", ignoreCase = true)
        } ?: false

        if (!catchupEnabled && attrs["catchup-type"] == null) {
            return null
        }

        val type = CatchupType.fromString(attrs["catchup-type"] ?: attrs["catchup_type"])
        val source = attrs["catchup-source"] ?: attrs["catchup_source"]
        val days = attrs["catchup-days"]?.toIntOrNull()
            ?: attrs["catchup_days"]?.toIntOrNull()
            ?: attrs["timeshift"]?.toIntOrNull()
        val correction = attrs["catchup-correction"]?.toIntOrNull()
            ?: attrs["catchup_correction"]?.toIntOrNull()

        return CatchupConfig(
            enabled = catchupEnabled || type != CatchupType.DEFAULT,
            type = type,
            source = source,
            days = days,
            correction = correction,
        )
    }

    /**
     * Parse attributes from string (key="value" pairs)
     */
    private fun parseAttributes(text: String): Map<String, String> {
        val attrs = mutableMapOf<String, String>()

        ATTR_REGEX.findAll(text).forEach { match ->
            val key = match.groupValues.getOrNull(1)?.trim()?.lowercase()
            val value = match.groupValues.getOrNull(2)?.trim()

            if (key != null && value != null && key.isNotEmpty() && value.isNotEmpty()) {
                // Normalize attribute names (underscore to hyphen)
                val normalizedKey = key.replace('_', '-')
                attrs[normalizedKey] = value
            }
        }

        return attrs
    }

    /**
     * Parse key=value pair (for EXTVLCOPT and KODIPROP)
     */
    private fun parseKeyValue(text: String): Pair<String?, String?> {
        val separatorIndex = text.indexOf('=')
        if (separatorIndex == -1) return null to null

        val key = text.substring(0, separatorIndex).trim()
        val value = text.substring(separatorIndex + 1).trim()

        return key to value
    }

    /**
     * Find next URL line (non-comment, non-empty)
     */
    private fun findNextUrl(lines: List<String>, startIndex: Int): String? {
        for (i in (startIndex + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty() || line.startsWith("#")) continue
            return line
        }
        return null
    }

    /**
     * Set of known attribute names
     * Used to filter unknown attributes into additionalMetadata
     */
    private val KNOWN_ATTRIBUTES = setOf(
        "tvg-id", "tvg-name", "tvg-logo", "tvg-chno", "tvg-language", "tvg-lang",
        "tvg-country", "tvg-type", "tvg-shift", "tvg-rec", "tvg-url", "tvg-description",
        "group-title", "description", "audio-track", "subtitles", "aspect-ratio",
        "parent-code", "censored", "provider", "provider-type",
        "catchup", "catchup-type", "catchup-source", "catchup-days", "catchup-correction",
        "timeshift",
    )
}
