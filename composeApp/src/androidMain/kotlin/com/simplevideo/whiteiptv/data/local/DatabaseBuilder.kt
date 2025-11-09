package com.simplevideo.whiteiptv.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return object : KoinComponent {}.getDatabaseBuilderInternal()
}

private fun KoinComponent.getDatabaseBuilderInternal(): RoomDatabase.Builder<AppDatabase> {
    val context: Context = get()
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("app.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
