package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific wireless streaming button.
 *
 * Android: Chromecast MediaRouteButton.
 * iOS: AirPlay AVRoutePickerView.
 */
@Composable
expect fun StreamingButton(modifier: Modifier = Modifier)
