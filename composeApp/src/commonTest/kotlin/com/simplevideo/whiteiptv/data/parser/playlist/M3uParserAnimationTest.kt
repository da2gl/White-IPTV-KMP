package com.simplevideo.whiteiptv.data.parser.playlist

import com.simplevideo.whiteiptv.data.parser.playlist.M3uParserTestResources.REAL_ANIMATION_PLAYLIST
import com.simplevideo.whiteiptv.data.parser.playlist.M3uParserTestResources.REAL_SCIENCE_PLAYLIST
import com.simplevideo.whiteiptv.data.parser.playlist.M3uParserTestResources.REAL_WEATHER_PLAYLIST
import kotlin.test.*

/**
 * Tests for M3U parser using real animation.m3u playlist from iptv-org
 * Contains 59 animation channels
 */
class M3uParserAnimationTest {

    @Test
    fun `parse real animation playlist from iptv-org`() {
        val (header, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        assertEquals(59, channels.size, "Should parse all 59 animation channels")
        assertNotNull(header)
        assertNull(header.urlTvg, "Animation playlist has no urlTvg")
    }

    @Test
    fun `animation playlist - verify Disney channels`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val disneyChannel = channels.find { it.tvgId == "DisneyChannelLatinAmerica.ar@Panregional" }
        assertNotNull(disneyChannel, "Disney Channel Latin America should exist")
        assertEquals("Disney Channel Latin America (1080p)", disneyChannel.title)
        assertEquals(listOf("Animation", "Kids"), disneyChannel.groupTitles)

        val disneyJrTurkey = channels.find { it.tvgId == "DisneyJr.tr@SD" }
        assertNotNull(disneyJrTurkey, "Disney Jr. Turkey should exist")
        assertEquals("Disney Jr. (1080p) [Geo-blocked]", disneyJrTurkey.title)
    }

    @Test
    fun `animation playlist - verify anime channels`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val animeAllDay = channels.find { it.tvgId == "AnimeAllDay.us@US" }
        assertNotNull(animeAllDay, "Anime All Day should exist")
        assertEquals("Anime All Day", animeAllDay.title)
        assertEquals(listOf("Animation"), animeAllDay.groupTitles)

        val animeVision = channels.find { it.tvgId == "AnimeVision.es@SD" }
        assertNotNull(animeVision, "Anime Vision should exist")
        assertEquals("Anime Vision (1080p)", animeVision.title)
    }

    @Test
    fun `animation playlist - verify Naruto channels for different regions`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val narutoChannels = channels.filter { it.title == "Naruto" }
        assertEquals(4, narutoChannels.size, "Should have 4 Naruto channels for different regions")

        val regions = narutoChannels.mapNotNull { it.tvgId?.substringAfter("@") }.toSet()
        assertTrue(regions.contains("Austria"), "Should have Naruto Austria")
        assertTrue(regions.contains("Canada"), "Should have Naruto Canada")
        assertTrue(regions.contains("Germany"), "Should have Naruto Germany")
        assertTrue(regions.contains("US"), "Should have Naruto US")
    }

    @Test
    fun `animation playlist - verify Pokemon and anime channels`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val pokemon = channels.find { it.tvgId == "PlutoTVPokemon.us@SD" }
        assertNotNull(pokemon, "Pokémon channel should exist")
        assertEquals("Pokémon", pokemon.title)

        val onePiece = channels.find { it.tvgId == "OnePiece.us@SD" }
        assertNotNull(onePiece, "One Piece should exist")
        assertEquals("One Piece", onePiece.title)

        val yuGiOh = channels.find { it.tvgId == "YuGiOh.us@SD" }
        assertNotNull(yuGiOh, "Yu-Gi-Oh! should exist")
        assertEquals("Yu-Gi-Oh!", yuGiOh.title)
    }

    @Test
    fun `animation playlist - verify channels with http-referrer`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val gikTv = channels.find { it.tvgId == "GikTVMX.mx@SD" }
        assertNotNull(gikTv, "GikTVMx should exist")
        assertEquals("GikTVMx (480p)", gikTv.title)
        assertEquals(1, gikTv.vlcOpts.size)
        assertEquals("https://giktvmx.g3radio.mx", gikTv.vlcOpts["http-referrer"])

        val pingvins = channels.find { it.tvgId == "Pingviins.lv@SD" }
        assertNotNull(pingvins, "Pingvīns should exist")
        assertEquals("Pingvīns (576p) [Geo-blocked]", pingvins.title)
        assertEquals(1, pingvins.vlcOpts.size)
        assertEquals("https://void.greenhosting.ru/", pingvins.vlcOpts["http-referrer"])

        val xtremaCartoons = channels.find { it.tvgId == "XtremaCartoons.ar@SD" }
        assertNotNull(xtremaCartoons, "Xtrema Cartoons should exist")
        assertEquals("https://xtrematv.com/?p=1390", xtremaCartoons.vlcOpts["http-referrer"])
    }

    @Test
    fun `animation playlist - verify group-title variations`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val multipleGroups = channels.filter { it.groupTitles.size > 1 }
        assertTrue(multipleGroups.size >= 30, "Should have many channels with multiple groups")

        val animationKids = channels.filter { it.groupTitles == listOf("Animation", "Kids") }
        assertTrue(animationKids.size >= 15, "Should have many Animation;Kids channels")

        val animationKidsReligious = channels.filter { it.groupTitles == listOf("Animation", "Kids", "Religious") }
        assertTrue(animationKidsReligious.isNotEmpty(), "Should have religious kids animation")

        val animationOnly = channels.filter { it.groupTitles == listOf("Animation") }
        assertTrue(animationOnly.size >= 10, "Should have animation-only channels")
    }

    @Test
    fun `animation playlist - verify special markers in titles`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val geoBlocked = channels.filter { it.title.contains("[Geo-blocked]") }
        assertTrue(geoBlocked.size >= 3, "Should have geo-blocked channels")

        val notAlways = channels.filter { it.title.contains("[Not 24/7]") }
        assertTrue(notAlways.size >= 5, "Should have channels marked as [Not 24/7]")

        val withResolution = channels.filter {
            it.title.contains("1080p") || it.title.contains("720p") ||
                it.title.contains("576p") || it.title.contains("480p")
        }
        assertTrue(withResolution.size >= 25, "Most channels should specify resolution")
    }

    @Test
    fun `animation playlist - verify BBC and British channels`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val bbcFour = channels.find { it.tvgId == "BBCFourCBeebies.uk@HD" }
        assertNotNull(bbcFour, "BBC Four/CBeebies should exist")
        assertEquals("BBC Four/CBeebies (1080p)", bbcFour.title)
        assertEquals(listOf("Animation", "General", "Kids"), bbcFour.groupTitles)

        val bbcThree = channels.find { it.tvgId == "BBCThreeCBBC.uk@HD" }
        assertNotNull(bbcThree, "BBC Three/CBBC should exist")
        assertEquals("BBC Three/CBBC (1080p)", bbcThree.title)
        assertEquals(listOf("Animation", "Family", "Kids"), bbcThree.groupTitles)
    }

    @Test
    fun `animation playlist - verify South Park channel`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val southPark = channels.find { it.tvgId == "SouthPark.us@Canada" }
        assertNotNull(southPark, "South Park should exist")
        assertEquals("South Park", southPark.title)
        assertEquals(listOf("Animation"), southPark.groupTitles)
        assertTrue(southPark.url.contains("pluto.tv"))
    }

    @Test
    fun `animation playlist - verify Nickelodeon channel`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val nickelodeon = channels.find { it.tvgId == "Nickelodeon.ee@SD" }
        assertNotNull(nickelodeon, "Nickelodeon should exist")
        assertEquals("Nickelodeon (576p)", nickelodeon.title)
        assertEquals(listOf("Animation", "Kids"), nickelodeon.groupTitles)
    }

    @Test
    fun `animation playlist - verify international channels with Unicode`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        // Russian channels
        val multimania = channels.find { it.tvgId == "Multimania.ru@SD" }
        assertNotNull(multimania, "Мультимания should exist")
        assertEquals("Мультимания (576p)", multimania.title)

        val solnce = channels.find { it.tvgId == "Solnce.ru@SD" }
        assertNotNull(solnce, "Солнце should exist")
        assertEquals("Солнце", solnce.title)

        // Chinese channel
        val youMan = channels.find { it.tvgId == "YouManCartoonChannel.cn@SD" }
        assertNotNull(youMan, "优漫卡通 should exist")
        assertEquals("优漫卡通 (576p)", youMan.title)
    }

    @Test
    fun `animation playlist - verify classic and retro animation channels`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val retroCrush = channels.find { it.tvgId == "RetroCrush.us@SD" }
        assertNotNull(retroCrush, "RetroCrush should exist")
        assertEquals("RetroCrush (1080p)", retroCrush.title)
        assertEquals(listOf("Animation", "Classic"), retroCrush.groupTitles)

        val cartoonClassics = channels.find { it.tvgId == "CartoonClassics.pl@FAST" }
        assertNotNull(cartoonClassics, "Cartoon Classics should exist")
        assertEquals("Cartoon Classics", cartoonClassics.title)
        assertEquals(listOf("Animation", "Kids"), cartoonClassics.groupTitles)
    }

    @Test
    fun `animation playlist - verify channels from different countries`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val countries = channels.mapNotNull { channel ->
            channel.tvgId?.substringAfterLast(".")?.substringBefore("@")
        }.distinct()

        assertTrue(countries.size >= 20, "Should have channels from many countries (found: ${countries.size})")
        assertTrue(countries.contains("us"), "Should have US channels")
        assertTrue(countries.contains("cn"), "Should have Chinese channels")
        assertTrue(countries.contains("uk"), "Should have UK channels")
        assertTrue(countries.contains("ru"), "Should have Russian channels")
        assertTrue(countries.contains("ar"), "Should have Argentinian channels")
        assertTrue(countries.contains("fr"), "Should have French channels")
    }

    @Test
    fun `animation playlist - verify all channels have required fields`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        channels.forEach { channel ->
            assertNotNull(channel.title, "Every channel must have a title")
            assertTrue(channel.title.isNotBlank(), "Channel title should not be blank")
            assertNotNull(channel.url, "Every channel must have a URL")
            assertTrue(
                channel.url.startsWith("http://") || channel.url.startsWith("https://"),
                "Channel ${channel.title} should have valid HTTP(S) URL",
            )
            assertNotNull(channel.tvgId, "Every channel must have tvg-id")
            assertTrue(channel.groupTitles.isNotEmpty(), "Every channel must have group-title")
            assertEquals(-1, channel.duration, "Duration should be -1 (unlimited)")
        }

        // Check that most channels have tvg-logo (some may have empty logo)
        val channelsWithLogo = channels.filter { !it.tvgLogo.isNullOrBlank() }
        assertTrue(
            channelsWithLogo.size >= 58,
            "Most channels should have tvg-logo (found ${channelsWithLogo.size})",
        )
    }

    @Test
    fun `animation playlist - verify quality indicators in tvg-id`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val sdChannels = channels.filter { it.tvgId?.contains("@SD") == true }
        val hdChannels = channels.filter { it.tvgId?.contains("@HD") == true }
        val otherQualities = channels.filter {
            it.tvgId?.contains("@") == true &&
                !it.tvgId.contains("@SD") &&
                !it.tvgId.contains("@HD")
        }

        assertTrue(sdChannels.size >= 30, "Should have many SD channels")
        assertTrue(hdChannels.size >= 2, "Should have HD channels")
        assertTrue(otherQualities.isNotEmpty(), "Should have channels with other quality indicators")
    }

    @Test
    fun `animation playlist - compare with science and weather playlists`() {
        val (_, animationChannels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)
        val (_, scienceChannels) = M3uParser.parse(REAL_SCIENCE_PLAYLIST)
        val (_, weatherChannels) = M3uParser.parse(REAL_WEATHER_PLAYLIST)

        assertEquals(59, animationChannels.size, "Animation should have 59 channels")
        assertEquals(24, scienceChannels.size, "Science should have 24 channels")
        assertEquals(16, weatherChannels.size, "Weather should have 16 channels")

        assertTrue(animationChannels.all { it.title.isNotBlank() })
        assertTrue(animationChannels.all { it.url.isNotBlank() })

        val animationWithVlcOpts = animationChannels.filter { it.vlcOpts.isNotEmpty() }
        val scienceWithVlcOpts = scienceChannels.filter { it.vlcOpts.isNotEmpty() }

        assertTrue(animationWithVlcOpts.size >= 3, "Animation should have channels with VLC options")
        assertTrue(scienceWithVlcOpts.size >= 3, "Science should have channels with VLC options")
    }

    @Test
    fun `animation playlist - verify Pluto TV channels with long URLs`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val plutoChannels = channels.filter { it.url.contains("pluto.tv") }
        assertTrue(plutoChannels.size >= 10, "Should have many Pluto TV channels")

        plutoChannels.forEach { channel ->
            assertTrue(channel.url.contains("appName=web"))
            assertTrue(channel.url.contains("deviceType=web"))
            assertTrue(channel.url.contains("serverSideAds=false"))
            assertTrue(channel.url.length > 200, "Pluto TV URLs should be long with many parameters")
        }
    }

    @Test
    fun `animation playlist - verify DreamWorks and studio channels`() {
        val (_, channels) = M3uParser.parse(REAL_ANIMATION_PLAYLIST)

        val dreamWorks = channels.find { it.tvgId == "DreamWorksChannelAsia.us@Vietnam" }
        assertNotNull(dreamWorks, "DreamWorks Channel Asia should exist")
        assertEquals("DreamWorks Channel Asia Vietnam (1080p)", dreamWorks.title)
        assertEquals(listOf("Animation"), dreamWorks.groupTitles)
    }
}
