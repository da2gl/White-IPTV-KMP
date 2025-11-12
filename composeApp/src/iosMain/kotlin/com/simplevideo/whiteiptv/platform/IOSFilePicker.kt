package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeAudio
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.darwin.NSObject

/**
 * iOS Compose implementation of FilePicker
 * Uses UIDocumentPickerViewController with modern UTType API
 */
@Composable
actual fun rememberFilePicker(): FilePicker {
    return remember {
        IOSFilePicker()
    }
}

private class IOSFilePicker : FilePicker {
    // Store delegate as instance variable to prevent GC
    private var currentDelegate: NSObject? = null

    override fun pickFile(onFileSelected: (uri: String, fileName: String) -> Unit) {
        val contentTypes = listOf(
            UTTypeAudio, // Audio files including .m3u
            UTTypePlainText, // Text files
            UTTypeItem, // All files (fallback)
        )

        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = contentTypes,
        )

        picker.allowsMultipleSelection = false

        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL,
            ) {
                val url = didPickDocumentAtURL.absoluteString ?: return
                val fileName = didPickDocumentAtURL.lastPathComponent ?: "playlist.m3u"
                onFileSelected(url, fileName)
                currentDelegate = null // Clear after use
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                currentDelegate = null // Clear on cancel
            }
        }

        currentDelegate = delegate // Strong reference to prevent GC
        picker.delegate = delegate

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}
