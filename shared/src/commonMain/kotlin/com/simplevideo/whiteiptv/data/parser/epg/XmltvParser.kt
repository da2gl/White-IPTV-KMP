package com.simplevideo.whiteiptv.data.parser.epg

import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.data.local.model.EpgProgramEntity
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

/**
 * Streaming XMLTV parser for EPG data.
 *
 * Parses XMLTV format using regex-based extraction of `<programme>` elements.
 * Handles standard XMLTV attributes: start, stop, channel, title, desc, category, icon.
 */
class XmltvParser {

    /**
     * Parse XMLTV content string into EPG program entities.
     * @param content Full XMLTV content as string
     * @param tvgShiftHours Global timezone shift from playlist header (hours)
     * @return List of parsed EPG programs
     */
    fun parse(content: String, tvgShiftHours: Int = 0): List<EpgProgramEntity> {
        val programs = mutableListOf<EpgProgramEntity>()
        val shiftMs = tvgShiftHours * MILLIS_PER_HOUR

        PROGRAM_REGEX.findAll(content).forEach { match ->
            try {
                val attrs = match.groupValues[1]
                val body = match.groupValues[2]

                val channelId = CHANNEL_REGEX.find(attrs)?.groupValues?.get(1) ?: return@forEach
                val startStr = START_REGEX.find(attrs)?.groupValues?.get(1) ?: return@forEach
                val stopStr = STOP_REGEX.find(attrs)?.groupValues?.get(1) ?: return@forEach

                val startTime = parseXmltvTime(startStr) + shiftMs
                val stopTime = parseXmltvTime(stopStr) + shiftMs
                if (startTime == 0L || stopTime == 0L) return@forEach

                val title = TITLE_REGEX.find(body)?.groupValues?.get(1)?.decodeXmlEntities() ?: return@forEach
                val description = DESC_REGEX.find(body)?.groupValues?.get(1)?.decodeXmlEntities()
                val category = CATEGORY_REGEX.find(body)?.groupValues?.get(1)?.decodeXmlEntities()
                val iconUrl = ICON_REGEX.find(body)?.groupValues?.get(1)

                programs.add(
                    EpgProgramEntity(
                        channelTvgId = channelId,
                        title = title,
                        description = description,
                        startTime = startTime,
                        endTime = stopTime,
                        category = category,
                        iconUrl = iconUrl,
                    ),
                )
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Logger.withTag(TAG).w { "Failed to parse programme element: ${e.message}" }
            }
        }

        Logger.withTag(TAG).d { "Parsed ${programs.size} EPG programs" }
        return programs
    }

    @OptIn(ExperimentalTime::class)
    companion object {
        private const val TAG = "XmltvParser"
        private const val MILLIS_PER_HOUR = 3_600_000L

        private val PROGRAM_REGEX = Regex(
            """<programme\s+([^>]*)>(.*?)</programme>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
        )
        private val START_REGEX = Regex("""start="([^"]+)"""")
        private val STOP_REGEX = Regex("""stop="([^"]+)"""")
        private val CHANNEL_REGEX = Regex("""channel="([^"]+)"""")
        private val TITLE_REGEX = Regex("""<title[^>]*>(.*?)</title>""", RegexOption.DOT_MATCHES_ALL)
        private val DESC_REGEX = Regex("""<desc[^>]*>(.*?)</desc>""", RegexOption.DOT_MATCHES_ALL)
        private val CATEGORY_REGEX = Regex("""<category[^>]*>(.*?)</category>""", RegexOption.DOT_MATCHES_ALL)
        private val ICON_REGEX = Regex("""<icon\s+src="([^"]+)"""")

        /**
         * Parse XMLTV timestamp format: "YYYYMMDDHHmmss +HHMM" or "YYYYMMDDHHmmss"
         * Returns UTC epoch milliseconds, or 0 on failure.
         */
        @Suppress("ReturnCount")
        internal fun parseXmltvTime(timeStr: String): Long {
            val parts = timeStr.trim().split(" ", limit = 2)
            val dt = parts[0]
            if (dt.length < 14) return 0L

            val year = dt.substring(0, 4).toIntOrNull() ?: return 0L
            val month = dt.substring(4, 6).toIntOrNull() ?: return 0L
            val day = dt.substring(6, 8).toIntOrNull() ?: return 0L
            val hour = dt.substring(8, 10).toIntOrNull() ?: return 0L
            val minute = dt.substring(10, 12).toIntOrNull() ?: return 0L
            val second = dt.substring(12, 14).toIntOrNull() ?: return 0L

            val localDateTime = LocalDateTime(year, month, day, hour, minute, second)

            val offset = if (parts.size > 1) {
                val tz = parts[1].trim()
                val sign = if (tz.startsWith("-")) -1 else 1
                val tzDigits = tz.removePrefix("+").removePrefix("-")
                val tzHours = tzDigits.substring(0, 2).toIntOrNull() ?: 0
                val tzMinutes = if (tzDigits.length >= 4) tzDigits.substring(2, 4).toIntOrNull() ?: 0 else 0
                UtcOffset(hours = sign * tzHours, minutes = sign * tzMinutes)
            } else {
                UtcOffset.ZERO
            }

            return localDateTime.toInstant(offset).toEpochMilliseconds()
        }

        internal fun String.decodeXmlEntities(): String {
            return this
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .trim()
        }
    }
}
