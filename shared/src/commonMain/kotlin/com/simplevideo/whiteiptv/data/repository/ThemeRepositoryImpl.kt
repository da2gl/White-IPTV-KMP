package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.ThemePreferences
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import com.simplevideo.whiteiptv.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepositoryImpl(
    private val themePreferences: ThemePreferences,
) : ThemeRepository {

    private val _themeMode = MutableStateFlow(themePreferences.getThemeMode())
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    override fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
        _themeMode.value = mode
    }
}
