package com.simplevideo.whiteiptv.data.parser.epg

import com.simplevideo.whiteiptv.data.parser.epg.XmltvParser.Companion.parseXmltvTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class XmltvParserTest {

    private val parser = XmltvParser()

    // --- parse(): multiple programmes ---

    @Test
    fun `parse valid XMLTV with multiple programmes`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tv>
              <programme start="20260101120000 +0000" stop="20260101130000 +0000" channel="ch1">
                <title>Program One</title>
              </programme>
              <programme start="20260101130000 +0000" stop="20260101140000 +0000" channel="ch1">
                <title>Program Two</title>
              </programme>
              <programme start="20260101120000 +0000" stop="20260101130000 +0000" channel="ch2">
                <title>Program Three</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertEquals(3, result.size)
        assertEquals("Program One", result[0].title)
        assertEquals("ch1", result[0].channelTvgId)
        assertEquals("Program Two", result[1].title)
        assertEquals("ch1", result[1].channelTvgId)
        assertEquals("Program Three", result[2].title)
        assertEquals("ch2", result[2].channelTvgId)
    }

    // --- parse(): all fields ---

    @Test
    fun `parse programme with all fields`() {
        val xml = """
            <tv>
              <programme start="20260315180000 +0000" stop="20260315190000 +0000" channel="bbc.one">
                <title lang="en">BBC News</title>
                <desc lang="en">Latest news from around the world</desc>
                <category lang="en">News</category>
                <icon src="https://example.com/bbc-news.png"/>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertEquals(1, result.size)
        val program = result[0]
        assertEquals("bbc.one", program.channelTvgId)
        assertEquals("BBC News", program.title)
        assertEquals("Latest news from around the world", program.description)
        assertEquals("News", program.category)
        assertEquals("https://example.com/bbc-news.png", program.iconUrl)
    }

    // --- parse(): minimal fields ---

    @Test
    fun `parse programme with minimal fields`() {
        val xml = """
            <tv>
              <programme start="20260101000000 +0000" stop="20260101010000 +0000" channel="test.ch">
                <title>Minimal Show</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertEquals(1, result.size)
        val program = result[0]
        assertEquals("test.ch", program.channelTvgId)
        assertEquals("Minimal Show", program.title)
        assertEquals(null, program.description)
        assertEquals(null, program.category)
        assertEquals(null, program.iconUrl)
    }

    // --- parse(): missing required fields ---

    @Test
    fun `skip programme missing channel attribute`() {
        val xml = """
            <tv>
              <programme start="20260101120000 +0000" stop="20260101130000 +0000">
                <title>No Channel</title>
              </programme>
              <programme start="20260101120000 +0000" stop="20260101130000 +0000" channel="valid">
                <title>Valid Program</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertEquals(1, result.size)
        assertEquals("Valid Program", result[0].title)
    }

    @Test
    fun `skip programme missing start attribute`() {
        val xml = """
            <tv>
              <programme stop="20260101130000 +0000" channel="ch1">
                <title>No Start</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `skip programme missing stop attribute`() {
        val xml = """
            <tv>
              <programme start="20260101120000 +0000" channel="ch1">
                <title>No Stop</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `skip programme missing title element`() {
        val xml = """
            <tv>
              <programme start="20260101120000 +0000" stop="20260101130000 +0000" channel="ch1">
                <desc>Description without title</desc>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertTrue(result.isEmpty())
    }

    // --- parse(): malformed timestamps ---

    @Test
    fun `skip programme with malformed start timestamp`() {
        val xml = """
            <tv>
              <programme start="not-a-date" stop="20260101130000 +0000" channel="ch1">
                <title>Bad Start Time</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `skip programme with too-short timestamp`() {
        val xml = """
            <tv>
              <programme start="2026010112" stop="20260101130000 +0000" channel="ch1">
                <title>Short Timestamp</title>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertTrue(result.isEmpty())
    }

    // --- parse(): XML entities ---

    @Test
    fun `decode XML entities in title, description, and category`() {
        val xml = """
            <tv>
              <programme start="20260101120000 +0000" stop="20260101130000 +0000" channel="ch1">
                <title>Tom &amp; Jerry &lt;Live&gt;</title>
                <desc>A &quot;classic&quot; show with &apos;fun&apos;</desc>
                <category>Kids &amp; Family</category>
              </programme>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertEquals(1, result.size)
        assertEquals("Tom & Jerry <Live>", result[0].title)
        assertEquals("A \"classic\" show with 'fun'", result[0].description)
        assertEquals("Kids & Family", result[0].category)
    }

    // --- parse(): tvgShift offset ---

    @Test
    fun `apply tvgShift offset correctly`() {
        val xml = """
            <tv>
              <programme start="20260101120000 +0000" stop="20260101130000 +0000" channel="ch1">
                <title>Shifted Show</title>
              </programme>
            </tv>
        """.trimIndent()

        val resultNoShift = parser.parse(xml, tvgShiftHours = 0)
        val resultWithShift = parser.parse(xml, tvgShiftHours = 3)

        assertEquals(1, resultNoShift.size)
        assertEquals(1, resultWithShift.size)

        val shiftMs = 3 * 3_600_000L
        assertEquals(resultNoShift[0].startTime + shiftMs, resultWithShift[0].startTime)
        assertEquals(resultNoShift[0].endTime + shiftMs, resultWithShift[0].endTime)
    }

    @Test
    fun `apply negative tvgShift offset`() {
        val xml = """
            <tv>
              <programme start="20260101120000 +0000" stop="20260101130000 +0000" channel="ch1">
                <title>Negative Shift</title>
              </programme>
            </tv>
        """.trimIndent()

        val resultNoShift = parser.parse(xml, tvgShiftHours = 0)
        val resultNegative = parser.parse(xml, tvgShiftHours = -2)

        val shiftMs = -2 * 3_600_000L
        assertEquals(resultNoShift[0].startTime + shiftMs, resultNegative[0].startTime)
    }

    // --- parse(): empty input ---

    @Test
    fun `empty input returns empty list`() {
        val result = parser.parse("")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `xml without programme elements returns empty list`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tv generator-info-name="test">
              <channel id="ch1">
                <display-name>Channel 1</display-name>
              </channel>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertTrue(result.isEmpty())
    }

    // --- parseXmltvTime(): timestamps with timezone offset ---

    @Test
    fun `parseXmltvTime with positive timezone offset`() {
        // 2026-01-01 12:00:00 +0300 = 2026-01-01 09:00:00 UTC
        val result = parseXmltvTime("20260101120000 +0300")

        // 2026-01-01 09:00:00 UTC in epoch ms
        val expected = parseXmltvTime("20260101090000 +0000")
        assertEquals(expected, result)
    }

    @Test
    fun `parseXmltvTime with negative timezone offset`() {
        // 2026-01-01 12:00:00 -0500 = 2026-01-01 17:00:00 UTC
        val result = parseXmltvTime("20260101120000 -0500")

        val expected = parseXmltvTime("20260101170000 +0000")
        assertEquals(expected, result)
    }

    // --- parseXmltvTime(): timestamps without timezone ---

    @Test
    fun `parseXmltvTime without timezone assumes UTC`() {
        val withExplicitUtc = parseXmltvTime("20260101120000 +0000")
        val withoutTimezone = parseXmltvTime("20260101120000")

        assertEquals(withExplicitUtc, withoutTimezone)
    }

    // --- parseXmltvTime(): edge cases ---

    @Test
    fun `parseXmltvTime returns 0 for empty string`() {
        assertEquals(0L, parseXmltvTime(""))
    }

    @Test
    fun `parseXmltvTime returns 0 for too-short string`() {
        assertEquals(0L, parseXmltvTime("2026010112"))
    }

    @Test
    fun `parseXmltvTime returns 0 for non-numeric string`() {
        assertEquals(0L, parseXmltvTime("abcdefghijklmn"))
    }

    @Test
    fun `parseXmltvTime parses known epoch value correctly`() {
        // 2026-01-01 00:00:00 UTC = 1767225600000 ms
        val result = parseXmltvTime("20260101000000 +0000")

        assertEquals(1767225600000L, result)
    }

    // --- parse(): case insensitivity ---

    @Test
    fun `parse handles PROGRAMME tag in different cases`() {
        val xml = """
            <tv>
              <PROGRAMME start="20260101120000 +0000" stop="20260101130000 +0000" channel="ch1">
                <title>Uppercase Tag</title>
              </PROGRAMME>
            </tv>
        """.trimIndent()

        val result = parser.parse(xml)

        assertEquals(1, result.size)
        assertEquals("Uppercase Tag", result[0].title)
    }
}
