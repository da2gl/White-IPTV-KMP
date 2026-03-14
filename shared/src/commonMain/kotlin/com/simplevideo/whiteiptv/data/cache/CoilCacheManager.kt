package com.simplevideo.whiteiptv.data.cache

import coil3.PlatformContext
import coil3.SingletonImageLoader

/**
 * [CacheManager] implementation that delegates to Coil's singleton ImageLoader
 * for disk and memory cache operations.
 */
class CoilCacheManager(
    private val platformContext: PlatformContext,
) : CacheManager {

    private val imageLoader
        get() = SingletonImageLoader.get(platformContext)

    override fun getCacheSizeBytes(): Long {
        val diskSize = imageLoader.diskCache?.size ?: 0L
        val memorySize = imageLoader.memoryCache?.size?.toLong() ?: 0L
        return diskSize + memorySize
    }

    override fun getFormattedCacheSize(): String = formatBytes(getCacheSizeBytes())

    override fun clearCache() {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }

    companion object {
        private const val BYTES_PER_KB = 1024L
        private const val BYTES_PER_MB = 1024L * 1024L
        private const val BYTES_PER_GB = 1024L * 1024L * 1024L

        fun formatBytes(bytes: Long): String = when {
            bytes <= 0L -> "0 B"
            bytes < BYTES_PER_KB -> "$bytes B"
            bytes < BYTES_PER_MB -> {
                val kb = bytes.toDouble() / BYTES_PER_KB
                "${formatNumber(kb)} KB"
            }
            bytes < BYTES_PER_GB -> {
                val mb = bytes.toDouble() / BYTES_PER_MB
                "${formatNumber(mb)} MB"
            }
            else -> {
                val gb = bytes.toDouble() / BYTES_PER_GB
                "${formatNumber(gb)} GB"
            }
        }

        private fun formatNumber(value: Double): String {
            val rounded = (value * 10).toLong() / 10.0
            return if (rounded == rounded.toLong().toDouble()) {
                rounded.toLong().toString()
            } else {
                rounded.toString()
            }
        }
    }
}
