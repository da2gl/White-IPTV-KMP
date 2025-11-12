package com.simplevideo.whiteiptv.data.parser.playlist

import com.simplevideo.whiteiptv.data.parser.playlist.M3uParserTestResources.REAL_SCIENCE_PLAYLIST
import kotlin.test.*

/**
 * Tests for M3U parser using real science.m3u playlist from iptv-org
 * Contains 24 science/education channels
 */
class M3uParserScienceTest {

    @Test
    fun `parse real science playlist from iptv-org`() {
        val (header, channels) = M3uParser.parse(REAL_SCIENCE_PLAYLIST)

        assertEquals(24, channels.size, "Should parse all 24 channels")

        // Verify header
        assertNotNull(header)
        assertNull(header.urlTvg, "Science playlist has no urlTvg")

        // Test first channel with EXTVLCOPT
        val bfmTech = channels[0]
        assertEquals("BFM Tech & Co (1080p)", bfmTech.title)
        assertEquals("BFMTechCo.fr@SD", bfmTech.tvgId)
        assertEquals("https://i.imgur.com/FQ0FpXV.png", bfmTech.tvgLogo)
        assertEquals("News;Science", bfmTech.groupTitle)
        assertEquals(
            "https://ncdn-live-bfm.pfd.sfr.net/shls/LIVE\$BFM_TECHANDCO/index.m3u8?end=END&start=LIVE",
            bfmTech.url,
        )
        assertEquals(1, bfmTech.vlcOpts.size)
        assertEquals(
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
            bfmTech.vlcOpts["http-user-agent"],
        )
    }

    @Test
    fun `parse channels with complex group titles`() {
        val science = REAL_SCIENCE_PLAYLIST
        val (_, channels) = M3uParser.parse(science)

        val brtv = channels.find { it.title.contains("BRTV") }
        assertNotNull(brtv)
        assertEquals("Education;Science", brtv.groupTitle)

        val jilin = channels.find { it.title.contains("Jilin Rural") }
        assertNotNull(jilin)
        assertEquals("Education;Lifestyle;Science", jilin.groupTitle)
    }

    @Test
    fun `parse channel with special characters in title`() {
        val science = REAL_SCIENCE_PLAYLIST
        val (_, channels) = M3uParser.parse(science)

        val explosionCreativa = channels.find { it.tvgId == "EXCTV.ve@SD" }
        assertNotNull(explosionCreativa)
        assertEquals("Explosión Creativa (720p) [Not 24/7]", explosionCreativa.title)
    }

    @Test
    fun `parse channel with Unicode characters`() {
        val science = REAL_SCIENCE_PLAYLIST
        val (_, channels) = M3uParser.parse(science)

        val cyrillic = channels.find { it.title.contains("ЭлТР") }
        assertNotNull(cyrillic)
        assertEquals("ЭлТР Билим Илим (480p) [Not 24/7]", cyrillic.title)

        val chinese = channels.find { it.tvgId == "ShandongTVAgriculturalScienceChannel.cn@SD" }
        assertNotNull(chinese)
        assertEquals("山东农科 (406p) [Geo-blocked]", chinese.title)
    }

    @Test
    fun `parse channels with different URL protocols`() {
        val science = REAL_SCIENCE_PLAYLIST
        val (_, channels) = M3uParser.parse(science)

        val httpsChannels = channels.filter { it.url.startsWith("https://") }
        val httpChannels = channels.filter { it.url.startsWith("http://") }

        assertTrue(httpsChannels.isNotEmpty(), "Should have HTTPS channels")
        assertTrue(httpChannels.isNotEmpty(), "Should have HTTP channels")
    }

    @Test
    fun `parse channel with long URL with query parameters`() {
        val science = REAL_SCIENCE_PLAYLIST
        val (_, channels) = M3uParser.parse(science)

        val plutoTV = channels.find { it.tvgId == "PlutoTVScience.us@US" }
        assertNotNull(plutoTV)
        assertTrue(plutoTV.url.contains("appName=web"))
        assertTrue(plutoTV.url.contains("deviceType=web"))
        assertTrue(plutoTV.url.contains("serverSideAds=false"))
    }

    @Test
    fun `parse channel with http-referrer attribute in EXTINF line`() {
        val science = REAL_SCIENCE_PLAYLIST
        val (_, channels) = M3uParser.parse(science)

        val umsaTV = channels.find { it.tvgId == "UMSATVUInternacional.bo@HD" }
        assertNotNull(umsaTV)
        assertEquals("UMSA TVU Internacional (720p)", umsaTV.title)
        assertEquals(1, umsaTV.vlcOpts.size)
        assertEquals("https://tvu.umsa.bo/tvu-internacional", umsaTV.vlcOpts["http-referrer"])
    }

    @Test
    fun `parse channel with http-user-agent attribute in EXTINF line`() {
        val science = REAL_SCIENCE_PLAYLIST
        val (_, channels) = M3uParser.parse(science)

        val bfmTech = channels.find { it.tvgId == "BFMTechCo.fr@SD" }
        assertNotNull(bfmTech)
        assertEquals(1, bfmTech.vlcOpts.size)
        assertTrue(bfmTech.vlcOpts["http-user-agent"]!!.contains("Chrome"))
    }

    @Test
    fun `verify all channels have required fields`() {
        val science = REAL_SCIENCE_PLAYLIST
        val (_, channels) = M3uParser.parse(science)

        channels.forEach { channel ->
            assertTrue(channel.title.isNotBlank(), "Channel title should not be blank")
            assertTrue(channel.url.isNotBlank(), "Channel URL should not be blank")
            assertTrue(
                channel.url.startsWith("http://") || channel.url.startsWith("https://"),
                "Channel URL should start with http:// or https://",
            )
        }
    }

    @Test
    fun `verify all fields are parsed correctly from science playlist`() {
        val (header, channels) = M3uParser.parse(REAL_SCIENCE_PLAYLIST)

        // Verify header
        assertNotNull(header)

        // Verify we got all 24 channels
        assertEquals(24, channels.size)

        // Check that ALL channels have required fields
        channels.forEach { channel ->
            assertNotNull(channel.title, "Every channel must have a title")
            assertNotNull(channel.url, "Every channel must have a URL")
            assertNotNull(channel.tvgId, "Every channel must have tvg-id")
            assertNotNull(channel.tvgLogo, "Every channel must have tvg-logo")
            assertNotNull(channel.groupTitle, "Every channel must have group-title")
            assertEquals(-1, channel.duration, "Duration should be -1 (unlimited)")
        }

        // Verify specific channels with all their fields
        val bfmTech = channels.find { it.tvgId == "BFMTechCo.fr@SD" }
        assertNotNull(bfmTech)
        assertEquals("BFM Tech & Co (1080p)", bfmTech.title)
        assertEquals("BFMTechCo.fr@SD", bfmTech.tvgId)
        assertEquals("https://i.imgur.com/FQ0FpXV.png", bfmTech.tvgLogo)
        assertEquals("News;Science", bfmTech.groupTitle)
        assertEquals(1, bfmTech.vlcOpts.size, "Should have 1 VLC option")
        assertTrue(bfmTech.vlcOpts.containsKey("http-user-agent"))

        // Verify channel with http-referrer
        val umsaTV = channels.find { it.tvgId == "UMSATVUInternacional.bo@HD" }
        assertNotNull(umsaTV)
        assertEquals("UMSA TVU Internacional (720p)", umsaTV.title)
        assertEquals(1, umsaTV.vlcOpts.size, "Should have 1 VLC option")
        assertEquals("https://tvu.umsa.bo/tvu-internacional", umsaTV.vlcOpts["http-referrer"])

        // Verify channel with complex group-title
        val jilin = channels.find { it.tvgId == "JilinRuralChannel.cn@SD" }
        assertNotNull(jilin)
        assertEquals("Education;Lifestyle;Science", jilin.groupTitle)

        // Count channels by group
        val scienceOnly = channels.filter { it.groupTitle == "Science" }
        assertTrue(scienceOnly.size >= 5, "Should have multiple Science-only channels")

        val multiGroup = channels.filter { it.groupTitle?.contains(";") == true }
        assertTrue(multiGroup.size >= 5, "Should have channels with multiple groups")
    }

    @Test
    fun `verify all tvg-id formats are parsed`() {
        val (_, channels) = M3uParser.parse(REAL_SCIENCE_PLAYLIST)

        // All channels should have tvg-id with format: "Name.country@quality"
        val withAtSign = channels.filter { it.tvgId?.contains("@") == true }
        assertEquals(24, withAtSign.size, "All channels should have @ in tvg-id")

        val withDot = channels.filter { it.tvgId?.contains(".") == true }
        assertTrue(withDot.size >= 20, "Most channels should have . in tvg-id")

        // Verify different quality markers
        val sdChannels = channels.filter { it.tvgId?.contains("@SD") == true }
        val hdChannels = channels.filter { it.tvgId?.contains("@HD") == true }

        assertTrue(sdChannels.isNotEmpty(), "Should have SD channels")
        assertTrue(hdChannels.isNotEmpty(), "Should have HD channels")
    }

    @Test
    fun `verify all tvg-logo URLs are valid`() {
        val (_, channels) = M3uParser.parse(REAL_SCIENCE_PLAYLIST)

        channels.forEach { channel ->
            assertNotNull(channel.tvgLogo, "Channel ${channel.title} should have logo")
            assertTrue(
                channel.tvgLogo!!.startsWith("http://") || channel.tvgLogo!!.startsWith("https://"),
                "Logo URL for ${channel.title} should be valid HTTP(S) URL",
            )
        }

        // Check for different logo providers
        val imgurLogos = channels.filter { it.tvgLogo?.contains("imgur.com") == true }
        val otherLogos = channels.filter { it.tvgLogo?.contains("imgur.com") == false }

        assertTrue(imgurLogos.isNotEmpty(), "Should have imgur.com logos")
        assertTrue(otherLogos.isNotEmpty(), "Should have logos from other providers")
    }

    @Test
    fun `verify inline http-user-agent attribute is parsed as vlcOpts`() {
        val (_, channels) = M3uParser.parse(REAL_SCIENCE_PLAYLIST)

        // BFM Tech has http-user-agent in both inline attribute AND EXTVLCOPT
        val bfmTech = channels.find { it.tvgId == "BFMTechCo.fr@SD" }
        assertNotNull(bfmTech)

        // Should have parsed the EXTVLCOPT (inline attributes are NOT auto-converted to vlcOpts)
        assertEquals(1, bfmTech.vlcOpts.size)
        assertTrue(bfmTech.vlcOpts.containsKey("http-user-agent"))
        assertTrue(
            bfmTech.vlcOpts["http-user-agent"]!!.contains("Chrome"),
            "User agent should contain browser info",
        )
    }

    @Test
    fun `verify inline http-referrer attribute is parsed as vlcOpts`() {
        val (_, channels) = M3uParser.parse(REAL_SCIENCE_PLAYLIST)

        // UMSA TV and Xtrema Animal have http-referrer
        val umsaTV = channels.find { it.tvgId == "UMSATVUInternacional.bo@HD" }
        assertNotNull(umsaTV)
        assertEquals("https://tvu.umsa.bo/tvu-internacional", umsaTV.vlcOpts["http-referrer"])

        val xtremaAnimal = channels.find { it.tvgId == "XtremaAnimal.ar@SD" }
        assertNotNull(xtremaAnimal)
        assertEquals("https://xtrematv.com/?p=1504", xtremaAnimal.vlcOpts["http-referrer"])
    }

    @Test
    fun `verify fields NOT present in science playlist are null`() {
        val (_, channels) = M3uParser.parse(REAL_SCIENCE_PLAYLIST)

        // These fields are not in science.m3u, should be null
        channels.forEach { channel ->
            assertNull(channel.tvgName, "tvg-name not present in science.m3u")
            assertNull(channel.tvgChno, "tvg-chno not present in science.m3u")
            assertNull(channel.tvgLanguage, "tvg-language not present in science.m3u")
            assertNull(channel.tvgCountry, "tvg-country not present in science.m3u")
            assertNull(channel.tvgType, "tvg-type not present in science.m3u")
            assertNull(channel.tvgShift, "tvg-shift not present in science.m3u")
            assertNull(channel.tvgRec, "tvg-rec not present in science.m3u")
            assertNull(channel.tvgUrl, "tvg-url not present in science.m3u")
            assertNull(channel.description, "description not present in science.m3u")
            assertNull(channel.catchup, "catchup not present in science.m3u")
            assertNull(channel.audioTrack, "audio-track not present in science.m3u")
            assertNull(channel.subtitles, "subtitles not present in science.m3u")
            assertNull(channel.aspectRatio, "aspect-ratio not present in science.m3u")
            assertNull(channel.parentCode, "parent-code not present in science.m3u")
            assertNull(channel.censored, "censored not present in science.m3u")
            assertNull(channel.provider, "provider not present in science.m3u")
            assertNull(channel.providerType, "provider-type not present in science.m3u")
            assertTrue(channel.kodiProps.isEmpty(), "kodi-props not present in science.m3u")
        }
    }
}
