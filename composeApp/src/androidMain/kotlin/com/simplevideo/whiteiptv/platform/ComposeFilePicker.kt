package com.simplevideo.whiteiptv.platform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Android Compose implementation of FilePicker
 * Uses rememberLauncherForActivityResult with SAF (Storage Access Framework)
 */
@Composable
actual fun rememberFilePicker(): FilePicker {
    val context = LocalContext.current

    var currentCallback by remember { mutableStateOf<((uri: String, fileName: String) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileName = getFileName(context, uri)
                currentCallback?.invoke(uri.toString(), fileName)
                currentCallback = null
            }
        }
    }

    return remember {
        object : FilePicker {
            override fun pickFile(onFileSelected: (uri: String, fileName: String) -> Unit) {
                currentCallback = onFileSelected

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf(
                            "audio/x-mpegurl",      // .m3u
                            "application/x-mpegurl", // .m3u
                            "application/vnd.apple.mpegurl", // .m3u8
                            "text/plain",            // .m3u as text
                        ),
                    )
                }

                launcher.launch(intent)
            }
        }
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String {
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
