package com.simplevideo.whiteiptv.platform

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
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
 * Uses ContextThemeWrapper with AppCompat theme to provide an opaque background,
 * which is required by MediaRouterThemeHelper.calculateContrast().
 */
@Composable
actual fun CastButton(modifier: Modifier) {
    AndroidView(
        factory = { context ->
            try {
                val themedContext = ContextThemeWrapper(
                    context,
                    androidx.appcompat.R.style.Theme_AppCompat,
                )
                MediaRouteButton(themedContext).also { button ->
                    CastButtonFactory.setUpMediaRouteButton(themedContext, button)
                }
            } catch (e: RuntimeException) {
                Log.e("WhiteIPTV:Cast", "Failed to create MediaRouteButton", e)
                View(context)
            }
        },
        modifier = modifier.size(48.dp),
    )
}
