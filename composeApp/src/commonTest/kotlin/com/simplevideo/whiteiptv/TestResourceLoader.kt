package com.simplevideo.whiteiptv

import kotlin.test.fail

/**
 * Utility for loading test resources in Kotlin Multiplatform tests
 *
 * Usage:
 * ```kotlin
 * val content = loadTestResource("playlists/science.m3u")
 * ```
 */
object TestResourceLoader {

    /**
     * Load test resource as string
     *
     * @param path Resource path relative to commonTest/resources/
     * @return Resource content as string
     * @throws AssertionError if resource not found
     */
    fun loadResource(path: String): String {
        val resource = this::class.java.classLoader?.getResourceAsStream(path)
            ?: fail("Test resource not found: $path")

        return resource.bufferedReader().use { it.readText() }
    }
}

/**
 * Extension function for convenient resource loading
 */
fun loadTestResource(path: String): String = TestResourceLoader.loadResource(path)
