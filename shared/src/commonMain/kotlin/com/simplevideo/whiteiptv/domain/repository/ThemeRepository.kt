package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

/**
 * Provides reactive access to the current theme preference.
 */
interface ThemeRepository {
    val themeMode: StateFlow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
