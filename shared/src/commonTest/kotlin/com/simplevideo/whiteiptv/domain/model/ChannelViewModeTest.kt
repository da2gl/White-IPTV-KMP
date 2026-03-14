package com.simplevideo.whiteiptv.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ChannelViewModeTest {

    @Test
    fun `entries contains exactly 2 values`() {
        assertEquals(2, ChannelViewMode.entries.size)
    }

    @Test
    fun `entries are List Grid in order`() {
        assertEquals(
            listOf(ChannelViewMode.List, ChannelViewMode.Grid),
            ChannelViewMode.entries.toList(),
        )
    }

    @Test
    fun `valueOf returns correct enum for valid names`() {
        assertEquals(ChannelViewMode.List, ChannelViewMode.valueOf("List"))
        assertEquals(ChannelViewMode.Grid, ChannelViewMode.valueOf("Grid"))
    }

    @Test
    fun `name returns expected string`() {
        assertEquals("List", ChannelViewMode.List.name)
        assertEquals("Grid", ChannelViewMode.Grid.name)
    }
}
