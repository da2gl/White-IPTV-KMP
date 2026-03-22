package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

class IOSKeepScreenOnController : KeepScreenOnController {
    @Composable
    override fun Effect() {
        DisposableEffect(Unit) {
            UIApplication.sharedApplication.idleTimerDisabled = true

            onDispose {
                UIApplication.sharedApplication.idleTimerDisabled = false
            }
        }
    }
}
