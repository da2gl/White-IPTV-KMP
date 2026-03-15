package com.simplevideo.whiteiptv.platform

import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.simplevideo.whiteiptv.shared.R

/**
 * Android Chromecast media route button using MediaRouteButton.
 *
 * Uses [ContextThemeWrapper] with a minimal [R.style.CastButtonTheme] that provides
 * an opaque `android:colorBackground`. This is required because Compose's [AndroidView]
 * context has a transparent background, and [MediaRouteButton] internally calls
 * `ColorUtils.calculateContrast()` which rejects translucent backgrounds.
 */
@Composable
actual fun CastButton(modifier: Modifier) {
    AndroidView(
        factory = { context ->
            val themedContext = ContextThemeWrapper(context, R.style.CastButtonTheme)
            MediaRouteButton(themedContext).also { button ->
                CastButtonFactory.setUpMediaRouteButton(themedContext, button)
            }
        },
        modifier = modifier.size(48.dp),
    )
}
