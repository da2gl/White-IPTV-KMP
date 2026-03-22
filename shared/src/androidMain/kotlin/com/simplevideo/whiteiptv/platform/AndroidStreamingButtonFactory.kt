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
 * Android: Chromecast MediaRouteButton.
 *
 * Uses [ContextThemeWrapper] with [R.style.CastButtonTheme] to provide an opaque
 * `android:colorBackground` required by `MediaRouterThemeHelper.calculateContrast()`.
 */
class AndroidStreamingButtonFactory : StreamingButtonFactory {

    @Composable
    override fun StreamingButton(modifier: Modifier) {
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
}
