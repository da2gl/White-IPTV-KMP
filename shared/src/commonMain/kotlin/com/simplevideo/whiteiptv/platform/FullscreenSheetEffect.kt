package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable

/**
 * Hides system bars inside a ModalBottomSheet dialog window to prevent
 * the sheet from breaking the app's fullscreen immersive mode.
 *
 * Call this inside any ModalBottomSheet content block.
 * On Android it finds the dialog window and hides system bars.
 * On iOS it's a no-op.
 */
@Composable
expect fun FullscreenSheetEffect()
