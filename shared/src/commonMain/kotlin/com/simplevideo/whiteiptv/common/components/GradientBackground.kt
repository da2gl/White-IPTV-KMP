package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.simplevideo.whiteiptv.designsystem.BackgroundDark
import com.simplevideo.whiteiptv.designsystem.BackgroundDarkGradientEnd
import com.simplevideo.whiteiptv.designsystem.BackgroundLight
import com.simplevideo.whiteiptv.designsystem.BackgroundLightGradientEnd

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(BackgroundDark, BackgroundDarkGradientEnd)
                    } else {
                        listOf(BackgroundLight, BackgroundLightGradientEnd)
                    },
                ),
            ),
    ) {
        content()
    }
}
