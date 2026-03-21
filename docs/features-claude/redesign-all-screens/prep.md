# Redesign All Screens -- Implementation Plan

## Summary

Visual redesign of the entire WhiteIPTV KMP app to match a new Figma-based design reference. The redesign updates the design system colors (gradient backgrounds, cyan accent, pink favorites), card styling (rounded-2xl, white/5 opacity backgrounds with white/10 borders), settings screen layout (colored section icon badges, individual card rows), player controls (circular buttons, sliders, pill-shaped action buttons), and all screen headers (backdrop blur, gradient backgrounds). This is a UI-only change; no business logic, navigation, or data layer modifications are needed. The AccentColor.Teal preset will be updated to use the new cyan gradient colors. All other accent colors (Blue, Red) remain functional.

## Decisions Made

### D1: Gradient backgrounds via Box wrapper, not Material3 colorScheme
- **Decision**: Use a `GradientBackground` composable wrapper that draws a vertical gradient behind screen content, rather than trying to encode gradients into Material3's single-color `background` field.
- **Rationale**: Material3 `ColorScheme.background` is a single `Color`, not a `Brush`. Wrapping each screen's content in a `Box` with `Modifier.background(brush)` is the simplest KMP-compatible approach.
- **Alternatives considered**: (a) Custom theme extension to hold gradient brush -- heavier, must thread through composition locals; (b) Setting `background` to the darker gradient endpoint -- loses the gradient effect.

### D2: Rename AccentColor.Teal to AccentColor.Cyan (no)
- **Decision**: Keep `AccentColor.Teal` as-is. Update the Teal dark/light primary color values to `#00d4ff` (from `#2badee`). The user-facing label in settings will change to "Cyan" via the `optionLabel` mapping in SettingsScreen.
- **Rationale**: Minimizes migration impact. The enum value is persisted via DataStore; renaming would break saved preferences.
- **Alternatives considered**: Adding a new `AccentColor.Cyan` and deprecating Teal -- unnecessary complexity for a color update.

### D3: Favorite heart color -- use pink `#ff006e` unconditionally
- **Decision**: Filled favorite hearts use a hardcoded `FavoritePink = Color(0xFFff006e)` regardless of accent color. Unfavorited hearts remain `Color.White.copy(alpha = 0.7f)`.
- **Rationale**: The design reference consistently uses pink for favorites across all screens. This creates a distinct visual language for favorites vs. the primary accent.
- **Alternatives considered**: Using `MaterialTheme.colorScheme.primary` for favorites -- inconsistent with the design reference which explicitly uses pink.

### D4: LIVE badge color -- use cyan `#00d4ff` with black text
- **Decision**: Update `LiveBadge` to use `Color(0xFF00d4ff)` background with `Color.Black` text (matching the reference design) instead of the current red background.
- **Rationale**: Direct match to the Figma reference.
- **Alternatives considered**: Keeping red for LIVE badge -- contradicts the reference design.

### D5: Player LIVE badge -- use pink `#ff006e` with white text
- **Decision**: The player top bar LIVE badge uses pink `#ff006e` per the player reference screenshot, which is different from the channel card LIVE badge (cyan).
- **Rationale**: The player screenshot clearly shows a pink LIVE badge, distinguishing the player overlay from channel grid badges.

### D6: Settings layout -- one card per item, not grouped cards
- **Decision**: Change settings from grouped cards (one SettingsCard per section containing multiple rows) to individual rounded-2xl cards per setting item. Each item is a standalone card with `white/5` bg and `white/10` border (dark) or white bg with `gray-200` border (light).
- **Rationale**: The reference design shows each setting as an individual card with visible spacing between items. This is the primary visual change for settings.

### D7: Settings section headers -- gradient icon badges
- **Decision**: Each settings section header gets a small (32dp) rounded-lg gradient icon badge beside the uppercase section title. Section-specific gradient colors per the reference design.
- **Rationale**: Direct match to the Figma reference which shows colored gradient icon badges for each section.

### D8: Channel name/category below card, not inside gradient overlay
- **Decision**: In the new design reference, channel name and category text appear BELOW the card image (outside the card), not inside the gradient overlay. The gradient overlay on the card itself only contains the LIVE badge and heart button.
- **Rationale**: The reference AllChannels, Favorites, and Home screens all show `<h3 className="mt-2">` and `<p>` elements after the card div, placing text below the image card.

### D9: PlaylistManager screen -- not implementing as a separate tab/screen
- **Decision**: The PlaylistManager design will NOT be implemented as a new MVI screen in this redesign phase. The existing playlist management via HomeScreen bottom sheets and Onboarding for import will remain. The PlaylistManager reference is noted for a future feature.
- **Rationale**: Adding a full new screen (PlaylistManager with navigation, ViewModel, etc.) goes beyond "visual redesign only." The current playlist management flow (dropdown + settings gear in HomeScreen) covers the same functionality. The design reference shows it as a settings sub-screen, which can be added later.

### D10: Home screen categories -- keep current horizontal scroll, not 2-column grid
- **Decision**: Keep the current pattern of horizontal LazyRow category sections on Home rather than switching to the reference's 2-column "Browse by Category" grid.
- **Rationale**: The current implementation shows actual channel cards in each category row (real data), while the reference shows a simple category label + count. The horizontal scroll of actual channels is more functional. The "Browse by Category" grid from the reference is essentially the group filter chips on the Channels screen.

### D11: Continue Watching cards -- update to full-width stacked layout
- **Decision**: Change Continue Watching from a horizontal LazyRow of small 220dp cards to full-width stacked cards (one per row), matching the reference design which shows large `aspect-video` cards.
- **Rationale**: The reference clearly shows full-width continue watching cards with LIVE badge, category, channel name, gradient progress bar, and time remaining text. This is a significant visual improvement.

### D12: Backdrop blur -- use semi-transparent background without platform blur
- **Decision**: Use `background(color.copy(alpha = 0.8f))` for sticky headers instead of actual backdrop blur. Compose Multiplatform does not have a cross-platform `backdrop-filter: blur()` equivalent.
- **Rationale**: True backdrop blur requires platform-specific implementations (`RenderEffect` on Android, which is API 31+, and `UIVisualEffectView` on iOS). A semi-transparent background achieves 90% of the visual effect with zero platform complexity.
- **Alternatives considered**: Using `graphicsLayer { renderEffect = BlurEffect() }` on Android + expect/actual -- too complex for a visual tweak, and not available on older Android versions.

### D13: Settings dropdown value display -- use cyan `#00d4ff` color
- **Decision**: Current setting values (e.g., "Dark", "Cyan", "Grid") displayed as trailing text in settings rows will use `Color(0xFF00d4ff)` (the new cyan accent) rather than `MaterialTheme.colorScheme.primary`.
- **Rationale**: The reference design explicitly uses `text-[#00d4ff]` for all value displays in settings, creating a consistent cyan accent regardless of accent color preference. However, for accent color consistency, we will use `MaterialTheme.colorScheme.primary` so values follow the user's chosen accent.

After further analysis: The design reference hardcodes cyan for values. Since this is a visual redesign to match the reference, we will use `MaterialTheme.colorScheme.primary` which will be cyan for Teal accent (the default). This naturally follows the user's accent choice.

### D14: Home header playlist selector -- styled button instead of dropdown
- **Decision**: Replace the `PlaylistDropdown` in Home header with a styled button (cyan border glow, rounded-xl) that opens the existing `PlaylistSheet`/bottom sheet. Keep the `PlaylistDropdown` component for Channels and Favorites screens.
- **Rationale**: The reference Home screen shows a styled button with `border-[#00d4ff]/30` and `bg-gradient-to-r from-[#00d4ff]/10` that opens a playlist selection sheet, not a dropdown menu. Other screens (Channels, Favorites) in the reference use filter chips instead.

### D15: Favorites filter -- replace PlaylistDropdown with filter chips
- **Decision**: Replace the `PlaylistDropdown` on Favorites screen with horizontal filter chips (similar to Channels screen group chips). "All Playlists" chip uses pink gradient when selected; individual playlist chips use cyan gradient when selected.
- **Rationale**: Direct match to the Figma reference which shows `rounded-xl` pill buttons for playlist filtering on Favorites.

## Current State

### Design System
- **Color.kt** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`): Primary is `#2badee`, Background dark is `#101c22`, Card dark is `#1e2e38`. Has Teal/Blue/Red accent color system.
- **Theme.kt** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Theme.kt`): Standard Material3 theme with accent color support, 31 lines.
- **Typography.kt** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Typography.kt`): Inter font family, standard M3 type scale. No changes needed.

### Components
- **ChannelCard.kt** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ChannelCard.kt`): `ChannelCardSquare` (grid) and `ChannelCardList` (list) composables. Card shape is 16dp, uses `surfaceContainer` color. Favorite heart uses `MaterialTheme.colorScheme.primary` for filled state. `LiveBadge` uses red background.
- **ContinueWatchingCard.kt** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ContinueWatchingCard.kt`): 220dp width, 16:9 aspect ratio, uses primary for badge and progress bar. 119 lines.
- **SectionHeader.kt** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/SectionHeader.kt`): Simple title + optional action. Uses `primary` color and `titleSmall` style.
- **GroupFilterChips.kt** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/GroupFilterChips.kt`): Standard Material3 `FilterChip` components. 75 lines.
- **PlaylistDropdown.kt** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/PlaylistDropdown.kt`): Standard `DropdownMenu` component. 138 lines.
- **SearchComponents.kt** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/SearchComponents.kt`): `SearchTopBar` and `SearchEmptyState`. 110 lines.

### Screens
- **HomeScreen.kt** (537 lines): Uses `PlaylistDropdown` in header, `SectionHeaderWithViewAll`, horizontal `LazyRow` for all sections.
- **ChannelsScreen.kt** (262 lines): Standard `TopAppBar`, `PlaylistDropdown`, `GroupFilterChips`, grid/list toggle.
- **FavoritesScreen.kt** (226 lines): Standard `TopAppBar`, `PlaylistDropdown`, grid/list toggle, empty state.
- **SettingsScreen.kt** (342 lines): Grouped `SettingsCard` containers, `SettingsSectionHeader`, dropdown/switch/action/info rows.
- **SettingsComponents.kt** (368 lines): `SettingsCard` (Surface with surfaceContainer color), `IconContainer`, `SettingsDropdownRow`, `SettingsActionRow`, `SettingsInfoRow`, `SettingsSwitchRow`.
- **PlayerScreen.kt** (362 lines): Full screen with gesture overlay and controls overlay.
- **PlayerControls.kt** (576 lines): `PlayerControlsOverlay`, `PlayerTopBar`, `PlayerBottomBar`, `LabeledControlButton`, `LiveIndicator`, `TrackSelectionDialog`.
- **MainScreen.kt** (158 lines): Bottom navigation with 4 tabs using Material3 `NavigationBar`.

## Changes Required

### Modified Files

#### 1. Color.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`
- What changes:
  - Update `Primary` from `Color(0xFF2badee)` to `Color(0xFF00d4ff)` (new cyan)
  - Update `PrimaryLight` from `Color(0xFF0284C7)` to `Color(0xFF0088ff)` (darker cyan for light theme contrast)
  - Update `BackgroundDark` from `Color(0xFF101c22)` to `Color(0xFF0a0f14)` (darker)
  - Update `BackgroundLight` from `Color(0xFFdfe2e6)` to `Color(0xFFf8f9fa)` (lighter)
  - Update `CardDark` from `Color(0xFF1e2e38)` to `Color(0x0DFFFFFF)` (white/5 opacity)
  - Update `BorderDark` from `Color(0xFF374151)` to `Color(0x1AFFFFFF)` (white/10 opacity)
  - Update `CardLight` from `Color(0xFFebecef)` to `Color.White`
  - Update `BorderLight` from `Color(0xFFc8ccd2)` to `Color(0xFFe5e7eb)` (gray-200)
  - Add new color constants:
    - `val FavoritePink = Color(0xFFff006e)`
    - `val LiveCyan = Color(0xFF00d4ff)`
    - `val CyanGradientStart = Color(0xFF00d4ff)`
    - `val CyanGradientEnd = Color(0xFF0088ff)`
    - `val BackgroundDarkGradientEnd = Color(0xFF0f1419)`
    - `val BackgroundLightGradientEnd = Color(0xFFe9ecef)`
    - `val HeaderDarkBg = Color(0xFF0a0f14)`
    - `val HeaderBorderDark = Color(0xFF1a2026)`
  - Add settings section gradient color pairs:
    - `val SettingsAppearanceGradient = listOf(Color(0xFF8b5cf6), Color(0xFF7c3aed))`
    - `val SettingsPlaybackGradient = listOf(Color(0xFF00d4ff), Color(0xFF0088ff))`
    - `val SettingsAppBehaviorGradient = listOf(Color(0xFF10b981), Color(0xFF059669))`
    - `val SettingsDataStorageGradient = listOf(Color(0xFFf59e0b), Color(0xFFd97706))`
    - `val SettingsPreferencesGradient = listOf(Color(0xFFec4899), Color(0xFFbe185d))`
    - `val SettingsAboutGradient = listOf(Color(0xFF64748b), Color(0xFF475569))`
  - Update `surfaceContainer` in dark scheme from `Color(0xFF1a2830)` to `Color(0x0DFFFFFF)` (white/5 to match card bg)
  - Update `surfaceContainerHigh` in dark scheme from `Color(0xFF213038)` to `Color(0x1AFFFFFF)` (white/10)
  - Update Teal accent colors: `TealDarkPrimaryContainer`, `TealLightPrimaryContainer`, etc. to harmonize with new cyan base
- Why: New design system colors from Figma reference

#### 2. Theme.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Theme.kt`
- What changes: No structural changes needed. The `accentColorScheme()` function already wires colors correctly. The color value changes in Color.kt will flow through automatically.
- Why: Colors are defined in Color.kt; Theme.kt just references them.

#### 3. ChannelCard.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ChannelCard.kt`
- What changes:
  - **ChannelCardSquare**:
    - Add `border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))` to the card for dark theme
    - Change favorite heart filled color from `MaterialTheme.colorScheme.primary` to `FavoritePink`
    - Move channel name and category text BELOW the card (outside the Box with aspect ratio), not inside the gradient overlay
    - Remove the gradient overlay text area inside the card
    - Add channel name Text and category Text after the card Box
    - Update `LiveBadge`: change background from `Color.Red` to `LiveCyan`, text color to `Color.Black`
    - Change badge shape from `RoundedCornerShape(6.dp)` to `RoundedCornerShape(50)` (fully rounded pill)
    - Remove the white dot from LiveBadge (new design has no dot)
  - **ChannelCardList**:
    - Change favorite heart filled color from `MaterialTheme.colorScheme.primary` to `FavoritePink`
  - **LiveBadge**: Rewrite to cyan pill badge
- Why: Match new card styling from reference design

#### 4. ContinueWatchingCard.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ContinueWatchingCard.kt`
- What changes:
  - Change from fixed 220dp width to `fillMaxWidth()`
  - Add border: `border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))`
  - Replace "Continue" badge with cyan LIVE badge (pill shape, `LiveCyan` bg, black text)
  - Add category text below LIVE badge
  - Update progress bar: use cyan gradient `Brush.linearGradient(CyanGradientStart, CyanGradientEnd)` instead of single primary color
  - Progress bar track: `Color.White.copy(alpha = 0.2f)` instead of `surfaceContainerHigh`
  - Add "Xm left" text below progress bar
  - The card container uses gradient: `from-[#1a2026] to-[#0f1419]`
- Why: Match new full-width continue watching card design

#### 5. SectionHeader.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/SectionHeader.kt`
- What changes:
  - Update `SectionHeader` title style from `titleSmall` with `primary` color to `titleLarge` (20sp bold) with `onSurface` color (white in dark theme)
  - Update `SectionHeaderWithViewAll` "View All" text: change to "See all" with `primary` color (cyan) instead of `onSurfaceVariant`
- Why: Reference design uses larger, white section titles with cyan "See all" links

#### 6. GroupFilterChips.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/GroupFilterChips.kt`
- What changes:
  - Replace Material3 `FilterChip` with custom pill-shaped buttons
  - Selected state: cyan gradient background (`from-[#00d4ff] to-[#0088ff]`), white text
  - Unselected state: `white/5` bg (dark) or `white` bg with border (light), `white/60` text (dark) or `gray-600` text (light)
  - Shape: `RoundedCornerShape(12.dp)` (rounded-xl equivalent)
  - Remove checkmark leading icon
  - Padding: horizontal 16dp, vertical 8dp
- Why: Reference design uses custom gradient pill chips, not Material3 FilterChips

#### 7. HomeScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`
- What changes:
  - **HomeTopAppBar**: Replace `PlaylistDropdown` with a styled playlist selector button:
    - Rounded-xl shape with `CyanGradientStart.copy(alpha = 0.1f)` background
    - Border: `CyanGradientStart.copy(alpha = 0.3f)`
    - Contains playlist name (bold) + MoreVertical icon in cyan
    - Opens `PlaylistSheet` bottom sheet on click (reuse existing `PlaylistSettingsBottomSheet` or create new selection sheet)
  - Replace settings gear icon with a gradient Plus button (cyan gradient bg, white Plus icon, rounded-xl) that navigates to onboarding/add playlist
  - Keep search icon button but style as: `white/5` bg, `white/10` border, rounded-xl
  - Wrap content in `GradientBackground` composable
  - **HomeContent**: Change from `Column.verticalScroll` to keep Column but wrap in gradient background
  - **Continue Watching section**: Change from horizontal `LazyRow` to vertical `Column` of full-width `ContinueWatchingCard`s
  - **Favorites section**: Keep horizontal LazyRow but update section header style
  - **Categories section**: Keep current horizontal LazyRow pattern (per D10)
  - Add sticky header effect: header has `background(HeaderDarkBg.copy(alpha = 0.8f))`
- Why: Match new Home screen design

#### 8. ChannelsScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt`
- What changes:
  - **ChannelsTopAppBar**: Replace Material3 `TopAppBar` with custom header:
    - "All Channels" title (2xl bold, white)
    - Integrated search bar below title: rounded-2xl input with search icon left, clear button right, `white/5` bg, `white/10` border
    - Category filter chips below search (using updated `GroupFilterChips`)
    - Sticky with backdrop blur effect (semi-transparent bg)
  - Remove separate `PlaylistDropdown` -- the reference design does not show a playlist selector on channels screen (filtering is done via category chips)
  - Wrap content in `GradientBackground`
  - **Search integration**: Replace `SearchTopBar` toggle with inline always-visible search field in the header
  - Keep grid/list toggle functionality but remove separate search mode -- search is always visible
- Why: Match new Channels screen design with inline search

#### 9. FavoritesScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesScreen.kt`
- What changes:
  - **Header**: Replace `TopAppBar` with custom header:
    - Heart icon (`FavoritePink`, filled) + "Favorites" title
    - Playlist filter chips below (replacing `PlaylistDropdown`):
      - "All Playlists" chip: pink gradient when selected (`#ff006e` to `#d41359`)
      - Individual playlist chips: cyan gradient when selected
    - Sticky with semi-transparent bg
  - Add favorites count text: "X favorites" above the grid
  - Wrap content in `GradientBackground`
  - Remove search toggle from header (Favorites in reference has no search)
- Why: Match new Favorites screen design

#### 10. SettingsScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsScreen.kt`
- What changes:
  - Replace `TopAppBar` with custom sticky header (matching other screens)
  - Update section calls to pass gradient colors for section icon badges
  - Remove grouped `SettingsCard` wrapping -- each row becomes its own card
  - Add "Preferences" section for toggle settings (Auto Update, Notifications) per reference
  - Move "Manage Playlists" to Data & Storage section
  - Add "Terms of Service" to About section
- Why: Match new Settings screen design

#### 11. SettingsComponents.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsComponents.kt`
- What changes:
  - **SettingsSectionHeader**: Add gradient icon badge (32dp rounded-lg box with gradient background and white icon). Accept `icon: ImageVector` and `gradientColors: List<Color>` params. Title becomes uppercase, small text, `white/50` opacity.
  - **SettingsCard**: REMOVE or repurpose. Each setting item will be its own card.
  - **New `SettingsItemCard`**: Individual rounded-2xl card wrapping a single setting row. Dark: `white/5` bg + `white/10` border. Light: white bg + gray-200 border.
  - **SettingsDropdownRow**: Wrap in `SettingsItemCard`. Simplify layout: icon (no background) + label + value in primary color + chevron. Remove `IconContainer` background.
  - **SettingsActionRow**: Wrap in `SettingsItemCard`. Same simplification.
  - **SettingsInfoRow**: Wrap in `SettingsItemCard`.
  - **SettingsSwitchRow**: Wrap in `SettingsItemCard`. Update switch to use gradient track when checked (cyan gradient).
  - Remove dividers between items (each item is its own card with spacing)
- Why: Each setting is an individual card per reference design

#### 12. PlayerControls.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/PlayerControls.kt`
- What changes:
  - **PlayerTopBar**:
    - Back button: circular (48dp) `bg-black/40 backdrop-blur` (use `Color.Black.copy(alpha = 0.4f)` + `CircleShape`)
    - Add LIVE badge next to channel name: pink `#ff006e` pill badge with white text
    - Streaming/Airplay button and Settings button: circular `bg-black/40` style
    - Remove channel logo from top bar (reference doesn't show it)
  - **PlayerBottomBar**:
    - Add volume slider row: Volume icon (circular button) + Slider
    - Add brightness slider row: Sun icon (circular button) + Slider
    - Replace `LabeledControlButton` layout with pill-shaped buttons:
      - PiP: `bg-black/40 backdrop-blur` pill with icon + text
      - Sleep: `bg-black/40 backdrop-blur` pill with icon + text
      - Channels: cyan gradient pill (`from-[#00d4ff] to-[#0088ff]`) with bold text
    - Remove individual track selection buttons from bottom bar (they go into a settings sheet)
  - **Add center controls**: Skip back (64dp circle) + Play/Pause (80dp circle, `white/20` bg) + Skip forward (64dp circle). These are currently handled by GestureOverlay swipes but the reference shows visible center buttons.
  - **LiveIndicator**: Change from red to pink for player context
- Why: Match new player controls design with visible center buttons and slider controls

#### 13. MainScreen.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/main/MainScreen.kt`
- What changes:
  - **BottomNavigationBar**: Update styling:
    - Active tab: cyan `#00d4ff` color (will be `MaterialTheme.colorScheme.primary`)
    - Navigation bar background: match the dark gradient endpoint `#0f1419` with border top `#1a2026`
    - Change tab order: Home, Channels, Favorites, Settings (currently: Home, Favorites, Channels, Settings)
  - Update tab order in `bottomNavItems` to match reference: Home, Channels, Favorites, Settings
- Why: Match reference bottom nav styling and tab order

### New Files

#### 1. GradientBackground.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/GradientBackground.kt`
- Purpose: Reusable composable that applies the gradient background to screens
- Key contents:
```kotlin
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isDark = !MaterialTheme.colorScheme.background.luminance().let { it > 0.5f }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(BackgroundDark, BackgroundDarkGradientEnd)
                    } else {
                        listOf(BackgroundLight, BackgroundLightGradientEnd)
                    }
                )
            ),
    ) {
        content()
    }
}
```

#### 2. PlaylistFilterChips.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/PlaylistFilterChips.kt`
- Purpose: Horizontal scrolling playlist filter chips for Favorites screen with pink/cyan gradient selection
- Key contents:
```kotlin
@Composable
fun PlaylistFilterChips(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    modifier: Modifier = Modifier,
)
```
- "All Playlists" chip uses pink gradient when selected
- Individual playlist chips use cyan gradient when selected
- Unselected: `white/5` bg

#### 3. StyledSearchBar.kt
- Path: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/StyledSearchBar.kt`
- Purpose: Inline rounded-2xl search input used in Channels screen header
- Key contents:
```kotlin
@Composable
fun StyledSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search channels...",
    modifier: Modifier = Modifier,
)
```
- Rounded-2xl shape, search icon left, clear button right
- Dark: `white/5` bg, `white/10` border, `white/30` placeholder
- Light: white bg, gray-200 border

### Database Changes
None. This is a visual-only redesign.

### DI Changes
None. No new ViewModels or services.

## Implementation Order

1. **Color.kt** -- Update all color constants and accent scheme values. This is the foundation all other changes depend on.

2. **GradientBackground.kt** -- Create the new shared component for gradient screen backgrounds.

3. **ChannelCard.kt** -- Update `ChannelCardSquare`, `ChannelCardList`, and `LiveBadge` with new styling (text below card, pink hearts, cyan LIVE badges). This component is used by all content screens.

4. **ContinueWatchingCard.kt** -- Update to full-width layout with new badge and progress styling.

5. **SectionHeader.kt** -- Update title sizing, color, and "See all" link styling.

6. **GroupFilterChips.kt** -- Replace Material3 FilterChips with custom gradient pill buttons.

7. **StyledSearchBar.kt** -- Create the new inline search input component.

8. **PlaylistFilterChips.kt** -- Create the new playlist filter chips for Favorites.

9. **SettingsComponents.kt** -- Rewrite settings components: add `SettingsItemCard`, update section headers with gradient badges, remove dividers, wrap each row in individual cards.

10. **SettingsScreen.kt** -- Update to use new settings components layout.

11. **HomeScreen.kt** -- Update header (playlist button, gradient plus button), continue watching layout, wrap in gradient background.

12. **ChannelsScreen.kt** -- Replace header with custom header (inline search + filter chips), remove PlaylistDropdown, wrap in gradient background.

13. **FavoritesScreen.kt** -- Replace header (heart icon + title), add playlist filter chips, add count text, wrap in gradient background.

14. **PlayerControls.kt** -- Update top bar (circular buttons, pink LIVE), add center controls, update bottom bar (sliders, pill buttons).

15. **MainScreen.kt** -- Update bottom nav tab order and styling.

## Testing Strategy

### Visual Verification
- Build and run on Android device/emulator with `./gradlew :androidApp:installDebug`
- Verify each screen against reference screenshots in `docs/IPTV App ReDesign/screenshots/`
- Test both dark and light themes
- Test all three accent colors (Teal/Cyan, Blue, Red) to ensure non-Teal accents still look coherent

### Functional Regression
- Verify all navigation flows still work (Home -> Player, Home -> Favorites, etc.)
- Verify favorites toggle works on all screens (Home, Channels, Favorites)
- Verify playlist selection still works on Home screen
- Verify search still works on Home and Channels screens
- Verify settings changes (theme, accent, view mode) still apply correctly
- Verify player controls (play/pause, channel switching, sleep timer, track selection) still function
- Verify continue watching section appears when channels have watch history

### Edge Cases
- Empty state: Favorites with no favorites
- Empty state: Search with no results
- Long playlist/channel names (text overflow)
- Single playlist vs multiple playlists (filter chips visibility)
- Player controls auto-hide timing

### Build Verification
```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```

### Coroutine Test Notes
No new async code is being added. All changes are UI-layer composable modifications. Existing ViewModel tests remain valid without modification.

## Doc Updates Required

- Update `docs/constraints/current-limitations.md` -- AFTER implementation: note that the new design has been applied
- No feature doc updates needed (this is a visual redesign, not new functionality)

## Build & Test Commands
```bash
./gradlew formatAll
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
