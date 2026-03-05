# Playlist Management — Implementation Plan

## Summary

Add playlist management actions (rename, update, delete, view URL) accessible via a gear icon in the Home screen header. Actions are presented in a Material3 ModalBottomSheet and operate on the currently selected playlist. This feature resolves the "Playlist management actions not implemented" limitation.

## Decisions Made

### 1. Bottom Sheet for Actions
- **Decision**: Use Material3 `ModalBottomSheet` as overlay on Home screen
- **Rationale**: Standard Android/iOS pattern for contextual actions; keeps user in context; no new route needed — managed by HomeState flags
- **Alternatives considered**: Separate screen (too heavy for 4 actions), Dialog (less mobile-native for action lists)

### 2. No Separate UpdatePlaylistUseCase
- **Decision**: Reuse `ImportPlaylistUseCase` for playlist update since it already detects existing playlists by URL and calls `updateExistingPlaylist()` which preserves favorites
- **Rationale**: `ImportPlaylistUseCase.invoke()` (line 100) checks for `existingPlaylist` and handles updates. Creating a wrapper would add no value.
- **Alternatives considered**: New `UpdatePlaylistUseCase` wrapping import — rejected as unnecessary indirection

### 3. Foreign Key Cascades Handle Delete
- **Decision**: Use the existing `PlaylistDao.deletePlaylist(playlistId)` for deletion. No new `deletePlaylistWithData` transaction method needed.
- **Rationale**: All child entities (`ChannelEntity`, `ChannelGroupEntity`) have `ForeignKey(onDelete = CASCADE)` pointing to `PlaylistEntity`. `ChannelGroupCrossRef` has CASCADE on both `ChannelEntity` and `ChannelGroupEntity`. SQLite foreign key cascades execute at the database engine level regardless of whether Room uses `@Delete` or `@Query DELETE` — the raw SQL `DELETE FROM playlists WHERE id = :playlistId` triggers cascades automatically.
- **Alternatives considered**: Manual transaction deleting cross-refs → channels → groups → playlist — rejected as redundant with FK cascades

### 4. Gear Icon Disabled for "All Playlists"
- **Decision**: Disable (gray out) the gear icon when `PlaylistSelection.All` is active
- **Rationale**: Per spec: "The gear icon is disabled when 'All Playlists' is selected (no single playlist to manage)"
- **Alternatives considered**: Hide icon entirely — rejected, consistent presence is better UX

### 5. Error Display via Snackbar-style State
- **Decision**: Use `playlistManagementError: String?` in `HomeState` rendered as a dismissible Snackbar
- **Rationale**: Errors from rename/update/delete are transient — Snackbar is the standard Material3 pattern for transient feedback
- **Alternatives considered**: AlertDialog for errors — too intrusive for operational feedback

## Current State

### Existing code that supports this feature:
- **`PlaylistDao.deletePlaylist()`** (`PlaylistDao.kt:83-84`) — deletes playlist by ID, FK cascades handle children
- **`PlaylistDao.deleteChannelsByPlaylistId()`** (`PlaylistDao.kt:80-81`) — exists but not needed for this feature
- **`PlaylistRepository.deletePlaylist()`** (`PlaylistRepository.kt:20`) — interface method exists
- **`PlaylistRepository.updatePlaylist()`** (`PlaylistRepository.kt:19`) — interface method exists
- **`PlaylistRepositoryImpl.deletePlaylist()`** (`PlaylistRepositoryImpl.kt:44-46`) — implementation exists
- **`PlaylistRepositoryImpl.updatePlaylist()`** (`PlaylistRepositoryImpl.kt:40-42`) — implementation exists
- **`ImportPlaylistUseCase`** (`ImportPlaylistUseCase.kt`) — handles re-import with favorites preservation
- **`HomeScreen` gear icon** (`HomeScreen.kt:120-122`) — exists as `IconButton(onClick = { /* TODO */ })`
- **`PlaylistEntity`** (`PlaylistEntity.kt`) — has `name` and `url` fields needed for rename/view URL
- **`CurrentPlaylistRepository`** (`CurrentPlaylistRepository.kt`) — manages current selection state
- **`BaseViewModel`** (`BaseViewModel.kt`) — standard MVI base with `viewState`/`viewAction`/`clearAction`

### What does NOT exist:
- No `RenamePlaylistUseCase` or `DeletePlaylistUseCase`
- No playlist management UI (bottom sheet, dialogs)
- No playlist management events/actions in `HomeMvi.kt`
- No `NavigateToOnboarding` action
- No `onNavigateToOnboarding` callback in `HomeScreen`/`MainScreen`/`NavGraph`

## Changes Required

### New Files

#### 1. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/RenamePlaylistUseCase.kt`
- **Purpose**: Rename a playlist's display name
- **Key contents**:
```kotlin
class RenamePlaylistUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    /**
     * Rename playlist display name
     * @throws PlaylistException.NotFound if playlist doesn't exist
     * @throws IllegalArgumentException if name is blank
     */
    suspend operator fun invoke(playlistId: Long, newName: String) {
        require(newName.isNotBlank()) { "Playlist name cannot be blank" }
        val playlist = playlistRepository.getPlaylistById(playlistId)
            ?: throw PlaylistException.NotFound(playlistId)
        playlistRepository.updatePlaylist(playlist.copy(name = newName.trim()))
    }
}
```

#### 2. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/DeletePlaylistUseCase.kt`
- **Purpose**: Delete a playlist and check if it was the last one
- **Key contents**:
```kotlin
class DeletePlaylistUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    /**
     * Delete playlist and all associated data (channels, groups, cross-refs cascade via FK)
     * @return true if this was the last playlist (caller should navigate to Onboarding)
     */
    suspend operator fun invoke(playlistId: Long): Boolean {
        playlistRepository.deletePlaylist(playlistId)
        return !playlistRepository.hasPlaylist()
    }
}
```

#### 3. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/components/PlaylistSettingsBottomSheet.kt`
- **Purpose**: Material3 ModalBottomSheet showing playlist actions
- **Key contents**:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSettingsBottomSheet(
    playlist: PlaylistEntity,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    onViewUrl: () -> Unit,
)
```
- **UI layout**:
  - Sheet title: playlist name (`titleMedium` typography)
  - Action rows with leading icon + label text, clickable:
    - `Icons.Default.Edit` + "Rename"
    - `Icons.Default.Refresh` + "Update Playlist" — **hidden** when `playlist.url.startsWith("file://")` (local file playlists have no source URL)
    - `Icons.Default.Delete` + "Delete Playlist" — uses `MaterialTheme.colorScheme.error` color
    - `Icons.Default.Link` + "View URL"
  - Uses `ModalBottomSheet` with `rememberModalBottomSheetState()`

### Modified Files

#### 4. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/HomeMvi.kt`
- **What changes**: Add playlist management state fields, events, and actions
- **HomeState additions**:
  - `showPlaylistSettings: Boolean = false`
  - `showRenameDialog: Boolean = false`
  - `showDeleteConfirmation: Boolean = false`
  - `showViewUrlDialog: Boolean = false`
  - `isUpdatingPlaylist: Boolean = false`
  - `playlistManagementError: String? = null`
- **HomeEvent additions**:
  - `OnPlaylistSettingsClick` — gear icon tapped
  - `OnPlaylistSettingsDismiss` — bottom sheet dismissed
  - `OnRenameClick` — rename action selected in bottom sheet
  - `OnRenameDialogDismiss` — rename dialog cancelled
  - `OnRenameConfirm(newName: String)` — rename confirmed
  - `OnUpdatePlaylistClick` — update action selected
  - `OnDeleteClick` — delete action selected in bottom sheet
  - `OnDeleteDialogDismiss` — delete confirmation cancelled
  - `OnDeleteConfirm` — delete confirmed
  - `OnViewUrlClick` — view URL action selected
  - `OnViewUrlDialogDismiss` — URL dialog dismissed
  - `OnPlaylistManagementErrorDismiss` — error snackbar dismissed
- **HomeAction additions**:
  - `NavigateToOnboarding` — emitted when last playlist deleted

#### 5. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModel.kt`
- **What changes**: Add use case dependencies and event handlers
- **New constructor parameters**:
  - `renamePlaylistUseCase: RenamePlaylistUseCase`
  - `deletePlaylistUseCase: DeletePlaylistUseCase`
  - `importPlaylistUseCase: ImportPlaylistUseCase`
- **New event handling in `obtainEvent()`**: Handle all new `HomeEvent` variants
- **New private methods**:
  - `handleRename(newName: String)` — calls `renamePlaylistUseCase`, closes dialog on success, shows error on failure
  - `handleUpdatePlaylist()` — calls `importPlaylistUseCase(PlaylistSource.Url(playlist.url))`, shows loading/error state
  - `handleDelete()` — calls `deletePlaylistUseCase`, emits `NavigateToOnboarding` if last playlist, otherwise selects `PlaylistSelection.All`
  - `getSelectedPlaylist(): PlaylistEntity?` — helper to get currently selected playlist from state

#### 6. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`
- **What changes**:
  - Add `onNavigateToOnboarding: () -> Unit` parameter to `HomeScreen()`
  - Handle `HomeAction.NavigateToOnboarding` in `LaunchedEffect`
  - Wire gear icon `onClick` to `viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsClick)`
  - Disable gear icon when `state.selection is PlaylistSelection.All`
  - Add `onPlaylistSettingsClick: () -> Unit` and `isPlaylistSettingsEnabled: Boolean` params to `HomeTopAppBar`
  - Conditionally show `PlaylistSettingsBottomSheet` when `state.showPlaylistSettings && selectedPlaylist != null`
  - Show `AlertDialog` for rename (with `OutlinedTextField`, pre-filled name, "Rename" disabled when blank)
  - Show `AlertDialog` for delete confirmation ("Delete '{name}'? All channels and groups will be removed.")
  - Show `AlertDialog` for view URL (read-only selectable text, "Close" button)
  - Show `Snackbar` via `SnackbarHost` for `state.playlistManagementError`
  - Show loading indicator overlay when `state.isUpdatingPlaylist`

#### 7. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/main/MainScreen.kt`
- **What changes**: Add `onNavigateToOnboarding: () -> Unit` parameter and pass to `HomeScreen`
- **Why**: HomeScreen needs to navigate to Onboarding when last playlist is deleted; MainScreen is the intermediary between HomeScreen and NavGraph

#### 8. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/navigation/NavGraph.kt`
- **What changes**: Pass `onNavigateToOnboarding` lambda to `MainScreen` that navigates to `Route.Onboarding` with `popUpTo(Route.Main) { inclusive = true }`
- **Why**: When last playlist is deleted, app should navigate from Main to Onboarding, clearing Main from back stack

#### 9. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt`
- **What changes**: Add `factoryOf(::RenamePlaylistUseCase)` and `factoryOf(::DeletePlaylistUseCase)` to `useCaseModule`
- **Why**: Register new use cases for Koin auto-injection into HomeViewModel

### Database Changes

**None required.** All needed DAO methods already exist:
- `deletePlaylist(playlistId)` — deletes playlist, FK cascades delete channels/groups/cross-refs
- `updatePlaylist(playlist)` — used by rename (updates the entity with new name)
- `getPlaylistById(playlistId)` — used by rename to fetch current entity

### DI Changes

Add to `useCaseModule` in `KoinModule.kt`:
```kotlin
factoryOf(::RenamePlaylistUseCase)
factoryOf(::DeletePlaylistUseCase)
```

`ImportPlaylistUseCase` is already registered — Koin will auto-inject it into `HomeViewModel`'s constructor.

## Implementation Order

1. **`RenamePlaylistUseCase.kt`** — Create new file (no dependencies on other new code)
2. **`DeletePlaylistUseCase.kt`** — Create new file (no dependencies on other new code)
3. **`KoinModule.kt`** — Register new use cases (2 lines added)
4. **`HomeMvi.kt`** — Add state fields, events, action (pure data changes)
5. **`HomeViewModel.kt`** — Add use case dependencies and event handlers
6. **`PlaylistSettingsBottomSheet.kt`** — Create new composable
7. **`HomeScreen.kt`** — Wire gear icon, bottom sheet, dialogs, snackbar
8. **`MainScreen.kt`** — Add `onNavigateToOnboarding` callback
9. **`NavGraph.kt`** — Wire onboarding navigation from Main

## Testing Strategy

### Unit Tests
- **`RenamePlaylistUseCase`**: rename success, rename with blank name throws, rename nonexistent playlist throws NotFound
- **`DeletePlaylistUseCase`**: delete returns false when other playlists remain, delete returns true when last playlist deleted
- **`HomeViewModel`**: gear icon click sets `showPlaylistSettings = true`, rename flow updates state correctly, delete last playlist emits `NavigateToOnboarding`, update playlist shows/hides loading state

### Edge Cases
| Edge Case | Handling |
|-----------|----------|
| Gear icon when "All Playlists" selected | Disabled (grayed out), not clickable |
| Delete last playlist | `DeletePlaylistUseCase` returns `true` → emit `NavigateToOnboarding` |
| Update local-file playlist | "Update Playlist" row hidden in bottom sheet (`url.startsWith("file://")`) |
| Rename to blank name | "Rename" button disabled when TextField is blank; UseCase has `require()` guard |
| Rename to whitespace-only name | `trim()` + `isNotBlank()` prevents this |
| Network error during update | `playlistManagementError` set, `isUpdatingPlaylist` reset to false |
| DB error during delete | `playlistManagementError` set, confirmation dialog dismissed |
| Bottom sheet open, then playlist updated externally | Reactive `getPlaylists()` Flow updates state; bottom sheet references current state |
| Only one playlist, it's selected, user deletes it | Navigate to Onboarding with back stack cleared |

## Doc Updates Required

- **`docs/features/playlist-settings.md`** — Already complete, no changes needed
- **`docs/constraints/current-limitations.md`** — Remove "Playlist management actions not implemented" section after implementation
- **`docs/constraints/open-questions.md`** — No playlist-related open questions to remove (the "Playlist Settings UI" question was already resolved)
