package com.simplevideo.whiteiptv.domain.model

/**
 * Represents the app theme selection.
 */
sealed interface ThemeMode {
    data object System : ThemeMode
    data object Light : ThemeMode
    data object Dark : ThemeMode
}
