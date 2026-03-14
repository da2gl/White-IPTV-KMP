package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.ThemePreferences
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import com.simplevideo.whiteiptv.domain.repository.ThemeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeRepositoryImpl(
    private val themePreferences: ThemePreferences,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ThemeRepository {
    private val _themeMode = MutableStateFlow<ThemeMode>(ThemeMode.System)
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        scope.launch {
            themePreferences.themeModeFlow.collect { mode ->
                _themeMode.value = mode
            }
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
        _themeMode.value = mode
    }
}
