# Design Sync -- Align All Screens with Stitch Design

## Summary

Update all screen UI code to match the canonical Stitch designs. This is a visual-only change -- all existing logic, MVI state/events/actions, ViewModels, and navigation remain untouched. The work covers: (1) adding the Inter font family to Typography, (2) overhauling Settings screen with icons and missing sections, (3) creating reusable `ChannelCard` composables shared across Home, Favorites, and Channels, (4) improving filter/dropdown visuals on Channels screen (keeping existing logic), (5) updating Favorites to grid layout, (6) improving Continue Watching card styling (no progress bar), and (7) standardizing 8dp corner radius and dark surface colors across all screens.

## USER CONSTRAINTS (MUST FOLLOW)
- **ContinueWatching**: NO progress bar. Just improve card visual styling.
- **Channels GroupDropdown**: Keep existing dropdown logic and component. Only improve visual appearance.
- **Bottom navigation**: DO NOT TOUCH MainScreen.kt at all. Current labels/icons are correct.
- **Settings**: Main focus. Icons on every row + missing Playback section + visual overhaul.

## Decisions Made

### Decision 1: Inter font via Compose Resources
- **Decision**: Bundle Inter font files (Regular, Medium, SemiBold, Bold) as Compose Resources and reference them in `Typography.kt` via `FontFamily` with `Font(Res.font.inter_regular)` etc.
- **Rationale**: Stitch design specifies Inter. Compose Multiplatform supports custom fonts via `composeResources/font/` directory. No platform-specific code needed.
- **Alternatives considered**: System default font (current state, does not match design), Google Fonts API (not available in KMP).

### Decision 2: Material Symbols via material-icons-extended
- **Decision**: Use `compose.materialIconsExtended` (already in dependencies) for Settings screen icons. Map Stitch icon names to their Material Icons equivalents: `palette` -> `Icons.Outlined.Palette`, `brush` -> `Icons.Outlined.Brush`, `view_list` -> `Icons.AutoMirrored.Outlined.ViewList`, `play_circle` -> `Icons.Outlined.PlayCircle`, `high_quality` -> `Icons.Outlined.HighQuality`, `playlist_play` -> `Icons.AutoMirrored.Outlined.PlaylistPlay`, `language` -> `Icons.Outlined.Language`, `sync` -> `Icons.Outlined.Sync`, `delete` -> `Icons.Outlined.Delete`, `star_border` -> `Icons.Outlined.StarBorder`, `restart_alt` -> `Icons.Outlined.RestartAlt`, `info` -> `Icons.Outlined.Info`, `mail` -> `Icons.Outlined.Mail`, `policy` -> `Icons.Outlined.Policy`.
- **Rationale**: The `materialIconsExtended` dependency is already present in `build.gradle.kts` line 84. No new dependency needed.
- **Alternatives considered**: Custom SVG icons (unnecessary complexity when Material Icons match).

### Decision 3: Filter chips as horizontal scrolling FilterChip row
- **Decision**: Create a reusable `FilterChipRow` composable using Material 3 `FilterChip`. For Channels screen, chips are derived from existing `groups` state. For Favorites screen, chips are derived from channel groups present in favorites.
- **Rationale**: Stitch shows filter chips (All, Sports, News, etc.) on both Favorites and Channels screens. The groups data already exists in state. Using Material 3 `FilterChip` matches the design language.
- **Alternatives considered**: `ScrollableTabRow` (heavier, tab semantics are wrong for filtering).

### Decision 4: ChannelCard as a shared component
- **Decision**: Create `common/components/ChannelCard.kt` with two variants: `ChannelCardSquare` (grid card with thumbnail background, star overlay, channel name) and `ChannelCardList` (row item with logo, name, subtitle, star toggle). Both use 8dp corner radius.
- **Rationale**: Three screens (Home, Favorites, Channels) each have their own card implementations with slight inconsistencies. Centralizing eliminates drift and matches Stitch's consistent card style.
- **Alternatives considered**: Keep per-screen card implementations (causes the design drift the user reported).

### Decision 5: Continue Watching card -- visual polish only, NO progress bar
- **Decision**: Update the Continue Watching card to use 16:9 aspect ratio (`aspectRatio(16f/9f)`) with better visual styling (rounded corners, proper image scaling, gradient overlay for readability). NO progress bar. Show channel name below image.
- **Rationale**: User explicitly said "прогресс не нужен". Just improve card appearance to match Stitch's visual quality.
- **Alternatives considered**: Adding progress bar (rejected by user).

### Decision 6: Dark surface container colors for cards
- **Decision**: Add `SurfaceContainer` and `SurfaceContainerHigh` colors to the dark theme. Use `Color(0xFF1a2830)` for card backgrounds in dark mode (slightly lighter than `#101c22` background). Map this to `surfaceContainer` in the color scheme.
- **Rationale**: Stitch uses visually distinct card backgrounds against the dark bg. Material 3 `surfaceContainer` role is designed for this purpose.
- **Alternatives considered**: Using `surfaceVariant` (already used but mapped to `Slate800 = #1e293b` which is too blue/grey for the teal-tinted dark theme).

### Decision 7: Bottom navigation -- DO NOT CHANGE
- **Decision**: Do not touch MainScreen.kt or bottom navigation at all. Current labels and icons are correct as-is.
- **Rationale**: User explicitly said "навигацию не трогать".

### Decision 8: Settings screen restructured with Playback section
- **Decision**: Add a "Playback" section to Settings with "Default Player" and "Preferred Quality" rows (non-functional for now, UI-only with placeholder subtitles). Add missing icons to all rows. Add red text color to "Reset to Defaults" item.
- **Rationale**: Stitch design shows Playback section. Adding it UI-only keeps the plan scoped to visuals while matching the design. Logic can be added later.
- **Alternatives considered**: Skip Playback section (would not match Stitch).

### Decision 9: Favorites screen uses grid layout matching Stitch
- **Decision**: Change Favorites from a list layout to a 2-column grid of square cards with thumbnail background, filled star overlay, and channel name + category label. Add filter chips below the header.
- **Rationale**: Stitch Favorites shows a grid of square cards, not a list. Current implementation uses a list.
- **Alternatives considered**: Keep list layout (does not match Stitch design).

### Decision 10: Channels screen -- keep existing filter logic, improve visuals only
- **Decision**: Keep `GroupDropdown` and `PlaylistDropdown` as-is functionally. Only improve their visual appearance (colors, spacing, corner radius). Do NOT replace with filter chips.
- **Rationale**: User explicitly said "логику фильтров оставить но отображение улучшить".
- **Alternatives considered**: Replace with filter chips (rejected by user).

## Current State

### Design System
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt` -- Colors correctly define `Primary = Color(0xFF2badee)`, `BackgroundDark = Color(0xFF101c22)`, `BackgroundLight = Color(0xFFf6f7f8)`. These match Stitch.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Typography.kt` -- Uses `FontFamily.Default` everywhere. No Inter font loaded.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Theme.kt` -- Correctly wires colors and typography.
- No font files exist in `shared/src/commonMain/composeResources/font/`.

### Settings Screen
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsScreen.kt` -- No icons on any settings row. Missing "Playback" section. "Reset to Defaults" has no red text.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsComponents.kt` -- `SettingsItem` has no `icon` parameter. `SettingsSection` is plain text header.

### Home Screen
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt` -- `ContinueWatchingItem` card at line 473-494 has no progress bar, uses fixed `200.dp` width with no aspect ratio. `ChannelItem` at line 497-518 is a simple card with no star overlay or LIVE badge.
- Top bar uses `Icons.Default.Settings` for playlist settings (Stitch shows `expand_more` dropdown).

### Favorites Screen
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesScreen.kt` -- Uses list layout (`LazyColumn`) with `ChannelListItem`. No filter chips. No grid layout. Star icon uses hardcoded `Color.Cyan` (line 242).

### Channels Screen
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt` -- Uses `GroupDropdown` and `PlaylistDropdown` as OutlinedCard dropdowns instead of filter chips. `ChannelGridItem` at line 273-336 has no 8dp corners. Star color is hardcoded `Color(0xFFFFD700)`.

### Bottom Navigation
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/main/MainScreen.kt` -- Tab labeled "Channels" (should be "All Channels" per Stitch). Icons use `Icons.Default.*` (filled variants). NavigationBar uses default Material 3 styling.

### Shared Components
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/` -- Contains `PlaylistDropdown.kt`, `GroupDropdown.kt`, `DropdownSelector.kt`, `SearchComponents.kt`. No `ChannelCard` or `FilterChipRow` component.

## Changes Required

### New Files

#### 1. Font resource files
- Path: `shared/src/commonMain/composeResources/font/inter_regular.ttf`
- Path: `shared/src/commonMain/composeResources/font/inter_medium.ttf`
- Path: `shared/src/commonMain/composeResources/font/inter_semibold.ttf`
- Path: `shared/src/commonMain/composeResources/font/inter_bold.ttf`
- Purpose: Inter font family for Stitch design compliance
- Key contents: Binary TTF files downloaded from Google Fonts

#### 2. ChannelCard shared component
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ChannelCard.kt`
- Purpose: Reusable channel card composables used across Home, Favorites, and Channels screens
- Key contents:
  ```kotlin
  @Composable
  fun ChannelCardSquare(
      channelName: String,
      logoUrl: String?,
      isFavorite: Boolean,
      onFavoriteToggle: (() -> Unit)? = null,
      onClick: () -> Unit,
      modifier: Modifier = Modifier,
      categoryLabel: String? = null,
      showLiveBadge: Boolean = false,
  )

  @Composable
  fun ChannelCardList(
      channelName: String,
      logoUrl: String?,
      isFavorite: Boolean,
      onFavoriteToggle: () -> Unit,
      onClick: () -> Unit,
      modifier: Modifier = Modifier,
      subtitle: String? = null,
  )
  ```
  - Square card: 1:1 aspect ratio, AsyncImage background fill, 8dp RoundedCornerShape, semi-transparent gradient at bottom for text, star icon top-end, channel name bottom-start, optional LIVE badge, optional category label
  - List card: Same as current `ChannelListItem` but with consistent 8dp corners, primary-colored star icon, and proper theme colors

#### 3. FilterChipRow shared component
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/FilterChipRow.kt`
- Purpose: Horizontally scrolling filter chip row for Favorites and Channels screens
- Key contents:
  ```kotlin
  @Composable
  fun <T> FilterChipRow(
      items: List<T>,
      selectedItem: T?,
      onItemSelected: (T?) -> Unit,
      label: (T) -> String,
      modifier: Modifier = Modifier,
      allLabel: String = "All",
  )
  ```
  - Uses `LazyRow` with `FilterChip` from Material 3
  - First chip is always "All" (selected when `selectedItem == null`)
  - 8dp chip shape, primary color when selected

#### 4. ContinueWatchingCard component
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ContinueWatchingCard.kt`
- Purpose: Improved Continue Watching card for Home screen (NO progress bar)
- Key contents:
  ```kotlin
  @Composable
  fun ContinueWatchingCard(
      channelName: String,
      logoUrl: String?,
      onClick: () -> Unit,
      modifier: Modifier = Modifier,
  )
  ```
  - 16:9 aspect ratio card (200.dp width)
  - AsyncImage background with gradient scrim overlay at bottom
  - 8dp rounded corners
  - Channel name below image
  - NO progress bar, NO timeLeft

### Modified Files

#### 1. Typography.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Typography.kt`
- What changes: Replace all `FontFamily.Default` references with a custom `InterFontFamily` built from Compose Resources:
  ```kotlin
  @Composable
  fun InterFontFamily(): FontFamily = FontFamily(
      Font(Res.font.inter_regular, FontWeight.Normal),
      Font(Res.font.inter_medium, FontWeight.Medium),
      Font(Res.font.inter_semibold, FontWeight.SemiBold),
      Font(Res.font.inter_bold, FontWeight.Bold),
  )
  ```
  Since Compose Multiplatform resource fonts require `@Composable` context, convert `AppTypography` from a top-level `val` to a `@Composable` function `AppTypography()` that returns `Typography`. Update `Theme.kt` to call it within the composable scope.
- Why: Stitch specifies Inter font.

#### 2. Theme.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Theme.kt`
- What changes: Call `AppTypography()` composable function instead of referencing the `AppTypography` val.
  ```kotlin
  MaterialTheme(
      colorScheme = colorScheme,
      typography = AppTypography(),
      content = content,
  )
  ```
- Why: Support the composable font family.

#### 3. Color.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`
- What changes: Add `surfaceContainer` and `surfaceContainerHigh` assignments to both dark and light color schemes. For dark: `surfaceContainer = Color(0xFF1a2830)`, `surfaceContainerHigh = Color(0xFF213038)`. For light: use default Material 3 values. Add these to all accent color scheme variants too.
- Why: Cards need a distinct background from the main surface in both themes.

#### 4. SettingsComponents.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsComponents.kt`
- What changes:
  - Add `icon: ImageVector? = null` parameter to `SettingsItem`. Render icon with `Icon(icon, ...)` at the start of the row when non-null, followed by 16dp spacer.
  - Add `icon: ImageVector? = null` parameter to `SettingsSwitchItem`. Same icon rendering.
  - Add optional `titleColor: Color = MaterialTheme.colorScheme.onSurface` parameter to `SettingsItem` for the red "Reset to Defaults" text.
- Why: Stitch Settings shows an icon on every row. "Reset to Defaults" uses red text.

#### 5. SettingsScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/mvi/SettingsMvi.kt`
- What changes: No changes needed to MVI. State is sufficient.

- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsScreen.kt`
- What changes:
  - Add icon parameter to every `SettingsItem` call
  - Add "Playback" section between Appearance and App Behavior with two placeholder items:
    - "Default Player" (icon: `Icons.Outlined.PlayCircle`, subtitle: "Built-in")
    - "Preferred Quality" (icon: `Icons.Outlined.HighQuality`, subtitle: "Auto")
  - Appearance section: add icons to Theme (`Icons.Outlined.Palette`), Accent Color (`Icons.Outlined.Brush`), Channel View (`Icons.AutoMirrored.Outlined.ViewList`)
  - App Behavior section: Language icon (`Icons.Outlined.Language`), Auto Update icon (`Icons.Outlined.Sync`)
  - Data & Storage section: Clear Cache (`Icons.Outlined.Delete`), Clear Favorites (`Icons.Outlined.StarBorder`), Reset to Defaults (`Icons.Outlined.RestartAlt`, titleColor = `MaterialTheme.colorScheme.error`)
  - About section: Version (`Icons.Outlined.Info`), Contact Support (`Icons.Outlined.Mail`), Privacy Policy (`Icons.Outlined.Policy`)
- Why: Stitch design has icons on every settings row and includes a Playback section.

#### 6. HomeScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`
- What changes:
  - Replace inline `ContinueWatchingItem` composable (line 473-494) with the new `ContinueWatchingCard` component
  - Replace inline `ChannelItem` composable (line 497-518) with `ChannelCardSquare` from shared components (no favorite toggle on home, just click to play)
  - For Favorites section cards: add `showLiveBadge = true` on `ChannelCardSquare` to show LIVE badge overlay
  - Update `HomeTopAppBar` to remove the Settings gear icon from actions (Stitch shows only search icon; playlist settings via dropdown)
- Why: Match Stitch Home design with progress bar cards, square channel cards, and LIVE badges.

#### 7. FavoritesScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesScreen.kt`
- What changes:
  - Remove `PlaylistDropdown` from the body (Stitch Favorites has no playlist dropdown, just filter chips)
  - Add `FilterChipRow` below the top bar using group data (requires adding groups to FavoritesState -- see MVI change below)
  - Replace `ChannelsList` (LazyColumn) with a `LazyVerticalGrid(columns = GridCells.Fixed(2))` using `ChannelCardSquare` with filled star overlay
  - Replace hardcoded `Color.Cyan` star with `MaterialTheme.colorScheme.primary`
  - Remove old `ChannelListItem` composable
- Why: Stitch Favorites shows a grid with filter chips, not a list with playlist dropdown.

#### 8. FavoritesMvi.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/mvi/FavoritesMvi.kt`
- What changes:
  - Add `groups: List<ChannelGroup> = emptyList()` to `FavoritesState`
  - Add `selectedGroup: ChannelGroup? = null` to `FavoritesState`
  - Add `data class OnGroupSelected(val group: ChannelGroup?) : FavoritesEvent`
- Why: Filter chips need group data and selection state.

#### 9. FavoritesViewModel.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesViewModel.kt`
- What changes:
  - Load groups from `ChannelRepository.getAllGroups()` and populate `state.groups`
  - Handle `OnGroupSelected` event to filter favorites by group
  - Filter the favorites list by selected group when non-null
- Why: Support filter chip functionality.

#### 10. ChannelsScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt`
- What changes:
  - **Keep `GroupDropdown` and `PlaylistDropdown` as-is** (user requirement: keep filter logic, only improve visuals)
  - Replace inline `ChannelGridItem` (line 273-336) with `ChannelCardSquare` from shared components
  - Replace inline `ChannelListItem` (line 338-415) with `ChannelCardList` from shared components
  - Replace hardcoded `Color(0xFFFFD700)` star color with `MaterialTheme.colorScheme.primary`
  - Use consistent 8dp corner radius on all cards
  - Improve dropdown visual appearance (better colors, spacing)
- Why: Match Stitch card design while preserving existing filter logic.

#### 11. MainScreen.kt -- DO NOT MODIFY
- User constraint: "навигацию не трогать". Skip this file entirely.

#### 12. OnboardingScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/OnboardingScreen.kt`
- What changes:
  - Minor: Ensure `RoundedCornerShape(8.dp)` on the "Import playlist" button and "Choose file" button (Stitch shows 8dp roundness)
  - Ensure the text field uses 8dp corner radius on the OutlinedTextField shape
- Why: Stitch specifies 8dp roundness globally.

### Database Changes
None.

### DI Changes
None. All changes are UI-only. The `FavoritesViewModel` already has access to `ChannelRepository` which provides `getAllGroups()`.

## Implementation Order

1. **Download and add Inter font files** to `shared/src/commonMain/composeResources/font/`. Download from Google Fonts (Inter Regular 400, Medium 500, SemiBold 600, Bold 700).

2. **Update Typography.kt** to use Inter font family via Compose Resources. Convert `AppTypography` val to `@Composable fun AppTypography(): Typography`.

3. **Update Theme.kt** to call the composable `AppTypography()`.

4. **Update Color.kt** to add `surfaceContainer` and `surfaceContainerHigh` to all color schemes.

5. **Create `FilterChipRow.kt`** in `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/`.

6. **Create `ChannelCard.kt`** in `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/` with `ChannelCardSquare` and `ChannelCardList`.

7. **Create `ContinueWatchingCard.kt`** in `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/`.

8. **Update SettingsComponents.kt** to add icon and titleColor parameters.

9. **Update SettingsScreen.kt** to add icons to all rows, add Playback section, add red text to Reset.

10. **Update MainScreen.kt** to rename "Channels" tab label.

11. **Update HomeScreen.kt** to use `ContinueWatchingCard` and `ChannelCardSquare`, remove inline card composables.

12. **Update FavoritesMvi.kt** to add groups and selectedGroup to state.

13. **Update FavoritesViewModel.kt** to load groups and handle group filtering.

14. **Update FavoritesScreen.kt** to use grid layout with `ChannelCardSquare` and `FilterChipRow`.

15. **Update ChannelsScreen.kt** to replace `GroupDropdown` with `FilterChipRow` and use shared card components.

16. **Update OnboardingScreen.kt** for 8dp corner radius on buttons and text field.

17. **Run `./gradlew formatAll`** to fix formatting.

18. **Build and verify** with `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`.

## Testing Strategy

### What to test
- **Compilation**: All screens compile without errors after changes.
- **Visual verification**: Install on device/emulator and visually compare each screen against Stitch designs.
- **Font rendering**: Verify Inter font loads on both Android and iOS.
- **Settings icons**: Every settings row has an icon visible.
- **Filter chips**: Tapping chips on Favorites and Channels filters the list.
- **Card consistency**: Channel cards look identical across Home, Favorites, and Channels screens.
- **Dark/Light theme**: Both themes render correctly with proper surface container colors.
- **Accent colors**: All three accent color options (Teal, Blue, Red) still work with the updated cards and chips.

### Edge cases
- Empty favorites with filter chips (should show empty state).
- Very long group names in filter chips (should truncate with ellipsis).
- Channels with no logo URL (card should show a placeholder background).
- Continue Watching with progress = 0.0 and progress = 1.0.

### Key assertions
- No `Color.Cyan` or `Color(0xFFFFD700)` hardcoded colors remain in UI code (use `MaterialTheme.colorScheme.primary` instead).
- All `RoundedCornerShape` values in card components use 8.dp.
- `FontFamily.Default` no longer appears in `Typography.kt`.

### Coroutine test patterns
- FavoritesViewModel group loading: Use `runTest` with `StandardTestDispatcher`. The existing test patterns for ViewModels in the project should be followed.
- No new suspend functions or repositories are added, only new state fields populated from existing repository calls.

## Doc Updates Required

> [!NOTE] Update AFTER implementation

- `docs/constraints/current-limitations.md` -- Add entry: "Settings: Playback section items (Default Player, Preferred Quality) are UI placeholders only. No functional implementation yet."
- `docs/features/settings.md` -- Update to reflect new section structure with Playback section.
- `docs/features/favorites.md` -- Update to reflect grid layout and filter chips.
- `docs/features/channels.md` -- Update to reflect filter chips replacing dropdown.

## Build & Test Commands

```bash
# Format code
./gradlew formatAll

# Run unit tests
./gradlew :shared:testAndroidHostTest

# Build Android app
./gradlew :androidApp:assembleDebug

# Build iOS framework (verify KMP compilation)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
