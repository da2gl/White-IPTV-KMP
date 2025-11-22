package com.simplevideo.whiteiptv.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

/**
 * iOS implementation of FileReader
 * Reads files from file:// paths using Foundation APIs
 */
class IOSFileReader : FileReader {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun readFile(uri: String): String = withContext(Dispatchers.Default) {
        val url = NSURL.URLWithString(uri)
            ?: throw IllegalArgumentException("Invalid file URI: $uri")

        val data = NSData.dataWithContentsOfURL(url)
        checkNotNull(data) { "Failed to read file: $uri" }

        val byteArray = ByteArray(data.length.toInt())
        byteArray.usePinned { pinnedArray ->
            memcpy(pinnedArray.addressOf(0), data.bytes, data.length)
        }

        byteArray.decodeToString()
    }
}
