package com.simplevideo.whiteiptv.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No-op CastManager for iOS. AirPlay is handled natively by AVPlayer via AVRoutePickerView.
 */
actual class CastManager {

    private val _castState = MutableStateFlow(CastConnectionState.NOT_CONNECTED)
    actual val castState: StateFlow<CastConnectionState> = _castState.asStateFlow()

    actual fun startCasting(url: String, title: String?, logoUrl: String?) {
        // No-op: AirPlay is handled by AVPlayer
    }

    actual fun stopCasting() {
        // No-op
    }

    actual fun isAvailable(): Boolean = false
}
