package com.simplevideo.whiteiptv.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.simplevideo.whiteiptv.data.cache.CacheManager
import com.simplevideo.whiteiptv.data.cache.CoilCacheManager
import com.simplevideo.whiteiptv.data.local.AppDatabase
import com.simplevideo.whiteiptv.data.local.DATA_STORE_FILE_NAME
import com.simplevideo.whiteiptv.data.local.createDataStore
import com.simplevideo.whiteiptv.data.local.getRoomDatabase
import com.simplevideo.whiteiptv.platform.AndroidBackgroundScheduler
import com.simplevideo.whiteiptv.platform.AndroidFilePickerFactory
import com.simplevideo.whiteiptv.platform.AndroidFileReader
import com.simplevideo.whiteiptv.platform.BackgroundScheduler
import com.simplevideo.whiteiptv.platform.FilePickerFactory
import com.simplevideo.whiteiptv.platform.FileReader
import com.simplevideo.whiteiptv.platform.VideoPlayerFactory
import com.simplevideo.whiteiptv.platform.exoplayer.ExoPlayerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<AppDatabase> {
        val appContext = get<Context>().applicationContext
        val dbFile = appContext.getDatabasePath("app.db")

        getRoomDatabase(
            Room.databaseBuilder<AppDatabase>(
                context = appContext,
                name = dbFile.absolutePath,
            ),
        )
    }

    single<FileReader> {
        AndroidFileReader(get())
    }

    single<VideoPlayerFactory> {
        ExoPlayerFactory(get())
    }

    single<FilePickerFactory> {
        AndroidFilePickerFactory(get())
    }

    single<BackgroundScheduler> {
        AndroidBackgroundScheduler(get())
    }

    single<DataStore<Preferences>> {
        createDataStore(
            producePath = {
                get<Context>().filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
            },
        )
    }

    single<CacheManager> { CoilCacheManager(androidContext()) }
}
