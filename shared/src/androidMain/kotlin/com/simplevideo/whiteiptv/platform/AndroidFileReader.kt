package com.simplevideo.whiteiptv.platform

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Android implementation of FileReader
 * Reads files from content:// URIs using ContentResolver
 */
class AndroidFileReader(
    private val context: Context,
) : FileReader {

    override suspend fun readFile(uri: String): String = withContext(Dispatchers.IO) {
        val contentUri = uri.toUri()
        val contentResolver = context.contentResolver

        val inputStream = contentResolver.openInputStream(contentUri)
        checkNotNull(inputStream) { "Failed to open file: $uri" }
        inputStream.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }
    }
}
