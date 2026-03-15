# Settings Stitch Design Sync -- Implementation Plan

## Summary

Refactor the Settings screen to exactly match the Stitch design mockup. This involves three categories of changes: (1) replacing SegmentedButton controls with dropdown selectors for Theme, Accent Color, and Channel View; (2) correcting all icons to match Stitch's icon names; (3) adding trailing chevron icons to navigable/actionable rows; (4) adding the missing "Default Playlist" setting; (5) fixing subtitle text to match Stitch. No new data layer or domain logic is needed -- this is purely a presentation layer refactoring.

## Decisions Made

### 1. Dropdown implementation approach
- **Decision**: Create a new `SettingsDropdownItem` composable that combines the existing `SettingsItem` layout (icon + title + subtitle) with a trailing `expand_more` chevron and a Material3 `DropdownMenu` that appears on click.
- **Rationale**: This keeps the component self-contained. The dropdown opens inline below the row, matching standard Android UX patterns. Material3 `DropdownMenu` handles positioning and dismissal automatically.
- **Alternatives considered**: Bottom sheet picker (heavier UI for 2-3 options), Dialog picker (breaks visual flow), Exposed dropdown menu (designed for text fields, not settings rows).

### 2. Icons -- Contrast, CleaningServices, Hd, Favorite, Update availability
- **Decision**: Use `Icons.Outlined.Contrast`, `Icons.Outlined.CleaningServices`, `Icons.Outlined.Hd`, `Icons.Outlined.FavoriteBorder` (closest to filled `favorite` from Stitch), and `Icons.Outlined.Update`. The project already includes `compose.materialIconsExtended` so all of these are available.
- **Rationale**: The extended icon set contains all named Material icons. `FavoriteBorder` is the outlined variant of `Favorite` -- Stitch uses `favorite` which in Material Icons Outlined maps to `FavoriteBorder`. However, since the Stitch HTML literally says icon is `favorite`, we will use `Icons.Outlined.Favorite` (filled heart in outlined style).
- **Alternatives considered**: None -- the Stitch spec is explicit about icon names.

### 3. Default Playlist -- non-functional placeholder for now
- **Decision**: Add the "Default Playlist" row to the App Behavior section as a dropdown item. It will show the first playlist name (or "None") as subtitle. Clicking it will open a dropdown with available playlists. This requires reading playlist names from the repository.
- **Rationale**: The Stitch design shows this item. However, the app currently does not have a "default playlist" concept in preferences. We will add a `defaultPlaylistId` to `SettingsPreferences` and wire it up to the playlist repository to fetch names.
- **Alternatives considered**: Static placeholder only -- rejected because the dropdown needs real playlist data to look correct.

### 4. Language dropdown -- visual only with "English" selected
- **Decision**: Show Language as a dropdown with "English" as the only option (selected). The dropdown will display just ["English"] and selecting it does nothing. This matches the Stitch design visually while respecting the current limitation that language switching is not implemented.
- **Rationale**: Stitch shows subtitle "English" with a dropdown chevron. Current limitation docs confirm no locale override exists. Showing a single-option dropdown is honest UX.

### 5. Auto Update subtitle -- "Daily" vs long description
- **Decision**: Change the `SettingsSwitchItem` subtitle from "Automatically refresh playlists on app start" to "Daily". This matches Stitch exactly.
- **Rationale**: Stitch spec is explicit. The subtitle describes the frequency, not the behavior.

### 6. Default Player subtitle -- "ExoPlayer" vs "Built-in"
- **Decision**: Change subtitle from "Built-in" to "ExoPlayer". On iOS, this should say "AVPlayer". Use an expect/actual or a platform check.
- **Rationale**: Stitch spec says "ExoPlayer". Since this is a KMP app, we should show the actual player name per platform. We will use a simple `expect val defaultPlayerName: String` approach. However, to keep it simpler, we will just use a compile-time constant in the screen since the Playback section items are non-functional dropdowns anyway.
- **Final approach**: Use `getPlatformPlayerName()` expect/actual function -- but this is over-engineered for a static label. Instead, just hardcode "ExoPlayer" in commonMain since the Stitch design targets Android. If iOS divergence is needed later, it can be changed. Actually, to be KMP-correct, we will define a simple `expect fun platformPlayerName(): String` in `platform/` with actual implementations returning "ExoPlayer" on Android and "AVPlayer" on iOS.

### 7. Trailing chevrons -- which rows get them
- **Decision**: Per Stitch design:
  - Dropdown rows (Theme, Accent Color, Channel View, Default Player, Preferred Quality, Default Playlist, Language): trailing `Icons.Default.ArrowDropDown` (expand_more equivalent)
  - Switch row (Auto Update): trailing Switch control (no chevron)
  - Action rows (Clear Cache, Clear Favorites, Reset to Defaults, Contact Support, Privacy Policy): trailing `Icons.AutoMirrored.Outlined.KeyboardArrowRight` (chevron_right)
  - Info-only row (App Version): no trailing icon
- **Rationale**: Matches Stitch HTML structure exactly. Dropdown items use expand_more, action items use chevron_right.

### 8. SettingsSegmentedButton removal
- **Decision**: Remove `SettingsSegmentedButton` composable entirely after migration. No other screen uses it.
- **Rationale**: Dead code after dropdown migration.

## Current State

### Files to modify

- **`shared/src/commonMain/.../feature/settings/SettingsScreen.kt`** (306 lines): Contains all section composables. Uses `SettingsSegmentedButton` for Appearance section. Wrong icons throughout. No trailing chevrons. Missing Default Playlist item.

- **`shared/src/commonMain/.../feature/settings/components/SettingsComponents.kt`** (157 lines): Contains `SettingsSection`, `SettingsItem`, `SettingsSwitchItem`, `SettingsSegmentedButton`. `SettingsItem` has no trailing icon slot. `SettingsSegmentedButton` will be removed.

- **`shared/src/commonMain/.../feature/settings/mvi/SettingsMvi.kt`** (40 lines): `SettingsState` has no `defaultPlaylistId` or `defaultPlaylistName`. No events for dropdown expand/collapse (not needed -- local composable state). No events for Default Playlist changes.

- **`shared/src/commonMain/.../feature/settings/SettingsViewModel.kt`** (136 lines): No Default Playlist logic. No playlist repository dependency.

- **`shared/src/commonMain/.../data/local/SettingsPreferences.kt`** (77 lines): No `defaultPlaylistId` key.

### Domain models (no changes needed)

- `shared/src/commonMain/.../domain/model/ThemeMode.kt`: enum `System, Light, Dark`
- `shared/src/commonMain/.../domain/model/AccentColor.kt`: enum `Teal, Blue, Red`
- `shared/src/commonMain/.../domain/model/ChannelViewMode.kt`: enum `List, Grid`

## Changes Required

### New Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsDropdownItem.kt`
- Purpose: Dropdown selector component matching Stitch design
- Key contents:
```kotlin
@Composable
fun <T> SettingsDropdownItem(
    title: String,
    icon: ImageVector,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
)
```
- Layout: Row with icon (24dp) + 16dp spacer + Column(title as bodyLarge, subtitle as bodySmall showing `optionLabel(selectedOption)`) + weight(1f) + `Icons.Default.ArrowDropDown` trailing icon
- On click: toggles `expanded` local state, shows `DropdownMenu` anchored to the row
- `DropdownMenu` contains `DropdownMenuItem` for each option, with a checkmark or highlight for the selected one

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/platform/PlatformPlayerName.kt`
- Purpose: Provide platform-specific default player name
- Key contents:
```kotlin
expect fun platformPlayerName(): String
```

#### 3. `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/PlatformPlayerName.android.kt`
- Purpose: Android actual
- Key contents:
```kotlin
actual fun platformPlayerName(): String = "ExoPlayer"
```

#### 4. `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/platform/PlatformPlayerName.ios.kt`
- Purpose: iOS actual
- Key contents:
```kotlin
actual fun platformPlayerName(): String = "AVPlayer"
```

### Modified Files

#### 1. `shared/src/commonMain/.../feature/settings/components/SettingsComponents.kt`

**Changes:**
1. Add `trailingIcon` parameter to `SettingsItem`:
```kotlin
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    titleColor: Color = Color.Unspecified,
    trailingIcon: ImageVector? = null,  // NEW
    trailingIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,  // NEW
)
```
After the `Column(modifier = Modifier.weight(1f))` block, add:
```kotlin
if (trailingIcon != null) {
    Spacer(modifier = Modifier.width(8.dp))
    Icon(
        imageVector = trailingIcon,
        contentDescription = null,
        tint = trailingIconTint,
        modifier = Modifier.size(24.dp),
    )
}
```

2. **Remove** `SettingsSegmentedButton` composable entirely (lines 136-156).

3. Remove imports: `ExperimentalMaterial3Api`, `SegmentedButton`, `SegmentedButtonDefaults`, `SingleChoiceSegmentedButtonRow`.

#### 2. `shared/src/commonMain/.../feature/settings/SettingsScreen.kt`

**Icon changes (all in imports and usages):**

| Current import | New import |
|---|---|
| `Icons.Outlined.Palette` (Theme) | `Icons.Outlined.Contrast` |
| `Icons.Outlined.Brush` (Accent Color) | `Icons.Outlined.Palette` |
| `Icons.Outlined.HighQuality` | `Icons.Outlined.Hd` |
| `Icons.Outlined.Delete` | `Icons.Outlined.CleaningServices` |
| `Icons.Outlined.StarBorder` | `Icons.Outlined.Favorite` |
| `Icons.Outlined.Sync` | `Icons.Outlined.Update` |

Remove unused imports: `Icons.Outlined.Brush`, `Icons.Outlined.Delete`, `Icons.Outlined.HighQuality`, `Icons.Outlined.StarBorder`, `Icons.Outlined.Sync`.

Add new imports: `Icons.Outlined.Contrast`, `Icons.Outlined.CleaningServices`, `Icons.Outlined.Hd`, `Icons.Outlined.Favorite`, `Icons.Outlined.Update`, `Icons.AutoMirrored.Outlined.KeyboardArrowRight`, `Icons.Outlined.PlaylistPlay`.

**AppearanceSection rewrite** -- Replace SettingsSegmentedButton usage with SettingsDropdownItem:
```kotlin
@Composable
private fun AppearanceSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSection(title = "Appearance") {
        SettingsDropdownItem(
            title = "Theme",
            icon = Icons.Outlined.Contrast,
            options = listOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark),
            selectedOption = state.themeMode,
            onOptionSelected = { onEvent(SettingsEvent.OnThemeModeChanged(it)) },
            optionLabel = { mode ->
                when (mode) {
                    ThemeMode.System -> "System"
                    ThemeMode.Light -> "Light"
                    ThemeMode.Dark -> "Dark"
                }
            },
        )
        SettingsDropdownItem(
            title = "Accent Color",
            icon = Icons.Outlined.Palette,
            options = AccentColor.entries.toList(),
            selectedOption = state.accentColor,
            onOptionSelected = { onEvent(SettingsEvent.OnAccentColorChanged(it)) },
            optionLabel = { it.name },
        )
        SettingsDropdownItem(
            title = "Channel View",
            icon = Icons.AutoMirrored.Outlined.ViewList,
            options = ChannelViewMode.entries.toList(),
            selectedOption = state.channelViewMode,
            onOptionSelected = { onEvent(SettingsEvent.OnChannelViewModeChanged(it)) },
            optionLabel = { it.name },
        )
    }
}
```

Remove: All `Spacer(modifier = Modifier.height(16.dp))` between segmented buttons. Remove `SettingsItem` calls that served as labels for the segmented buttons (the dropdown item is self-contained with icon + title + subtitle).

**PlaybackSection rewrite** -- Convert to dropdowns (non-functional, single option each):
```kotlin
@Composable
private fun PlaybackSection() {
    SettingsSection(title = "Playback") {
        SettingsDropdownItem(
            title = "Default Player",
            icon = Icons.Outlined.PlayCircle,
            options = listOf(platformPlayerName()),
            selectedOption = platformPlayerName(),
            onOptionSelected = {},
            optionLabel = { it },
        )
        SettingsDropdownItem(
            title = "Preferred Quality",
            icon = Icons.Outlined.Hd,
            options = listOf("Auto", "1080p", "720p", "480p"),
            selectedOption = "Auto",
            onOptionSelected = {},
            optionLabel = { it },
        )
    }
}
```

**AppBehaviorSection rewrite** -- Add Default Playlist dropdown, fix Language, fix Auto Update:
```kotlin
@Composable
private fun AppBehaviorSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSection(title = "App Behavior") {
        SettingsDropdownItem(
            title = "Default Playlist",
            icon = Icons.Outlined.PlaylistPlay,
            options = state.playlistNames,
            selectedOption = state.defaultPlaylistName,
            onOptionSelected = { onEvent(SettingsEvent.OnDefaultPlaylistChanged(it)) },
            optionLabel = { it },
        )
        SettingsDropdownItem(
            title = "Language",
            icon = Icons.Outlined.Language,
            options = listOf("English"),
            selectedOption = "English",
            onOptionSelected = {},
            optionLabel = { it },
        )
        SettingsSwitchItem(
            title = "Auto Update Playlists",
            subtitle = "Daily",
            checked = state.autoUpdateEnabled,
            onCheckedChange = { onEvent(SettingsEvent.OnAutoUpdateChanged(it)) },
            icon = Icons.Outlined.Update,
        )
    }
}
```

**DataStorageSection** -- Fix icons, add trailing chevrons:
```kotlin
SettingsItem(
    title = "Clear Cache",
    subtitle = state.cacheSize,
    onClick = { onEvent(SettingsEvent.OnClearCacheClick) },
    icon = Icons.Outlined.CleaningServices,
    trailingIcon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
)
SettingsItem(
    title = "Clear Favorites",
    onClick = { onEvent(SettingsEvent.OnClearFavoritesClick) },
    icon = Icons.Outlined.Favorite,
    trailingIcon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
)
SettingsItem(
    title = "Reset to Defaults",
    onClick = { onEvent(SettingsEvent.OnResetClick) },
    icon = Icons.Outlined.RestartAlt,
    titleColor = MaterialTheme.colorScheme.error,
    trailingIcon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
)
```

Note: Remove `subtitle` from Clear Favorites ("Remove all favorite channels") and Reset to Defaults ("Restore all settings to default values") -- Stitch design does not show subtitles for these rows.

**AboutSection** -- Add trailing chevrons to Contact Support and Privacy Policy:
```kotlin
SettingsItem(
    title = "App Version",
    subtitle = state.appVersion,
    onClick = {},
    icon = Icons.Outlined.Info,
)
SettingsItem(
    title = "Contact Support",
    onClick = { onEvent(SettingsEvent.OnContactSupportClick) },
    icon = Icons.Outlined.Mail,
    trailingIcon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
)
SettingsItem(
    title = "Privacy Policy",
    onClick = { onEvent(SettingsEvent.OnPrivacyPolicyClick) },
    icon = Icons.Outlined.Policy,
    trailingIcon = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
)
```

Note: Remove `subtitle` from Contact Support ("support@simplevideo.com") -- Stitch design does not show it.

Remove import: `SettingsSegmentedButton` (no longer used).
Add import: `SettingsDropdownItem`, `platformPlayerName`.

#### 3. `shared/src/commonMain/.../feature/settings/mvi/SettingsMvi.kt`

**Add to `SettingsState`:**
```kotlin
val defaultPlaylistName: String = "None",
val playlistNames: List<String> = emptyList(),
```

**Add to `SettingsEvent`:**
```kotlin
data class OnDefaultPlaylistChanged(val playlistName: String) : SettingsEvent
```

#### 4. `shared/src/commonMain/.../feature/settings/SettingsViewModel.kt`

**Add constructor dependency:**
```kotlin
private val playlistRepository: PlaylistRepository,
```

**In `init` block, add playlist loading:**
```kotlin
viewModelScope.launch {
    playlistRepository.getPlaylists().collect { playlists ->
        val names = playlists.map { it.name }
        val defaultName = settingsPreferences.getDefaultPlaylistName()
            ?: names.firstOrNull() ?: "None"
        viewState = viewState.copy(
            playlistNames = names.ifEmpty { listOf("None") },
            defaultPlaylistName = if (names.contains(defaultName)) defaultName else names.firstOrNull() ?: "None",
        )
    }
}
```

**Add event handler in `obtainEvent`:**
```kotlin
is SettingsEvent.OnDefaultPlaylistChanged -> {
    viewModelScope.launch {
        settingsPreferences.setDefaultPlaylistName(viewEvent.playlistName)
    }
    viewState = viewState.copy(defaultPlaylistName = viewEvent.playlistName)
}
```

#### 5. `shared/src/commonMain/.../data/local/SettingsPreferences.kt`

**Add new key and methods:**
```kotlin
private val DEFAULT_PLAYLIST_NAME_KEY = stringPreferencesKey("default_playlist_name")

suspend fun getDefaultPlaylistName(): String? {
    val prefs = dataStore.data.first()
    return prefs[DEFAULT_PLAYLIST_NAME_KEY]
}

suspend fun setDefaultPlaylistName(name: String) {
    dataStore.edit { prefs -> prefs[DEFAULT_PLAYLIST_NAME_KEY] = name }
}
```

Update `resetAll()` -- no change needed since `edit { it.clear() }` already clears all keys.

#### 6. `shared/src/commonMain/.../di/KoinModule.kt`

**Add `PlaylistRepository` to `SettingsViewModel` injection.** Since `PlaylistRepository` is already registered as a singleton, Koin will resolve it. The `viewModelOf(::SettingsViewModel)` call should already pick up the new constructor parameter automatically (Koin constructor injection).

Verify that the SettingsViewModel registration uses `viewModelOf` -- if so, no DI change is needed since Koin resolves all constructor parameters from the graph.

### Database Changes

None.

### DI Changes

No explicit DI changes needed. `SettingsViewModel` uses constructor injection via `viewModelOf(::SettingsViewModel)`. Adding `PlaylistRepository` to the constructor will be auto-resolved by Koin since `PlaylistRepository` is already registered as a singleton in `repositoryModule`.

## Implementation Order

1. **Create `PlatformPlayerName` expect/actual** (3 files) -- provides `platformPlayerName()` for Playback section.

2. **Create `SettingsDropdownItem` component** (`components/SettingsDropdownItem.kt`) -- the new dropdown composable used by all dropdown rows.

3. **Modify `SettingsComponents.kt`** -- Add `trailingIcon` parameter to `SettingsItem`. Remove `SettingsSegmentedButton`.

4. **Modify `SettingsMvi.kt`** -- Add `defaultPlaylistName`, `playlistNames` to state. Add `OnDefaultPlaylistChanged` event.

5. **Modify `SettingsPreferences.kt`** -- Add `getDefaultPlaylistName()` and `setDefaultPlaylistName()`.

6. **Modify `SettingsViewModel.kt`** -- Add `PlaylistRepository` dependency, load playlist names, handle new event.

7. **Modify `SettingsScreen.kt`** -- Rewrite all sections: replace segmented buttons with dropdowns, fix all icons, add trailing chevrons, add Default Playlist item, fix subtitles.

8. **Verify build** -- `./gradlew :shared:compileKotlinAndroidHost :androidApp:assembleDebug`

## Testing Strategy

### Manual verification
- Build and run on Android device/emulator
- Verify each section matches Stitch design visually
- Test all dropdowns open and close correctly
- Test dropdown selection persists (Theme, Accent Color, Channel View)
- Test Default Playlist shows actual playlist names
- Test trailing chevrons appear on correct rows
- Test action items (Clear Cache, Clear Favorites, Reset) still work

### Unit tests
- **SettingsViewModel**: Test `OnDefaultPlaylistChanged` event updates state and persists
- **SettingsPreferences**: Test `getDefaultPlaylistName` / `setDefaultPlaylistName` round-trip

### Edge cases
- No playlists imported -- Default Playlist should show "None"
- Single playlist -- Default Playlist dropdown shows one option
- Reset to Defaults -- should reset default playlist name too
- Screen rotation -- dropdown should dismiss on configuration change (Material3 handles this)

### Coroutine test patterns
- ViewModel tests need `Dispatchers.setMain(UnconfinedTestDispatcher())` in `@Before`
- DataStore tests need a test CoroutineScope for DataStore creation
- PlaylistRepository Flow collection in ViewModel uses `viewModelScope.launch` -- test by emitting to a `MutableStateFlow` in a fake repository

## Doc Updates Required

After implementation:
- Update `docs/features/settings.md` -- Add Playback section, Default Playlist setting, note about dropdown controls
- Update `docs/constraints/current-limitations.md` -- Add note about Playback section being non-functional (Default Player and Preferred Quality are visual-only)

## Build & Test Commands

```bash
./gradlew :shared:compileKotlinAndroidHost :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew formatAll
```
