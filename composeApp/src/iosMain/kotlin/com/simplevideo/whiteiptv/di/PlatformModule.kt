package com.simplevideo.whiteiptv.di

import androidx.room.Room
import com.simplevideo.whiteiptv.data.local.AppDatabase
import com.simplevideo.whiteiptv.data.local.getRoomDatabase
import com.simplevideo.whiteiptv.platform.FileReader
import com.simplevideo.whiteiptv.platform.IOSFileReader
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun platformModule(): Module = module {
    single<AppDatabase> {
        val dbFilePath = documentDirectory() + "/app.db"

        getRoomDatabase(
            Room.databaseBuilder<AppDatabase>(
                name = dbFilePath,
            ),
        )
    }

    single<FileReader> {
        IOSFileReader()
    }

    // Note: FilePicker is provided via rememberFilePicker() Composable
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}
