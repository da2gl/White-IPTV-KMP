package com.simplevideo.whiteiptv.data.local

import androidx.room.RoomDatabase.Builder
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Migration from v2 to v3: add watch_history table for Continue Watching feature
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `watch_history` (
                `channelId` INTEGER NOT NULL,
                `playlistId` INTEGER NOT NULL,
                `lastWatchedAt` INTEGER NOT NULL,
                `watchDurationMs` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`channelId`),
                FOREIGN KEY(`channelId`) REFERENCES `channels`(`id`) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_watch_history_lastWatchedAt` ON `watch_history` (`lastWatchedAt`)",
        )
    }
}

/**
 * Migration from v3 to v4: add epg_programs table for Electronic Program Guide
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `epg_programs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `channelTvgId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT,
                `startTime` INTEGER NOT NULL,
                `endTime` INTEGER NOT NULL,
                `category` TEXT,
                `iconUrl` TEXT
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_epg_programs_channelTvgId_startTime` " +
                "ON `epg_programs` (`channelTvgId`, `startTime`)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_epg_programs_channelTvgId_endTime` " +
                "ON `epg_programs` (`channelTvgId`, `endTime`)",
        )
    }
}

/**
 * Migration from v4 to v5: add FTS4 virtual table for full-text search on channel names
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS `channels_fts`
            USING FTS4(`name`, content=`channels`, tokenize=unicode61)
            """.trimIndent(),
        )
        connection.execSQL(
            "INSERT INTO `channels_fts`(rowid, name) SELECT id, name FROM channels",
        )
    }
}

/**
 * Creates Room database with proper configuration for all platforms
 * Sets bundled SQLite driver and IO coroutine context
 */
fun getRoomDatabase(builder: Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .build()
}
