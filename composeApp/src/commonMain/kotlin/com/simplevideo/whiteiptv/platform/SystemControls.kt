package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable

/**
 * Platform-specific system controls for volume and brightness
 *
 * Android: Uses AudioManager for volume, WindowManager for brightness
 * iOS: Uses AVAudioSession for volume, UIScreen for brightness
 */
interface SystemControls {
    /**
     * Get current volume level (0.0 to 1.0)
     */
    fun getVolume(): Float

    /**
     * Set volume level (0.0 to 1.0)
     */
    fun setVolume(level: Float)

    /**
     * Get current brightness level (0.0 to 1.0)
     */
    fun getBrightness(): Float

    /**
     * Set brightness level (0.0 to 1.0)
     */
    fun setBrightness(level: Float)

    /**
     * Restore brightness to original system value
     * Call when exiting player to avoid leaving screen dim
     */
    fun restoreBrightness()
}

/**
 * Creates platform-specific SystemControls instance
 * Must be called from Composable context on Android (needs Activity reference)
 */
@Composable
expect fun rememberSystemControls(): SystemControls
