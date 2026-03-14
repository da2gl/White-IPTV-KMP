package com.simplevideo.whiteiptv.platform

import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory

/**
 * Android Chromecast media route button using MediaRouteButton.
 * Displays the Cast device picker when tapped.
 * Uses ContextThemeWrapper with opaque theme to avoid ColorUtils crash.
 */
@Composable
actual fun CastButton(modifier: Modifier) {
    AndroidView(
        factory = { context ->
            val themedContext = ContextThemeWrapper(context, android.R.style.Theme_Material_Light)
            MediaRouteButton(themedContext).also { button ->
                CastButtonFactory.setUpMediaRouteButton(themedContext, button)
            }
        },
        modifier = modifier.size(48.dp),
    )
}
