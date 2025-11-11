package com.simplevideo.whiteiptv.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataWithContentsOfURL
import kotlin.IllegalArgumentException
import kotlin.IllegalStateException
import kotlin.String
import kotlin.toString

/**
 * iOS implementation of FileReader
 * Reads files from file:// paths using Foundation APIs
 */
class IOSFileReader : FileReader {

    override suspend fun readFile(uri: String): String = withContext(Dispatchers.Default) {
        val url = NSURL.URLWithString(uri)
            ?: throw IllegalArgumentException("Invalid file URI: $uri")

        val data: NSData = NSData.dataWithContentsOfURL(url)
            ?: throw IllegalStateException("Failed to read file: $uri")

        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
            ?: throw IllegalStateException("Failed to decode file content: $uri")
    }
}
