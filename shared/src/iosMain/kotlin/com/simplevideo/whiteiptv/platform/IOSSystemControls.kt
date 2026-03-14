package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIScreen

/**
 * iOS implementation of SystemControls
 * Uses UIScreen for brightness
 * Note: Volume control on iOS requires MPVolumeView which is more complex
 */
class IOSSystemControls : SystemControls {

    // Store original brightness to restore on exit
    private val originalBrightness: Float = UIScreen.mainScreen.brightness.toFloat()

    override fun getVolume(): Float {
        // iOS volume control is complex — requires MPVolumeView
        // For now, return a default value
        // TODO: Implement with AVAudioSession.sharedInstance().outputVolume
        return 0.5f
    }

    override fun setVolume(level: Float) {
        // iOS doesn't allow programmatic volume control without MPVolumeView
        // This is an Apple restriction for user privacy
        // TODO: Consider showing MPVolumeView or system volume HUD
    }

    override fun getBrightness(): Float {
        return UIScreen.mainScreen.brightness.toFloat()
    }

    override fun setBrightness(level: Float) {
        UIScreen.mainScreen.brightness = level.coerceIn(0.0f, 1.0f).toDouble()
    }

    override fun restoreBrightness() {
        UIScreen.mainScreen.brightness = originalBrightness.toDouble()
    }
}

@Composable
actual fun rememberSystemControls(): SystemControls {
    return remember { IOSSystemControls() }
}
