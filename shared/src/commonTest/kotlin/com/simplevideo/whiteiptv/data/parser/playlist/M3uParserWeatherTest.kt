package com.simplevideo.whiteiptv.data.parser.playlist

import com.simplevideo.whiteiptv.data.parser.playlist.M3uParserTestResources.REAL_SCIENCE_PLAYLIST
import com.simplevideo.whiteiptv.data.parser.playlist.M3uParserTestResources.REAL_WEATHER_PLAYLIST
import kotlin.test.*

/**
 * Tests for M3U parser using real weather.m3u playlist from iptv-org
 * Contains 16 weather channels
 */
class M3uParserWeatherTest {

    @Test
    fun `parse real weather playlist from iptv-org`() {
        val (header, channels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        assertEquals(16, channels.size, "Should parse all 16 weather channels")
        assertNotNull(header)
        assertNull(header.urlTvg, "Weather playlist has no urlTvg")
    }

    @Test
    fun `weather playlist - verify first channel AccuWeather`() {
        val (_, channels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        val accuWeather = channels.find { it.tvgId == "AccuWeatherNOW.us@SD" }
        assertNotNull(accuWeather, "AccuWeather channel should exist")
        assertEquals("AccuWeather Now (1080p)", accuWeather.title)
        assertEquals("https://i.imgur.com/M8wbVYK.png", accuWeather.tvgLogo)
        assertEquals(listOf("Weather"), accuWeather.groupTitles)
        assertTrue(accuWeather.url.contains("amagi.tv"))
    }

    @Test
    fun `weather playlist - verify Fox Weather channel`() {
        val (_, channels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        val foxWeather = channels.find { it.tvgId == "FoxWeather.us@SD" }
        assertNotNull(foxWeather, "Fox Weather channel should exist")
        assertEquals("Fox Weather (720p)", foxWeather.title)
        assertTrue(foxWeather.tvgLogo!!.contains("Fox_Weather"))
        assertEquals(listOf("Weather"), foxWeather.groupTitles)
        assertTrue(foxWeather.url.contains("foxweather.com"))
    }

    @Test
    fun `weather playlist - verify all channels are Weather category`() {
        val (_, channels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        val weatherChannels = channels.filter {
            it.groupTitles.contains("Weather")
        }

        assertTrue(weatherChannels.size >= 14, "Most channels should be in Weather category")
    }

    @Test
    fun `weather playlist - verify Japanese weather channels`() {
        val (_, channels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        val nhkKishou = channels.find { it.tvgId == "NHKKishouSaigai.jp@SD" }
        assertNotNull(nhkKishou, "NHK Kishou Saigai should exist")
        assertEquals("NHK Kishou Saigai (360p) [Not 24/7]", nhkKishou.title)

        val weathernews = channels.find { it.tvgId == "Weathernews.jp@SD" }
        assertNotNull(weathernews, "Weathernews should exist")
        assertEquals("Weathernews (720p)", weathernews.title)
    }

    @Test
    fun `weather playlist - verify channels from different countries`() {
        val (_, channels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        // Extract country codes from tvg-id format: "ChannelName.COUNTRY@QUALITY"
        // Example: "AccuWeatherNOW.us@SD" → "us"
        val countries = channels.mapNotNull { channel ->
            channel.tvgId?.substringAfterLast(".")?.substringBefore("@")
        }.distinct()

        assertTrue(countries.size >= 5, "Should have channels from multiple countries (found: ${countries.size})")
        assertTrue(countries.any { it.equals("us", ignoreCase = true) }, "Should have US channels")
        assertTrue(countries.any { it.equals("jp", ignoreCase = true) }, "Should have Japanese channels")
    }

    @Test
    fun `weather playlist - verify resolution tags in titles`() {
        val (_, channels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        val with1080p = channels.filter { it.title.contains("1080p") }
        val with720p = channels.filter { it.title.contains("720p") }
        val with480p = channels.filter { it.title.contains("480p") }
        val with360p = channels.filter { it.title.contains("360p") }

        assertTrue(with1080p.isNotEmpty(), "Should have 1080p channels")
        assertTrue(with720p.isNotEmpty(), "Should have 720p channels")

        val totalWithResolution = with1080p.size + with720p.size + with480p.size + with360p.size
        assertTrue(totalWithResolution >= 10, "Most channels should specify resolution")
    }

    @Test
    fun `weather playlist - verify special markers in titles`() {
        val (_, channels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        val notAlways = channels.filter { it.title.contains("[Not 24/7]") }
        assertTrue(notAlways.isNotEmpty(), "Should have channels marked as [Not 24/7]")
    }

    @Test
    fun `weather playlist - verify all URLs are valid`() {
        val (_, channels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        channels.forEach { channel ->
            assertTrue(
                channel.url.startsWith("http://") || channel.url.startsWith("https://"),
                "Weather channel ${channel.title} should have valid URL",
            )
            assertTrue(
                channel.url.contains(".m3u8") || channel.url.contains("playlist"),
                "Weather channel ${channel.title} should have streaming URL",
            )
        }
    }

    @Test
    fun `weather playlist - compare with science playlist structure`() {
        val (_, weatherChannels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)
        val (_, scienceChannels) = M3uParser.parse(REAL_SCIENCE_PLAYLIST)

        assertEquals(16, weatherChannels.size, "Weather should have 16 channels")
        assertEquals(24, scienceChannels.size, "Science should have 24 channels")

        assertTrue(weatherChannels.all { it.title.isNotBlank() })
        assertTrue(scienceChannels.all { it.title.isNotBlank() })
    }
}
