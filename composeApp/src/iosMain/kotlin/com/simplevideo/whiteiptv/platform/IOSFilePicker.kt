package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject

/**
 * iOS Compose implementation of FilePicker
 * Uses UIDocumentPickerViewController
 */
@Composable
actual fun rememberFilePicker(): FilePicker {
    return remember {
        IOSFilePicker()
    }
}

private class IOSFilePicker : FilePicker {

    override fun pickFile(onFileSelected: (uri: String, fileName: String) -> Unit) {
        val documentTypes = listOf(
            "public.audio", // Audio files including .m3u
            "public.text", // Text files
            "public.item", // All files
        )

        val picker = UIDocumentPickerViewController(
            documentTypes = documentTypes,
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
        )

        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL,
            ) {
                val url = didPickDocumentAtURL.absoluteString ?: return
                val fileName = didPickDocumentAtURL.lastPathComponent ?: "playlist.m3u"
                onFileSelected(url, fileName)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                // User cancelled - do nothing
            }
        }

        picker.delegate = delegate

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}
