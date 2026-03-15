# Settings Screen Pixel-Perfect Stitch Match -- Implementation Plan

## Summary

Complete visual overhaul of the Settings screen to match the Stitch HTML design pixel-for-pixel. The current implementation uses flat rows with bare 24dp icons, no card containers, and no row dividers. The Stitch design uses grouped card containers (`rounded-xl`, `bg-card-dark`/`bg-card-light`), 48dp icon containers with `bg-primary/20` backgrounds, dividers between rows, uppercase bold section headers, and distinct row types for dropdown, action, and info rows. This plan also adds the missing Stitch colors to the theme (`CardDark`, `CardLight`, `BorderDark`, `BorderLight`, `TextDarkSecondary`, `TextLightSecondary`) and verifies that `ThemeMode.System` is already the default.

## Decisions Made

### Decision 1: Use custom `SettingsColors` object instead of Material3 ColorScheme slots
- **Decision**: Define a `SettingsColors` composable-local object with explicit card, border, and text colors from the Stitch spec rather than mapping them to existing Material3 ColorScheme slots.
- **Rationale**: The Stitch design specifies exact hex colors (`#18262d` for card-dark, `#374151` for border-dark, `#9ca3af` for text-dark-secondary) that do not cleanly map to standard Material3 slots. Using a dedicated object avoids confusion and makes the mapping from HTML to Compose explicit.
- **Alternatives considered**: Using `surfaceContainer` for card and `outline` for border, but the existing values do not match the Stitch spec (`surfaceContainer` is `#1a2830`, not `#18262d`).

### Decision 2: Dark theme Stitch is the primary source; light theme HTML is the secondary
- **Decision**: Use the dark-mode HTML (`code.html`) for section structure and row content, and the light-mode HTML for light-theme colors. The dark-mode screenshot shows `expand_more` trailing icons for dropdown rows; the light-mode screenshot shows `chevron_right` with a primary-colored selected value text. Use the **dark-mode** pattern (expand_more dropdown) as the canonical interaction model since that matches the current code's dropdown behavior.
- **Rationale**: The dark and light Stitch files differ slightly in trailing content (dark uses expand_more icon only; light shows "value > chevron_right"). The dark version matches the existing dropdown behavior better and is simpler. The light version's trailing value display can be added later.
- **Alternatives considered**: Adopting the light theme's "value + chevron_right" pattern, but this would change interaction behavior (from dropdown to navigation) which is out of scope.

### Decision 3: No Playback section -- merge into App Behavior
- **Decision**: The current code has no separate Playback section. The Stitch dark design has Appearance (3 rows), Playback (2 rows), App Behavior (3 rows), Data & Storage (3 rows), About (3 rows). Keep this exact Stitch section structure.
- **Rationale**: Match the Stitch design exactly. The current code's "App Behavior" section mixes Playback items in. We need to separate them per the Stitch spec.

### Decision 4: Auto Update row uses a toggle (switch), not a dropdown
- **Decision**: The Stitch dark-mode HTML shows a checkbox/toggle for "Auto Update Playlists" instead of a dropdown. Keep the existing `SettingsSwitchItem` behavior but restyle it to match the Stitch icon container design.
- **Rationale**: Both the Stitch HTML and current code agree on toggle behavior.

### Decision 5: Theme default is System -- already correct
- **Decision**: No change needed. `SettingsState` initializes `themeMode = ThemeMode.System`, and `ThemePreferences` defaults to `"system"` which maps to `ThemeMode.System`.
- **Rationale**: Verified in `SettingsMvi.kt` line 8 and `ThemePreferences.mapToThemeMode()`.

### Decision 6: Match dark-mode Stitch section structure exactly
- **Decision**: 5 sections in this order: APPEARANCE, PLAYBACK, APP BEHAVIOR, DATA & STORAGE, ABOUT.
  - APPEARANCE: Theme, Accent Color, Channel View (all dropdown type)
  - PLAYBACK: Default Player, Preferred Quality (both dropdown type)
  - APP BEHAVIOR: Default Playlist, Language, Auto Update Playlists (2 dropdown + 1 switch)
  - DATA & STORAGE: Clear Cache, Clear Favorites, Reset to Defaults (all action type, NO icon bg)
  - ABOUT: App Version (info), Contact Support (nav), Privacy Policy (nav) (all NO icon)

## Current State

### Settings Screen (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsScreen.kt`)
- Uses `LazyColumn` with flat items separated by `HorizontalDivider`
- 4 sections: Appearance, App Behavior, Data & Storage, About
- No card containers, no rounded corners on groups
- Section headers use `titleSmall` style with `primary` color (wrong: should be secondary, uppercase, bold, tracking-wider)
- Missing Playback section entirely

### Settings Components (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsComponents.kt`)
- `SettingsSection`: Column with `titleSmall` header in primary color, no card wrapping
- `SettingsItem`: Simple row with optional subtitle and trailing icon, no leading icon container, 12dp vertical padding (should be min-height 60dp)
- `SettingsSwitchItem`: Row with bare 24dp icon, no icon container, 12dp vertical padding

### Settings Dropdown Item (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsDropdownItem.kt`)
- Bare 24dp icon (no 48dp container, no bg-primary/20)
- Single-line title (no subtitle with selected value)
- ArrowDropDown trailing icon (should be `expand_more` which is visually equivalent)
- 12dp vertical padding (should be min-height 72dp)

### Color Theme (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`)
- Missing `CardDark` (`#18262d`), `CardLight` (`#ffffff`)
- Missing `BorderDark` (`#374151`), `BorderLight` (`#e5e7eb`)
- Missing `TextDarkSecondary` (`#9ca3af`), `TextLightSecondary` (`#6b7280`)
- `surfaceContainer` = `#1a2830` (close but not exact match to card-dark `#18262d`)
- `accentColorScheme()` does NOT set `surfaceContainer` for any accent color variant

### Theme (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Theme.kt`)
- Correctly defaults to `isSystemInDarkTheme()` -- no issue here

## Changes Required

### New Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/SettingsColors.kt`
- **Purpose**: Provides Stitch-exact colors for the Settings screen that don't map cleanly to Material3 slots.
- **Key contents**:
```kotlin
package com.simplevideo.whiteiptv.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SettingsColors(
    val cardBackground: Color,
    val borderColor: Color,
    val sectionHeaderColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val destructive: Color,
    val iconContainerBg: Color,   // primary.copy(alpha = 0.2f) computed from theme
    val iconTint: Color,          // primary color
)

val LocalSettingsColors = staticCompositionLocalOf { SettingsColorsDark }

val SettingsColorsDark = SettingsColors(
    cardBackground = Color(0xFF18262d),
    borderColor = Color(0xFF374151),
    sectionHeaderColor = Color(0xFF9ca3af),
    textPrimary = Color(0xFFe5e7eb),
    textSecondary = Color(0xFF9ca3af),
    destructive = Color(0xFFef4444),  // Tailwind red-500
    iconContainerBg = Color(0xFF2badee).copy(alpha = 0.2f),
    iconTint = Color(0xFF2badee),
)

val SettingsColorsLight = SettingsColors(
    cardBackground = Color(0xFFffffff),
    borderColor = Color(0xFFe5e7eb),
    sectionHeaderColor = Color(0xFF6b7280),
    textPrimary = Color(0xFF1f2937),
    textSecondary = Color(0xFF6b7280),
    destructive = Color(0xFFef4444),
    iconContainerBg = Color(0xFF2badee).copy(alpha = 0.2f),
    iconTint = Color(0xFF2badee),
)

@Composable
fun settingsColors(darkTheme: Boolean = isSystemInDarkTheme()): SettingsColors =
    if (darkTheme) SettingsColorsDark else SettingsColorsLight
```

### Modified Files

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsComponents.kt`
**Complete rewrite.** New composables:

- **`SettingsSection`**: Section header (uppercase, bold, `labelSmall` with letterSpacing=1.sp, `sectionHeaderColor`) + `Card` container with `cardBackground`, `RoundedCornerShape(16.dp)` (rounded-xl = 24px in web, but 16.dp is standard in mobile; we use 16.dp), `clip` + no elevation.
  - Actually, `rounded-xl` in Tailwind = 1.5rem = 24px. In mobile density-independent context, 16.dp is the standard iOS-style card radius. **Decision**: Use 16.dp for rounded-xl as it's the standard mobile equivalent and matches the visual feel. The Stitch HTML is a web mockup.

- **`SettingsDropdownRow`**: Replaces `SettingsDropdownItem` conceptually (but we keep the file). Min height 72.dp. Leading: 48.dp Box with `iconContainerBg` background, `RoundedCornerShape(12.dp)`, centered 24.dp Icon in `iconTint`. Title (bodyLarge, fontWeight Medium, textPrimary). Subtitle (bodySmall, textSecondary) showing selected label. Trailing: `Icons.Default.KeyboardArrowDown` (expand_more equivalent) in `textSecondary`.

- **`SettingsActionRow`**: For Data & Storage items. Min height 60.dp. Leading: 48.dp Box with NO background, just centered 24.dp icon in `iconTint` (or `destructive` for Reset). Title only (bodyLarge, fontWeight Medium, textPrimary or `destructive`). Trailing: `Icons.AutoMirrored.Filled.KeyboardArrowRight` (chevron_right) in `textSecondary`.

- **`SettingsInfoRow`**: For About items. Min height 60.dp. NO icon. Title on left (bodyLarge, fontWeight Medium, textPrimary). Trailing: either value text (bodySmall, textSecondary) OR chevron_right icon (textSecondary).

- **`SettingsSwitchRow`**: For Auto Update. Min height 72.dp. Same leading icon container as DropdownRow. Title + subtitle. Trailing: Material3 Switch.

- **`SettingsCardDivider`**: `HorizontalDivider(color = settingsColors.borderColor, thickness = 1.dp)` (actually 0.5.dp for a hairline, or 1.dp to match border-b).

- **`SettingsCard`**: A `Surface` with `cardBackground`, `RoundedCornerShape(16.dp)`, no elevation, clips content.

**Exact composable signatures:**

```kotlin
@Composable
fun SettingsSection(
    title: String,
    colors: SettingsColors = LocalSettingsColors.current,
    content: @Composable ColumnScope.() -> Unit,
)

@Composable
fun SettingsCard(
    colors: SettingsColors = LocalSettingsColors.current,
    content: @Composable ColumnScope.() -> Unit,
)

@Composable
fun SettingsDropdownRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colors: SettingsColors = LocalSettingsColors.current,
    onClick: () -> Unit,
    showDivider: Boolean = true,
)

@Composable
fun SettingsActionRow(
    title: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    colors: SettingsColors = LocalSettingsColors.current,
    onClick: () -> Unit,
    showDivider: Boolean = true,
)

@Composable
fun SettingsInfoRow(
    title: String,
    value: String? = null,
    showChevron: Boolean = false,
    colors: SettingsColors = LocalSettingsColors.current,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
)

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: SettingsColors = LocalSettingsColors.current,
    showDivider: Boolean = true,
)

@Composable
fun IconContainer(
    icon: ImageVector,
    tint: Color,
    showBackground: Boolean = true,
    backgroundColor: Color = LocalSettingsColors.current.iconContainerBg,
)
```

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsDropdownItem.kt`
**Complete rewrite.** The generic `<T>` dropdown composable now uses the new visual style internally:

```kotlin
@Composable
fun <T> SettingsDropdownItem(
    title: String,
    icon: ImageVector,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
)
```

Internally uses `SettingsDropdownRow` visual layout (48dp icon container, 72dp min height, expand_more trailing icon) plus a `DropdownMenu` for selection.

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsScreen.kt`
**Major restructure:**

- Remove `HorizontalDivider` separators between sections
- Add vertical spacing between sections (`Spacer(32.dp)` matching `space-y-8` = 32px)
- Wrap LazyColumn content with `CompositionLocalProvider(LocalSettingsColors provides settingsColors(darkTheme))`. Get `darkTheme` from `isSystemInDarkTheme()` + themeMode check, or pass through from MaterialTheme.
- Actually, simpler: use `MaterialTheme.colorScheme.surface` to detect dark mode since the theme is already applied above. Or use `isSystemInDarkTheme()`. But the theme may be forced. **Decision**: Use `LocalSettingsColors provides` at the `SettingsScreen` level. Detect dark mode via checking if `MaterialTheme.colorScheme.surface` luminance is < 0.5, or simply pass `isSystemInDarkTheme()`. Since the app already applies the correct theme in `App.kt`, using `isSystemInDarkTheme()` inside an already-themed screen could be wrong if user forced Light/Dark. **Better**: Compare `MaterialTheme.colorScheme.background` to `BackgroundDark`. **Simplest**: Add a `isDarkTheme` parameter or use the actual theme state. Since `SettingsScreen` has access to `viewModel` and `state.themeMode`, derive it from there:
  ```kotlin
  val isDark = when (state.themeMode) {
      ThemeMode.System -> isSystemInDarkTheme()
      ThemeMode.Light -> false
      ThemeMode.Dark -> true
  }
  ```

- Restructure into 5 sections: `AppearanceSection`, `PlaybackSection`, `AppBehaviorSection`, `DataStorageSection`, `AboutSection`

- **AppearanceSection**: Wrap in `SettingsSection("APPEARANCE") { SettingsCard { ... } }`. Three `SettingsDropdownItem` rows: Theme (icon=Contrast), Accent Color (icon=Palette), Channel View (icon=ViewList). Dividers between rows 1-2 and 2-3, no divider after row 3.

- **PlaybackSection**: `SettingsSection("PLAYBACK") { SettingsCard { ... } }`. Two rows: Default Player (icon=PlayCircle, disabled/stub), Preferred Quality (icon=Hd, stub). Divider between them.

- **AppBehaviorSection**: `SettingsSection("APP BEHAVIOR") { SettingsCard { ... } }`. Three rows: Default Playlist (icon=PlaylistPlay, stub dropdown), Language (icon=Language, stub dropdown), Auto Update Playlists (icon=Update, switch). Dividers between rows.

- **DataStorageSection**: `SettingsSection("DATA & STORAGE") { SettingsCard { ... } }`. Three action rows: Clear Cache (icon=CleaningServices -- need to use material extended or a custom approach), Clear Favorites (icon=Favorite), Reset to Defaults (icon=RestartAlt, isDestructive=true). Dividers between rows.

- **AboutSection**: `SettingsSection("ABOUT") { SettingsCard { ... } }`. Three info rows: App Version (value=state.appVersion), Contact Support (showChevron=true), Privacy Policy (showChevron=true). Dividers between rows.

**Material Icons note**: The Stitch uses Material Symbols names. Need to map to available Compose Material Icons:
  - `contrast` -> `Icons.Outlined.Contrast` (already used)
  - `palette` -> `Icons.Outlined.Palette` (already used)
  - `view_list` -> `Icons.AutoMirrored.Outlined.ViewList` (already used)
  - `play_circle` -> `Icons.Outlined.PlayCircle` (available in material-icons-extended)
  - `hd` -> `Icons.Outlined.Hd` (already used)
  - `playlist_play` -> `Icons.AutoMirrored.Outlined.PlaylistPlay` (already used)
  - `language` -> `Icons.Outlined.Language` (available)
  - `update` -> `Icons.Outlined.Update` (already used)
  - `cleaning_services` -> `Icons.Outlined.CleaningServices` (available in material-icons-extended)
  - `favorite` -> `Icons.Outlined.Favorite` (available)
  - `restart_alt` -> `Icons.Outlined.RestartAlt` (available in material-icons-extended)
  - `expand_more` -> `Icons.Default.KeyboardArrowDown` (visual equivalent)
  - `chevron_right` -> `Icons.AutoMirrored.Filled.KeyboardArrowRight` (visual equivalent, already used)

**Check**: Verify `material-icons-extended` is already a dependency.

#### 5. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`
- Add named color constants (for documentation/reference, not used in Material3 scheme directly):
```kotlin
val CardDark = Color(0xFF18262d)
val CardLight = Color(0xFFffffff)
val BorderDark = Color(0xFF374151)
val BorderLight = Color(0xFFe5e7eb)
val TextDarkSecondary = Color(0xFF9ca3af)
val TextLightSecondary = Color(0xFF6b7280)
val TextDarkPrimary = Color(0xFFe5e7eb)
val TextLightPrimary = Color(0xFF1f2937)
val Destructive = Color(0xFFef4444)
```

- Update `surfaceContainer` in `AppDarkColorScheme` from `Color(0xFF1a2830)` to `Color(0xFF18262d)` to match Stitch card-dark. This also fixes `ChannelCard` and `ContinueWatchingCard` which use `surfaceContainer`.

- Add `surfaceContainer` to ALL `accentColorScheme()` dark variants (Teal, Blue, Red) since it is currently missing. Set to `Color(0xFF18262d)`.

### No Database Changes

### No DI Changes

## Implementation Order

1. **Add color constants to `Color.kt`** -- Add `CardDark`, `CardLight`, `BorderDark`, `BorderLight`, `TextDarkSecondary`, `TextLightSecondary`, `TextDarkPrimary`, `TextLightPrimary`, `Destructive`. Update `surfaceContainer` in `AppDarkColorScheme` to `#18262d`. Add `surfaceContainer = Color(0xFF18262d)` to all three dark accent color schemes in `accentColorScheme()`.
   - File: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`

2. **Create `SettingsColors.kt`** -- New file with `SettingsColors` data class, `LocalSettingsColors` CompositionLocal, dark/light instances, and `settingsColors()` helper.
   - File: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/SettingsColors.kt`

3. **Rewrite `SettingsComponents.kt`** -- Replace all existing composables with `SettingsSection`, `SettingsCard`, `SettingsDropdownRow`, `SettingsActionRow`, `SettingsInfoRow`, `SettingsSwitchRow`, `IconContainer`.
   - File: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsComponents.kt`

4. **Rewrite `SettingsDropdownItem.kt`** -- Update the generic `<T>` composable to use new visual layout with icon container, min height 72dp, expand_more icon, dropdown menu.
   - File: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsDropdownItem.kt`

5. **Rewrite `SettingsScreen.kt`** -- Restructure into 5 Stitch sections, provide `LocalSettingsColors`, remove `HorizontalDivider` separators, add `Spacer(32.dp)` between sections, update icon references.
   - File: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsScreen.kt`

6. **Build and verify** -- Run `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug` to verify compilation.
   - Note: `compose.materialIconsExtended` is already included (`shared/build.gradle.kts` line 84), so all required icons are available.

## Detailed Composable Specifications

### `SettingsSection` Layout
```
Column(fillMaxWidth, padding(horizontal=16.dp)) {
    // Section header
    Text(
        text = title,  // already uppercase from caller e.g. "APPEARANCE"
        style = labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,  // tracking-wider
        ),
        color = settingsColors.sectionHeaderColor,
        modifier = padding(start=16.dp, bottom=12.dp, top=0.dp),
    )
    // Card container
    SettingsCard { content() }
}
```

### `SettingsCard` Layout
```
Surface(
    shape = RoundedCornerShape(16.dp),
    color = settingsColors.cardBackground,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
) {
    Column { content() }
}
```

### `SettingsDropdownRow` (inside SettingsDropdownItem) Layout
```
Row(
    modifier = fillMaxWidth()
        .clickable(onClick)
        .padding(horizontal=16.dp)
        .defaultMinSize(minHeight=72.dp),
    verticalAlignment = CenterVertically,
) {
    // Icon container: 48x48 Box, rounded-lg (12dp), bg-primary/20
    IconContainer(icon, tint=iconTint, showBackground=true)
    Spacer(width=16.dp)
    // Text column
    Column(Modifier.weight(1f)) {
        Text(title, bodyLarge.copy(fontWeight=Medium), color=textPrimary)
        Text(subtitle, bodySmall, color=textSecondary)
    }
    // Trailing
    Icon(KeyboardArrowDown, tint=textSecondary, size=24.dp)
}
if (showDivider) {
    HorizontalDivider(
        modifier = Modifier.padding(start=80.dp),  // 16 + 48 + 16 = 80 indent
        color = borderColor,
        thickness = 0.5.dp,
    )
}
```

### `SettingsActionRow` Layout
```
Row(
    modifier = fillMaxWidth()
        .clickable(onClick)
        .padding(horizontal=16.dp)
        .defaultMinSize(minHeight=60.dp),
    verticalAlignment = CenterVertically,
) {
    // Icon container: 48x48 Box, NO background, just centered icon
    IconContainer(icon, tint = if(isDestructive) destructive else iconTint, showBackground=false)
    Spacer(width=16.dp)
    Text(title, bodyLarge.copy(fontWeight=Medium),
         color = if(isDestructive) destructive else textPrimary,
         modifier = Modifier.weight(1f))
    Icon(KeyboardArrowRight, tint=textSecondary, size=24.dp)
}
if (showDivider) {
    HorizontalDivider(
        modifier = Modifier.padding(start=80.dp),
        color = borderColor,
        thickness = 0.5.dp,
    )
}
```

### `SettingsInfoRow` Layout
```
Row(
    modifier = fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick=onClick) else Modifier)
        .padding(horizontal=16.dp)
        .defaultMinSize(minHeight=60.dp),
    verticalAlignment = CenterVertically,
) {
    // No icon
    Text(title, bodyLarge.copy(fontWeight=Medium), color=textPrimary, modifier=Modifier.weight(1f))
    if (value != null) {
        Text(value, bodySmall, color=textSecondary)
    }
    if (showChevron) {
        Icon(KeyboardArrowRight, tint=textSecondary, size=24.dp)
    }
}
if (showDivider) {
    HorizontalDivider(
        modifier = Modifier.padding(start=16.dp),  // No icon indent for about rows
        color = borderColor,
        thickness = 0.5.dp,
    )
}
```

### `SettingsSwitchRow` Layout
```
Row(
    modifier = fillMaxWidth()
        .clickable { onCheckedChange(!checked) }
        .padding(horizontal=16.dp)
        .defaultMinSize(minHeight=72.dp),
    verticalAlignment = CenterVertically,
) {
    IconContainer(icon, tint=iconTint, showBackground=true)
    Spacer(width=16.dp)
    Column(Modifier.weight(1f)) {
        Text(title, bodyLarge.copy(fontWeight=Medium), color=textPrimary)
        Text(subtitle, bodySmall, color=textSecondary)
    }
    Switch(checked=checked, onCheckedChange=onCheckedChange)
}
if (showDivider) {
    HorizontalDivider(modifier=Modifier.padding(start=80.dp), color=borderColor, thickness=0.5.dp)
}
```

### `IconContainer` Layout
```
Box(
    modifier = Modifier
        .size(48.dp)
        .then(
            if (showBackground)
                Modifier.background(backgroundColor, RoundedCornerShape(12.dp))
            else Modifier
        ),
    contentAlignment = Center,
) {
    Icon(imageVector=icon, tint=tint, modifier=Modifier.size(24.dp))
}
```

## SettingsScreen Section Structure (exact)

```kotlin
// Inside LazyColumn, padding(horizontal=0.dp) -- sections handle their own padding
item { Spacer(Modifier.height(24.dp)) }  // top padding matching py-6

item { AppearanceSection(state, onEvent) }
item { Spacer(Modifier.height(32.dp)) }  // space-y-8 = 32px between sections

item { PlaybackSection() }
item { Spacer(Modifier.height(32.dp)) }

item { AppBehaviorSection(state, onEvent) }
item { Spacer(Modifier.height(32.dp)) }

item { DataStorageSection(onEvent) }
item { Spacer(Modifier.height(32.dp)) }

item { AboutSection(state, onEvent) }
item { Spacer(Modifier.height(32.dp)) }  // bottom padding
```

Each section function wraps content in `SettingsSection(title) { SettingsCard { ... } }`.

### AppearanceSection
```kotlin
SettingsSection("APPEARANCE") {
    SettingsCard {
        SettingsDropdownItem(title="Theme", icon=Icons.Outlined.Contrast,
            options=listOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark),
            selected=state.themeMode, onSelect={onEvent(OnThemeModeChanged(it))},
            label={when(it){System->"System";Light->"Light";Dark->"Dark"}},
            showDivider=true)
        SettingsDropdownItem(title="Accent Color", icon=Icons.Outlined.Palette,
            options=AccentColor.entries.toList(), selected=state.accentColor,
            onSelect={onEvent(OnAccentColorChanged(it))}, label={it.name},
            showDivider=true)
        SettingsDropdownItem(title="Channel View", icon=Icons.AutoMirrored.Outlined.ViewList,
            options=ChannelViewMode.entries.toList(), selected=state.channelViewMode,
            onSelect={onEvent(OnChannelViewModeChanged(it))}, label={it.name},
            showDivider=false)
    }
}
```

### PlaybackSection
```kotlin
SettingsSection("PLAYBACK") {
    SettingsCard {
        SettingsDropdownItem(title="Default Player", icon=Icons.Outlined.PlayCircle,
            options=listOf("ExoPlayer"), selected="ExoPlayer", onSelect={},
            label={it}, showDivider=true)
        SettingsDropdownItem(title="Preferred Quality", icon=Icons.Outlined.Hd,
            options=listOf("Auto"), selected="Auto", onSelect={},
            label={it}, showDivider=false)
    }
}
```

### AppBehaviorSection
```kotlin
SettingsSection("APP BEHAVIOR") {
    SettingsCard {
        SettingsDropdownItem(title="Default Playlist", icon=Icons.AutoMirrored.Outlined.PlaylistPlay,
            options=listOf("My Main Playlist"), selected="My Main Playlist",
            onSelect={}, label={it}, showDivider=true)
        SettingsDropdownItem(title="Language", icon=Icons.Outlined.Language,
            options=listOf("English"), selected="English",
            onSelect={}, label={it}, showDivider=true)
        SettingsSwitchRow(title="Auto Update Playlists", subtitle="Daily",
            icon=Icons.Outlined.Update, checked=state.autoUpdateEnabled,
            onCheckedChange={onEvent(OnAutoUpdateChanged(it))}, showDivider=false)
    }
}
```

### DataStorageSection
```kotlin
SettingsSection("DATA & STORAGE") {
    SettingsCard {
        SettingsActionRow(title="Clear Cache", icon=Icons.Outlined.CleaningServices,
            onClick={onEvent(OnClearCacheClick)}, showDivider=true)
        SettingsActionRow(title="Clear Favorites", icon=Icons.Outlined.Favorite,
            onClick={onEvent(OnClearFavoritesClick)}, showDivider=true)
        SettingsActionRow(title="Reset to Defaults", icon=Icons.Outlined.RestartAlt,
            isDestructive=true, onClick={onEvent(OnResetClick)}, showDivider=false)
    }
}
```

### AboutSection
```kotlin
SettingsSection("ABOUT") {
    SettingsCard {
        SettingsInfoRow(title="App Version", value=state.appVersion, showDivider=true)
        SettingsInfoRow(title="Contact Support", showChevron=true,
            onClick={onEvent(OnContactSupportClick)}, showDivider=true)
        SettingsInfoRow(title="Privacy Policy", showChevron=true,
            onClick={onEvent(OnPrivacyPolicyClick)}, showDivider=false)
    }
}
```

## Testing Strategy

This is a purely visual/UI change with no business logic modifications. Testing focuses on:

1. **Compilation test**: `./gradlew :androidApp:assembleDebug` must succeed
2. **Existing unit tests**: `./gradlew :shared:testAndroidHostTest` must pass (no behavior changes)
3. **Manual visual verification**: Compare running app against Stitch screenshots
4. **Theme switching test**: Manually verify Settings screen renders correctly in System, Light, and Dark modes
5. **Edge cases**:
   - Long section titles (should not overflow)
   - Dropdown menus still function correctly after visual overhaul
   - Switch toggle still works for Auto Update
   - Dialog confirmations for Clear Favorites and Reset still appear

No new automated tests needed since this is a visual-only change with no logic modifications.

## Doc Updates Required

After implementation:
- Update `docs/constraints/current-limitations.md` -- remove any mention of "Settings screen not matching design" if present
- Update `docs/features/settings.md` -- add note about Stitch-matched visual implementation (if this doc exists)

## Build & Test Commands

```bash
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
```

## Appendix: Icon Dependency Check

Need to verify `material-icons-extended` is available. The following icons from the Stitch design require it:
- `Icons.Outlined.PlayCircle`
- `Icons.Outlined.CleaningServices`
- `Icons.Outlined.RestartAlt`
- `Icons.Outlined.Language`
- `Icons.Outlined.Favorite`

If not available, these can be replaced with similar icons from the base material-icons set or custom vector drawables.

## Appendix: Color Mapping Reference

| Stitch Token | Hex | Usage |
|---|---|---|
| `background-dark` | `#101c22` | Screen background (dark) |
| `background-light` | `#f6f7f8` | Screen background (light) |
| `card-dark` | `#18262d` | Card containers (dark) |
| `card-light` | `#ffffff` | Card containers (light) |
| `border-dark` | `#374151` | Row dividers (dark) |
| `border-light` | `#e5e7eb` | Row dividers (light) |
| `text-dark-primary` | `#e5e7eb` | Titles (dark) |
| `text-dark-secondary` | `#9ca3af` | Subtitles, section headers (dark) |
| `text-light-primary` | `#1f2937` | Titles (light) |
| `text-light-secondary` | `#6b7280` | Subtitles, section headers (light) |
| `primary` | `#2badee` | Icon tint, accent elements |
| `primary/20` | `#2badee` @ 20% | Icon container background |
| `red-500` | `#ef4444` | Reset to Defaults (destructive) |

## Appendix: Dimension Reference

| Element | Size |
|---|---|
| Card corner radius | 16.dp |
| Icon container size | 48x48.dp |
| Icon container corner radius | 12.dp |
| Icon size (inside container) | 24.dp |
| Dropdown row min height | 72.dp |
| Action row min height | 60.dp |
| Info row min height | 60.dp |
| Row horizontal padding | 16.dp |
| Gap between icon and text | 16.dp |
| Section spacing | 32.dp |
| Divider indent (with icon) | 80.dp (16+48+16) |
| Divider indent (no icon) | 16.dp |
| Divider thickness | 0.5.dp |
| Section header left padding | 16.dp (additional, within section's 16.dp = total 32.dp from screen edge) |
