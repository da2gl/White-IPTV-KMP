package com.simplevideo.whiteiptv.data.parser.playlist

import com.simplevideo.whiteiptv.loadTestResource

/**
 * Shared test resources for M3U parser tests
 * All playlists from iptv-org.github.io
 */
object M3uParserTestResources {
    /**
     * Real M3U playlist from iptv-org.github.io/iptv/categories/science.m3u
     * Loaded from: commonTest/resources/playlists/science.m3u
     * Downloaded: 2025-01-12
     * Contains 24 science/education channels from various countries
     */
    val REAL_SCIENCE_PLAYLIST: String by lazy {
        loadTestResource("playlists/science.m3u")
    }

    /**
     * Real M3U playlist from iptv-org.github.io/iptv/categories/weather.m3u
     * Loaded from: commonTest/resources/playlists/weather.m3u
     * Downloaded: 2025-01-12
     * Contains 16 weather channels from various countries
     */
    val REAL_WEATHER_PLAYLIST: String by lazy {
        loadTestResource("playlists/weather.m3u")
    }

    /**
     * Real M3U playlist from iptv-org.github.io/iptv/categories/animation.m3u
     * Loaded from: commonTest/resources/playlists/animation.m3u
     * Downloaded: 2025-01-12
     * Contains 59 animation channels from various countries
     */
    val REAL_ANIMATION_PLAYLIST: String by lazy {
        loadTestResource("playlists/animation.m3u")
    }
}
