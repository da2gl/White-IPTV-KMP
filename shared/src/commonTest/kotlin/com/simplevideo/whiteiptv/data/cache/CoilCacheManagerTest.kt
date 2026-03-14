package com.simplevideo.whiteiptv.data.cache

import kotlin.test.Test
import kotlin.test.assertEquals

class CoilCacheManagerTest {

    @Test
    fun `formatBytes returns 0 B for zero`() {
        assertEquals("0 B", CoilCacheManager.formatBytes(0))
    }

    @Test
    fun `formatBytes returns 0 B for negative`() {
        assertEquals("0 B", CoilCacheManager.formatBytes(-1))
    }

    @Test
    fun `formatBytes returns bytes for small values`() {
        assertEquals("500 B", CoilCacheManager.formatBytes(500))
    }

    @Test
    fun `formatBytes returns bytes for 1023`() {
        assertEquals("1023 B", CoilCacheManager.formatBytes(1023))
    }

    @Test
    fun `formatBytes returns 1 KB for 1024`() {
        assertEquals("1 KB", CoilCacheManager.formatBytes(1024))
    }

    @Test
    fun `formatBytes returns 1_5 KB for 1536`() {
        assertEquals("1.5 KB", CoilCacheManager.formatBytes(1536))
    }

    @Test
    fun `formatBytes returns 1 MB for 1048576`() {
        assertEquals("1 MB", CoilCacheManager.formatBytes(1048576))
    }

    @Test
    fun `formatBytes returns 1_5 MB for 1572864`() {
        assertEquals("1.5 MB", CoilCacheManager.formatBytes(1572864))
    }

    @Test
    fun `formatBytes returns 10 MB for 10485760`() {
        assertEquals("10 MB", CoilCacheManager.formatBytes(10485760))
    }

    @Test
    fun `formatBytes returns 1 GB for 1073741824`() {
        assertEquals("1 GB", CoilCacheManager.formatBytes(1073741824))
    }
}
