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
 * Platform-specific file picker factory
 * Provides DI-injectable way to create FilePicker instances
 * Android: uses ActivityResultLauncher (requires Compose context)
 * iOS: uses UIDocumentPickerViewController
 */
interface FilePickerFactory {
    /**
     * Create a FilePicker instance
     * Must be called from Composable context on Android due to ActivityResultLauncher requirements
     */
    @Composable
    fun createFilePicker(): FilePicker
}
