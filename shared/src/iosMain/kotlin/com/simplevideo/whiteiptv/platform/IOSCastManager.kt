package com.simplevideo.whiteiptv.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * No-op CastManager for iOS. AirPlay is handled natively by AVPlayer via AVRoutePickerView.
 */
class IOSCastManager : CastManager {

    private val _castState = MutableStateFlow(CastConnectionState.NOT_CONNECTED)
    override val castState: StateFlow<CastConnectionState> = _castState.asStateFlow()

    override fun startCasting(url: String, title: String?, logoUrl: String?) {
        // No-op: AirPlay is handled by AVPlayer
    }

    override fun stopCasting() {
        // No-op
    }

    override fun isAvailable(): Boolean = false
}
