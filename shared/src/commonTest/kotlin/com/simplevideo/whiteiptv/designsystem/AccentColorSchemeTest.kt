package com.simplevideo.whiteiptv.designsystem

import androidx.compose.ui.graphics.Color
import com.simplevideo.whiteiptv.domain.model.AccentColor
import kotlin.test.Test
import kotlin.test.assertEquals

class AccentColorSchemeTest {

    @Test
    fun `teal light scheme has expected primary color`() {
        val scheme = accentColorScheme(AccentColor.Teal, darkTheme = false)
        assertEquals(PrimaryLight, scheme.primary)
    }

    @Test
    fun `teal dark scheme has expected primary color`() {
        val scheme = accentColorScheme(AccentColor.Teal, darkTheme = true)
        assertEquals(Primary, scheme.primary)
    }

    @Test
    fun `blue light scheme has expected primary color`() {
        val scheme = accentColorScheme(AccentColor.Blue, darkTheme = false)
        assertEquals(Color(0xFF1a73e8), scheme.primary)
    }

    @Test
    fun `blue dark scheme has expected primary color`() {
        val scheme = accentColorScheme(AccentColor.Blue, darkTheme = true)
        assertEquals(Color(0xFF8ab4f8), scheme.primary)
    }

    @Test
    fun `red light scheme has expected primary color`() {
        val scheme = accentColorScheme(AccentColor.Red, darkTheme = false)
        assertEquals(Color(0xFFc62828), scheme.primary)
    }

    @Test
    fun `red dark scheme has expected primary color`() {
        val scheme = accentColorScheme(AccentColor.Red, darkTheme = true)
        assertEquals(Color(0xFFef9a9a), scheme.primary)
    }

    @Test
    fun `all light schemes share the same background color`() {
        val teal = accentColorScheme(AccentColor.Teal, darkTheme = false)
        val blue = accentColorScheme(AccentColor.Blue, darkTheme = false)
        val red = accentColorScheme(AccentColor.Red, darkTheme = false)

        assertEquals(teal.background, blue.background)
        assertEquals(teal.background, red.background)
    }

    @Test
    fun `all dark schemes share the same background color`() {
        val teal = accentColorScheme(AccentColor.Teal, darkTheme = true)
        val blue = accentColorScheme(AccentColor.Blue, darkTheme = true)
        val red = accentColorScheme(AccentColor.Red, darkTheme = true)

        assertEquals(teal.background, blue.background)
        assertEquals(teal.background, red.background)
    }

    @Test
    fun `all light schemes share the same error color`() {
        val teal = accentColorScheme(AccentColor.Teal, darkTheme = false)
        val blue = accentColorScheme(AccentColor.Blue, darkTheme = false)
        val red = accentColorScheme(AccentColor.Red, darkTheme = false)

        assertEquals(teal.error, blue.error)
        assertEquals(teal.error, red.error)
    }

    @Test
    fun `all dark schemes share the same error color`() {
        val teal = accentColorScheme(AccentColor.Teal, darkTheme = true)
        val blue = accentColorScheme(AccentColor.Blue, darkTheme = true)
        val red = accentColorScheme(AccentColor.Red, darkTheme = true)

        assertEquals(teal.error, blue.error)
        assertEquals(teal.error, red.error)
    }

    @Test
    fun `all light schemes share the same surface color`() {
        val teal = accentColorScheme(AccentColor.Teal, darkTheme = false)
        val blue = accentColorScheme(AccentColor.Blue, darkTheme = false)
        val red = accentColorScheme(AccentColor.Red, darkTheme = false)

        assertEquals(teal.surface, blue.surface)
        assertEquals(teal.surface, red.surface)
    }

    @Test
    fun `each accent has distinct primary in light mode`() {
        val teal = accentColorScheme(AccentColor.Teal, darkTheme = false)
        val blue = accentColorScheme(AccentColor.Blue, darkTheme = false)
        val red = accentColorScheme(AccentColor.Red, darkTheme = false)

        assert(teal.primary != blue.primary)
        assert(teal.primary != red.primary)
        assert(blue.primary != red.primary)
    }
}
