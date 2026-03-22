package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

/**
 * Hides system bars inside a ModalBottomSheet dialog window to prevent
 * the sheet from breaking the app's fullscreen immersive mode.
 *
 * Platform implementations are injected via Koin.
 * On Android it finds the dialog window and hides system bars.
 * On iOS it's a no-op.
 */
interface FullscreenSheetController {
    @Composable
    fun Effect()
}

/**
 * Convenience composable that injects [FullscreenSheetController] via Koin
 * and applies the fullscreen effect. Call this inside any ModalBottomSheet content block.
 */
@Composable
fun FullscreenSheetEffect() {
    val controller = koinInject<FullscreenSheetController>()
    controller.Effect()
}
