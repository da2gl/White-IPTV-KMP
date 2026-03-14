package com.simplevideo.whiteiptv

import kotlin.test.fail

actual fun loadTestResource(path: String): String {
    val stream = TestResourceLoader::class.java.classLoader?.getResourceAsStream(path)
        ?: fail("Test resource not found: $path")
    return stream.bufferedReader().use { it.readText() }
}

private object TestResourceLoader
