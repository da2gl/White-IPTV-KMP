package com.simplevideo.whiteiptv

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import kotlin.test.fail

@OptIn(ExperimentalForeignApi::class)
@Suppress("CAST_NEVER_SUCCEEDS")
actual fun loadTestResource(path: String): String {
    val lastDot = path.lastIndexOf('.')
    val lastSlash = path.lastIndexOf('/')
    val name = if (lastDot > lastSlash) path.substring(lastSlash + 1, lastDot) else path.substring(lastSlash + 1)
    val ext = if (lastDot > lastSlash) path.substring(lastDot + 1) else ""

    val filePath = NSBundle.mainBundle.pathForResource(name, ext)
        ?: fail("Test resource not found in bundle: $path")

    return NSString.stringWithContentsOfFile(filePath, NSUTF8StringEncoding, null) as? String
        ?: fail("Cannot read test resource: $filePath")
}
