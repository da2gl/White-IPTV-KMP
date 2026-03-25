package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import com.simplevideo.whiteiptv.designsystem.BackgroundDark
import com.simplevideo.whiteiptv.designsystem.BackgroundDarkGradientEnd
import com.simplevideo.whiteiptv.designsystem.BackgroundLight
import com.simplevideo.whiteiptv.designsystem.BackgroundLightGradientEnd

/**
 * Detects whether the current MaterialTheme is dark, respecting the app's theme override
 * (not just the system setting). Uses background color luminance as the indicator.
 */
@Composable
fun isDarkTheme(): Boolean {
    val background = MaterialTheme.colorScheme.background
    return remember(background) { background.luminance() < 0.5f }
}

private val DARK_GRADIENT = Brush.verticalGradient(listOf(BackgroundDark, BackgroundDarkGradientEnd))
private val LIGHT_GRADIENT = Brush.verticalGradient(listOf(BackgroundLight, BackgroundLightGradientEnd))

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val brush = if (isDarkTheme()) DARK_GRADIENT else LIGHT_GRADIENT
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = brush),
    ) {
        content()
    }
}
