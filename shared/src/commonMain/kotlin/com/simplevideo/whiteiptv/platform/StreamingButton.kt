package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject

/**
 * Factory for platform-specific wireless streaming button.
 *
 * Android: Chromecast MediaRouteButton.
 * iOS: AirPlay AVRoutePickerView.
 */
interface StreamingButtonFactory {

    @Composable
    fun StreamingButton(modifier: Modifier)
}

@Composable
fun StreamingButton(modifier: Modifier = Modifier) {
    val factory = koinInject<StreamingButtonFactory>()
    factory.StreamingButton(modifier)
}
