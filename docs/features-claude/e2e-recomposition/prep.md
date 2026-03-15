# E2E Test Plan: All Screens + Recomposition Tracking

## Summary

End-to-end test plan covering visual verification of recomposition tracking (red borders + Kermit logs) on all screens, recomposition analysis to identify performance issues, and a smoke test of all recent features. The recomposition tracking system uses a custom `Modifier.trackRecomposition()` and `LogRecomposition()` composable gated by `RecompositionConfig.isEnabled = true`. Logs are emitted via Kermit with tag "Recomposition" using `Logger.d`.

## Decisions Made

### Decision 1: Log Tag Filter

- **Decision**: Use `--tag "Recomposition"` for log filtering. The `LogRecomposition()` function uses `Logger.d("Recomposition") { ... }` (Kermit tag, not Android logcat tag). On Android, Kermit maps this to logcat tag "Recomposition".
- **Rationale**: The `RecompositionTracker.kt` at line 39 uses `Logger.d("Recomposition")` which becomes the logcat tag.

### Decision 2: CastButton Crash Status

- **Decision**: The CastButton crash from the previous E2E report has been fixed. `StreamingButton.kt` now wraps `MediaRouteButton` in a `ContextThemeWrapper` with `R.style.CastButtonTheme` providing an opaque background. Player tests should now be runnable.
- **Rationale**: Reading `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/StreamingButton.kt` confirms the fix is in place.

### Decision 3: Recomposition Count Thresholds

- **Decision**: Flag as "excessive" any composable that recomposes more than 3x on initial load, or any list item that recomposes when it is NOT the item being interacted with. Flag any scroll that triggers recomposition of off-screen items that were already composed.
- **Rationale**: Industry standard -- Compose should skip recomposition for items whose inputs have not changed. One recomposition on first render is expected; 2-3 may occur during state settlement. More than 3 indicates an unstable parameter or missing key.
- **Alternatives considered**: Stricter threshold of 1x (too aggressive given async state loading), no threshold (defeats the purpose of tracking).

### Decision 4: GestureOverlay Not Tracked

- **Decision**: GestureOverlay does NOT have `trackRecomposition` or `LogRecomposition` applied in the current code. It will not show red borders or logs. The prep plan from the recomposition-tracking feature listed it as a target, but it was not implemented. We will note its absence in the report but not treat it as a test failure.
- **Rationale**: Reading `GestureOverlay.kt` confirms no tracking modifiers are present. The feature was partially implemented (7 out of 10 planned composables were tracked).

### Decision 5: Onboarding Screen Has No Tracked Composables

- **Decision**: Onboarding screen has no recomposition tracking applied. The test for Onboarding will verify the screen loads correctly but will note "no tracked composables" rather than expecting red borders.
- **Rationale**: OnboardingScreen was not included in the tracked composable list, which focused on high-risk list items and state-heavy screens.

## Current State: Tracked Composables

The following composables have `trackRecomposition()` modifier AND `LogRecomposition()` calls:

| Composable | File | Line (modifier) | Line (log) |
|---|---|---|---|
| `ChannelCardSquare` | `shared/src/commonMain/.../common/components/ChannelCard.kt` | 56 | 54 |
| `ChannelCardList` | `shared/src/commonMain/.../common/components/ChannelCard.kt` | 152 | 150 |
| `ContinueWatchingCard` | `shared/src/commonMain/.../common/components/ContinueWatchingCard.kt` | 43 | 41 |
| `HomeContent` | `shared/src/commonMain/.../feature/home/HomeScreen.kt` | 379 | 375 |
| `ChannelsContent` | `shared/src/commonMain/.../feature/channels/ChannelsScreen.kt` | 146 | 145 |
| `PlayerControlsOverlay` | `shared/src/commonMain/.../feature/player/components/PlayerControls.kt` | 72 | 71 |
| `BottomNavigationBar` | `shared/src/commonMain/.../feature/main/MainScreen.kt` | 111 | 110 |

NOT tracked (listed in original plan but not implemented):
- `SearchResultItem` (HomeScreen.kt line 507) -- no tracking modifier
- `GestureOverlay` (GestureOverlay.kt) -- no tracking modifier
- `PlayerScreenContent` (PlayerScreen.kt) -- no tracking modifier

## Pre-Requisites

1. `RecompositionConfig.isEnabled` is already `true` (confirmed in `shared/src/commonMain/.../common/RecompositionConfig.kt` line 5)
2. App must be installed as debug build: `./gradlew :androidApp:installDebug`
3. Device/emulator connected and accessible via `claude-in-mobile`

---

## Phase 1: All Screens Visual Check

### Test 1.1: Onboarding Screen

**Steps:**
1. Clear app data or fresh install
2. Launch app: `claude-in-mobile launch com.simplevideo.whiteiptv`
3. Wait for Onboarding screen to appear
4. `claude-in-mobile screenshot` -- capture Onboarding
5. `claude-in-mobile logs --tag "Recomposition" --lines 50`

**Expected:**
- No red borders visible (no tracked composables on this screen)
- No "Recomposition" logs for this screen
- Screen renders correctly: logo, title, URL input, file picker button, import button, demo link

**Pass criteria:** Screen loads, no crash, no recomposition tracking expected.

---

### Test 1.2: Home Screen

**Steps:**
1. From Onboarding, tap "Use demo playlist" to import
2. Wait for import to complete and navigate to Main
3. `claude-in-mobile wait 3000` (let state settle after large playlist load)
4. `claude-in-mobile clear-logs`
5. `claude-in-mobile screenshot` -- capture Home screen
6. `claude-in-mobile logs --tag "Recomposition" --lines 50`

**Expected:**
- Red borders visible on:
  - `HomeContent` (the scrollable column area)
  - `ChannelCardSquare` cards in LazyRows (Favorites section if favorites exist, category sections)
  - `ContinueWatchingCard` if continue watching section is present
  - `BottomNavigationBar` at bottom
- Kermit logs show entries like:
  - `HomeContent recomposed`
  - `ChannelCardSquare recomposed` (multiple)
  - `BottomNavigationBar recomposed`

**Pass criteria:** At least `HomeContent` and `BottomNavigationBar` show red borders. Log entries present.

---

### Test 1.3: Favorites Screen

**Steps:**
1. Navigate to Favorites tab: `claude-in-mobile tap-text "Favorites"`
2. `claude-in-mobile wait 1000`
3. `claude-in-mobile clear-logs`
4. `claude-in-mobile screenshot`
5. `claude-in-mobile logs --tag "Recomposition" --lines 50`

**Expected:**
- If favorites exist: Red borders on `ChannelCardSquare` (grid mode) or `ChannelCardList` (list mode) items
- `BottomNavigationBar` shows red border
- If empty state: no card tracking visible, only nav bar

**Pass criteria:** `BottomNavigationBar` red border visible. Card borders if favorites exist.

---

### Test 1.4: Channels Screen

**Steps:**
1. Navigate to Channels tab: `claude-in-mobile tap-text "Channels"`
2. `claude-in-mobile wait 2000` (paging load)
3. `claude-in-mobile clear-logs`
4. `claude-in-mobile screenshot`
5. `claude-in-mobile logs --tag "Recomposition" --lines 50`

**Expected:**
- Red borders on:
  - `ChannelsContent` (the main column)
  - `ChannelCardSquare` (grid mode, default) or `ChannelCardList` (list mode) items
  - `BottomNavigationBar`
- Logs: `ChannelsContent recomposed`, multiple `ChannelCardSquare recomposed`

**Pass criteria:** `ChannelsContent` and card items show red borders.

---

### Test 1.5: Settings Screen

**Steps:**
1. Navigate to Settings tab: `claude-in-mobile tap-text "Settings"`
2. `claude-in-mobile wait 1000`
3. `claude-in-mobile clear-logs`
4. `claude-in-mobile screenshot`
5. `claude-in-mobile logs --tag "Recomposition" --lines 50`

**Expected:**
- Red border on `BottomNavigationBar` only
- No tracked composables in Settings screen content (none were instrumented)
- Settings screen renders correctly: Appearance, Playback, App Behavior, Data & Storage, About sections

**Pass criteria:** Settings loads correctly. `BottomNavigationBar` border visible.

---

### Test 1.6: Player Screen

**Steps:**
1. Navigate to Home: `claude-in-mobile tap-text "Home"`
2. `claude-in-mobile wait 1000`
3. Tap a channel card in any category section
4. `claude-in-mobile wait 3000` (player load + buffering)
5. Tap screen center to show controls
6. `claude-in-mobile clear-logs`
7. `claude-in-mobile screenshot`
8. `claude-in-mobile logs --tag "Recomposition" --lines 50`

**Expected:**
- Red border on `PlayerControlsOverlay` (visible when controls are shown)
- No `BottomNavigationBar` (player is full-screen, no bottom nav)
- Log: `PlayerControlsOverlay recomposed`

**Pass criteria:** Player opens without crash (CastButton fix confirmed). `PlayerControlsOverlay` border visible when controls shown. If stream fails, controls overlay still renders with error state.

---

## Phase 2: Recomposition Analysis

### Test 2.1: ChannelCardSquare -- Initial Render Count

**Steps:**
1. Navigate to Home
2. `claude-in-mobile clear-logs`
3. Force a state update: switch playlist dropdown or navigate away and back
4. `claude-in-mobile logs --tag "Recomposition" --lines 100`
5. Count occurrences of "ChannelCardSquare recomposed"

**Expected:**
- Each visible card should recompose 1-2 times on initial render (once for creation, possibly once for image load state change)
- Count should roughly equal the number of visible cards (not total cards in data)

**Threshold:** EXCESSIVE if any single card recomposes >3 times on initial load.

**Known risk:** Lambda parameters `onClick` and `onToggleFavorite` are inline lambdas in `HomeContent`. If these lambdas capture unstable references, ALL cards will recompose when ANY HomeState field changes. This is the most likely performance issue to find.

---

### Test 2.2: ChannelCardSquare -- Scroll Recomposition

**Steps:**
1. Navigate to Home
2. `claude-in-mobile clear-logs`
3. Scroll a category LazyRow horizontally: `claude-in-mobile swipe 300 500 50 500` (adjust Y based on category row position)
4. `claude-in-mobile logs --tag "Recomposition" --lines 100`

**Expected:**
- Only newly-visible cards should log recomposition
- Cards that were already visible and remain visible should NOT recompose
- Cards that scrolled off-screen and were disposed are expected to recompose when scrolling back

**Threshold:** EXCESSIVE if visible, unchanged cards recompose during horizontal scroll.

---

### Test 2.3: ChannelsContent -- Scroll Paging

**Steps:**
1. Navigate to Channels tab
2. `claude-in-mobile clear-logs`
3. Scroll down: `claude-in-mobile swipe 200 700 200 200` (vertical scroll)
4. Wait for paging to load: `claude-in-mobile wait 1000`
5. `claude-in-mobile logs --tag "Recomposition" --lines 100`

**Expected:**
- `ChannelsContent` recomposes when new paged data arrives (expected, 1-2x per page load)
- Individual `ChannelCardSquare`/`ChannelCardList` items recompose only when first composed or when their data changes
- Items at the top of the list (scrolled out of view) should NOT recompose

**Threshold:** EXCESSIVE if `ChannelsContent` recomposes >3x per scroll gesture, or if all visible cards recompose when only new page data arrives.

---

### Test 2.4: Tab Switch -- Full Recomposition Check

**Steps:**
1. Start on Home tab
2. `claude-in-mobile clear-logs`
3. `claude-in-mobile tap-text "Channels"` -- switch to Channels
4. `claude-in-mobile wait 500`
5. `claude-in-mobile tap-text "Home"` -- switch back to Home
6. `claude-in-mobile wait 500`
7. `claude-in-mobile logs --tag "Recomposition" --lines 100`

**Expected:**
- `BottomNavigationBar` recomposes 2x (once per tab switch)
- `HomeContent` recomposes 1x on return (restored state, not full reload)
- `ChannelsContent` recomposes 1x on entry
- Card items: should restore from saved state, not re-create from scratch

**Threshold:** EXCESSIVE if `BottomNavigationBar` recomposes >3x per tab switch, or if `HomeContent` triggers full re-render of all LazyRow items.

---

### Test 2.5: Favorites Toggle -- Single Item vs All Items

**Steps:**
1. Navigate to Channels tab
2. Find a channel card with a star icon
3. `claude-in-mobile clear-logs`
4. Tap the star icon on ONE card to toggle favorite
5. `claude-in-mobile wait 500`
6. `claude-in-mobile logs --tag "Recomposition" --lines 100`

**Expected:**
- The toggled card recomposes (expected: its `isFavorite` prop changed)
- Other visible cards should NOT recompose (their props did not change)
- `ChannelsContent` may recompose once (paging data refresh triggers)

**Threshold:** EXCESSIVE if ALL visible cards recompose when only ONE card's favorite is toggled. This would indicate unstable lambda callbacks or missing `key` in the lazy list.

**Known risk:** `onToggleFavorite` lambda in `ChannelsScreen.kt` captures `viewModel` which is stable (koinViewModel is remembered). The lambda `{ onToggleFavorite(channel.id) }` at line 203 captures `channel` from the items block -- this should be fine since each item gets its own closure. However, if paging data refresh causes a new `LazyPagingItems` emission, all items may recompose.

---

### Test 2.6: PlayerControlsOverlay -- Buffering State Changes

**Steps:**
1. Open a channel in the player
2. Wait for playback to start
3. Tap screen to show controls
4. `claude-in-mobile clear-logs`
5. Wait 5 seconds (controls auto-hide, buffering state may toggle)
6. Tap screen again
7. `claude-in-mobile logs --tag "Recomposition" --lines 50`

**Expected:**
- `PlayerControlsOverlay` recomposes when `isVisible` toggles (2x: hide + show)
- It should NOT recompose continuously while visible and idle

**Threshold:** EXCESSIVE if `PlayerControlsOverlay` recomposes >5x during a show/hide cycle.

---

### Test 2.7: BottomNavigationBar -- Idle Recomposition

**Steps:**
1. On Home tab, do nothing for 5 seconds
2. `claude-in-mobile clear-logs`
3. Wait 5 more seconds (no interaction)
4. `claude-in-mobile logs --tag "Recomposition" --lines 50`

**Expected:**
- ZERO recompositions of `BottomNavigationBar` during idle
- ZERO recompositions of `HomeContent` during idle (unless background data refresh triggers state update)

**Threshold:** EXCESSIVE if any tracked composable recomposes during idle with no interaction.

---

## Phase 3: Feature Verification (Smoke Tests)

### Test 3.1: Home Screen Features

| # | Check | Steps | Pass Criteria |
|---|-------|-------|---------------|
| 3.1.1 | Custom top bar | Screenshot Home | Row with playlist dropdown, search icon, settings gear icon visible (not standard TopAppBar) |
| 3.1.2 | Playlist dropdown | Tap playlist name area | Dropdown opens showing "All" and playlist name(s) |
| 3.1.3 | Search overlay | Tap search icon | Search bar appears at top with "Search channels..." placeholder |
| 3.1.4 | Search results | Type "News" in search | Channel results appear in LazyColumn |
| 3.1.5 | Settings gear | Tap gear icon | Playlist settings bottom sheet opens with Rename, Update, Delete, View URL |
| 3.1.6 | Category sections | Scroll Home | Multiple sections with "View All" links (e.g., News, Sports, Music) |

---

### Test 3.2: Favorites Screen Features

| # | Check | Steps | Pass Criteria |
|---|-------|-------|---------------|
| 3.2.1 | Empty state | Clear all favorites first (Settings > Clear Favorites) | Star icon with "No favorites yet" message and hint text |
| 3.2.2 | Grid mode | Add favorites, check Favorites tab | Cards displayed in 2-column grid (ChannelCardSquare) |
| 3.2.3 | List mode | Switch Channel View to List in Settings, return to Favorites | Cards displayed as list rows (ChannelCardList) |
| 3.2.4 | No star on cards | Screenshot Favorites | `showFavoriteButton = false` -- no star icon on individual cards |
| 3.2.5 | Playlist dropdown | Check top of Favorites | PlaylistDropdown visible for filtering |

---

### Test 3.3: Channels Screen Features

| # | Check | Steps | Pass Criteria |
|---|-------|-------|---------------|
| 3.3.1 | Grid view (default) | Navigate to Channels | 2-column grid of ChannelCardSquare |
| 3.3.2 | List view | Set Channel View = List in Settings, return to Channels | ChannelCardList rows with logo, name, subtitle, star |
| 3.3.3 | Group filter | Tap group dropdown | Dropdown shows available groups (News, Sports, etc.) |
| 3.3.4 | Filter applies | Select a group | Only channels from that group displayed |
| 3.3.5 | Paging | Scroll to bottom | Loading spinner appears, more items load |
| 3.3.6 | Search | Tap search icon, type query | Filtered results appear |

---

### Test 3.4: Settings Screen Features

| # | Check | Steps | Pass Criteria |
|---|-------|-------|---------------|
| 3.4.1 | Card groups | Screenshot Settings | Sections grouped in cards: Appearance, Playback, App Behavior, Data & Storage, About |
| 3.4.2 | Icons | Visual check | Each row has a Material icon (Palette, Star, ViewList, PlayCircle, Hd, Language, Refresh, Cached, Delete, RestartAlt) |
| 3.4.3 | Theme dropdown | Tap Theme row | Dropdown shows System, Light, Dark |
| 3.4.4 | Theme switch | Select Light | Background becomes light, text becomes dark |
| 3.4.5 | Accent Color | Tap Accent Color row | Dropdown shows Teal, Blue, Red (or AccentColor entries) |
| 3.4.6 | Channel View | Tap Channel View row | Dropdown shows Grid, List |
| 3.4.7 | Auto-Update toggle | Tap toggle | Switch state changes |
| 3.4.8 | Clear Cache | Tap Clear Cache | Snackbar "Cache cleared" |
| 3.4.9 | App version | Scroll to About | "v1.0" or similar version string displayed |

---

### Test 3.5: Player Screen Features

| # | Check | Steps | Pass Criteria |
|---|-------|-------|---------------|
| 3.5.1 | Player opens | Tap any channel card | Player screen opens, no crash (CastButton fix verified) |
| 3.5.2 | Controls overlay | Tap screen center | Top bar (back + channel name) and bottom bar (timer, streaming button) appear |
| 3.5.3 | Cast/Streaming button | Check bottom bar | StreamingButton (MediaRouteButton) visible without crash |
| 3.5.4 | Back button | Tap back arrow | Returns to previous screen |
| 3.5.5 | Buffering indicator | During stream load | CircularProgressIndicator visible |

---

### Test 3.6: Bottom Navigation Features

| # | Check | Steps | Pass Criteria |
|---|-------|-------|---------------|
| 3.6.1 | Outlined icons | Screenshot nav bar | Icons are outlined variants: Home, FavoriteBorder, Tv, Settings (not filled) |
| 3.6.2 | Tab switching | Tap each tab | Correct screen loads for each tab |
| 3.6.3 | Active indicator | Check selected tab | Material3 NavigationBar selection indicator visible |
| 3.6.4 | Labels | Check nav items | Text labels: "Home", "Favorites", "Channels", "Settings" |

---

## Report Format

Write results to `docs/features-claude/e2e-recomposition/e2e-report.md` with this structure:

```markdown
# E2E Report: Recomposition Tracking + Full Screen Test

## Environment
- Device: [device name]
- Android API: [version]
- Date: 2026-03-15
- RecompositionConfig.isEnabled: true

## Phase 1: Visual Check Results

### [Screen Name]
- Screenshot: [path or description]
- Red borders visible: [list of composables with borders]
- Recomposition log entries: [count and composable names]
- Result: PASS / FAIL

## Phase 2: Recomposition Analysis

### [Test Name]
- Recomposition counts: [composable: count]
- Assessment: OK / EXCESSIVE
- Details: [explanation if excessive]

## Phase 2 Summary Table
| Composable | Initial Render | Scroll | Tab Switch | State Change | Idle | Verdict |
|---|---|---|---|---|---|---|

## Phase 3: Feature Smoke Test

| # | Feature | Result | Notes |
|---|---------|--------|-------|

## Performance Issues Found
[List any composables with excessive recomposition, with root cause analysis]

## Recommendations
[Specific code changes to fix performance issues]

## Verdict
PASS / FAIL with summary
```

## Key Risks and Mitigations

1. **CastButton crash may still occur** -- The fix is in place (`ContextThemeWrapper`), but if `R.style.CastButtonTheme` is missing or misconfigured, the crash returns. Mitigation: if player crashes, log the error and skip player tests, noting the regression.

2. **Kermit log tag may differ on Android** -- Kermit's `Logger.d("Recomposition")` creates a logcat tag. If filtering fails, try `claude-in-mobile logs --tag "D" --lines 100` and grep for "Recomposition" manually.

3. **Demo playlist is large (~10k channels)** -- Initial load may take 30-60 seconds. Account for this in wait times. If the app was previously set up, the demo playlist may already be imported (Splash redirects to Main).

4. **Red borders may be subtle** -- The `trackRecomposition` modifier draws a 2dp red `Stroke` border. On small cards (150dp wide), this may be hard to see in screenshots. Look for red outlines around card edges.

## Build & Test Commands

```bash
# Build and install debug APK
./gradlew :androidApp:installDebug

# Verify recomposition is enabled
grep "isEnabled" shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/RecompositionConfig.kt

# Clear logs before testing
claude-in-mobile clear-logs

# Launch app
claude-in-mobile launch com.simplevideo.whiteiptv

# Capture recomposition logs
claude-in-mobile logs --tag "Recomposition" --lines 100
```
