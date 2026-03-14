package com.simplevideo.whiteiptv.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.simplevideo.whiteiptv.domain.model.AccentColor

/**
 * WhiteIPTV App Theme
 *
 * Provides Material 3 theming with light/dark mode and accent color support.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting
 * @param accentColor The accent color to apply to primary/secondary/tertiary roles
 * @param content The composable content to be themed
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: AccentColor = AccentColor.Teal,
    content: @Composable () -> Unit,
) {
    val colorScheme = accentColorScheme(accentColor, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography(),
        content = content,
    )
}
