# E2E Test Report: Fix Excessive Recomposition on Favorite Toggle

## Environment
- Device: Medium_Phone_API_36.1(AVD) - Android 16
- Android API: 36
- App version: debug (commit b6dc28d)

## Test Results

| # | Scenario | Steps | Expected | Actual | Status |
|---|----------|-------|----------|--------|--------|
| 1 | Channels: favorite toggle | Tap star on "&TV (576p)" | Star fills, scroll preserved | Star filled (blue), scroll preserved, instant response | PASS |
| 2 | Channels: unfavorite toggle | Tap filled star on "&TV (576p)" | Star outlines, scroll preserved | Star outlined (gray), scroll preserved | PASS |
| 3 | Channels: scroll position on toggle | Scroll down to "101tv Malaga", tap star | Scroll position preserved, only star changes | Scroll position perfectly preserved, same items visible | PASS |
| 4 | Cross-screen: add favorite | Favorite on Channels, check Favorites tab | Channel appears in Favorites | "&TV (576p)" appeared in Favorites list | PASS |
| 5 | Cross-screen: remove favorite | Unfavorite on Channels, check Favorites tab | Channel removed from Favorites | "&TV (576p)" removed, only "12 канал" remains | PASS |
| 6 | Search: type query | Open search, type "BBC" | Results appear, no crash | App crashes with FTS MATCH SQL error | FAIL |
| 7 | Smoke: Home tab loads | Tap Home tab | Screen loads with groups | Loaded correctly with Continue Watching, Favorites, News, Sports, Music sections | PASS |
| 8 | Smoke: Favorites tab loads | Tap Favorites tab | Screen loads | Loaded with favorite channels list | PASS |
| 9 | Smoke: Channels tab loads | Tap Channels tab | Screen loads with paged list | Loaded with alphabetical channel list and group filter | PASS |
| 10 | Smoke: Settings tab loads | Tap Settings tab | Screen loads | Loaded with Appearance, Playback, App Behavior, Data & Storage sections | PASS |

## Screenshots

- **01_initial.png**: Home screen at launch showing all sections populated
- **04_channels_tab.png**: Channels list view with star icons (all outline/unfavorited)
- **07_after_favorite_toggle.png**: After tapping star on "&TV (576p)" -- star is now filled blue
- **08_favorites_after_add.png**: Favorites tab showing both "12 канал" and "&TV (576p)"
- **10_channels_after_unfav.png**: After unfavoriting -- star back to outline
- **11_favorites_after_unfav.png**: Favorites tab back to only "12 канал"
- **17_search_bbc_result.png**: Crash dialog "WhiteIPTVKMP keeps stopping"
- **19_channels_scrolled.png**: Channels list scrolled down to "100% News" through "111 TV"
- **21_after_scroll_favorite.png**: After favoriting "101tv Malaga" while scrolled -- scroll position preserved perfectly

## Recomposition Analysis

The app does not include runtime recomposition logging (the `RecompositionConfig.isEnabled` mentioned in the prep is not built into the current debug build). Therefore, recomposition counts could not be measured directly via logs.

However, the behavioral evidence strongly supports that the fix is working:

1. **Scroll position preserved**: Before the fix, `refreshTrigger` would recreate the entire PagingSource, causing the LazyColumn to reset to the top. After the fix, toggling favorite while scrolled to "101tv Malaga" (several pages down) kept the exact same scroll position.
2. **Instant response**: The star icon toggles immediately with no visible delay or flicker, consistent with the optimistic UI overlay approach.
3. **No list flicker**: Other channel cards remain visually identical after toggle -- no visible recomposition artifacts (e.g., image flicker from Coil reloading).

## Issues Found

### Issue 1: Search crashes app (CRITICAL, pre-existing)

- **Severity**: critical
- **Component**: FTS search query in PlaylistDao
- **Steps to reproduce**:
  1. Launch app
  2. Tap search icon (magnifying glass) on Home screen
  3. Type any text (e.g., "BBC") into the search field
  4. App crashes immediately
- **Expected**: Search results appear matching the query
- **Actual**: App crashes with `android.database.SQLException: Error code: 1, message: malformed MATCH expression: ["BBC*]`
- **Root cause**: In `PlaylistDao.kt` line 90, the FTS MATCH expression is:
  ```sql
  WHERE channels_fts MATCH '"' || :query || '*'
  ```
  This produces `"BBC*` which is malformed FTS5 syntax -- the opening double-quote is never closed. The correct syntax should be either `"BBC"*` (quoted prefix) or just `BBC*` (unquoted prefix).
- **Note**: This is a pre-existing bug, NOT introduced by the favorite toggle fix.

## Verdict

**PASS** -- The favorite toggle recomposition fix works correctly. All toggle operations are instant, scroll position is preserved, and cross-screen consistency (Channels <-> Favorites) is maintained.

The search crash (Issue 1) is a separate pre-existing bug unrelated to this fix and should be tracked independently.
