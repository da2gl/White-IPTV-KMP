# Accent Color Visual Application -- Implementation Plan

## Summary

Wire the persisted AccentColor preference (Teal/Blue/Red) through to the Material3 theme so that
changing the accent color in Settings visually updates the primary, secondary, and tertiary color
roles across the entire app. This follows the same reactive pattern already used for ThemeMode
(ThemeRepository -> StateFlow -> App.kt -> AppTheme).

## Decisions Made

### Decision 1: Reuse the existing SettingsPreferences for persistence, add a reactive Flow

- **Decision**: Add an `accentColorFlow` to `SettingsPreferences` (mirroring `themeModeFlow` in
  `ThemePreferences`) rather than creating a separate `AccentColorPreferences` class or a new
  repository.
- **Rationale**: AccentColor is already persisted in `SettingsPreferences`. The only missing piece
  is a reactive Flow. Adding a Flow property keeps all accent color logic in one place. Creating a
  full repository (like ThemeRepository) would be over-engineering for a single enum preference.
- **Alternatives considered**: (1) New `AccentColorRepository` interface + impl -- adds 3 files for
  a simple preference, too heavy. (2) Move accent color into `ThemePreferences` -- would require
  migrating the DataStore key, unnecessary churn.

### Decision 2: Pass AccentColor directly to AppTheme, no intermediate repository

- **Decision**: Read `accentColorFlow` from `SettingsPreferences` in `App.kt` via
  `collectAsState()` and pass the `AccentColor` value to `AppTheme`. The theme composable selects
  the appropriate color scheme based on the combination of `darkTheme` and `accentColor`.
- **Rationale**: This mirrors the existing pattern where `App.kt` collects `themeMode` and passes
  `darkTheme` to `AppTheme`. Keeping it at the composition root ensures the entire app recomposes
  with new colors.
- **Alternatives considered**: Creating an `AccentColorRepository` with a StateFlow that `App.kt`
  observes -- unnecessary indirection since SettingsPreferences can expose a Flow directly.

### Decision 3: Define color palettes per accent color with primary/secondary/tertiary variants

- **Decision**: Define 3 color palettes (Teal, Blue, Red) each providing primary, onPrimary,
  primaryContainer, onPrimaryContainer, secondary, onSecondary, secondaryContainer,
  onSecondaryContainer, tertiary, onTertiary, tertiaryContainer, onTertiaryContainer values for
  both light and dark modes. Non-accent colors (background, surface, error) remain unchanged.
- **Rationale**: Material3 accent roles are primary, secondary, and tertiary. Changing only
  `primary` would leave secondary/tertiary as the default purple/pink which clashes. Each accent
  needs a harmonious set of all three roles.
- **Alternatives considered**: (1) Change only `primary` color -- visually inconsistent with
  leftover default secondary/tertiary. (2) Use Material3 dynamic color -- not available
  cross-platform in KMP.

### Decision 4: Teal palette matches existing Primary color

- **Decision**: The Teal accent color uses the existing `Primary = Color(0xFF2badee)` and the
  current secondary/tertiary values, so there is zero visual change for the default accent.
- **Rationale**: Teal is the default. Users who never touch settings should see no difference.

## Current State

### AccentColor enum
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/model/AccentColor.kt:3`
- Contains: `enum class AccentColor { Teal, Blue, Red }`

### Persistence (works, tested)
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferences.kt`
- `getAccentColor()` (line 25) and `setAccentColor()` (line 31) -- suspend functions only, no Flow.
- DataStore key: `accent_color` (line 59)

### Theme (does NOT use AccentColor)
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Theme.kt`
- `AppTheme` takes only `darkTheme: Boolean` (line 16-18)
- Selects between `AppDarkColorScheme` and `AppLightColorScheme` (line 20-23)

### Color definitions
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`
- Single `Primary = Color(0xFF2badee)` used by both light and dark schemes (lines 7, 55, 81)
- Secondary defaults to purple-ish `Color(0xFF625B71)` light / `Color(0xFFCCC2DC)` dark
- Tertiary defaults to pink `Color(0xFF7D5260)` light / `Color(0xFFEFB8C8)` dark

### App.kt (root composable)
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/App.kt`
- Already injects `SettingsPreferences` (line 23) but only uses it for auto-update check
- Passes `darkTheme` to `AppTheme` (line 40), no accent color parameter

### SettingsViewModel
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModel.kt`
- `OnAccentColorChanged` event saves via `settingsPreferences.setAccentColor()` (line 48-50)
- Updates local state but has no mechanism to propagate to theme

## Changes Required

### Modified Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferences.kt`
- **What changes**: Add a `accentColorFlow: Flow<AccentColor>` property that maps DataStore data
  to `AccentColor`, mirroring how `ThemePreferences.themeModeFlow` works.
- **Why**: `App.kt` needs to reactively observe accent color changes. Currently only suspend
  getters exist.
- **Details**:
  ```kotlin
  val accentColorFlow: Flow<AccentColor> = dataStore.data
      .map { prefs ->
          val name = prefs[ACCENT_COLOR_KEY] ?: AccentColor.Teal.name
          runCatching { AccentColor.valueOf(name) }.getOrDefault(AccentColor.Teal)
      }
      .distinctUntilChanged()
  ```

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`
- **What changes**: Add accent-specific color constants and a helper function
  `accentColorScheme(accentColor: AccentColor, darkTheme: Boolean): ColorScheme` that returns
  the full color scheme for a given accent + theme combination.
- **Why**: Centralizes all color definitions in the existing Color.kt file.
- **Details**: Add the following color constants and function:

  **Teal accent** (existing colors, no change):
  - Light primary: `0xFF2badee`, dark primary: `0xFF2badee` (existing `Primary`)
  - Keep existing secondary/tertiary but with teal-harmonious alternatives:
    - Light secondary: `0xFF4a6267`, dark secondary: `0xFF9ecbd2`
    - Light tertiary: `0xFF4d6357`, dark tertiary: `0xFF9ecbab`

  **Blue accent**:
  - Light primary: `0xFF1a73e8`, dark primary: `0xFF8ab4f8`
  - Light secondary: `0xFF535f70`, dark secondary: `0xFFbbc7db`
  - Light tertiary: `0xFF6b5778`, dark tertiary: `0xFFd6bee4`
  - Plus container variants for each

  **Red accent**:
  - Light primary: `0xFFc62828`, dark primary: `0xFFef9a9a`
  - Light secondary: `0xFF775651`, dark secondary: `0xFFe7bdb6`
  - Light tertiary: `0xFF6f5b2e`, dark tertiary: `0xFFdbc06c`
  - Plus container variants for each

  Add a function that builds the full `ColorScheme`:
  ```kotlin
  fun accentColorScheme(accentColor: AccentColor, darkTheme: Boolean): ColorScheme
  ```
  This function returns a `lightColorScheme(...)` or `darkColorScheme(...)` with the accent-specific
  primary/secondary/tertiary colors and shared background/surface/error colors (unchanged).

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Theme.kt`
- **What changes**: Add `accentColor: AccentColor = AccentColor.Teal` parameter to `AppTheme`.
  Replace the hardcoded scheme selection with a call to `accentColorScheme(accentColor, darkTheme)`.
- **Why**: Theme needs to know which accent to apply.
- **Details**:
  ```kotlin
  @Composable
  fun AppTheme(
      darkTheme: Boolean = isSystemInDarkTheme(),
      accentColor: AccentColor = AccentColor.Teal,
      content: @Composable () -> Unit,
  ) {
      val colorScheme = accentColorScheme(accentColor, darkTheme)

      MaterialTheme(
          colorScheme = colorScheme,
          typography = AppTypography,
          content = content,
      )
  }
  ```

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/App.kt`
- **What changes**: Collect `settingsPreferences.accentColorFlow` as state and pass it to
  `AppTheme`.
- **Why**: This is the composition root where theme parameters are resolved.
- **Details**:
  ```kotlin
  val accentColor by settingsPreferences.accentColorFlow
      .collectAsState(initial = AccentColor.Teal)

  AppTheme(darkTheme = darkTheme, accentColor = accentColor) {
      AppNavGraph()
  }
  ```
  Import `com.simplevideo.whiteiptv.domain.model.AccentColor` and
  `kotlinx.coroutines.flow.Flow` (collectAsState for Flow is in
  `androidx.compose.runtime.collectAsState`).

### No New Files Required

All changes fit within existing files. No new classes, interfaces, or modules needed.

### No Database Changes

AccentColor is stored in DataStore, not Room.

### No DI Changes

`SettingsPreferences` is already registered as a singleton in `settingsModule` and already injected
in `App.kt`.

## Implementation Order

1. **Add `accentColorFlow` to `SettingsPreferences`** -- Add the Flow property. This is the
   foundation that App.kt will observe. File:
   `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferences.kt`

2. **Add accent color palettes and `accentColorScheme()` to Color.kt** -- Define all color
   constants for Blue and Red accents, and create the scheme builder function. File:
   `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`

3. **Update `AppTheme` to accept `AccentColor` parameter** -- Add the parameter and use
   `accentColorScheme()` instead of hardcoded scheme selection. File:
   `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Theme.kt`

4. **Wire accent color in `App.kt`** -- Collect the flow and pass to AppTheme. File:
   `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/App.kt`

5. **Verify build and run** -- Build Android and run the app to confirm accent color switching
   works visually.

## Testing Strategy

### Unit Tests

**`SettingsPreferencesTest`** (if exists, extend; otherwise new test in `commonTest` or
`androidHostTest`):
- Test that `accentColorFlow` emits `AccentColor.Teal` by default
- Test that after `setAccentColor(AccentColor.Blue)`, `accentColorFlow` emits `AccentColor.Blue`
- Test that invalid stored value falls back to `AccentColor.Teal`

**`accentColorScheme` function tests** (in `commonTest`):
- Test that `accentColorScheme(AccentColor.Teal, darkTheme = false)` returns a scheme with the
  expected teal primary color
- Test that `accentColorScheme(AccentColor.Blue, darkTheme = true)` returns the expected blue
  primary for dark mode
- Test that `accentColorScheme(AccentColor.Red, darkTheme = false)` returns the expected red primary
- Test that background/surface/error colors are the same across all accent colors

### Coroutine Test Patterns

- DataStore-backed Flow tests need `runTest` with `UnconfinedTestDispatcher`
- The `accentColorFlow` is a cold Flow from DataStore, so use `flow.first()` or `turbine` for
  assertions

### Edge Cases

- App launch with no stored accent preference (should default to Teal)
- Rapid switching between accent colors (Flow distinctUntilChanged handles dedup)
- Reset to defaults (SettingsPreferences.resetAll clears DataStore, flow should emit Teal)

### Manual Verification

- Launch app with default settings -- should look identical to current app (Teal)
- Go to Settings > Accent Color > Blue -- entire app should turn blue
- Switch to Red -- entire app should turn red
- Kill and relaunch -- selected color should persist
- Reset to Defaults -- should revert to Teal

## Doc Updates Required

After implementation, update the following:

- **`docs/features/settings.md`**: Remove the implementation note on line 54 that says "Accent
  Color: Preference is persisted but does not yet alter the Material theme color scheme. Visual
  application is a separate task." Replace with a note confirming it is implemented.
  > [!NOTE] Update AFTER implementation

- **`docs/constraints/current-limitations.md`**: Remove the "Settings: Accent Color is persisted
  but not applied to theme" section (lines 29-34).
  > [!NOTE] Update AFTER implementation

## Build & Test Commands

```bash
# Build shared module tests
./gradlew :shared:testDebugUnitTest

# Build Android app to verify compilation
./gradlew :androidApp:assembleDebug

# Run all shared tests (Android host)
./gradlew :shared:testAndroidHostTest

# Full check
./gradlew :shared:allTests :androidApp:assembleDebug
```
