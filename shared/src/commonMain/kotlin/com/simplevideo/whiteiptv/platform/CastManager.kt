package com.simplevideo.whiteiptv.platform

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-specific cast/streaming manager.
 *
 * Android: Manages Chromecast session via Cast SDK.
 * iOS: No-op stub (AirPlay is handled natively by AVPlayer via AVRoutePickerView).
 */
expect class CastManager {
    val castState: StateFlow<CastConnectionState>
    fun startCasting(url: String, title: String?, logoUrl: String?)
    fun stopCasting()
    fun isAvailable(): Boolean
}

enum class CastConnectionState {
    NOT_CONNECTED,
    CONNECTING,
    CONNECTED,
}
