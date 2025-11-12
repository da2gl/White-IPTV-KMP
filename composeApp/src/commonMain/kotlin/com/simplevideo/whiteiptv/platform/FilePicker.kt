package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable

/**
 * Platform-specific file picker interface
 * Android: uses ActivityResultLauncher
 * iOS: uses UIDocumentPickerViewController
 */
interface FilePicker {
    /**
     * Show file picker for M3U/M3U8 files
     * @param onFileSelected callback with file URI and name
     */
    fun pickFile(onFileSelected: (uri: String, fileName: String) -> Unit)
}

/**
 * Composable function to remember platform-specific FilePicker
 * Android: uses rememberLauncherForActivityResult
 * iOS: uses UIViewControllerRepresentable
 */
@Composable
expect fun rememberFilePicker(): FilePicker
