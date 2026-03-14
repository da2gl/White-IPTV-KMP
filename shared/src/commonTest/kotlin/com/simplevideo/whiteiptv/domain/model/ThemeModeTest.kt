package com.simplevideo.whiteiptv.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class ThemeModeTest {

    @Test
    fun `System is a ThemeMode`() {
        assertIs<ThemeMode>(ThemeMode.System)
    }

    @Test
    fun `Light is a ThemeMode`() {
        assertIs<ThemeMode>(ThemeMode.Light)
    }

    @Test
    fun `Dark is a ThemeMode`() {
        assertIs<ThemeMode>(ThemeMode.Dark)
    }

    @Test
    fun `all three variants are distinct`() {
        val modes: Set<ThemeMode> = setOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark)
        assertEquals(3, modes.size)
    }

    @Test
    fun `data object equality holds`() {
        assertEquals(ThemeMode.System, ThemeMode.System)
        assertEquals(ThemeMode.Light, ThemeMode.Light)
        assertEquals(ThemeMode.Dark, ThemeMode.Dark)
    }

    @Test
    fun `different variants are not equal`() {
        assertNotEquals<ThemeMode>(ThemeMode.System, ThemeMode.Light)
        assertNotEquals<ThemeMode>(ThemeMode.System, ThemeMode.Dark)
        assertNotEquals<ThemeMode>(ThemeMode.Light, ThemeMode.Dark)
    }

    @Test
    fun `exhaustive when covers all variants`() {
        val modes = listOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark)
        val results = modes.map { mode ->
            when (mode) {
                ThemeMode.System -> "system"
                ThemeMode.Light -> "light"
                ThemeMode.Dark -> "dark"
            }
        }
        assertEquals(listOf("system", "light", "dark"), results)
    }
}
