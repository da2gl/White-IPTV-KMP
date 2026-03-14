package com.simplevideo.whiteiptv.platform

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Android implementation of FilePickerFactory
 * Uses ActivityResultLauncher which requires Compose lifecycle context
 */
class AndroidFilePickerFactory(private val context: Context) : FilePickerFactory {

    @Composable
    override fun createFilePicker(): FilePicker {
        val callbackHolder = remember { CallbackHolder() }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let {
                val fileName = getFileName(context, it)
                callbackHolder.callback?.invoke(it.toString(), fileName)
            }
        }

        return remember {
            object : FilePicker {
                override fun pickFile(onFileSelected: (uri: String, fileName: String) -> Unit) {
                    callbackHolder.callback = onFileSelected

                    launcher.launch(
                        arrayOf(
                            "audio/x-mpegurl", // .m3u
                            "application/x-mpegurl", // .m3u
                            "application/vnd.apple.mpegurl", // .m3u8
                            "text/plain", // .m3u as text
                        ),
                    )
                }
            }
        }
    }
}

private class CallbackHolder(var callback: ((uri: String, fileName: String) -> Unit)? = null)

private fun getFileName(context: Context, uri: Uri): String {
    var fileName = "playlist.m3u"

    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
    }

    return fileName
}
