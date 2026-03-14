package com.simplevideo.whiteiptv.data.cache

class FakeCacheManager(
    initialCacheSizeBytes: Long = 0L,
) : CacheManager {
    var fakeCacheSizeBytes: Long = initialCacheSizeBytes
    var clearCalled = false
        private set

    override fun getCacheSizeBytes(): Long = fakeCacheSizeBytes

    override fun getFormattedCacheSize(): String = CoilCacheManager.formatBytes(fakeCacheSizeBytes)

    override fun clearCache() {
        clearCalled = true
        fakeCacheSizeBytes = 0L
    }
}
