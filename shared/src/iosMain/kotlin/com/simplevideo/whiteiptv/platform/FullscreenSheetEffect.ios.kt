package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable

class IOSFullscreenSheetController : FullscreenSheetController {
    @Composable
    override fun Effect() {
        // No-op on iOS — system bars are managed by the view controller
    }
}
