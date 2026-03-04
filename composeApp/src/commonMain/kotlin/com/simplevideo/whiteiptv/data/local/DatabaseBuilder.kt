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
 * Creates Room database with proper configuration for all platforms
 * Sets bundled SQLite driver and IO coroutine context
 */
fun getRoomDatabase(builder: Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_2_3)
        .build()
}
