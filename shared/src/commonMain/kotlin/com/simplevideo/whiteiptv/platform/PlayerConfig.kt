package com.simplevideo.whiteiptv.platform

/**
 * Configuration for video player optimized for IPTV live streaming
 *
 * Buffer settings use "drip-feeding" technique for stable playback:
 * - Lower buffers = faster start but more rebuffering risk
 * - Higher buffers = slower start but more stable playback
 */
data class PlayerConfig(
    // Buffer configuration
    val minBufferMs: Int = 10_000,
    val maxBufferMs: Int = 30_000,
    val bufferForPlaybackMs: Int = 1_000,
    val bufferForPlaybackAfterRebufferMs: Int = 2_000,
    val backBufferMs: Int = 10_000,

    // Network configuration
    val connectTimeoutMs: Int = 15_000,
    val readTimeoutMs: Int = 15_000,

    // Live streaming configuration
    val targetLiveOffsetMs: Long = 10_000,
    val minLiveOffsetMs: Long = 5_000,
    val maxLiveOffsetMs: Long = 30_000,
    val minPlaybackSpeed: Float = 0.97f,
    val maxPlaybackSpeed: Float = 1.03f,

    // Error handling
    val minRetryCount: Int = 6,
    val maxRetryDelayMs: Long = 5_000,

    // Network stack
    val useCronet: Boolean = true,
) {
    companion object {
        /** Default configuration for standard IPTV streams */
        val Default = PlayerConfig()

        /** Configuration for low-latency streams (experimental) */
        val LowLatency = PlayerConfig(
            minBufferMs = 3_000,
            maxBufferMs = 3_000,
            bufferForPlaybackMs = 500,
            bufferForPlaybackAfterRebufferMs = 500,
            targetLiveOffsetMs = 3_000,
            minLiveOffsetMs = 1_000,
            maxLiveOffsetMs = 5_000,
        )

        /** Configuration for unstable networks */
        val HighBuffer = PlayerConfig(
            minBufferMs = 30_000,
            maxBufferMs = 60_000,
            bufferForPlaybackMs = 5_000,
            bufferForPlaybackAfterRebufferMs = 10_000,
            connectTimeoutMs = 30_000,
            readTimeoutMs = 30_000,
            minRetryCount = 10,
        )
    }
}
