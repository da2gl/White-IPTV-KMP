package com.simplevideo.whiteiptv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity

@Database(entities = [PlaylistEntity::class, ChannelEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}
