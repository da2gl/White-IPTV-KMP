package com.simplevideo.whiteiptv.data.mapper

import com.simplevideo.whiteiptv.data.local.model.EpgProgramEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EpgProgramMapperTest {

    private val mapper = EpgProgramMapper()

    @Test
    fun `maps all fields correctly from entity to domain model`() {
        val entity = EpgProgramEntity(
            id = 42,
            channelTvgId = "bbc.one",
            title = "BBC News",
            description = "World news coverage",
            startTime = 1000000L,
            endTime = 2000000L,
            category = "News",
            iconUrl = "https://example.com/icon.png",
        )

        val domain = mapper.toDomain(entity)

        assertEquals("BBC News", domain.title)
        assertEquals("World news coverage", domain.description)
        assertEquals(1000000L, domain.startTimeMs)
        assertEquals(2000000L, domain.endTimeMs)
        assertEquals("News", domain.category)
        assertEquals("https://example.com/icon.png", domain.iconUrl)
    }

    @Test
    fun `maps entity with null optional fields`() {
        val entity = EpgProgramEntity(
            channelTvgId = "ch1",
            title = "Minimal Program",
            startTime = 100L,
            endTime = 200L,
        )

        val domain = mapper.toDomain(entity)

        assertEquals("Minimal Program", domain.title)
        assertNull(domain.description)
        assertEquals(100L, domain.startTimeMs)
        assertEquals(200L, domain.endTimeMs)
        assertNull(domain.category)
        assertNull(domain.iconUrl)
    }
}
