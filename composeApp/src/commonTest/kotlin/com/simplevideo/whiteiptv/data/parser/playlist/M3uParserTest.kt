package com.simplevideo.whiteiptv.data.parser.playlist

import com.simplevideo.whiteiptv.data.parser.playlist.model.CatchupType
import kotlin.test.*

/**
 * Basic tests for M3U/M3U8 IPTV playlist parser
 * Tests synthetic/hand-crafted playlists for specific features
 *
 * For tests with real-world playlists, see:
 * - M3uParserScienceTest.kt
 * - M3uParserWeatherTest.kt
 * - M3uParserAnimationTest.kt
 */
class M3uParserTest {

    @Test
    fun `parse channel with multiple EXTVLCOPT options`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 tvg-id="test.id",Test Channel
            #EXTVLCOPT:http-user-agent=TestAgent
            #EXTVLCOPT:http-referrer=https://example.com
            https://test.stream/video.m3u8
        """.trimIndent()

        val (_, channels) = M3uParser.parse(playlist)

        assertEquals(1, channels.size)
        val channel = channels[0]
        assertEquals(2, channel.vlcOpts.size)
        assertEquals("TestAgent", channel.vlcOpts["http-user-agent"])
        assertEquals("https://example.com", channel.vlcOpts["http-referrer"])
    }

    @Test
    fun `parse simple channel without attributes`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1,Simple Channel
            https://stream.example.com/channel.m3u8
        """.trimIndent()

        val (header, channels) = M3uParser.parse(playlist)

        assertNotNull(header)
        assertEquals(1, channels.size)

        val channel = channels[0]
        assertEquals("Simple Channel", channel.title)
        assertEquals("https://stream.example.com/channel.m3u8", channel.url)
        assertEquals(-1, channel.duration)
        assertNull(channel.tvgId)
        assertNull(channel.tvgLogo)
        assertTrue(channel.groupTitles.isEmpty())
    }

    @Test
    fun `parse channel with full TVG attributes`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 tvg-id="channel.id" tvg-name="Channel Name" tvg-logo="https://logo.png" tvg-chno="123" tvg-language="English" tvg-country="US" tvg-type="video" group-title="Entertainment",Full TVG Channel
            https://stream.example.com/channel.m3u8
        """.trimIndent()

        val (_, channels) = M3uParser.parse(playlist)

        assertEquals(1, channels.size)
        val channel = channels[0]

        assertEquals("channel.id", channel.tvgId)
        assertEquals("Channel Name", channel.tvgName)
        assertEquals("https://logo.png", channel.tvgLogo)
        assertEquals("123", channel.tvgChno)
        assertEquals("English", channel.tvgLanguage)
        assertEquals("US", channel.tvgCountry)
        assertEquals("video", channel.tvgType)
        assertEquals(listOf("Entertainment"), channel.groupTitles)
        assertEquals("Full TVG Channel", channel.title)
    }

    @Test
    fun `parse playlist header with attributes`() {
        val playlist = """
            #EXTM3U url-tvg="https://epg.example.com/guide.xml" tvg-shift="2" user-agent="CustomAgent" refresh="3600"
            #EXTINF:-1,Test Channel
            https://stream.example.com/channel.m3u8
        """.trimIndent()

        val (header, channels) = M3uParser.parse(playlist)

        assertNotNull(header)
        assertEquals("https://epg.example.com/guide.xml", header.urlTvg)
        assertEquals(2, header.tvgShift)
        assertEquals("CustomAgent", header.userAgent)
        assertEquals(3600, header.refresh)

        assertEquals(1, channels.size)
    }

    @Test
    fun `parse header with underscore attributes (alias normalization)`() {
        val playlist = """
            #EXTM3U url_tvg="https://epg.example.com/guide.xml" tvg_shift="3" user_agent="Agent"
            #EXTINF:-1,Test Channel
            https://stream.example.com/channel.m3u8
        """.trimIndent()

        val (header, _) = M3uParser.parse(playlist)

        assertEquals("https://epg.example.com/guide.xml", header.urlTvg)
        assertEquals(3, header.tvgShift)
        assertEquals("Agent", header.userAgent)
    }

    @Test
    fun `parse channel with catchup configuration`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 catchup="true" catchup-type="flussonic" catchup-source="https://catchup.example.com" catchup-days="7",Catchup Channel
            https://stream.example.com/channel.m3u8
        """.trimIndent()

        val (_, channels) = M3uParser.parse(playlist)

        assertEquals(1, channels.size)
        val channel = channels[0]

        assertNotNull(channel.catchup)
        assertTrue(channel.catchup!!.enabled)
        assertEquals(CatchupType.FLUSSONIC, channel.catchup!!.type)
        assertEquals("https://catchup.example.com", channel.catchup!!.source)
        assertEquals(7, channel.catchup!!.days)
    }

    @Test
    fun `parse channel with KODIPROP options`() {
        val playlist = """
            #EXTM3U
            #KODIPROP:inputstream=inputstream.adaptive
            #KODIPROP:inputstream.adaptive.manifest_type=mpd
            #EXTINF:-1,Kodi Channel
            https://stream.example.com/channel.mpd
        """.trimIndent()

        val (_, channels) = M3uParser.parse(playlist)

        assertEquals(1, channels.size)
        val channel = channels[0]

        assertEquals(2, channel.kodiProps.size)
        assertEquals("inputstream.adaptive", channel.kodiProps["inputstream"])
        assertEquals("mpd", channel.kodiProps["inputstream.adaptive.manifest_type"])
    }

    @Test
    fun `parse empty playlist returns empty channels`() {
        val playlist = "#EXTM3U"

        val (header, channels) = M3uParser.parse(playlist)

        assertNotNull(header)
        assertEquals(0, channels.size)
    }

    @Test
    fun `parse playlist without EXTM3U header`() {
        val playlist = """
            #EXTINF:-1,Channel Without Header
            https://stream.example.com/channel.m3u8
        """.trimIndent()

        val (header, channels) = M3uParser.parse(playlist)

        assertNotNull(header)
        assertEquals(1, channels.size)
        assertEquals("Channel Without Header", channels[0].title)
    }

    @Test
    fun `parse channel with blank lines and comments`() {
        val playlist = """
            #EXTM3U

            # This is a comment
            #EXTINF:-1,Channel One
            https://stream1.example.com/channel.m3u8

            # Another comment

            #EXTINF:-1,Channel Two
            https://stream2.example.com/channel.m3u8
        """.trimIndent()

        val (_, channels) = M3uParser.parse(playlist)

        assertEquals(2, channels.size)
        assertEquals("Channel One", channels[0].title)
        assertEquals("Channel Two", channels[1].title)
    }

    @Test
    fun `parse channel with duration and negative duration`() {
        val playlist = """
            #EXTM3U
            #EXTINF:3600,Channel with duration
            https://stream1.example.com/channel.m3u8
            #EXTINF:-1,Channel without duration
            https://stream2.example.com/channel.m3u8
        """.trimIndent()

        val (_, channels) = M3uParser.parse(playlist)

        assertEquals(2, channels.size)
        assertEquals(3600, channels[0].duration)
        assertEquals(-1, channels[1].duration)
    }

    @Test
    fun `parse channel with all optional TVG fields`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 tvg-id="test.id" tvg-name="Test Name" tvg-logo="https://logo.png" tvg-chno="123" tvg-language="English" tvg-country="US" tvg-type="video" tvg-shift="2" tvg-rec="3" tvg-url="https://epg.xml" group-title="Test" description="Test description",Full TVG Test
            https://test.stream/channel.m3u8
        """.trimIndent()

        val (_, channels) = M3uParser.parse(playlist)
        assertEquals(1, channels.size)

        val channel = channels[0]
        assertEquals("test.id", channel.tvgId)
        assertEquals("Test Name", channel.tvgName)
        assertEquals("https://logo.png", channel.tvgLogo)
        assertEquals("123", channel.tvgChno)
        assertEquals("English", channel.tvgLanguage)
        assertEquals("US", channel.tvgCountry)
        assertEquals("video", channel.tvgType)
        assertEquals(2, channel.tvgShift)
        assertEquals(3, channel.tvgRec)
        assertEquals("https://epg.xml", channel.tvgUrl)
        assertEquals(listOf("Test"), channel.groupTitles)
        assertEquals("Test description", channel.description)
        assertEquals("Full TVG Test", channel.title)
    }

    @Test
    fun `parse channel with media attributes`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 audio-track="eng" subtitles="https://subs.srt" aspect-ratio="16:9",Media Test
            https://test.stream/channel.m3u8
        """.trimIndent()

        val (_, channels) = M3uParser.parse(playlist)
        assertEquals(1, channels.size)

        val channel = channels[0]
        assertEquals("eng", channel.audioTrack)
        assertEquals("https://subs.srt", channel.subtitles)
        assertEquals("16:9", channel.aspectRatio)
    }

    @Test
    fun `parse channel with parental control and provider`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 parent-code="1234" censored="true" provider="TestProvider" provider-type="IPTV",Restricted Channel
            https://test.stream/channel.m3u8
        """.trimIndent()

        val (_, channels) = M3uParser.parse(playlist)
        assertEquals(1, channels.size)

        val channel = channels[0]
        assertEquals("1234", channel.parentCode)
        assertEquals(true, channel.censored)
        assertEquals("TestProvider", channel.provider)
        assertEquals("IPTV", channel.providerType)
    }
}
