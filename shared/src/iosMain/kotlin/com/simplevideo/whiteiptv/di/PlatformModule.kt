package com.simplevideo.whiteiptv.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import coil3.PlatformContext
import com.simplevideo.whiteiptv.data.local.AppDatabase
import com.simplevideo.whiteiptv.data.local.DATA_STORE_FILE_NAME
import com.simplevideo.whiteiptv.data.local.createDataStore
import com.simplevideo.whiteiptv.data.local.getRoomDatabase
import com.simplevideo.whiteiptv.platform.BackgroundScheduler
import com.simplevideo.whiteiptv.platform.CastManager
import com.simplevideo.whiteiptv.platform.FilePickerFactory
import com.simplevideo.whiteiptv.platform.FileReader
import com.simplevideo.whiteiptv.platform.FullscreenSheetController
import com.simplevideo.whiteiptv.platform.IOSBackgroundScheduler
import com.simplevideo.whiteiptv.platform.IOSCastManager
import com.simplevideo.whiteiptv.platform.IOSFilePickerFactory
import com.simplevideo.whiteiptv.platform.IOSFileReader
import com.simplevideo.whiteiptv.platform.IOSFullscreenSheetController
import com.simplevideo.whiteiptv.platform.IOSKeepScreenOnController
import com.simplevideo.whiteiptv.platform.IOSPictureInPictureController
import com.simplevideo.whiteiptv.platform.IOSStreamingButtonFactory
import com.simplevideo.whiteiptv.platform.IOSSystemControlsFactory
import com.simplevideo.whiteiptv.platform.IOSVideoPlayerFactory
import com.simplevideo.whiteiptv.platform.KeepScreenOnController
import com.simplevideo.whiteiptv.platform.PictureInPictureController
import com.simplevideo.whiteiptv.platform.StreamingButtonFactory
import com.simplevideo.whiteiptv.platform.SystemControlsFactory
import com.simplevideo.whiteiptv.platform.VideoPlayerFactory
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

    single<VideoPlayerFactory> {
        IOSVideoPlayerFactory()
    }

    single<FilePickerFactory> {
        IOSFilePickerFactory()
    }

    single<BackgroundScheduler> {
        IOSBackgroundScheduler(get())
    }

    single<DataStore<Preferences>> {
        createDataStore(
            producePath = {
                documentDirectory() + "/$DATA_STORE_FILE_NAME"
            },
        )
    }

    single<com.simplevideo.whiteiptv.data.cache.CacheManager> {
        com.simplevideo.whiteiptv.data.cache.CoilCacheManager(PlatformContext.INSTANCE)
    }

    single<StreamingButtonFactory> {
        IOSStreamingButtonFactory()
    }

    single<FullscreenSheetController> { IOSFullscreenSheetController() }
    single<KeepScreenOnController> { IOSKeepScreenOnController() }
    single<SystemControlsFactory> { IOSSystemControlsFactory() }
    factory<PictureInPictureController> {
        val playerFactory = get<VideoPlayerFactory>() as IOSVideoPlayerFactory
        IOSPictureInPictureController(playerFactory.lastCreatedPlayer)
    }

    single<CastManager> { IOSCastManager() }
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
