# Settings Screen — Implementation Plan

## Summary

Replace the placeholder Settings screen with a full MVI implementation containing four sections: Appearance (theme mode, accent color, channel view), App Behavior (language, auto-update toggle), Data & Storage (clear cache, clear favorites, reset to defaults), and About (version, support, privacy). All preferences persist via `multiplatform-settings`. The existing `ThemeRepository`/`ThemePreferences` from Wave 1 is reused for theme mode; a new `SettingsPreferences` class handles all other settings. Three new UseCases handle destructive data operations with confirmation dialogs.

## Decisions Made

### 1. Accent Color — Enum instead of sealed interface
- **Decision**: Use a simple `enum class AccentColor { Teal, Blue, Red }` in `domain/model/`
- **Rationale**: All variants carry the same data shape (just a name). Enum is simpler and provides `entries` for iteration. The spec lists exactly 3 options with no variant-specific data.
- **Alternatives considered**: Sealed interface (CLAUDE.md preference) — overkill here since all subtypes are identical data objects.

### 2. Channel View — Enum
- **Decision**: `enum class ChannelViewMode { List, Grid }` in `domain/model/`
- **Rationale**: Binary toggle with no variant-specific data. Simpler than sealed interface.

### 3. Language — System-only for MVP
- **Decision**: Do NOT implement language switching in this iteration. Show "System" as the only option, disabled. Language switching requires `Locale` manipulation which is platform-specific and complex in KMP.
- **Rationale**: The spec says "default = System language". No actual multi-language string resources exist. Implementing locale override would require expect/actual + Activity recreation on Android. This can be added later when localization resources are prepared.
- **Alternatives considered**: Full locale switching with expect/actual — too complex for MVP, no translated strings exist.

### 4. Auto-Update Toggle — Persistence only
- **Decision**: Store the boolean preference. The actual auto-refresh job will be wired in the Playlist Auto-Refresh feature later.
- **Rationale**: Task description explicitly says "just persistence for now."

### 5. Clear Cache — No-op with placeholder text
- **Decision**: Show "Clear Cache" with "0 MB" text. The actual cache clearing requires platform-specific Coil/Ktor cache access. Wire as a no-op that shows a success snackbar.
- **Rationale**: No shared cache size API exists in KMP. This is a UI placeholder that will be wired when platform cache APIs are implemented.
- **Alternatives considered**: Expect/actual for cache size — too much scope for this feature.

### 6. Clear Favorites — DAO-level bulk update
- **Decision**: Add `clearAllFavorites()` query to `PlaylistDao` that sets `isFavorite = 0` on all channels. Expose via `ChannelRepository` → `ClearFavoritesUseCase`.
- **Rationale**: Simple SQL UPDATE is atomic and fast. No need to delete entities.

### 7. Reset to Defaults — Clear all settings
- **Decision**: `Settings().clear()` from multiplatform-settings, plus reset ThemeRepository to System. Does NOT clear playlists/channels/favorites.
- **Rationale**: "Reset to Defaults" means settings only, not user data. The confirmation dialog makes this clear.

### 8. Contact Support — mailto link
- **Decision**: Open `mailto:support@simplevideo.com` using platform URI handler.
- **Rationale**: Simple and works cross-platform via `androidx.compose.ui.platform.LocalUriHandler`.

### 9. Privacy Policy — Web URL
- **Decision**: Open `https://simplevideo.com/privacy` using `LocalUriHandler`.
- **Rationale**: Standard approach, no expect/actual needed.

### 10. No separate SettingsRepository
- **Decision**: SettingsViewModel reads directly from `SettingsPreferences` and `ThemeRepository`. No separate `SettingsRepository` interface.
- **Rationale**: Settings screen is the only consumer. Adding a repository layer adds indirection without benefit. ThemeRepository already exists and is shared with App.kt.

### 11. SettingsPreferences shares the same Settings instance
- **Decision**: `SettingsPreferences` takes the same `Settings()` singleton from Koin that `ThemePreferences` uses.
- **Rationale**: `multiplatform-settings` uses a single backing store per platform (SharedPreferences on Android, NSUserDefaults on iOS). Different key prefixes avoid collisions.

## Current State

### Existing files (reuse):
- `domain/model/ThemeMode.kt:1-10` — `ThemeMode` sealed interface (System/Light/Dark)
- `data/local/ThemePreferences.kt:1-35` — Theme preference persistence via `Settings`
- `data/repository/ThemeRepositoryImpl.kt:1-21` — Reactive theme state via `StateFlow`
- `domain/repository/ThemeRepository.kt:1-12` — Theme repository interface
- `App.kt:1-29` — Reads `ThemeRepository.themeMode` for theme switching
- `di/KoinModule.kt:94-98` — `settingsModule` with `Settings()`, `ThemePreferences`, `ThemeRepositoryImpl`

### Existing files (modify):
- `feature/settings/SettingsScreen.kt:1-18` — Placeholder, will be replaced entirely
- `feature/main/MainScreen.kt:97` — `SettingsScreen()` call, no changes needed (signature stays the same)
- `di/KoinModule.kt` — Add SettingsViewModel, SettingsPreferences, new UseCases
- `data/local/PlaylistDao.kt` — Add `clearAllFavorites()` query
- `domain/repository/ChannelRepository.kt` — Add `clearAllFavorites()` method
- `data/repository/ChannelRepositoryImpl.kt` — Implement `clearAllFavorites()`

### Existing files (no change needed):
- `designsystem/Theme.kt` — Accent color will NOT modify the Material theme in this iteration (see Decision 1 — the accent color preference is persisted but the actual theme color switching is a separate visual task)
- `navigation/Route.kt` — Settings is already a tab, no route changes needed
- `navigation/NavGraph.kt` — No changes needed

## Changes Required

### New Files

#### 1. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/model/AccentColor.kt`
- Purpose: Accent color preference enum
- Contents:
```kotlin
enum class AccentColor { Teal, Blue, Red }
```

#### 2. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/model/ChannelViewMode.kt`
- Purpose: Channel list/grid view preference enum
- Contents:
```kotlin
enum class ChannelViewMode { List, Grid }
```

#### 3. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferences.kt`
- Purpose: Persistence for all non-theme settings via multiplatform-settings
- Key contents:
```kotlin
class SettingsPreferences(private val settings: Settings) {
    // Accent Color
    fun getAccentColor(): AccentColor
    fun setAccentColor(color: AccentColor)

    // Channel View Mode
    fun getChannelViewMode(): ChannelViewMode
    fun setChannelViewMode(mode: ChannelViewMode)

    // Auto-Update Playlists
    fun getAutoUpdateEnabled(): Boolean
    fun setAutoUpdateEnabled(enabled: Boolean)

    // Reset all settings (clears all keys from Settings)
    fun resetAll()

    companion object {
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_CHANNEL_VIEW_MODE = "channel_view_mode"
        private const val KEY_AUTO_UPDATE = "auto_update_playlists"
    }
}
```

#### 4. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/mvi/SettingsMvi.kt`
- Purpose: MVI State, Event, and Action definitions
- Contents:
```kotlin
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val accentColor: AccentColor = AccentColor.Teal,
    val channelViewMode: ChannelViewMode = ChannelViewMode.List,
    val autoUpdateEnabled: Boolean = false,
    val appVersion: String = "",
    val showClearFavoritesDialog: Boolean = false,
    val showResetDialog: Boolean = false,
)

sealed interface SettingsEvent {
    // Appearance
    data class OnThemeModeChanged(val mode: ThemeMode) : SettingsEvent
    data class OnAccentColorChanged(val color: AccentColor) : SettingsEvent
    data class OnChannelViewModeChanged(val mode: ChannelViewMode) : SettingsEvent
    // App Behavior
    data class OnAutoUpdateChanged(val enabled: Boolean) : SettingsEvent
    // Data & Storage
    data object OnClearCacheClick : SettingsEvent
    data object OnClearFavoritesClick : SettingsEvent
    data object OnClearFavoritesConfirm : SettingsEvent
    data object OnResetClick : SettingsEvent
    data object OnResetConfirm : SettingsEvent
    data object OnDismissDialog : SettingsEvent
    // About
    data object OnContactSupportClick : SettingsEvent
    data object OnPrivacyPolicyClick : SettingsEvent
}

sealed interface SettingsAction {
    data object ShowCacheCleared : SettingsAction
    data object ShowFavoritesCleared : SettingsAction
    data object ShowSettingsReset : SettingsAction
    data class OpenUrl(val url: String) : SettingsAction
    data class OpenEmail(val email: String) : SettingsAction
}
```

#### 5. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModel.kt`
- Purpose: ViewModel managing settings state and events
- Dependencies: `ThemeRepository`, `SettingsPreferences`, `ClearFavoritesUseCase`
- Key behavior:
  - `init`: Load current values from `ThemeRepository.themeMode` and `SettingsPreferences`
  - `obtainEvent`: Handle each event type — persist changes, show dialogs, trigger actions
  - Theme changes go through `ThemeRepository.setThemeMode()` (reactive, App.kt observes)
  - Other settings go through `SettingsPreferences` set methods
  - Clear favorites delegates to `ClearFavoritesUseCase`
  - Reset delegates to `SettingsPreferences.resetAll()` + `ThemeRepository.setThemeMode(System)`

#### 6. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsScreen.kt` (replace)
- Purpose: Full settings UI with sections
- Structure: `Scaffold` with `TopAppBar("Settings")` + `LazyColumn` containing section composables
- Sections: AppearanceSection, AppBehaviorSection, DataStorageSection, AboutSection
- Confirmation dialogs: `AlertDialog` for Clear Favorites and Reset to Defaults
- Actions consumed via `LaunchedEffect` — snackbars for success feedback, `LocalUriHandler` for URLs

#### 7. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsComponents.kt`
- Purpose: Reusable settings UI primitives
- Contents:
  - `SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit)` — Section with header
  - `SettingsItem(title: String, subtitle: String?, onClick: () -> Unit)` — Clickable row
  - `SettingsSwitchItem(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit)` — Toggle row
  - `SettingsSegmentedButton(options: List<T>, selected: T, onSelect: (T) -> Unit, label: (T) -> String)` — Segmented button group for Theme/AccentColor/ViewMode

#### 8. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/ClearFavoritesUseCase.kt`
- Purpose: Clear all favorite flags from channels
- Contents:
```kotlin
class ClearFavoritesUseCase(private val channelRepository: ChannelRepository) {
    suspend operator fun invoke() {
        channelRepository.clearAllFavorites()
    }
}
```

### Modified Files

#### 1. `data/local/PlaylistDao.kt`
- **Add** `clearAllFavorites()` query:
```kotlin
@Query("UPDATE channels SET isFavorite = 0 WHERE isFavorite = 1")
suspend fun clearAllFavorites()
```

#### 2. `domain/repository/ChannelRepository.kt`
- **Add** method:
```kotlin
suspend fun clearAllFavorites()
```

#### 3. `data/repository/ChannelRepositoryImpl.kt`
- **Add** implementation:
```kotlin
override suspend fun clearAllFavorites() {
    playlistDao.clearAllFavorites()
}
```

#### 4. `di/KoinModule.kt`
- **Add** to `viewModelModule`:
```kotlin
viewModelOf(::SettingsViewModel)
```
- **Add** to `useCaseModule`:
```kotlin
factoryOf(::ClearFavoritesUseCase)
```
- **Add** to `settingsModule`:
```kotlin
singleOf(::SettingsPreferences)
```

### Database Changes
- **No migration needed.** The `clearAllFavorites()` query operates on the existing `channels` table's `isFavorite` column. No schema change.

### DI Changes
- `viewModelModule` += `SettingsViewModel`
- `useCaseModule` += `ClearFavoritesUseCase`
- `settingsModule` += `SettingsPreferences`

## Implementation Order

1. **Domain models** — Create `AccentColor.kt` and `ChannelViewMode.kt` in `domain/model/`
2. **DAO update** — Add `clearAllFavorites()` to `PlaylistDao`
3. **Repository update** — Add `clearAllFavorites()` to `ChannelRepository` interface and `ChannelRepositoryImpl`
4. **SettingsPreferences** — Create `data/local/SettingsPreferences.kt`
5. **ClearFavoritesUseCase** — Create `domain/usecase/ClearFavoritesUseCase.kt`
6. **SettingsMvi** — Create `feature/settings/mvi/SettingsMvi.kt`
7. **SettingsViewModel** — Create `feature/settings/SettingsViewModel.kt`
8. **SettingsComponents** — Create `feature/settings/components/SettingsComponents.kt`
9. **SettingsScreen** — Replace `feature/settings/SettingsScreen.kt`
10. **DI wiring** — Update `KoinModule.kt` with new registrations
11. **Build verification** — `./gradlew :composeApp:assembleDebug`

## Testing Strategy

### Unit Tests
- **SettingsPreferencesTest** — Verify get/set for each setting, verify `resetAll()` clears all keys
- **ClearFavoritesUseCaseTest** — Verify it calls `channelRepository.clearAllFavorites()`
- **SettingsViewModelTest** — Verify:
  - Initial state loads current preferences
  - Theme change events update ThemeRepository
  - AccentColor/ChannelViewMode changes persist to SettingsPreferences
  - AutoUpdate toggle persists
  - Clear Favorites shows dialog, confirm clears, dismiss closes
  - Reset shows dialog, confirm resets all settings to defaults
  - Contact/Privacy events emit correct actions

### Edge Cases
- Reset to defaults while theme is Dark → should revert to System
- Clear favorites when no favorites exist → should succeed silently
- Multiple rapid theme switches → ThemeRepository StateFlow deduplicates

### Key Assertions
- After reset: `themeMode == System`, `accentColor == Teal`, `channelViewMode == List`, `autoUpdate == false`
- After clear favorites: all channels have `isFavorite = false`
- Settings persist after ViewModel recreation (simulate process death)

## Doc Updates Required
- `docs/constraints/current-limitations.md` — Remove "Settings screen is a placeholder" section
- `docs/features/settings.md` — Add note about Language being system-only for now, Cache being a placeholder
