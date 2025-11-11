package com.simplevideo.whiteiptv.platform

/**
 * Platform-specific file reader interface
 * Android: reads from content:// URI using ContentResolver
 * iOS: reads from file:// path
 */
interface FileReader {
    /**
     * Read file content as string
     * @param uri platform-specific file URI/path
     * @return file content as UTF-8 string
     * @throws Exception if file cannot be read
     */
    suspend fun readFile(uri: String): String
}
