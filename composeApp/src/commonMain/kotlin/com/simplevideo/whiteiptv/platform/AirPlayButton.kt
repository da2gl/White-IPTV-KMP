package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific AirPlay/casting route picker button.
 *
 * iOS: Renders AVRoutePickerView for AirPlay device selection.
 * Android: Empty composable (Chromecast handled separately).
 */
@Composable
expect fun AirPlayButton(modifier: Modifier = Modifier)
