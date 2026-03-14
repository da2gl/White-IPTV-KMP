# Code Report: Fix Favorites Toggle

## Files Created
None.

## Files Modified
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsViewModel.kt` -- Added `refreshTrigger` MutableStateFlow, included it in `combine()` for `pagedChannels`, and incremented it after successful `toggleFavorite()` call.

## Deviations from Plan
None. Implementation follows the plan exactly.

## Build Status
Compiles and tests pass.

## Notes
The fix causes `flatMapLatest` to re-execute after each favorite toggle, creating a fresh Pager/PagingSource that reads updated `isFavorite` values from Room. Scroll position is preserved by Pager's anchor-based re-fetch mechanism.
