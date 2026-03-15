# Home Screen Top Bar + Playlist Selection -- Implementation Plan

## Summary

Replace the current Material3 `TopAppBar`-based home screen header with a custom composable that matches the Stitch design exactly. The new top bar features a minimal playlist name with expand_more chevron (opening a `DropdownMenu`), plus search and settings icon buttons on the right side -- all on a semi-transparent blurred background. The existing MVI logic, events, and state remain untouched; only the UI composables in `HomeScreen.kt` and `PlaylistDropdown.kt` change.

## Decisions Made

### Decision 1: Keep DropdownMenu for playlist selection (no bottom sheet)

- **Decision**: Use `DropdownMenu` anchored below the playlist name text, matching the current behavior.
- **Rationale**: The Stitch design shows a simple dropdown chevron (`expand_more`), which maps naturally to `DropdownMenu`. A bottom sheet would be over-engineered for a short list of playlists.
- **Alternatives considered**: `ModalBottomSheet` for playlist selection -- rejected because the design shows an inline dropdown pattern.

### Decision 2: Settings gear opens playlist settings (not app settings)

- **Decision**: The settings gear icon in the top bar triggers `HomeEvent.OnPlaylistSettingsClick`, opening the existing `PlaylistSettingsBottomSheet`. It is disabled (reduced alpha) when no specific playlist is selected (i.e., "All" mode).
- **Rationale**: The Stitch design places a gear icon in the home top bar. Contextually, the home screen already has playlist management. The bottom navigation already has a dedicated Settings tab for app settings.
- **Alternatives considered**: Navigating to app settings -- rejected because the bottom nav already handles that.

### Decision 3: Semi-transparent background approach

- **Decision**: Use `MaterialTheme.colorScheme.background.copy(alpha = 0.80f)` as the background color. Skip `Modifier.blur()` / backdrop blur since Compose Multiplatform does not have a native backdrop-blur equivalent that works cross-platform. The 80% opacity alone achieves the visual intent.
- **Rationale**: True CSS-style `backdrop-blur` is not available in Compose Multiplatform. `graphicsLayer { renderEffect = BlurEffect(...) }` blurs the element itself, not what is behind it. The alpha transparency provides the key visual cue from the Stitch design.
- **Alternatives considered**: Using `graphicsLayer` blur on a separate layer -- too complex and fragile for minimal visual gain; platform-specific blur implementations -- violates KMP simplicity.

### Decision 4: Light theme support via MaterialTheme tokens

- **Decision**: Use `MaterialTheme.colorScheme.onBackground` for text/icon colors, `MaterialTheme.colorScheme.onSurfaceVariant` for the chevron icon. This automatically adapts to light/dark.
- **Rationale**: The light theme HTML shows `text-[#212121]` for dark-on-light, while dark theme uses white. Our existing `onBackground` maps to `#101c22` (light) and `#f6f7f8` (dark), which are close enough. The design tokens already handle this correctly.
- **Alternatives considered**: Hardcoded color values per theme -- rejected to keep code DRY and consistent with design system.

### Decision 5: Playlist name display when "All" is selected

- **Decision**: Show "All Playlists" when `PlaylistSelection.All` is active. Show the playlist name when a specific playlist is selected.
- **Rationale**: The Stitch design shows "My Playlist" which corresponds to a selected playlist. "All Playlists" is a clear label for the aggregate view and maintains consistency.
- **Alternatives considered**: Show "My Playlist" as a static label -- rejected because it would be misleading when multiple playlists exist.

### Decision 6: Remove `PlaylistDropdown.kt` OutlinedCard variant

- **Decision**: Rewrite `PlaylistDropdown.kt` to use the new minimal text+chevron style. The `OutlinedCard`-based variant is only used in `HomeScreen`, so there is no backward compatibility concern.
- **Rationale**: Grep shows `PlaylistDropdown` is only referenced from `HomeScreen.kt`. No other screen uses it.
- **Alternatives considered**: Creating a second variant -- unnecessary complexity since there is only one consumer.

## Current State

### Files involved (with current behavior):

1. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`**
   - Lines 236-268: `HomeTopAppBar` -- uses Material3 `TopAppBar` with `PlaylistDropdown` in title slot, `Icons.Default.Search` and `Icons.Default.Settings` as actions.
   - Lines 352-364: `HomeTopAppBarTitle` -- wrapper that delegates to `PlaylistDropdown`.
   - Lines 130-154: Scaffold `topBar` lambda -- switches between `SearchTopBar` and `HomeTopAppBar`.

2. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/PlaylistDropdown.kt`**
   - Lines 28-103: `PlaylistDropdown` -- `OutlinedCard` with "Playlist: ..." text + `KeyboardArrowDown` icon. `DropdownMenu` contains "All", each playlist, and optional "+ Add new playlist".

3. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/HomeMvi.kt`**
   - No changes needed. All events (`OnPlaylistSelected`, `OnPlaylistSettingsClick`, `OnToggleSearch`, `OnAddPlaylistClick`) already exist.

4. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModel.kt`**
   - No changes needed. All event handlers already exist.

5. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/designsystem/Color.kt`**
   - Lines 31-37: Slate color variants already defined (`Slate300`, `Slate400`, etc.) -- useful for icon tint in dark theme.

6. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/DropdownSelector.kt`**
   - Not used by `PlaylistDropdown`. No changes needed. Verify no other code imports `PlaylistDropdown` for non-home usage.

## Changes Required

### Modified Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`

**What changes:**
- Remove `HomeTopAppBar` composable (lines 236-268) and `HomeTopAppBarTitle` composable (lines 352-364).
- Replace with a new `HomeTopBar` composable that is a custom `Row`-based layout (not Material `TopAppBar`).
- Remove imports: `TopAppBar`, `ExperimentalMaterial3Api` (if no longer needed), `Icons.Default.Settings`, `Icons.Default.Search`.
- Add imports: `Icons.Outlined.Search`, `Icons.Outlined.Settings` (or keep `Default` -- both work; Stitch uses outlined style).
- Update the Scaffold's `topBar` lambda to use the new `HomeTopBar`.

**New `HomeTopBar` composable signature and structure:**

```kotlin
@Composable
private fun HomeTopBar(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    onAddPlaylistClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPlaylistSettingsClick: () -> Unit,
    isPlaylistSettingsEnabled: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.80f))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: PlaylistDropdown (text + chevron)
        PlaylistDropdown(
            playlists = playlists,
            selection = selection,
            onPlaylistSelect = onPlaylistSelect,
            onAddPlaylistClick = onAddPlaylistClick,
        )

        // Right: search + settings icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onPlaylistSettingsClick,
                enabled = isPlaylistSettingsEnabled,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Playlist Settings",
                    tint = if (isPlaylistSettingsEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    },
                )
            }
        }
    }
}
```

**Why:** The current Material `TopAppBar` has built-in elevation, color tokens, and sizing that do not match the Stitch design. A custom `Row` gives full control over background transparency, padding, and icon sizing.

**Additional change in Scaffold:** The `topBar` in `Scaffold` currently relies on `TopAppBar` providing status bar insets. With a custom composable, we must add `Modifier.statusBarsPadding()` (from `WindowInsets.statusBars`) to the top bar Row to avoid content rendering under the status bar.

Add import: `import androidx.compose.foundation.layout.statusBarsPadding`

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/PlaylistDropdown.kt`

**What changes:** Complete rewrite of the composable's visual presentation. Keep the same external API signature but change internals from `OutlinedCard` to a clickable `Row` with bold text + `expand_more` chevron icon.

**New implementation:**

```kotlin
@Composable
fun PlaylistDropdown(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    onAddPlaylistClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = when (selection) {
        is PlaylistSelection.Selected -> playlists.find { it.id == selection.id }?.name ?: "All Playlists"
        PlaylistSelection.All -> "All Playlists"
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = selectedText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 200.dp),
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select playlist",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All Playlists") },
                onClick = {
                    onPlaylistSelect(PlaylistSelection.All)
                    expanded = false
                },
            )
            playlists.forEach { playlist ->
                DropdownMenuItem(
                    text = { Text(playlist.name) },
                    onClick = {
                        onPlaylistSelect(PlaylistSelection.Selected(playlist.id))
                        expanded = false
                    },
                )
            }
            if (onAddPlaylistClick != null) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Add new playlist") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        onAddPlaylistClick()
                        expanded = false
                    },
                )
            }
        }
    }
}
```

**Why:** The `OutlinedCard` with "Playlist: X" label and full-width layout does not match the Stitch design. The Stitch design shows just the playlist name in bold text with a small chevron icon, no card outline, no label prefix.

**Removed imports:** `OutlinedCard`, `fillMaxWidth`, `padding` (if unused after rewrite).
**Added imports:** `FontWeight`, `TextOverflow`, `widthIn`, `Arrangement`.

### No New Files Required

All changes fit within existing files. No new composables, view models, state classes, or modules are needed.

### No Database Changes

### No DI Changes

## Implementation Order

1. **Rewrite `PlaylistDropdown.kt`** -- Change from `OutlinedCard` to minimal text+chevron `Row` with `DropdownMenu`. Update imports. This file has no dependency on the top bar layout.

2. **Rewrite `HomeTopAppBar` in `HomeScreen.kt`** -- Replace the `@OptIn(ExperimentalMaterial3Api::class)` Material `TopAppBar` with custom `Row`-based `HomeTopBar`. Remove `HomeTopAppBarTitle` composable (now inlined). Add `statusBarsPadding()`. Update the Scaffold `topBar` lambda call site to use the new composable name.

3. **Clean up unused imports in `HomeScreen.kt`** -- Remove `TopAppBar`, `ExperimentalMaterial3Api` (check if still needed by other code in the file -- it is not, since `SearchTopBar` in `SearchComponents.kt` has its own opt-in). Remove any other dead imports.

4. **Run formatAll** -- `./gradlew formatAll` to ensure code style compliance.

5. **Build and test** -- Verify compilation and visual appearance on both light and dark themes.

## Testing Strategy

### Manual Visual Testing (primary for this UI-only change)

- **Dark theme**: Verify top bar shows playlist name in white bold text, chevron in slate-400, search/settings icons in slate-400, background is semi-transparent dark.
- **Light theme**: Verify text is dark (#101c22), icons are slate-600, background is semi-transparent light.
- **Playlist dropdown**: Tap playlist name area -> dropdown opens with "All Playlists", each playlist name, divider, "Add new playlist".
- **Select playlist**: Tap a playlist -> dropdown closes, name updates, home content reloads.
- **Settings gear enabled**: When a specific playlist is selected, gear icon is full opacity and tappable, opens `PlaylistSettingsBottomSheet`.
- **Settings gear disabled**: When "All Playlists" is selected, gear icon is dimmed (38% alpha) and not tappable.
- **Search icon**: Tap search -> `SearchTopBar` replaces `HomeTopBar` (existing behavior preserved).
- **Add new playlist**: Tap "Add new playlist" in dropdown -> navigates to onboarding (existing behavior).
- **Long playlist name**: Name truncates with ellipsis at 200dp max width, does not push icons off screen.
- **Single playlist**: Dropdown still shows "All Playlists" option plus the single playlist.
- **Scroll behavior**: Content scrolls behind the semi-transparent top bar (the `background.copy(alpha=0.80f)` makes content partially visible behind it).

### No Automated Tests Required

This change is purely presentational. The existing MVI tests (if any) for `HomeViewModel` remain valid since no state/event/action changes are made. No new business logic is introduced.

## Doc Updates Required

- No product docs need updating -- the feature docs describe behavior, not implementation details.
- `docs/constraints/current-limitations.md` -- no changes needed (this feature was not listed as a limitation).
- `docs/constraints/open-questions.md` -- no changes needed (no open questions resolved).

## Build & Test Commands

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
./gradlew formatAll
```
