package com.simplevideo.whiteiptv.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelFtsEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.data.local.model.EpgProgramEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity

@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        ChannelFtsEntity::class,
        ChannelGroupEntity::class,
        ChannelGroupCrossRef::class,
        WatchHistoryEntity::class,
        EpgProgramEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun epgDao(): EpgDao
}

@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
