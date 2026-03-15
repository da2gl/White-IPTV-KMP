package com.simplevideo.whiteiptv.platform

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVKit.AVRoutePickerView
import platform.UIKit.UIColor

/**
 * iOS: AirPlay route picker using AVRoutePickerView.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun StreamingButton(modifier: Modifier) {
    UIKitView(
        factory = {
            AVRoutePickerView().apply {
                prioritizesVideoDevices = true
                setTintColor(UIColor.whiteColor)
                setActiveTintColor(UIColor.whiteColor)
                backgroundColor = UIColor.clearColor
            }
        },
        modifier = modifier.size(48.dp),
    )
}
