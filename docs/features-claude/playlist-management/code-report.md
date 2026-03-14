# Code Report: Playlist Management

## Files Created
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/RenamePlaylistUseCase.kt` — UseCase to rename playlist display name with validation
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/DeletePlaylistUseCase.kt` — UseCase to delete playlist, returns whether it was the last one
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/components/PlaylistSettingsBottomSheet.kt` — Material3 ModalBottomSheet with Rename, Update, Delete, View URL actions

## Files Modified
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/HomeMvi.kt` — Added 6 state fields, 12 events, and NavigateToOnboarding action
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModel.kt` — Added RenamePlaylistUseCase, DeletePlaylistUseCase, ImportPlaylistUseCase dependencies; added handleRename, handleUpdatePlaylist, handleDelete methods; uses viewState.copy() to preserve reactive state
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt` — Added onNavigateToOnboarding param, gear icon wiring with enabled/disabled state, PlaylistSettingsBottomSheet, RenameDialog (AlertDialog with OutlinedTextField), DeleteConfirmationDialog, ViewUrlDialog (with SelectionContainer), Snackbar for errors, loading overlay for playlist update
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/main/MainScreen.kt` — Added onNavigateToOnboarding param, passed to HomeScreen
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/navigation/NavGraph.kt` — Wired onNavigateToOnboarding from MainScreen to Route.Onboarding with popUpTo(Route.Main, inclusive=true)
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt` — Added factoryOf(::RenamePlaylistUseCase) and factoryOf(::DeletePlaylistUseCase) to useCaseModule

## Deviations from Plan
- **HomeViewModel init block**: Changed from creating fresh HomeState to using `viewState.copy()` in the Flow combine, so that playlist management state fields (showPlaylistSettings, showRenameDialog, etc.) are preserved when the reactive data updates. This is essential — the plan's original approach of creating `HomeState(...)` would reset dialog/bottom sheet state whenever playlists, favorites, or categories update.
- **FavoritesScreen.kt**: Fixed missing imports for `Icons.AutoMirrored.Filled.ArrowBack` and `Icons.Default.Close` — these were left broken by the parallel Light Theme coder and blocked compilation.

## Build Status
✅ Compiles (`./gradlew :composeApp:compileDebugKotlinAndroid` — BUILD SUCCESSFUL)

## Notes
- The gear icon uses `Icons.Default.Settings` (matching existing code) — the plan mentioned this was already a TODO in the original code
- The "Update Playlist" action is hidden for local-file playlists (`url.startsWith("file://")`)
- Snackbar error display uses `LaunchedEffect(state.playlistManagementError)` with auto-dismiss
- After deleting the last playlist, navigation goes to Onboarding with Main popped from back stack
- After deleting a non-last playlist, selection resets to `PlaylistSelection.All`
