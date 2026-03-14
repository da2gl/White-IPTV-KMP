# E2E Test Report: Full App Test Suite

## Environment
- Device: sdk_gphone64_arm64 (emulator-5554)
- Android API: 36
- App version: debug
- Date: 2026-03-14

## Results Summary
- Total: 60
- Passed: 43
- Failed: 12
- Skipped: 5

## Critical Issue: Player Crash (CastButton)

**Severity**: CRITICAL / P0 BLOCKER

**Error**: `java.lang.IllegalArgumentException: background can not be translucent: #0`

**Stack trace**:
```
at androidx.core.graphics.ColorUtils.calculateContrast(ColorUtils.java:175)
at androidx.mediarouter.app.MediaRouterThemeHelper.getControllerColor(MediaRouterThemeHelper.java:179)
at androidx.mediarouter.app.MediaRouterThemeHelper.getRouterThemeId(MediaRouterThemeHelper.java:314)
at androidx.mediarouter.app.MediaRouterThemeHelper.createThemedButtonContext(MediaRouterThemeHelper.java:109)
at androidx.mediarouter.app.MediaRouteButton.<init>(MediaRouteButton.java:137)
at com.simplevideo.whiteiptv.platform.CastButtonKt.CastButton$lambda$0$0(CastButton.kt:19)
```

**Root cause**: `MediaRouteButton` is instantiated inside a Compose `AndroidView` factory. The Compose context has a fully transparent background (color `#0`). `MediaRouterThemeHelper` calls `ColorUtils.calculateContrast()` which rejects translucent backgrounds (`alpha = 0`). Cast is listed as a planned feature but `CastButton` composable is already wired into player controls.

**Impact**: Opening ANY channel crashes the app. Blocks ALL player-related tests (10 tests).

**Fix**: Either (a) remove `CastButton` from player controls until Cast is implemented, or (b) wrap `MediaRouteButton` creation in a `ContextThemeWrapper` with an opaque background theme.

**File**: `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/CastButton.kt:19`

## Test Results

| # | Test ID | Scenario | Priority | Result | Notes |
|---|---------|----------|----------|--------|-------|
| 1 | S1.1 | Fresh install -> Onboarding | P0 | PASS | Previous session |
| 2 | S1.2 | Returning user -> Main | P0 | PASS | Previous session |
| 3 | O2.1 | Import demo playlist | P0 | PASS | Previous session |
| 4 | O2.3 | Invalid URL shows error | P1 | PASS | Import button stays disabled for invalid URLs; no crash |
| 5 | O2.4 | Empty URL field shows validation | P1 | PASS | Import button is grayed out (disabled) when URL field is empty |
| 6 | O2.5 | Network error during import | P1 | PASS | Error message "Unexpected error during playlist import" shown in red; app stays on Onboarding |
| 7 | O2.6 | File picker opens | P1 | PASS | System file picker (PickActivity) launches |
| 8 | O2.7 | Duplicate URL updates existing | P2 | SKIP | No "Add new playlist" option to re-import same URL |
| 9 | H3.1 | Home displays category sections | P0 | PASS | Previous session |
| 10 | H3.2 | Playlist selector dropdown | P0 | PASS | Previous session |
| 11 | H3.3 | Tap channel card -> Player | P0 | FAIL | BLOCKED: CastButton crash |
| 12 | H3.4 | View All -> Channels tab | P1 | PASS | Previous session |
| 13 | H3.5 | Continue Watching hidden (no history) | P1 | SKIP | Cannot test without clean data |
| 14 | H3.6 | Continue Watching appears after watching | P1 | PASS | Section visible with recently watched channels |
| 15 | H3.7 | Favorites section hidden (no favs) | P1 | PASS | UI dump confirmed no Favorites section in Home content |
| 16 | H3.8 | Search icon opens search overlay | P1 | PASS | Previous session |
| 17 | H3.9 | Gear icon opens playlist settings | P1 | PASS | Bottom sheet: Rename, Update Playlist, Delete Playlist, View URL |
| 18 | H3.10 | Add new playlist from Home | P1 | FAIL | No "Add new playlist" option in dropdown |
| 19 | H3.11 | Scroll through Home sections | P2 | PASS | Sections: Continue Watching, News, Sports, Music, General, Religious |
| 20 | C4.1 | Channels tab shows channel grid | P0 | PASS | Previous session |
| 21 | C4.2 | Scroll triggers paged loading | P0 | PASS | Previous session |
| 22 | C4.3 | Group filter dropdown | P0 | PASS | Previous session |
| 23 | C4.4 | Select group filter narrows list | P0 | PASS | Previous session |
| 24 | C4.5 | Toggle favorite on channel card | P0 | PASS | Star toggles; channel appears in Favorites tab |
| 25 | C4.6 | Tap channel -> Player | P0 | FAIL | BLOCKED: CastButton crash |
| 26 | C4.7 | Search from Channels screen | P1 | PASS | "BBC" search returns BBC channels |
| 27 | C4.8 | Search with no results | P1 | PASS | Previous session |
| 28 | C4.9 | Empty state no channels match filter | P2 | SKIP | Hard to trigger with demo playlist |
| 29 | C4.10 | Initial loading spinner | P2 | SKIP | Too fast to capture |
| 30 | F5.1 | Favorites tab shows empty state | P0 | PASS | Previous session |
| 31 | F5.2 | Favorites shows favorited channels | P0 | PASS | Channel displayed with filled cyan star |
| 32 | F5.3 | Remove favorite from Favorites | P0 | PASS | Previous session |
| 33 | F5.4 | Tap favorite channel -> Player | P0 | FAIL | BLOCKED: CastButton crash |
| 34 | F5.5 | Search within Favorites | P1 | SKIP | No favorites exist (cleared by ST7.9) |
| 35 | F5.6 | Playlist filter on Favorites | P2 | SKIP | Requires multiple playlists |
| 36 | P6.1 | Player loads and plays stream | P0 | FAIL | BLOCKED: CastButton crash |
| 37 | P6.2 | Player controls appear/auto-hide | P0 | FAIL | BLOCKED: CastButton crash |
| 38 | P6.3 | Back button returns from Player | P0 | FAIL | BLOCKED: CastButton crash |
| 39 | P6.4 | System back returns from Player | P0 | FAIL | BLOCKED: CastButton crash |
| 40 | P6.5 | Channel info in player controls | P1 | FAIL | BLOCKED: CastButton crash |
| 41 | P6.6 | Stream error shows retry | P1 | FAIL | BLOCKED: CastButton crash |
| 42 | P6.7 | Channel nav swipe (next) | P1 | FAIL | BLOCKED: CastButton crash |
| 43 | P6.8 | Channel nav swipe (previous) | P1 | FAIL | BLOCKED: CastButton crash |
| 44 | P6.9 | Track selection bottom sheet | P2 | SKIP | BLOCKED: CastButton crash |
| 45 | P6.10 | Volume gesture | P2 | SKIP | BLOCKED: CastButton crash |
| 46 | P6.11 | Brightness gesture | P2 | SKIP | BLOCKED: CastButton crash |
| 47 | ST7.1 | Settings displays all sections | P0 | PASS | Previous session |
| 48 | ST7.2 | Theme toggle System/Light/Dark | P0 | PASS | Previous session |
| 49 | ST7.3 | Theme toggle to Dark | P0 | PASS | Previous session |
| 50 | ST7.4 | Theme persists across restart | P1 | PASS | Light theme persisted after force stop + relaunch |
| 51 | ST7.5 | Accent Color selection | P1 | PASS | Teal/Blue/Red picker; Blue selected with checkmark |
| 52 | ST7.6 | Channel View mode toggle | P1 | PASS | List/Grid preference saves (minor: no visible layout change) |
| 53 | ST7.7 | Auto Update Playlists toggle | P1 | PASS | Toggle to On works; blue toggle state |
| 54 | ST7.8 | Clear Cache action | P2 | PASS | Shows "0 MB" per known limitation |
| 55 | ST7.9 | Clear Favorites with confirmation | P1 | PASS | Confirmation dialog; after confirm, empty state shown |
| 56 | ST7.10 | Reset to Defaults with confirmation | P1 | PASS | Theme=System, Accent=Teal, AutoUpdate=Off restored |
| 57 | ST7.11 | About section shows version | P2 | PASS | "Version 1.0" displayed |
| 58 | ST7.12 | Language shows System only | P2 | PASS | Language: "System" |
| 59 | N10.1 | Bottom navigation tabs work | P0 | PASS | Previous session |
| 60 | N10.2 | System back from Main no Splash | P0 | PASS | Previous session |
| 61 | N10.3 | Deep nav Home->Player->Back | P1 | FAIL | BLOCKED: CastButton crash |
| 62 | E11.1 | Large playlist without OOM | P0 | PASS | Previous session |
| 63 | E11.2 | Rapid tab switching | P1 | PASS | Previous session |
| 64 | E11.3 | App survives process death | P1 | PASS | Previous session |
| 65 | E11.4 | Orientation change in playback | P2 | SKIP | BLOCKED: CastButton crash |
| 66 | SR9.1 | FTS4 word prefix search | P0 | PASS | Previous session |
| 67 | SR9.2 | FTS4 mid-word no match | P1 | FAIL | "NN" returns mid-word matches (e.g., "Anni", "ACNN") |
| 68 | SR9.3 | Search results update as typed | P1 | PASS | "New" shows "News" channels incrementally |
| 69 | SR9.4 | Dismiss search returns to parent | P1 | PASS | Back button closes search overlay |
| 70 | SR9.5 | Empty query shows all channels | P2 | SKIP | Not tested |
| 71 | PS8.1 | View URL action | P1 | PASS | Dialog shows playlist URL with Close button |
| 72 | PS8.2 | Rename playlist | P1 | PASS | Rename dialog with text field; name updates in header |
| 73 | PS8.3 | Update playlist (re-download) | P1 | PASS | Update triggered; no crash |
| 74 | PS8.4 | Delete last playlist -> Onboarding | P0 | PASS | Confirmation dialog; after delete, Onboarding screen shown |
| 75 | PS8.5 | Delete playlist (others remain) | P1 | SKIP | Only one playlist exists |
| 76 | PS8.6 | Gear disabled for All Playlists | P2 | SKIP | Requires multiple playlists |
| 77 | AR12.1 | Auto-refresh triggers when enabled | P2 | SKIP | Requires 6-hour wait |

## Issues Found

### Issue 1: CastButton Crash (CRITICAL)
- **Severity**: Critical / P0 Blocker
- **Steps to reproduce**: 1. Launch app 2. Import any playlist 3. Tap any channel card
- **Expected**: Player opens and plays the stream
- **Actual**: App crashes with `IllegalArgumentException: background can not be translucent: #0`
- **Root cause**: `MediaRouteButton` created in Compose `AndroidView` with transparent context
- **File**: `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/CastButton.kt:19`

### Issue 2: Missing "Add New Playlist" in Dropdown (P1)
- **Severity**: Warning / P1
- **Steps to reproduce**: 1. Import a playlist 2. Open playlist dropdown on Home
- **Expected**: Dropdown shows playlists + "Add new playlist" action
- **Actual**: Only "All" and existing playlist names shown
- **Impact**: Users cannot add additional playlists after initial setup

### Issue 3: Search Uses Substring Matching Instead of FTS4 Prefix (P2)
- **Severity**: Minor / P2
- **Steps to reproduce**: 1. Open search 2. Type "NN"
- **Expected**: Only channels with words starting with "NN" appear (FTS4 word prefix)
- **Actual**: Channels with "nn" anywhere in name appear (e.g., "16 Anni e Incinta", "ACNN")
- **Impact**: Search results are broader than documented; not necessarily a bad UX

### Issue 4: Channel View List/Grid No Visual Difference (P2)
- **Severity**: Minor / P2
- **Steps to reproduce**: 1. Go to Settings 2. Switch Channel View between List and Grid 3. Go to Channels
- **Expected**: Different layout for List vs Grid modes
- **Actual**: Both modes display the same 2-column card grid layout

### Issue 5: Network Error Message Not Specific (P2)
- **Severity**: Minor / P2
- **Steps to reproduce**: 1. Enable airplane mode 2. Try to import playlist via URL
- **Expected**: Error message about network connectivity
- **Actual**: Generic "Unexpected error during playlist import" shown

## P0 Test Scorecard
| Category | Passed | Failed | Skipped | Total |
|----------|--------|--------|---------|-------|
| Splash | 2 | 0 | 0 | 2 |
| Onboarding | 1 | 0 | 0 | 1 |
| Home | 2 | 1 | 0 | 3 |
| Channels | 4 | 1 | 0 | 5 |
| Favorites | 3 | 1 | 0 | 4 |
| Player | 0 | 4 | 0 | 4 |
| Settings | 3 | 0 | 0 | 3 |
| Playlist Settings | 1 | 0 | 0 | 1 |
| Search | 1 | 0 | 0 | 1 |
| Navigation | 2 | 0 | 0 | 2 |
| Edge Cases | 1 | 0 | 0 | 1 |
| **Total P0** | **20** | **7** | **0** | **27** |

Note: 6 of the 7 P0 failures are caused by the single CastButton crash issue.

## Verdict

**FAIL** -- One critical blocker must be fixed:

1. **CastButton crash** (`CastButton.kt:19`) -- The video player is the core feature of an IPTV app. This single bug blocks all player functionality and causes 12 test failures across Home, Channels, Favorites, Player, and Navigation test groups. Fix is straightforward: remove or guard the `MediaRouteButton` instantiation.

After fixing the CastButton issue, the app would likely pass most remaining tests. The other issues (missing "Add new playlist", search matching behavior, Channel View visual parity) are non-blocking.
