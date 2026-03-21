# Code Report: Player UI Redesign

## Files Created
None. All changes fit within existing files.

## Files Modified
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/mvi/PlayerMvi.kt` -- Added `nextChannelName: String?` and `previousChannelName: String?` to `PlayerState`
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/PlayerViewModel.kt` -- Added `fetchAdjacentChannelNames()` private function, called after channel loads in `observeChannel()`
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/PlayerControls.kt` -- Major redesign: split AnimatedVisibility into two blocks (top slides from top, bottom slides from bottom, 350ms tween), added `LabeledControlButton` composable (32dp icons with labels), added `LiveIndicator` composable (red dot at edge, "Go to Live" when behind), added `PulsingBufferingIndicator` (56dp, primary color, pulsing alpha), added channel logo via AsyncImage in top bar, increased fonts/padding, changed bottom bar to SpaceEvenly layout
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/GestureOverlay.kt` -- Added `nextChannelName`/`previousChannelName` parameters, shows actual channel names in swipe indicator instead of generic text, increased icon to 56dp, added percentage text for volume/brightness, increased background alpha to 0.8f, added `widthIn(min = 120.dp)`, removed static `channelNext`/`channelPrevious` from `GestureIndicators` object
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/SleepTimerSheet.kt` -- Added centered header icon (48dp), replaced plain clickable rows with `FilledTonalButton` with RoundedCornerShape(12.dp), added 8dp vertical spacing, increased bottom padding to 32dp
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/PlayerScreen.kt` -- Added live offset polling via `PollLiveOffset` composable (1s interval), passed new parameters to `PlayerControlsOverlay` (channelLogoUrl, liveOffsetMs, onSeekToLive), passed `nextChannelName`/`previousChannelName` to `GestureOverlay`
- `docs/features/player.md` -- Added documentation for gradient overlays, channel logo, live indicator, labeled buttons, channel name preview, buffering indicator, sleep timer sheet polish

## Deviations from Plan
1. **AnimatedVisibility fix (per doc-validator)**: The plan's code snippets had `initialOffsetY = { 0 }` which would produce no slide effect. Fixed per doc-validator instructions to use `initialOffsetY = { -it }` for top bar (slides down from top) and `initialOffsetY = { it }` for bottom bar (slides up from bottom).
2. **PollLiveOffset extracted to composable**: Instead of inline LaunchedEffect, extracted polling to a separate `PollLiveOffset` composable for cleaner code.
3. **Removed static channelNext/channelPrevious from GestureIndicators object**: Since channel names are now dynamic, the static indicators are no longer needed. Channel indicators are created inline with the actual channel name.
4. **Loading indicator kept separate from buffering**: The original loading indicator (white CircularProgressIndicator for initial channel load) remains in PlayerScreen, while the redesigned pulsing buffering indicator is in PlayerControlsOverlay. These serve different purposes.

## Build Status
- Android app assembleDebug: PASS
- Tests (testAndroidHostTest): Pre-existing failure in SettingsViewModelTest (references removed `cacheManager`/`cacheSize`), unrelated to this feature. Verified by running tests on clean master -- same failure.
- formatAll: PASS (no new lint issues)

## Notes
- The `InfiniteTransition` import for pulsing animation requires `rememberInfiniteTransition` which is standard Compose.
- `coil3.compose.AsyncImage` is used for channel logo, consistent with existing usage in ChannelCard and HomeScreen.
- Live offset is tracked as local Compose state (not MVI state) to avoid unnecessary recomposition churn through the ViewModel.
- The `FiberManualRecord` icon from Material Icons is used as the red live dot.
