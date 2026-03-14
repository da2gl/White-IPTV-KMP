package com.simplevideo.whiteiptv.platform

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of SystemControls
 * Uses AudioManager for volume and Window attributes for brightness
 */
class AndroidSystemControls(
    private val activity: Activity,
) : SystemControls {

    private val audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    // Store original brightness to restore on exit
    private val originalBrightness: Float = activity.window.attributes.screenBrightness

    override fun getVolume(): Float {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return currentVolume.toFloat() / maxVolume
    }

    override fun setVolume(level: Float) {
        val volumeIndex = (level.coerceIn(0f, 1f) * maxVolume).toInt()
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volumeIndex,
            0, // No UI flag — we show our own indicator
        )
    }

    override fun getBrightness(): Float {
        val layoutParams = activity.window.attributes
        return if (layoutParams.screenBrightness < 0) {
            // System default brightness, get from settings
            try {
                android.provider.Settings.System.getInt(
                    activity.contentResolver,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                ) / 255f
            } catch (e: Exception) {
                0.5f // Default fallback
            }
        } else {
            layoutParams.screenBrightness
        }
    }

    override fun setBrightness(level: Float) {
        val window = activity.window
        val layoutParams = window.attributes
        layoutParams.screenBrightness = level.coerceIn(0.01f, 1f)
        window.attributes = layoutParams

        // Force window to apply brightness by adding and clearing flag
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Log.d("SystemControls", "setBrightness: $level -> ${window.attributes.screenBrightness}")
    }

    override fun restoreBrightness() {
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = originalBrightness
        activity.window.attributes = layoutParams
    }
}

@Composable
actual fun rememberSystemControls(): SystemControls {
    val context = LocalContext.current
    return remember {
        val activity = context as? Activity
        checkNotNull(activity) { "SystemControls requires Activity context" }
        AndroidSystemControls(activity)
    }
}
