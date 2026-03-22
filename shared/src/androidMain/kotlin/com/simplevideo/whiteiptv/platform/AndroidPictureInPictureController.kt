package com.simplevideo.whiteiptv.platform

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational

/**
 * Android implementation of PiP using Activity.enterPictureInPictureMode().
 * Requires API 26+ and `android:supportsPictureInPicture="true"` in manifest.
 * Accepts [Context] and traverses the ContextWrapper chain to find the host Activity.
 */
class AndroidPictureInPictureController(
    context: Context,
) : PictureInPictureController {

    private val activity: Activity? = context.findActivity()

    override fun isPipSupported(): Boolean =
        activity != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    override fun enterPipMode() {
        if (!isPipSupported()) return
        val act = activity ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            act.enterPictureInPictureMode(params)
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
