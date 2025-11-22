package com.simplevideo.whiteiptv.di

import android.content.Context
import androidx.room.Room
import com.simplevideo.whiteiptv.data.local.AppDatabase
import com.simplevideo.whiteiptv.data.local.getRoomDatabase
import com.simplevideo.whiteiptv.platform.AndroidFileReader
import com.simplevideo.whiteiptv.platform.FileReader
import com.simplevideo.whiteiptv.platform.VideoPlayerFactory
import com.simplevideo.whiteiptv.platform.exoplayer.ExoPlayerFactory
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
}
