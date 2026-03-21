# UI Channel Cards Redesign -- Implementation Plan

## Summary

Redesign the three channel card composables (`ChannelCardSquare`, `ChannelCardList`, `ContinueWatchingCard`) and the Home screen section layout to achieve a modern, polished look. This is a pure UI/presentation change -- no new domain logic, data layer changes, database migrations, or DI registrations are needed. All modifications stay within `shared/src/commonMain/` and affect only composable functions and color constants.

## Decisions Made

### 1. Placeholder avatar colors -- deterministic hashing, not random

- **Decision**: Generate a colorful placeholder from a fixed palette using `name.hashCode() % palette.size`. The placeholder shows the first letter of the channel name in white on a colored circular background.
- **Rationale**: Deterministic colors mean the same channel always gets the same placeholder color, preventing visual jitter on recomposition or list scroll. A palette of 8 distinct colors provides enough variety without needing any new dependencies.
- **Alternatives considered**: Random color on each recomposition (flickery), Coil placeholder/error drawables (less control over appearance), calculating dominant color from logo URL domain (overcomplicated).

### 2. Shimmer loading state -- deferred

- **Decision**: Do not implement shimmer loading placeholders in this task.
- **Rationale**: Shimmer requires either a third-party library or a custom infinite-animation composable. The cards are already loaded from Room/Paging so the loading gap is negligible. Adding shimmer would increase scope significantly for minimal UX benefit. This can be a follow-up task.
- **Alternatives considered**: Accompanist placeholder (Android-only, not KMP), custom shimmer modifier (significant effort).

### 3. ContinueWatchingCard animation -- simple fade-in only

- **Decision**: Use `AnimatedVisibility` with `fadeIn` for the continue watching section, not individual card animations.
- **Rationale**: Per-card staggered animation on a `LazyRow` is complex and can hurt scroll performance. A section-level fade-in provides a subtle polish without performance cost.
- **Alternatives considered**: `animateItemPlacement` on LazyRow items (limited to placement, not appear), per-card `AnimatedVisibility` with staggered delay (complex, perf risk).

### 4. Favorite button style -- filled circle background

- **Decision**: Replace the plain `IconButton` with a semi-transparent circular background behind the heart/star icon. Switch from star to heart icon (`Icons.Filled.Favorite` / `Icons.Outlined.FavoriteBorder`) for a more modern look.
- **Rationale**: Heart icons are the industry standard for favorites in media apps. A semi-transparent dark circle behind the icon ensures visibility on any logo background.
- **Alternatives considered**: Keep star (feels dated), use `FilledIconButton` from M3 (heavier, opinionated colors).

### 5. Card elevation approach -- `tonalElevation` not `shadowElevation`

- **Decision**: Use `tonalElevation = 2.dp` on cards for subtle depth via surface tint, rather than drop shadows.
- **Rationale**: Tonal elevation is the Material 3 recommended approach for dark themes. Shadow elevation on dark backgrounds is nearly invisible and looks odd on some platforms. Tonal elevation works reliably on both Android and iOS.
- **Alternatives considered**: `shadowElevation` (poor dark-theme support in Compose Multiplatform), custom `Modifier.shadow()` (platform-inconsistent).

### 6. Section header redesign scope

- **Decision**: Upgrade the `Section` composable with `titleLarge` typography (bold), more vertical padding, and a styled "View All" button using `TextButton` with an arrow icon.
- **Rationale**: Heavier section headers create visual hierarchy. Arrow icon on "View All" makes it clearly tappable.
- **Alternatives considered**: Full section header cards (too heavy), underline decoration (dated).

### 7. Card width increase on Home screen

- **Decision**: Increase `ChannelCardSquare` width in Home `LazyRow` from `150.dp` to `160.dp`, and `ContinueWatchingCard` from `200.dp` to `220.dp`.
- **Rationale**: Slightly larger cards improve visual impact and give more room for text. The increase is small enough to still fit 2+ cards on screen in the horizontal row.
- **Alternatives considered**: Much larger cards (reduces visible count, hurts scannability), keep same size (misses the redesign goal).

## Current State

### Files that contain the card implementations:

1. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ChannelCard.kt`** (lines 1-231)
   - `ChannelCardSquare` (line 43): 1:1 aspect ratio card, `RoundedCornerShape(8.dp)`, simple vertical gradient (`Transparent -> Black@0.7`), star icon for favorite, `surfaceContainer` background.
   - `ChannelCardList` (line 140): Row layout, `RoundedCornerShape(8.dp)`, 56dp logo box with `surfaceContainerHigh` background, star icon favorite.
   - `LiveBadge` (line 217): Private composable, red background with "LIVE" text.

2. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ContinueWatchingCard.kt`** (lines 1-87)
   - `ContinueWatchingCard` (line 35): Fixed `200.dp` width, 16:9 aspect ratio, `RoundedCornerShape(8.dp)`, simple gradient overlay, channel name below image. No progress bar despite `ContinueWatchingItem` having `progress` and `timeLeft` fields.

3. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`** (lines 1-549)
   - `HomeContent` (line 368): Vertical scroll column with sections.
   - `Section` (line 453): Simple Row with `titleMedium` text and clickable "View All" text.
   - Card widths: `Modifier.width(150.dp)` for square cards, ContinueWatchingCard uses internal `200.dp`.

### Callers of these cards:
- `HomeScreen.kt` lines 390, 411, 436 -- ContinueWatchingCard and ChannelCardSquare in LazyRows
- `ChannelsScreen.kt` lines 198, 233 -- ChannelCardSquare in grid, ChannelCardList in list
- `FavoritesScreen.kt` lines 196, 213 -- ChannelCardSquare in grid, ChannelCardList in list

### Design system:
- `Color.kt`: Dark theme `surfaceContainer = Color(0xFF1a2830)`, `surfaceContainerHigh = Color(0xFF213038)`
- `Theme.kt`: `AppTheme` with accent color support
- `Typography.kt`: Inter font family, standard M3 type scale

## Changes Required

### Modified Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ChannelCard.kt`

**ChannelCardSquare changes:**
- Change `RoundedCornerShape(8.dp)` to `RoundedCornerShape(16.dp)`
- Add `CardDefaults.cardElevation(defaultElevation = 0.dp)` and set `tonalElevation` via `CardDefaults.cardColors()` -- actually use `CardDefaults.elevatedCardColors()` with `ElevatedCard` for subtle tonal lift
- Replace simple 2-stop gradient with a 3-stop gradient: `Transparent @0f`, `Black@0.15 @0.5f`, `Black@0.75 @1.0f` for a more sophisticated scrim
- Add a letter-based colored placeholder `Box` when `logoUrl` is null or empty, using `ChannelPlaceholder` composable (defined in same file)
- Replace star icon with heart icon (`Icons.Filled.Favorite` / `Icons.Outlined.FavoriteBorder`)
- Wrap favorite `IconButton` in a `Box` with `CircleShape` background at `Color.Black.copy(alpha = 0.4f)`, size 32dp
- Add `Modifier.padding(6.dp)` to the favorite button alignment area

**ChannelCardList changes:**
- Change `RoundedCornerShape(8.dp)` to `RoundedCornerShape(12.dp)` on the Surface
- Change logo container shape from `RoundedCornerShape(8.dp)` to `RoundedCornerShape(12.dp)`
- Change logo container size from `56.dp` to `52.dp` (slightly tighter, cleaner)
- Add letter-based colored placeholder for missing logos
- Change padding from `8.dp` to `12.dp` for more breathing room
- Upgrade name text from `bodyMedium` to `titleSmall` (fontWeight Medium, 14sp) for stronger hierarchy
- Replace star with heart icon, same as ChannelCardSquare
- Add `tonalElevation = 1.dp` to Surface

**LiveBadge changes:**
- Change shape from `RoundedCornerShape(4.dp)` to `RoundedCornerShape(6.dp)`
- Add a small pulsing dot (8dp circle with red color) before "LIVE" text using `Row`

**New private composable `ChannelPlaceholder`:**
- Takes `name: String`, `modifier: Modifier`
- Displays first letter of name (uppercase) in white, centered in a colored `Box`
- Color selected from an 8-color palette using `abs(name.hashCode()) % 8`
- Typography: `titleLarge` for square cards, `titleSmall` for list cards (controlled by caller via text style parameter)
- Palette: muted, visually distinct colors that work on both light and dark themes

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ContinueWatchingCard.kt`

- Change `RoundedCornerShape(8.dp)` to `RoundedCornerShape(16.dp)`
- Change fixed width from `200.dp` to `220.dp`
- Replace 2-stop gradient with 3-stop gradient (same as ChannelCardSquare)
- Move channel name from below the image to overlaid on the gradient (inside the Box, aligned `BottomStart`)
- Add `progress: Float = 0f` parameter
- Add a thin (3dp height) progress bar at the very bottom of the card using `Box` with `Modifier.fillMaxWidth(fraction = progress)` and `MaterialTheme.colorScheme.primary` color, on top of a `surfaceContainerHigh` track
- Add "Continue" label badge in top-left corner: small rounded rectangle with `MaterialTheme.colorScheme.primary` background and white text, similar to LiveBadge
- Remove the `Column` wrapper -- make the whole card a single `Box` with the image, gradient overlay, text overlay, and progress bar stacked

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`

**HomeContent changes:**
- Change `ChannelCardSquare` modifier from `Modifier.width(150.dp)` to `Modifier.width(160.dp)` (lines 417, 442)
- Increase `LazyRow` horizontal `Arrangement.spacedBy` from `8.dp` to `12.dp` for continue watching and favorites rows

**Section composable changes (line 453):**
- Change title from `titleMedium` to `titleLarge`
- Change title fontWeight to `FontWeight.SemiBold` via style copy
- Increase vertical padding from `8.dp` to `12.dp`
- Replace plain "View All" text with a `Row` containing text + small forward arrow icon (`Icons.AutoMirrored.Filled.ArrowForward` or `Icons.Default.ChevronRight`)
- Use `MaterialTheme.colorScheme.primary` for the "View All" text (already the case)
- Make "View All" a `TextButton` instead of clickable text for proper touch target

**ContinueWatchingCard call site (line 390):**
- Pass `progress = item.progress` to the card

**Continue Watching section:**
- Wrap in `AnimatedVisibility(visible = state.continueWatchingItems.isNotEmpty(), enter = fadeIn())` instead of plain `if` check

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`

**New constants** (add after existing Slate colors, around line 38):
```kotlin
// Channel placeholder avatar palette (8 distinct muted colors)
val PlaceholderColors = listOf(
    Color(0xFF5C6BC0), // Indigo
    Color(0xFF26A69A), // Teal
    Color(0xFFEF5350), // Red
    Color(0xFFAB47BC), // Purple
    Color(0xFF42A5F5), // Blue
    Color(0xFFFF7043), // Deep Orange
    Color(0xFF66BB6A), // Green
    Color(0xFFFFCA28), // Amber
)
```

### New Files

None. All changes are modifications to existing files.

### Database Changes

None.

### DI Changes

None.

## Implementation Order

1. **Add `PlaceholderColors` palette to `Color.kt`**
   - File: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`
   - Add the 8-color list constant after line 37 (after Slate colors)

2. **Redesign `ChannelCard.kt`** -- the core of this feature
   - File: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ChannelCard.kt`
   - Add `ChannelPlaceholder` private composable
   - Rewrite `ChannelCardSquare` with all visual improvements
   - Rewrite `ChannelCardList` with all visual improvements
   - Update `LiveBadge` styling
   - Add necessary imports: `Icons.Filled.Favorite`, `Icons.Outlined.FavoriteBorder`, `CircleShape`, `PlaceholderColors`, `kotlin.math.abs`

3. **Redesign `ContinueWatchingCard.kt`**
   - File: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ContinueWatchingCard.kt`
   - Add `progress` parameter
   - Restructure layout to overlay text on image
   - Add progress bar
   - Add "Continue" badge
   - Add necessary imports

4. **Update `HomeScreen.kt`**
   - File: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`
   - Update `Section` composable styling
   - Update card widths and spacing
   - Pass `progress` to `ContinueWatchingCard`
   - Add `AnimatedVisibility` for continue watching section
   - Add necessary imports: `AnimatedVisibility`, `fadeIn`, `Icons.AutoMirrored.Filled.ArrowForward`, `FontWeight`

5. **Build and verify**
   - Run `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`
   - Visual inspection on Android emulator

## Testing Strategy

This is a purely visual/UI change with no business logic modifications. Testing strategy:

### Automated
- **Existing unit tests must still pass**: `./gradlew :shared:testAndroidHostTest` -- these tests cover ViewModel/UseCase logic which is unaffected.
- **Build verification**: `./gradlew :androidApp:assembleDebug` must succeed.

### Manual visual verification
- **ChannelCardSquare**: Verify rounded corners (16dp), gradient overlay, placeholder letter when no logo, heart icon with dark circle background, tonal elevation visible in both light/dark themes.
- **ChannelCardList**: Verify rounded corners (12dp), larger padding, placeholder letter, heart icon, tonal elevation.
- **ContinueWatchingCard**: Verify rounded corners (16dp), progress bar at bottom, "Continue" badge, text overlaid on gradient, larger width.
- **Home screen sections**: Verify larger section headers, "View All" button with arrow, increased spacing.
- **Edge cases**:
  - Channel with very long name (text truncation still works)
  - Channel with null/empty `logoUrl` (placeholder appears)
  - Channel with empty name (placeholder shows "?" or first available character)
  - `progress = 0f` on ContinueWatchingCard (no visible bar, or full-width track only)
  - `progress = 1f` on ContinueWatchingCard (full bar)
  - Light theme and dark theme appearance
  - LazyRow scroll performance with new card design (no jank)

### Key assertions for placeholder
- Same channel name always produces same placeholder color
- Placeholder is only shown when `logoUrl` is null or blank
- First letter is uppercase

## Doc Updates Required

No documentation updates are required. This is a visual-only change that does not alter:
- Feature behavior described in `docs/features/home.md`
- Domain models
- Navigation flows
- Constraints or limitations

The home.md doc describes *what* sections exist, not *how* cards look, so it remains accurate.

## Build & Test Commands

```bash
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
```
