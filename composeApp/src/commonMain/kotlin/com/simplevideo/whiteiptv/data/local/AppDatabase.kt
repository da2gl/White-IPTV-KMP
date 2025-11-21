package com.simplevideo.whiteiptv.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity

@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        ChannelGroupEntity::class,
        ChannelGroupCrossRef::class,
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}

@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
