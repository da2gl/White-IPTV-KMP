# UI Consistency Fixes -- Implementation Plan

## Summary

This feature addresses 7 UI consistency issues across the app: unifying section header styles, creating a shared section container component, adding dividers to channel lists, fixing the non-functional favorite toggle on the Home screen, enabling unfavoriting on the Favorites screen, and polishing the "View All" button. The goal is to bring the visual quality of Settings screen patterns (section headers, card containers, dividers) to the rest of the app, and fix two broken favorite interactions.

## Decisions Made

### Decision 1: Shared SectionHeader vs reusing SettingsSectionHeader directly
- **Decision**: Create a new `SectionHeader` composable in `common/components/SectionHeader.kt` that uses the same style as `SettingsSectionHeader` (uppercase, bold, 12sp, 1sp letter spacing). Refactor `SettingsSectionHeader` to delegate to this shared component.
- **Rationale**: Settings-specific components live in `feature/settings/components/`. A shared component in `common/components/` follows the existing pattern (ChannelCard, PlaylistDropdown, etc.) and avoids cross-feature imports.
- **Alternatives considered**: Importing `SettingsSectionHeader` directly from settings package -- violates feature isolation. Duplicating the style in each screen -- creates drift.

### Decision 2: Shared SectionCard container
- **Decision**: Create a `SectionCard` composable in `common/components/SectionHeader.kt` (same file) that wraps content in a `Surface` with 16dp rounded corners and `surfaceContainer` color, identical to `SettingsCard`. Refactor `SettingsCard` to delegate to this shared component. Do NOT apply `SectionCard` to channel lists in Channels/Favorites screens -- those use paging/lazy lists where wrapping in a card container would break scroll behavior. Only use where discrete grouped content exists.
- **Rationale**: Channel lists are paginated and scrollable; wrapping them in cards would create nested scroll issues. The card grouping pattern works for discrete settings rows, not for arbitrarily long lists. We will not force this pattern where it does not fit.
- **Alternatives considered**: Wrapping channel groups in cards -- breaks lazy list performance and creates visual clutter with hundreds of channels.

### Decision 3: Dividers in channel list view
- **Decision**: Add thin 0.5dp `HorizontalDivider` between items in `ChannelCardList` when used in List mode in ChannelsScreen and FavoritesScreen. Remove the current `Arrangement.spacedBy(8.dp)` gap and replace with dividers (matching Settings pattern). The divider will be added as a parameter `showDivider: Boolean = true` on `ChannelCardList`.
- **Rationale**: Matches the Settings visual pattern. The list view already uses `Surface` rows, so dividers between them look more polished than gaps.
- **Alternatives considered**: Adding dividers only at the LazyColumn level -- this is cleaner and avoids coupling divider logic to the card component. We will add dividers at the LazyColumn level in ChannelsScreen and FavoritesScreen instead of modifying ChannelCardList.

### Decision 4: Home screen favorite toggle fix
- **Decision**: Add `OnToggleFavorite(channelId: Long)` event to `HomeEvent`, inject `ToggleFavoriteUseCase` into `HomeViewModel`, handle the event. Wire the `onToggleFavorite` lambda in `HomeContent` to emit this event.
- **Rationale**: The Channels screen already uses this exact pattern (ChannelsEvent.OnToggleFavorite). HomeViewModel currently has empty `{}` lambdas for onToggleFavorite.
- **Alternatives considered**: None -- this is a straightforward bug fix.

### Decision 5: Favorites screen unfavorite button
- **Decision**: Change `showFavoriteButton = false` to `showFavoriteButton = true` in FavoritesScreen for both Grid and List modes. The button is already wired to `FavoritesEvent.OnToggleFavorite` which works correctly in `FavoritesViewModel` (line 68-75 -- it optimistically removes from the list and calls the use case).
- **Rationale**: The toggle logic already works; it was simply hidden. Setting `showFavoriteButton = true` restores it. Users expect to be able to unfavorite from the Favorites screen.
- **Alternatives considered**: None -- single-line fix.

### Decision 6: Home section header style unification
- **Decision**: Replace the `Section` composable in HomeScreen with a new shared `SectionHeader` component that uses the Settings-style uppercase/bold/12sp pattern. The "View All" button becomes part of the `SectionHeader` composable as an optional trailing action.
- **Rationale**: Unifies all headers across the app. The Section composable is private to HomeScreen and only used there.

### Decision 7: "View All" button polish
- **Decision**: Restyle the "View All" button to use `labelSmall` (matching the section header size) with `fontWeight = Medium`, remove the arrow icon (it adds visual noise), and use `onSurfaceVariant` color to make it secondary to the header text. Wrap in a `TextButton` with minimal padding.
- **Rationale**: The current style (`labelLarge` + primary color + arrow icon) is visually heavy and competes with the section header. A subtler treatment keeps the header as the primary visual element. This matches iOS-style "See All" patterns in streaming apps.
- **Alternatives considered**: Keeping the arrow but making it smaller -- still adds unnecessary visual weight.

## Current State

### Section Headers

1. **SettingsSectionHeader** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsComponents.kt:44-59`): Uppercase, bold, 12sp, 1sp letter spacing, `onSurfaceVariant` color, padding start=4dp bottom=8dp. Used in SettingsScreen for "Appearance", "Playback", "App Behavior", "Data & Storage", "About".

2. **Home Section** (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt:459-500`): Private `Section` composable using `titleLarge.copy(fontWeight = FontWeight.SemiBold)`. Very different visual weight. Includes optional "View All" button with `labelLarge` + primary color + ArrowForward icon.

3. **ChannelsScreen**: No section headers -- just `TopAppBar(title = { Text("All Channels") })`.

4. **FavoritesScreen**: No section headers -- just `TopAppBar(title = { Text("Favorites") })`.

### Card Containers

1. **SettingsCard** (`SettingsComponents.kt:61-73`): `Surface` with `RoundedCornerShape(16.dp)`, `surfaceContainer` color.

2. **ChannelsScreen / FavoritesScreen**: No card containers grouping items. Individual channel cards are separate.

### Dividers

1. **Settings rows** (`SettingsComponents.kt:172-178`): `HorizontalDivider` with `0.5.dp` thickness, `outlineVariant` color, `padding(start = 80.dp)`.

2. **ChannelsScreen list mode** (`ChannelsScreen.kt:222-256`): `Arrangement.spacedBy(8.dp)` gap, no dividers.

3. **FavoritesScreen list mode** (`FavoritesScreen.kt:207-224`): `Arrangement.spacedBy(12.dp)` gap, no dividers.

### Favorite Toggle

1. **HomeScreen** (`HomeScreen.kt:423,449`): `onToggleFavorite = {}` -- empty lambda, non-functional.

2. **HomeViewModel** (`HomeViewModel.kt`): No `OnToggleFavorite` event, no `ToggleFavoriteUseCase` dependency.

3. **HomeMvi** (`HomeMvi.kt:27-48`): No toggle favorite event defined.

4. **FavoritesScreen** (`FavoritesScreen.kt:196-224`): Both Grid and List pass `showFavoriteButton = false`, hiding the heart icon entirely. The `onToggleFavorite` callback IS wired correctly to `FavoritesEvent.OnToggleFavorite`.

5. **ChannelsScreen** (`ChannelsScreen.kt:198-204,233-239`): Favorite toggle works correctly -- `onToggleFavorite = { onToggleFavorite(channel.id) }`.

6. **ChannelCardSquare** (`ChannelCard.kt:57,121`): Has `showFavoriteButton` param, default `true`. Shows heart icon when enabled.

7. **ChannelCardList** (`ChannelCard.kt:178,238`): Has `showFavoriteButton` param, default `true`. Shows heart icon when enabled.

### ToggleFavoriteUseCase

Used in `ChannelsViewModel` (line 39) and `FavoritesViewModel` (line 26). Registered in `useCaseModule` (KoinModule.kt:92). NOT injected into `HomeViewModel`.

## Changes Required

### New Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/SectionHeader.kt`
- **Purpose**: Shared section header and section card components used across all screens.
- **Key contents**:
  ```kotlin
  @Composable
  fun SectionHeader(
      title: String,
      modifier: Modifier = Modifier,
      action: (@Composable () -> Unit)? = null,
  )
  ```
  Style: `title.uppercase()`, `labelSmall.copy(fontWeight = Bold, fontSize = 12.sp, letterSpacing = 1.sp)`, color = `onSurfaceVariant`, padding = `start = 4.dp, bottom = 8.dp`. Row layout with `Spacer(weight)` between title and optional action.

  ```kotlin
  @Composable
  fun SectionCard(
      modifier: Modifier = Modifier,
      content: @Composable ColumnScope.() -> Unit,
  )
  ```
  Surface with `RoundedCornerShape(16.dp)`, `surfaceContainer` color, `fillMaxWidth()`.

  ```kotlin
  @Composable
  fun SectionHeaderWithViewAll(
      title: String,
      onViewAllClick: () -> Unit,
      modifier: Modifier = Modifier,
  )
  ```
  Convenience composable that wraps `SectionHeader` with a "View All" `TextButton` as the action. "View All" styled with `labelSmall`, `fontWeight = Medium`, `onSurfaceVariant` color.

### Modified Files

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/components/SettingsComponents.kt`
- **What changes**: Refactor `SettingsSectionHeader` to delegate to the shared `SectionHeader`. Refactor `SettingsCard` to delegate to the shared `SectionCard`.
- **Why**: Single source of truth for the section header/card style.
- **Details**:
  ```kotlin
  @Composable
  fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
      SectionHeader(title = title, modifier = modifier)
  }

  @Composable
  fun SettingsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
      SectionCard(modifier = modifier, content = content)
  }
  ```

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`
- **What changes**:
  1. Replace the private `Section` composable with `SectionHeader` and `SectionHeaderWithViewAll` from `common/components/`.
  2. Wire `onToggleFavorite` lambdas to a new callback parameter on `HomeContent`.
  3. Remove the ArrowForward icon import (no longer needed).
- **Why**: Unifies header style and fixes the favorite toggle passthrough.
- **Details**:
  - `HomeContent` gets a new parameter: `onToggleFavorite: (Long) -> Unit`.
  - `HomeScreen` passes `onToggleFavorite = { channelId -> viewModel.obtainEvent(HomeEvent.OnToggleFavorite(channelId)) }`.
  - Replace `Section(title = "Continue Watching")` with:
    ```kotlin
    SectionHeader(title = "Continue Watching", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
    ```
  - Replace `Section(title = "Favorites", onViewAllClick = ...)` with:
    ```kotlin
    SectionHeaderWithViewAll(
        title = "Favorites",
        onViewAllClick = onFavoritesViewAllClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
    ```
  - Same pattern for group sections.
  - Delete the private `Section` composable entirely.

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/HomeMvi.kt`
- **What changes**: Add `data class OnToggleFavorite(val channelId: Long) : HomeEvent` to `HomeEvent`.
- **Why**: Required for the favorite toggle to emit events from the UI.

#### 5. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModel.kt`
- **What changes**:
  1. Add `ToggleFavoriteUseCase` constructor parameter.
  2. Handle `HomeEvent.OnToggleFavorite` in `obtainEvent()` by calling `toggleFavorite(channelId)` in a coroutine.
- **Why**: Enables favorite toggling from the Home screen.
- **Details**:
  ```kotlin
  class HomeViewModel(
      // ...existing params...
      private val toggleFavorite: ToggleFavoriteUseCase,
  ) : BaseViewModel<...>(...) {
      override fun obtainEvent(viewEvent: HomeEvent) {
          when (viewEvent) {
              // ...existing cases...
              is HomeEvent.OnToggleFavorite -> {
                  viewModelScope.launch { toggleFavorite(viewEvent.channelId) }
              }
          }
      }
  }
  ```
  No Koin changes needed -- `ToggleFavoriteUseCase` is already registered as a factory, and Koin auto-resolves constructor parameters for `viewModelOf(::HomeViewModel)`.

#### 6. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesScreen.kt`
- **What changes**: Change `showFavoriteButton = false` to `showFavoriteButton = true` in both Grid and List mode `ChannelCardSquare` and `ChannelCardList` calls (lines 202 and 219).
- **Why**: Enables the heart icon so users can unfavorite channels from the Favorites screen.

#### 7. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt`
- **What changes**: In the List mode branch (lines 222-256), add `HorizontalDivider` between channel items. Replace `Arrangement.spacedBy(8.dp)` with no spacing, and use a divider item between each channel card.
- **Why**: Matches the Settings divider pattern for visual consistency.
- **Details**: Use `stickyHeader` is not applicable here. Instead, for paging items, add a divider after each item except the last:
  ```kotlin
  items(
      count = pagedItems.itemCount,
      key = pagedItems.itemKey { it.id },
  ) { index ->
      val channel = pagedItems[index]
      if (channel != null) {
          ChannelCardList(...)
          if (index < pagedItems.itemCount - 1) {
              HorizontalDivider(
                  modifier = Modifier.padding(start = 76.dp),
                  color = MaterialTheme.colorScheme.outlineVariant,
                  thickness = 0.5.dp,
              )
          }
      }
  }
  ```
  Note: The `padding(start = 76.dp)` aligns the divider to start after the logo area (52dp logo + 12dp spacing + 12dp list padding).

#### 8. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesScreen.kt` (additional change)
- **What changes**: In the List mode branch (lines 207-224), add `HorizontalDivider` between channel items, same pattern as ChannelsScreen.
- **Why**: Visual consistency with Channels and Settings.

### Database Changes
None.

### DI Changes
None. `ToggleFavoriteUseCase` is already registered as `factoryOf(::ToggleFavoriteUseCase)` in `useCaseModule`. Koin auto-resolves it when injected into `HomeViewModel` via `viewModelOf(::HomeViewModel)`.

## Implementation Order

1. **Create `SectionHeader.kt`** in `common/components/` -- shared `SectionHeader`, `SectionCard`, `SectionHeaderWithViewAll` composables. No dependencies on anything new.

2. **Refactor `SettingsComponents.kt`** -- make `SettingsSectionHeader` and `SettingsCard` delegate to the shared components. Verify Settings screen still renders identically.

3. **Add `OnToggleFavorite` event to `HomeMvi.kt`** -- single line addition to the sealed interface.

4. **Update `HomeViewModel.kt`** -- add `ToggleFavoriteUseCase` dependency, handle `OnToggleFavorite` event.

5. **Update `HomeScreen.kt`** -- replace `Section` with `SectionHeader`/`SectionHeaderWithViewAll`, wire `onToggleFavorite` through `HomeContent`, delete the private `Section` composable.

6. **Update `FavoritesScreen.kt`** -- set `showFavoriteButton = true` for both view modes, add dividers in list mode.

7. **Update `ChannelsScreen.kt`** -- add dividers in list mode.

8. **Build and verify** -- run `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`.

## Testing Strategy

### Manual Testing
1. **Settings screen**: Verify headers and cards look exactly the same as before refactoring.
2. **Home screen headers**: Verify "Continue Watching", "Favorites", and group headers now use uppercase/bold/small style matching Settings.
3. **Home screen "View All"**: Verify the button is visible but visually secondary, and navigates correctly.
4. **Home screen favorite toggle**: Tap heart icon on a channel card in Favorites section and in group sections. Verify the heart fills/unfills and the database is updated (channel appears/disappears in Favorites tab).
5. **Favorites screen unfavorite**: Verify heart icon is visible on each channel card (both grid and list modes). Tap it -- channel should animate out of the list.
6. **Channels screen dividers**: Switch to list view. Verify thin dividers appear between channel items, aligned after the logo area.
7. **Favorites screen dividers**: Switch to list view. Same verification as Channels.

### Edge Cases
- Toggle favorite on the last channel in a Home section -- section should disappear if it becomes empty.
- Toggle favorite rapidly multiple times -- should not crash or produce inconsistent state (handled by Room's Flow reactivity).
- Empty favorites screen still shows the empty state (no regression from showing the heart button).
- Dividers should NOT appear after the last item in a list.

### Automated Tests
- Existing `shared:testAndroidHostTest` tests should continue passing (no logic changes beyond wiring events).
- No new unit tests needed for UI-only changes (composable styling).
- The `HomeViewModel` change (adding `ToggleFavoriteUseCase`) could warrant a unit test verifying that `OnToggleFavorite` calls the use case, but this is low priority since the pattern is identical to `ChannelsViewModel`.

### Coroutine Test Patterns
- `HomeViewModel` test would use `runTest` with `StandardTestDispatcher` and a mock `ToggleFavoriteUseCase`.

## Doc Updates Required

- No doc updates required. These are UI polish fixes, not feature changes.

## Build & Test Commands

```bash
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
```
