# Favorites Screen Visual Improvements -- Implementation Plan

## Summary

Two visual improvements to the Favorites screen: (1) hide the favorite star toggle icon on channel cards since users already know these are favorites, and (2) replace the basic empty state with a polished, visually appealing design featuring a larger icon area with a decorative background circle, improved typography hierarchy, and better spacing.

## Decisions Made

### 1. Star icon behavior on Favorites screen
- **Decision**: Make the `onToggleFavorite` callback and `isFavorite` display optional on `ChannelCardList` and `ChannelCardSquare` by adding a `showFavoriteButton: Boolean = true` parameter. When `false`, the star `IconButton` is not rendered. The Favorites screen passes `showFavoriteButton = false`.
- **Rationale**: The Stitch design (screen.png) shows a filled star on cards, but user request explicitly says to remove it -- the star is redundant on a screen where every item is already a favorite. Making it a parameter rather than removing it entirely preserves the star for Channels and Home screens where it serves a purpose. A parameter approach is cleaner than passing a no-op lambda, because it avoids rendering an invisible but still-clickable touch target.
- **Alternatives considered**: (a) Pass empty lambda `{}` for `onToggleFavorite` -- rejected because it still renders a clickable icon that does nothing, which is confusing. (b) Create separate card composables without star -- rejected as code duplication. (c) Keep star as "remove from favorites" action -- rejected per user request to remove it entirely.

### 2. Empty state design approach
- **Decision**: Build a custom `FavoritesEmptyState` composable with: a decorative circular background behind a star icon (using `primaryContainer` color), a clear "No favorites yet" title, a helpful subtitle about how to add favorites, and generous vertical spacing. No animation -- keep it simple and performant.
- **Rationale**: The Stitch HTML comments show a simple icon+text empty state, but the user wants something "beautiful." A decorative circle behind the icon is a common Material Design pattern that adds visual polish without complexity. Avoiding animations keeps the implementation simple, avoids platform-specific concerns, and prevents battery drain on the empty state which users might leave open.
- **Alternatives considered**: (a) Lottie/animated illustration -- rejected because it requires a new dependency and platform-specific setup for KMP. (b) Gradient background on the whole empty state -- rejected as visually noisy. (c) SVG/vector illustration -- rejected because creating custom illustrations is out of scope and Material icons are sufficient.

### 3. Extract empty state as reusable component vs inline
- **Decision**: Keep `FavoritesEmptyState` as a private composable inside `FavoritesScreen.kt` rather than extracting to `common/components/`.
- **Rationale**: This empty state is specific to the Favorites screen (specific copy, specific icon). There is no other screen that needs the same empty state. If a generic empty state pattern emerges later, it can be extracted then. The `SearchEmptyState` in `SearchComponents.kt` is reusable because multiple screens use it with the same pattern; the favorites empty state is not.
- **Alternatives considered**: Extract to `common/components/EmptyState.kt` with configurable title/subtitle/icon -- over-engineering for a single use case right now.

### 4. Compatibility with channel-view-mode feature
- **Decision**: This plan does NOT touch the `ChannelsList` composable layout (grid vs list). It only modifies the `showFavoriteButton` parameter passed to cards and replaces the `EmptyState` composable body. The channel-view-mode feature may later change Favorites to support grid/list toggle, but that is out of scope here.
- **Rationale**: The channel-view-mode prep explicitly states "Scope limited to Channels screen only" and "Favorites stays as a list." No conflict.

## Current State

### Existing files and relevant code

- **`shared/src/commonMain/.../common/components/ChannelCard.kt`**
  - `ChannelCardSquare` (line 41): Has `isFavorite: Boolean` and `onToggleFavorite: () -> Unit` as required parameters. Star `IconButton` rendered at lines 102-115.
  - `ChannelCardList` (line 134): Has `isFavorite: Boolean` and `onToggleFavorite: () -> Unit` as required parameters. Star `IconButton` rendered at lines 191-203.
  - No mechanism to hide the star button.

- **`shared/src/commonMain/.../feature/favorites/FavoritesScreen.kt`**
  - `EmptyState()` (line 127): Basic implementation with 72dp star icon, single title, single subtitle. No decorative elements.
  - `ChannelsList` (line 160): Calls `ChannelCardList` with `isFavorite = true` and `onToggleFavorite` that dispatches `FavoritesEvent.OnToggleFavorite`. This event triggers unfavoriting.
  - FavoritesScreen currently always uses list layout (not grid).

- **`shared/src/commonMain/.../feature/channels/ChannelsScreen.kt`**: Calls both `ChannelCardSquare` (line 195) and `ChannelCardList` (line 230) with favorite toggle -- these should continue to show the star (no changes needed here).

- **`shared/src/commonMain/.../feature/home/HomeScreen.kt`**: Calls `ChannelCardSquare` (lines 407, 432) with favorite toggle -- should continue to show the star (no changes needed here).

## Changes Required

### Modified Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ChannelCard.kt`

**What changes**: Add `showFavoriteButton: Boolean = true` parameter to both `ChannelCardSquare` and `ChannelCardList`. Wrap the star `IconButton` in a conditional that checks this flag.

**Why**: Allows the Favorites screen to hide the star while all other screens continue showing it without any changes (default is `true`).

**Specific modifications**:

For `ChannelCardSquare` (line 41), add parameter:
```kotlin
@Composable
fun ChannelCardSquare(
    name: String,
    logoUrl: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    category: String? = null,
    showLiveBadge: Boolean = false,
    showFavoriteButton: Boolean = true,
)
```

Wrap the `IconButton` block (lines 102-115) in `if (showFavoriteButton) { ... }`.

For `ChannelCardList` (line 134), add parameter:
```kotlin
@Composable
fun ChannelCardList(
    name: String,
    logoUrl: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showFavoriteButton: Boolean = true,
)
```

Wrap the `IconButton` block (lines 191-203) in `if (showFavoriteButton) { ... }`.

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesScreen.kt`

**What changes**: Two modifications:

**(a) ChannelsList -- hide star button**

In `ChannelsList` composable (line 169), add `showFavoriteButton = false` to the `ChannelCardList` call. Also change `onToggleFavorite` to an empty lambda `{}` since the button won't be visible (the parameter is still required by the function signature):

```kotlin
ChannelCardList(
    name = channel.name,
    logoUrl = channel.logoUrl,
    isFavorite = true,
    onClick = { onEvent(FavoritesEvent.OnChannelClick(channel.id)) },
    onToggleFavorite = {},
    showFavoriteButton = false,
)
```

**(b) EmptyState -- replace with polished design**

Replace the `EmptyState()` composable (lines 127-157) with a new `FavoritesEmptyState()` composable featuring:

```kotlin
@Composable
private fun FavoritesEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            // Decorative circle behind the star icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.StarOutline,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No favorites yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap the star icon on any channel to add it here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
```

**New imports needed** in `FavoritesScreen.kt`:
- `androidx.compose.foundation.background`
- `androidx.compose.foundation.shape.CircleShape`
- `androidx.compose.material.icons.outlined.StarOutline`
- `androidx.compose.ui.text.style.TextAlign`

**Removed imports** (no longer used after changes):
- `androidx.compose.material.icons.filled.Star` (only used in old EmptyState; check if still needed elsewhere in file -- it is not)

Update the call site at line 117 from `EmptyState()` to `FavoritesEmptyState()`.

### No New Files

All changes fit within existing files.

### No Database Changes

### No DI Changes

## Implementation Order

1. **Modify `ChannelCard.kt`**: Add `showFavoriteButton` parameter to both `ChannelCardSquare` and `ChannelCardList` with default `true`. Wrap star `IconButton` blocks in conditional. This is backward-compatible -- all existing call sites continue working without changes.

2. **Modify `FavoritesScreen.kt`**: (a) Pass `showFavoriteButton = false` to `ChannelCardList` in `ChannelsList`. (b) Replace `EmptyState()` with `FavoritesEmptyState()` featuring the decorative circle design. Update imports.

3. **Run formatting**: `./gradlew formatAll` to ensure code style compliance.

4. **Build and test**: `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug` to verify compilation and existing tests pass.

## Testing Strategy

### Manual Testing
- **Favorites with channels**: Verify star icon is no longer visible on channel cards. Verify tapping a card still navigates to player. Verify cards look correct without the trailing star (name column should expand to fill available space due to `weight(1f)`).
- **Favorites empty state**: Remove all favorites, verify the new empty state renders with decorative circles, title "No favorites yet", and subtitle text. Verify it looks good in both light and dark themes.
- **Channels screen**: Verify star toggle still appears and functions on both grid and list channel cards.
- **Home screen**: Verify star toggle still appears and functions on square channel cards.

### Edge Cases
- Dark mode: `primaryContainer` colors should look correct in both themes (they come from the dynamic color scheme).
- RTL layouts: `TextAlign.Center` handles RTL correctly. The decorative circles are centered, so no RTL issues.
- Large font / accessibility: `headlineSmall` and `bodyMedium` scale with system font size. The `padding(32.dp)` provides breathing room.

### No Unit Tests Needed
These are purely visual changes (hiding a UI element, restyling an empty state). No business logic, state management, or data flow changes. Existing tests for `FavoritesViewModel` continue to pass unchanged.

## Doc Updates Required

No documentation updates are required. This is a visual polish change that does not affect product behavior, navigation flows, or architecture. The feature docs describe functional behavior, not visual styling details.

## Build & Test Commands

```bash
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
./gradlew formatAll
```
