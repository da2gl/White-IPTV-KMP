package com.simplevideo.whiteiptv.data.cache

/**
 * Manages image cache operations.
 * Provides cache size calculation and clearing functionality.
 */
interface CacheManager {
    /** Returns total cache size in bytes (disk + memory). */
    fun getCacheSizeBytes(): Long

    /** Returns formatted cache size string (e.g., "12.3 MB", "0 B"). */
    fun getFormattedCacheSize(): String

    /** Clears both disk and memory image caches. */
    fun clearCache()
}
