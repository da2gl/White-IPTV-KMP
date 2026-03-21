# Code Report: UI Dropdowns to Bottom Sheets & Chips

## Files Created
- `shared/src/commonMain/.../common/components/SelectionBottomSheet.kt` -- Generic reusable bottom sheet for single-option selection with radio buttons and optional leading content per option
- `shared/src/commonMain/.../common/components/GroupFilterChips.kt` -- Horizontal scrollable LazyRow of FilterChips for group selection with checkmark on selected chip

## Files Modified
- `shared/src/commonMain/.../common/components/PlaylistDropdown.kt` -- Replaced DropdownMenu with inline ModalBottomSheet; kept same function signature; added "Select Playlist" title, radio button indicators, and "Add new playlist" action row
- `shared/src/commonMain/.../feature/settings/components/SettingsComponents.kt` -- Rewrote SettingsDropdownRow to use SelectionBottomSheet instead of DropdownMenu; changed trailing icon from ExpandMore to ChevronRight; added optional `optionLeadingContent` parameter
- `shared/src/commonMain/.../feature/settings/SettingsScreen.kt` -- Added `optionLeadingContent` lambda to Accent Color row showing colored circle previews; added private accent color preview helper and constants
- `shared/src/commonMain/.../feature/channels/ChannelsScreen.kt` -- Replaced GroupDropdown with GroupFilterChips; updated import; removed horizontal padding (chips have their own contentPadding)
- `docs/features/channel-browsing.md` -- Changed "Group dropdown" to describe horizontal scrollable filter chips
- `docs/features/settings.md` -- Added note that settings option rows open ModalBottomSheet instead of inline dropdown
- `docs/features/home.md` -- Changed "dropdown" to "bottom sheet" for playlist selector
- `docs/features/favorites.md` -- Changed "Playlist dropdown" to describe bottom sheet selector

## Files Deleted
- `shared/src/commonMain/.../common/components/DropdownSelector.kt` -- Only used by GroupDropdown, which was replaced
- `shared/src/commonMain/.../common/components/GroupDropdown.kt` -- Replaced by GroupFilterChips
- `shared/src/commonMain/.../feature/settings/components/SettingsDropdownItem.kt` -- Already had zero usages (dead code)

## Deviations from Plan
- `GroupFilterChips` modifier does not include horizontal padding directly; instead it uses `contentPadding = PaddingValues(horizontal = 16.dp)` inside the LazyRow, which is functionally equivalent but avoids double-padding at the call site.

## Build Status
- assembleDebug: PASS
- testAndroidHostTest: Pre-existing compilation failure in `SettingsViewModelTest.kt` (references removed `cacheSize` property) -- unrelated to this feature, confirmed same failure on master branch.

## Notes
- All 9 dropdown instances (3 PlaylistDropdown, 6 SettingsDropdownRow) are migrated to bottom sheets.
- Group selection on ChannelsScreen now uses horizontal FilterChip row.
- No DI, ViewModel, or database changes were needed -- this is a pure UI refactoring.
- The SelectionBottomSheet component is reusable for future selection UIs.
