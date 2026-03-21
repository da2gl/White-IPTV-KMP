package com.simplevideo.whiteiptv.common

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger

/**
 * Modifier that draws a red border around tracked composables when recomposition tracking is enabled.
 * No-op when [RecompositionConfig.isEnabled] is false.
 */
@Suppress("UnusedParameter")
fun Modifier.trackRecomposition(
    name: String,
    enabled: Boolean = RecompositionConfig.isEnabled,
): Modifier {
    if (!enabled) return this
    return this.drawWithContent {
        drawContent()
        drawRect(
            color = Color.Red,
            size = Size(size.width, size.height),
            style = Stroke(2.dp.toPx()),
        )
    }
}

/**
 * Logs recomposition events for the given composable name when tracking is enabled.
 * Call this inside a @Composable function body.
 */
@androidx.compose.runtime.Composable
fun LogRecomposition(name: String, enabled: Boolean = RecompositionConfig.isEnabled) {
    if (!enabled) return
    SideEffect {
        Logger.d("Recomposition") { "$name recomposed" }
    }
}
