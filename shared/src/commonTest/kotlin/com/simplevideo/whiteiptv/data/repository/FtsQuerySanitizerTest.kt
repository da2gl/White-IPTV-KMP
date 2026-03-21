package com.simplevideo.whiteiptv.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FtsQuerySanitizerTest {

    @Test
    fun `plain text passes through unchanged`() {
        assertEquals("BBC World News", FtsQuerySanitizer.sanitize("BBC World News"))
    }

    @Test
    fun `double quotes are stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("\"test\""))
    }

    @Test
    fun `single embedded double quote is stripped`() {
        assertEquals("testhello", FtsQuerySanitizer.sanitize("test\"hello"))
    }

    @Test
    fun `asterisks are stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("test*"))
    }

    @Test
    fun `parentheses are stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("(test)"))
    }

    @Test
    fun `hyphens are stripped`() {
        assertEquals("breakingnews", FtsQuerySanitizer.sanitize("breaking-news"))
    }

    @Test
    fun `carets are stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("^test"))
    }

    @Test
    fun `tildes are stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("~test"))
    }

    @Test
    fun `colons are stripped`() {
        assertEquals("nametest", FtsQuerySanitizer.sanitize("name:test"))
    }

    @Test
    fun `plus signs are stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("+test"))
    }

    @Test
    fun `FTS keyword AND is stripped`() {
        assertEquals("breaking news", FtsQuerySanitizer.sanitize("breaking AND news"))
    }

    @Test
    fun `FTS keyword OR is stripped`() {
        assertEquals("breaking news", FtsQuerySanitizer.sanitize("breaking OR news"))
    }

    @Test
    fun `FTS keyword NOT is stripped`() {
        assertEquals("breaking news", FtsQuerySanitizer.sanitize("breaking NOT news"))
    }

    @Test
    fun `FTS keyword NEAR is stripped`() {
        assertEquals("breaking news", FtsQuerySanitizer.sanitize("breaking NEAR news"))
    }

    @Test
    fun `FTS keywords are case insensitive`() {
        assertEquals("test query", FtsQuerySanitizer.sanitize("test and query"))
    }

    @Test
    fun `words containing FTS keywords are preserved`() {
        assertEquals("android notification", FtsQuerySanitizer.sanitize("android notification"))
    }

    @Test
    fun `query with only special characters returns null`() {
        assertNull(FtsQuerySanitizer.sanitize("\"*()"))
    }

    @Test
    fun `empty string returns null`() {
        assertNull(FtsQuerySanitizer.sanitize(""))
    }

    @Test
    fun `whitespace only returns null`() {
        assertNull(FtsQuerySanitizer.sanitize("   "))
    }

    @Test
    fun `multiple spaces are collapsed`() {
        assertEquals("BBC News", FtsQuerySanitizer.sanitize("BBC   News"))
    }

    @Test
    fun `leading and trailing whitespace is trimmed`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("  test  "))
    }

    @Test
    fun `complex malicious input is fully sanitized`() {
        assertEquals("test query other", FtsQuerySanitizer.sanitize("\"test\" AND (query* OR ~other^)"))
    }

    @Test
    fun `backslash is stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("\\test"))
    }

    @Test
    fun `curly braces are stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("{test}"))
    }

    @Test
    fun `square brackets are stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("[test]"))
    }

    @Test
    fun `pipe is stripped`() {
        assertEquals("test", FtsQuerySanitizer.sanitize("|test"))
    }
}
