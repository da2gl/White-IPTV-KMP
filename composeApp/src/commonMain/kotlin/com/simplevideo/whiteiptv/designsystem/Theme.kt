package com.simplevideo.whiteiptv.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * WhiteIPTV App Theme
 *
 * Provides Material 3 theming with light and dark mode support
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting
 * @param content The composable content to be themed
 *
 * TODO: Add dynamic color support for Android 12+ if needed
 * TODO: Consider adding custom theme preferences from user settings
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> AppDarkColorScheme
        else -> AppLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
