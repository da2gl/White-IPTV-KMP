# Code Report: Add "Add New Playlist" to Playlist Dropdown

## Files Created
None.

## Files Modified
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/HomeMvi.kt` -- Added `OnAddPlaylistClick` event to `HomeEvent` sealed interface.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModel.kt` -- Handle `OnAddPlaylistClick` event by emitting `HomeAction.NavigateToOnboarding`.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/PlaylistDropdown.kt` -- Replaced `DropdownSelector` delegation with inline dropdown implementation. Added `HorizontalDivider` and "+ Add new playlist" menu item with `Icons.Default.Add` leading icon. Added optional `onAddPlaylistClick` parameter.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt` -- Threaded `onAddPlaylistClick` callback through `HomeTopAppBar` and `HomeTopAppBarTitle` down to `PlaylistDropdown`.

## Deviations from Plan
- **`onAddPlaylistClick` is nullable with default `null`** instead of required. The plan only mentioned `PlaylistDropdown` being used on `HomeScreen`, but it is also used on `ChannelsScreen` and `FavoritesScreen`. Making the parameter optional (nullable with default `null`) avoids breaking those call sites while keeping the add-playlist item Home-screen-only. When `null`, the divider and add item are not shown.

## Build Status
- Compilation: passes
- APK build: passes
- Tests: 10 pre-existing failures in `OnboardingViewModelTest` from another feature's uncommitted changes (error-messages feature). All 269 other tests pass. No test failures related to this feature.

## Notes
- The working tree contains uncommitted changes from other features (error-messages, channel-view-mode). These are unrelated to this feature but caused initial build issues that had to be resolved by other concurrent agents.
- The `ChannelsScreen` and `FavoritesScreen` dropdowns do not show the "+ Add new playlist" item since they don't pass the callback. This is intentional per the product spec which only describes this behavior on the Home screen.
