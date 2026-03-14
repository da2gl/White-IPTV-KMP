# Light Theme — Implementation Plan

## Overview

Add System/Light/Dark theme switching to the design system, persisted via key-value preferences.

**Key finding:** Both `AppLightColorScheme` and `AppDarkColorScheme` already exist in `Color.kt`. The light color palette is already defined. The work is wiring up preference storage, a theme model, and making `App.kt` read the preference to pass to `AppTheme`.

## Architecture Decisions

### Decision 1: Preferences library — `multiplatform-settings` (russhwolf)
- **Library:** `com.russhwolf:multiplatform-settings-no-arg:1.3.0`
- **Why not DataStore?** Over-engineered for key-value settings. `multiplatform-settings` is simpler, lighter, and well-established in KMP.
- **`no-arg` module** provides `Settings()` constructor with no platform arguments needed — uses `NSUserDefaults.standardUserDefaults` on iOS, `SharedPreferences` on Android (with Koin providing Context).
- **No coroutines module needed.** Theme preference is read once at startup and when changed — no need for Flow-based observation. A simple `StateFlow` in a repository is sufficient.

### Decision 2: ThemeMode model — sealed interface in `domain/model/`
Follows the project's existing sealed interface pattern (like `PlaylistSource`).

### Decision 3: System mode uses `isSystemInDarkTheme()`
This is a Compose API that works on both Android and iOS. It's already imported in `Theme.kt`.

### Decision 4: ThemePreferences in `data/local/`
A thin wrapper around `Settings` for theme-related preferences. Placed in data layer since it's a persistence concern.

---

## Files to Create

### 1. `commonMain/.../domain/model/ThemeMode.kt`

```kotlin
package com.simplevideo.whiteiptv.domain.model

/**
 * Represents the app theme selection
 */
sealed interface ThemeMode {
    data object System : ThemeMode
    data object Light : ThemeMode
    data object Dark : ThemeMode
}
```

### 2. `commonMain/.../data/local/ThemePreferences.kt`

```kotlin
package com.simplevideo.whiteiptv.data.local

import com.russhwolf.settings.Settings
import com.simplevideo.whiteiptv.domain.model.ThemeMode

/**
 * Manages theme preference persistence using multiplatform-settings.
 */
class ThemePreferences(private val settings: Settings) {

    fun getThemeMode(): ThemeMode {
        return when (settings.getString(KEY_THEME_MODE, DEFAULT_THEME)) {
            VALUE_LIGHT -> ThemeMode.Light
            VALUE_DARK -> ThemeMode.Dark
            else -> ThemeMode.System
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        val value = when (mode) {
            ThemeMode.System -> VALUE_SYSTEM
            ThemeMode.Light -> VALUE_LIGHT
            ThemeMode.Dark -> VALUE_DARK
        }
        settings.putString(KEY_THEME_MODE, value)
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val VALUE_SYSTEM = "system"
        private const val VALUE_LIGHT = "light"
        private const val VALUE_DARK = "dark"
        private const val DEFAULT_THEME = VALUE_SYSTEM
    }
}
```

### 3. `commonMain/.../domain/repository/ThemeRepository.kt`

```kotlin
package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

/**
 * Provides reactive access to the current theme preference.
 */
interface ThemeRepository {
    val themeMode: StateFlow<ThemeMode>
    fun setThemeMode(mode: ThemeMode)
}
```

### 4. `commonMain/.../data/repository/ThemeRepositoryImpl.kt`

```kotlin
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
```

---

## Files to Modify

### 5. `gradle/libs.versions.toml` — Add multiplatform-settings dependency

**Add to `[versions]`:**
```toml
multiplatformSettings = "1.3.0"
```

**Add to `[libraries]`:**
```toml
multiplatform-settings-no-arg = { module = "com.russhwolf:multiplatform-settings-no-arg", version.ref = "multiplatformSettings" }
```

### 6. `composeApp/build.gradle.kts` — Add dependency

**Add to `commonMain.dependencies`:**
```kotlin
implementation(libs.multiplatform.settings.no.arg)
```

### 7. `commonMain/.../di/KoinModule.kt` — Register theme dependencies

**Add new module** (after `databaseModule`):
```kotlin
val settingsModule = module {
    single { Settings() }
    singleOf(::ThemePreferences)
    singleOf(::ThemeRepositoryImpl) bind ThemeRepository::class
}
```

**Update `appModules`** to include `settingsModule`:
```kotlin
val appModules: List<Module> = listOf(
    platformModule(),
    viewModelModule,
    repositoryModule,
    mapperModule,
    useCaseModule,
    networkModule,
    databaseModule,
    settingsModule,
)
```

**Required imports to add:**
```kotlin
import com.russhwolf.settings.Settings
import com.simplevideo.whiteiptv.data.local.ThemePreferences
import com.simplevideo.whiteiptv.data.repository.ThemeRepositoryImpl
import com.simplevideo.whiteiptv.domain.repository.ThemeRepository
```

> **Merge conflict risk:** Other Wave 1 agents (Search Enhancement, Playlist Management, Continue Watching) will likely also modify `KoinModule.kt` to add their own modules to `appModules`. The changes are additive (new modules + new list entries), so conflicts are mechanical — just include all modules in the final list. To minimize conflict: add `settingsModule` as the LAST entry in `appModules`.

### 8. `commonMain/.../App.kt` — Wire theme preference to AppTheme

```kotlin
package com.simplevideo.whiteiptv

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.simplevideo.whiteiptv.designsystem.AppTheme
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import com.simplevideo.whiteiptv.domain.repository.ThemeRepository
import com.simplevideo.whiteiptv.navigation.AppNavGraph
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val themeRepository: ThemeRepository = koinInject()
    val themeMode by themeRepository.themeMode.collectAsState()

    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    AppTheme(darkTheme = darkTheme) {
        AppNavGraph()
    }
}
```

### 9. `commonMain/.../designsystem/Theme.kt` — Remove TODO comments

Remove the two TODO comments since theme preference support is now being implemented. No functional changes.

```kotlin
/**
 * WhiteIPTV App Theme
 *
 * Provides Material 3 theming with light and dark mode support.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting
 * @param content The composable content to be themed
 */
```

---

## Files NOT Modified

- **`Color.kt`** — Light palette already exists (`AppLightColorScheme`). No changes needed.
- **`Typography.kt`** — Typography is theme-independent. No changes needed.
- **`SettingsScreen.kt`** — Out of scope. The Settings UI for switching themes will be implemented when the Settings screen feature is built. Theme preference can be changed programmatically via `ThemeRepository`.
- **Platform modules** (`androidMain/di/PlatformModule.kt`, `iosMain/di/PlatformModule.kt`) — `multiplatform-settings-no-arg` module provides `Settings()` constructor that needs no platform arguments (uses Android SharedPreferences with default name on Android, NSUserDefaults on iOS). No platform-specific setup needed.

---

## Implementation Order

1. **Add dependency** — `libs.versions.toml` + `build.gradle.kts`
2. **Create ThemeMode** — `domain/model/ThemeMode.kt`
3. **Create ThemePreferences** — `data/local/ThemePreferences.kt`
4. **Create ThemeRepository** — `domain/repository/ThemeRepository.kt` (interface)
5. **Create ThemeRepositoryImpl** — `data/repository/ThemeRepositoryImpl.kt`
6. **Register in Koin** — `di/KoinModule.kt` (add `settingsModule` + update `appModules`)
7. **Wire App.kt** — Read theme from repository, pass to `AppTheme`
8. **Clean up Theme.kt** — Remove TODO comments
9. **Build & verify** — `./gradlew :composeApp:assembleDebug`

---

## Merge Conflict Analysis

| File | Other agents likely touching? | Conflict type | Resolution |
|------|------|------|------|
| `KoinModule.kt` | Yes — all 3 other Wave 1 agents | Additive (new modules + appModules list) | Merge all new modules into list |
| `App.kt` | Unlikely | — | — |
| `libs.versions.toml` | Possible — other agents may add dependencies | Additive (new version + library entries) | Merge all entries |
| `build.gradle.kts` | Possible | Additive (new dependency lines) | Merge all entries |
| New files | No | — | — |

**Highest risk:** `KoinModule.kt:appModules` list — all agents add entries. Resolution is straightforward (include all).

---

## Verification Plan

```bash
# 1. Build Android debug APK
./gradlew :composeApp:assembleDebug

# 2. Run unit tests
./gradlew :composeApp:testDebugUnitTest

# 3. Run code quality checks
./gradlew ktlintCheck
./gradlew detekt
```

---

## Out of Scope (Explicit)

- Settings screen UI for theme switching (separate feature)
- Accent color preference (separate feature)
- Dynamic colors on Android 12+ (future enhancement)
- iOS-specific status bar theming (platform feature)
