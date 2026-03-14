package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific Chromecast media route button.
 *
 * Android: Renders MediaRouteButton for Chromecast device selection.
 * iOS: Empty composable (AirPlay handles iOS casting).
 */
@Composable
expect fun CastButton(modifier: Modifier = Modifier)
