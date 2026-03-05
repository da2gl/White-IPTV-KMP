package com.simplevideo.whiteiptv.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AccentColorTest {

    @Test
    fun `entries contains exactly 3 values`() {
        assertEquals(3, AccentColor.entries.size)
    }

    @Test
    fun `entries are Teal Blue Red in order`() {
        assertEquals(
            listOf(AccentColor.Teal, AccentColor.Blue, AccentColor.Red),
            AccentColor.entries.toList(),
        )
    }

    @Test
    fun `valueOf returns correct enum for valid names`() {
        assertEquals(AccentColor.Teal, AccentColor.valueOf("Teal"))
        assertEquals(AccentColor.Blue, AccentColor.valueOf("Blue"))
        assertEquals(AccentColor.Red, AccentColor.valueOf("Red"))
    }

    @Test
    fun `all values are distinct`() {
        val values = AccentColor.entries.toSet()
        assertEquals(3, values.size)
    }

    @Test
    fun `name returns expected string`() {
        assertEquals("Teal", AccentColor.Teal.name)
        assertEquals("Blue", AccentColor.Blue.name)
        assertEquals("Red", AccentColor.Red.name)
    }
}
