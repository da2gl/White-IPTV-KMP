package com.simplevideo.whiteiptv.data.repository

/**
 * Sanitizes user input for safe use in FTS4 MATCH expressions.
 *
 * FTS4 treats certain characters and keywords as operators. Passing raw user input
 * into a MATCH expression like `'"' || :query || '*'` can produce malformed expressions
 * that crash with SQLiteException. This sanitizer strips all FTS operator characters
 * and keywords, returning a plain text query safe for phrase-prefix matching.
 */
object FtsQuerySanitizer {

    private val FTS_OPERATOR_CHARS = setOf(
        '"', '*', '(', ')', '-', '^', '~', ':', '+',
        '{', '}', '[', ']', '|', '\\', '/', '<', '>',
        '!', '@', '#', '$', '%', '&', '=', '?', ';',
    )
    private val ftsKeywords = Regex("""\b(AND|OR|NOT|NEAR)\b""", RegexOption.IGNORE_CASE)
    private val multipleSpaces = Regex("""\s+""")

    /**
     * Returns sanitized query safe for FTS4 MATCH, or null if the query
     * becomes empty after sanitization (caller should skip the FTS query).
     */
    fun sanitize(query: String): String? {
        val sanitized = query
            .filter { it !in FTS_OPERATOR_CHARS }
            .replace(ftsKeywords, "")
            .replace(multipleSpaces, " ")
            .trim()

        return sanitized.ifEmpty { null }
    }
}
