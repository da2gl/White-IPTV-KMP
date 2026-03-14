# Validation: Playlist Management

## Status: 🔄 REWORK NEEDED

## Checklist

### Plan vs Implementation

- [x] `RenamePlaylistUseCase.kt` — Created, matches plan exactly (validate blank, trim, lookup, update)
- [x] `DeletePlaylistUseCase.kt` — Created, matches plan exactly (delete + check hasPlaylist)
- [x] `PlaylistSettingsBottomSheet.kt` — Created with correct actions (Rename, Update, Delete, View URL), correct icon/color, hides Update for `file://` playlists
- [x] `HomeMvi.kt` — All 6 state fields added, all 12 events added, `NavigateToOnboarding` action added
- [x] `HomeViewModel.kt` — All 3 new use case dependencies added, all event handlers implemented
- [x] `HomeScreen.kt` — `onNavigateToOnboarding` param, gear icon wiring, bottom sheet, 3 dialogs, snackbar, loading overlay all present
- [x] `MainScreen.kt` — `onNavigateToOnboarding` param added, passed to `HomeScreen`
- [x] `NavGraph.kt` — `onNavigateToOnboarding` wired with `popUpTo(Route.Main) { inclusive = true }`
- [x] `KoinModule.kt` — `factoryOf(::RenamePlaylistUseCase)` and `factoryOf(::DeletePlaylistUseCase)` added
- [ ] ❌ **`docs/constraints/current-limitations.md`** — "Playlist management actions not implemented" section still present (lines 63-68). Plan says to remove it after implementation.

### Code Quality

- [x] MVI pattern followed correctly (State/Event/Action in HomeMvi.kt, BaseViewModel in HomeViewModel)
- [x] UseCase pattern correct — `suspend operator fun invoke()`, factory scope in Koin
- [x] Koin registrations present and correct (both use cases registered as factory)
- [x] Navigation wired correctly (Main → Onboarding with inclusive popUpTo)
- [x] No `Dispatchers.IO` in commonMain — uses `viewModelScope.launch` (Default dispatcher)
- [x] Error handling uses `runCatching` with `.onSuccess`/`.onFailure` in all 3 handlers
- [x] Gear icon disabled when `PlaylistSelection.All` selected (`enabled = isPlaylistSettingsEnabled`)
- [x] Deviation from plan justified — `viewState.copy()` in init block instead of creating fresh HomeState is correct (preserves dialog state during reactive updates)
- [x] `selectedPlaylist!!` non-null assertions in HomeScreen.kt are safe (guarded by `&& selectedPlaylist != null`)

### Test Coverage

- [x] `RenamePlaylistUseCase` — 7 tests covering success, trim, preserve fields, blank, whitespace, NotFound, validation failure
- [x] `DeletePlaylistUseCase` — 6 tests covering remove, last returns true, non-last returns false, correct ID, edge cases
- [x] `HomeViewModel` — 16 tests covering all playlist management event flows (settings, rename, delete, view URL, error dismiss, init)
- [x] FakePlaylistRepository properly implements all PlaylistRepository methods with state tracking
- [x] StubChannelRepository provides minimal stub for ViewModel tests
- [ ] ⚠️ `ImportPlaylistUseCase` update path not directly tested (acknowledged limitation — class is final, requires many dependencies)
- [x] All 29 tests passing per test report

### Documentation

- [x] `docs/features/playlist-settings.md` — Already complete and accurate, no changes needed
- [ ] ❌ `docs/constraints/current-limitations.md` — Still lists "Playlist management actions not implemented" — should be removed now that the feature is implemented

### Build & Lint

- [x] Compiles (`compileDebugKotlinAndroid` — BUILD SUCCESSFUL per code-report)
- [x] Tests pass (29/29 per test-report)
- [ ] ⚠️ `assembleDebug` not explicitly confirmed in reports (only `compileDebugKotlinAndroid` was run)
- [ ] ⚠️ `ktlintCheck` and `detekt` results not explicitly mentioned in reports (linter step may have run separately)

### E2E Testing

- ⏭️ Skipped — no emulator available. Manual verification recommended before release.

## Rework Required

1. **Who**: coder
   **What**: Remove the "Playlist management actions not implemented" section from `docs/constraints/current-limitations.md` (lines 62-68, from `---` through the end of that section)
   **Why**: The plan explicitly requires this doc update. The feature is now implemented, so this limitation is no longer accurate.
   **Acceptance**: The section starting with "## Playlist management actions not implemented" and its preceding `---` separator are removed from the file.

## Summary

The implementation is solid and closely follows the plan. All 9 planned files (3 new, 6 modified) are present with correct content. The MVI pattern, UseCase pattern, Koin registration, navigation, error handling, and edge cases are all handled properly. The one justified deviation (using `viewState.copy()` instead of fresh HomeState in init) is actually a bug fix that improves correctness. Test coverage is comprehensive at 29 tests across 3 test files. The only gap is the missing doc update in `current-limitations.md` — a minor but required change per the plan.
