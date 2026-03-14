package com.simplevideo.whiteiptv.platform

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of PiP using Activity.enterPictureInPictureMode().
 * Requires API 26+ and `android:supportsPictureInPicture="true"` in manifest.
 */
class AndroidPictureInPictureController(
    private val activity: Activity,
) : PictureInPictureController {

    override fun isPipSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    override fun enterPipMode() {
        if (!isPipSupported()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            activity.enterPictureInPictureMode(params)
        }
    }
}

@Composable
actual fun rememberPipController(): PictureInPictureController {
    val context = LocalContext.current
    return remember {
        val activity = context as? Activity
        if (activity != null) {
            AndroidPictureInPictureController(activity)
        } else {
            object : PictureInPictureController {
                override fun isPipSupported(): Boolean = false
                override fun enterPipMode() = Unit
            }
        }
    }
}
