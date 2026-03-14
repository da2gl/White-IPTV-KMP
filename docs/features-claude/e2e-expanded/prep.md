# Expanded E2E Test Plan -- Post CastButton Fix + Design Sync

## Summary

This expanded test plan covers 120+ test cases organized into three categories: (A) retesting the 17 tests that were FAILED or SKIPPED due to the CastButton crash, (B) new visual verification tests for the design sync changes (Inter font, Settings overhaul, shared ChannelCard components, Favorites grid, improved Channels visuals), and (C) net-new functional and edge-case tests not covered in the original 77-test suite. All tests are prioritized P0/P1/P2 and include logcat monitoring instructions.

## Environment Prerequisites

- Device: Android emulator or physical device, API 28+
- App: Debug build after CastButton fix AND design sync merge
- Demo playlist imported: `https://iptv-org.github.io/iptv/index.m3u`
- Logcat running with filter: `com.simplevideo.whiteiptv`

## Logcat Monitoring Guide

For every test, keep logcat running with the following setup:

```bash
# Full app logs (recommended default)
adb logcat --pid=$(adb shell pidof com.simplevideo.whiteiptv) 2>/dev/null

# Crash detection (run in separate terminal)
adb logcat AndroidRuntime:E *:S

# ExoPlayer errors only
adb logcat ExoPlayerImplInternal:E EventLogger:W *:S

# Room database operations
adb logcat Room:* RoomDatabase:* *:S

# Ktor network (HTTP requests)
adb logcat ktor:* OkHttp:* *:S

# Coil image loading
adb logcat RealImageLoader:* *:S
```

**Per-test log tags** are noted in the "Log Focus" column. Always check for `AndroidRuntime:E` (crash) after every test.

---

## Section A: Retest Previously Blocked Tests (CastButton Fix Verification)

These tests were FAILED or SKIPPED in the original report solely because the CastButton crash prevented the player from opening. After the fix, these should all pass.

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 1 | A-H3.3 | Tap channel card on Home -> Player opens | P0 | 1. Import demo playlist 2. Go to Home tab 3. Tap any channel card in a category row | Player screen opens, video begins loading/buffering, no crash | `AndroidRuntime:E`, `ExoPlayerImplInternal` |
| 2 | A-C4.6 | Tap channel card on Channels -> Player opens | P0 | 1. Go to Channels tab 2. Tap any channel card | Player screen opens, video begins loading/buffering | `AndroidRuntime:E`, `ExoPlayerImplInternal` |
| 3 | A-F5.4 | Tap favorite channel -> Player opens | P0 | 1. Favorite a channel from Channels tab 2. Go to Favorites tab 3. Tap the favorited channel | Player screen opens, video begins loading/buffering | `AndroidRuntime:E`, `ExoPlayerImplInternal` |
| 4 | A-P6.1 | Player loads and plays live stream | P0 | 1. Tap any channel with a working stream (try "France 24" or similar known-good) 2. Wait up to 10s | Video renders on screen, audio plays (if stream has audio), buffering indicator disappears | `ExoPlayerImplInternal`, `EventLogger` |
| 5 | A-P6.2 | Player controls appear and auto-hide | P0 | 1. Open player with a channel 2. Tap screen to show controls 3. Wait 3s without interaction | Controls (back arrow, channel name, bottom bar icons) appear on tap, then auto-hide after 3 seconds | None |
| 6 | A-P6.3 | Back button in player controls returns to previous screen | P0 | 1. Open player from Home tab 2. Tap screen to show controls 3. Tap back arrow button | Player closes, returns to Home tab | None |
| 7 | A-P6.4 | System back key returns from Player | P0 | 1. Open player from Channels tab 2. Press system back button (or gesture) | Player closes, returns to Channels tab | None |
| 8 | A-P6.5 | Channel name displayed in player controls | P1 | 1. Open player with "France 24" (or any named channel) 2. Tap screen to show controls | Channel name is displayed in the top bar of player controls overlay | None |
| 9 | A-P6.6 | Stream error shows error message | P1 | 1. Find a channel with a broken/offline stream URL 2. Open it in player 3. Wait for timeout | Error message displayed on screen (white text on black background). App does not crash | `ExoPlayerImplInternal:E`, `EventLogger:W` |
| 10 | A-P6.7 | Channel navigation swipe -- next channel | P1 | 1. Open player 2. Swipe left (or down, depending on gesture overlay direction) | Next channel loads, channel name updates in controls | `ExoPlayerImplInternal` |
| 11 | A-P6.8 | Channel navigation swipe -- previous channel | P1 | 1. Open player (not the first channel) 2. Swipe right (or up) | Previous channel loads, channel name updates in controls | `ExoPlayerImplInternal` |
| 12 | A-P6.9 | Track selection bottom sheet opens | P2 | 1. Open player with a multi-audio stream (if available) 2. Tap screen to show controls 3. Tap Audio Track icon (if visible) | Bottom sheet opens showing available audio tracks with current selection marked | None |
| 13 | A-P6.10 | Volume gesture works | P2 | 1. Open player 2. Swipe up/down on right side of screen | System volume changes, controls briefly appear | None |
| 14 | A-P6.11 | Brightness gesture works | P2 | 1. Open player 2. Swipe up/down on left side of screen | Screen brightness changes, controls briefly appear | None |
| 15 | A-N10.3 | Deep nav Home -> Player -> Back returns to Home | P1 | 1. Go to Home tab 2. Tap a channel card 3. Press back | Returns to Home tab, not Splash or Onboarding | None |
| 16 | A-E11.4 | Orientation change during playback | P2 | 1. Open player 2. Rotate device to landscape 3. Rotate back to portrait | Player continues playing without crash. Controls adapt to new orientation | `AndroidRuntime:E` |
| 17 | A-F5.5 | Search within Favorites | P1 | 1. Favorite 3+ channels 2. Go to Favorites tab 3. Tap search icon 4. Type part of a channel name | Search filters the favorites list to matching channels only | None |

---

## Section B: Design Sync Visual Verification Tests

These tests verify the UI changes from the design sync: Inter font, Settings icons/sections, shared ChannelCard components, Favorites grid, and Channels visual improvements.

### B1. Typography / Font

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 18 | B1.1 | Inter font renders on all screens | P1 | 1. Open each screen (Home, Channels, Favorites, Settings, Onboarding) 2. Visually inspect text rendering | Text uses Inter font family (distinguishable from system default by its rounder letterforms, tighter spacing). Compare with a screenshot of the old default font if available | None |
| 19 | B1.2 | Font weights are distinct | P1 | 1. Go to Settings 2. Compare section headers (should be Medium/SemiBold) with body text (Regular) and button text (Medium) | Visual weight difference is apparent between headers, body text, and labels | None |
| 20 | B1.3 | Font renders in Dark theme | P2 | 1. Switch to Dark theme in Settings 2. Visit Home, Channels, Favorites | Text is legible (white/light on dark), no rendering artifacts | None |
| 21 | B1.4 | Font renders in Light theme | P2 | 1. Switch to Light theme 2. Visit all screens | Text is legible (dark on light), no rendering artifacts | None |

### B2. Settings Screen Overhaul

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 22 | B2.1 | Settings section headers visible | P1 | 1. Open Settings tab 2. Scroll through entire page | Section headers visible: "Appearance", "Playback", "App Behavior", "Data & Storage", "About". Each in primary color | None |
| 23 | B2.2 | Appearance section has icons | P1 | 1. Open Settings 2. Look at Appearance section | "Theme" row has palette icon, "Accent Color" has brush icon, "Channel View" has view list icon | None |
| 24 | B2.3 | Playback section exists with placeholder items | P1 | 1. Open Settings 2. Scroll to Playback section | "Playback" header visible. "Default Player" row (subtitle: "Built-in") with play circle icon. "Preferred Quality" row (subtitle: "Auto") with high quality icon | None |
| 25 | B2.4 | Playback items are non-functional placeholders | P2 | 1. Tap "Default Player" 2. Tap "Preferred Quality" | Nothing happens (no crash, no dialog, no navigation). Items are display-only | `AndroidRuntime:E` |
| 26 | B2.5 | App Behavior section has icons | P1 | 1. Look at App Behavior section | "Language" has language/globe icon, "Auto-Update Playlists" has sync icon | None |
| 27 | B2.6 | Data & Storage section has icons | P1 | 1. Look at Data & Storage section | "Clear Cache" has delete icon, "Clear Favorites" has star border icon, "Reset to Defaults" has restart icon | None |
| 28 | B2.7 | Reset to Defaults text is red | P1 | 1. Look at "Reset to Defaults" row | Title text is in error/red color, visually distinct from other items | None |
| 29 | B2.8 | About section has icons | P1 | 1. Look at About section | "Version" has info icon, "Contact Support" has mail icon, "Privacy Policy" has policy/shield icon | None |
| 30 | B2.9 | Dividers separate sections | P2 | 1. Scroll through Settings | Horizontal dividers visible between each section | None |
| 31 | B2.10 | Settings scrolls fully without clipping | P2 | 1. Scroll to bottom of Settings page | All content visible, "Privacy Policy" row fully visible, no content clipped at bottom | None |

### B3. Home Screen Cards

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 32 | B3.1 | Channel cards in category rows use square format | P1 | 1. Open Home tab 2. Look at any category row (News, Sports, etc.) | Cards are square (1:1 aspect ratio) with channel logo as background image, 8dp rounded corners, channel name at bottom | None |
| 33 | B3.2 | Channel card images load from network | P1 | 1. Look at channel cards 2. Wait for images to load | Logo images load and display. Cards without logos show a placeholder background | `RealImageLoader` |
| 34 | B3.3 | Continue Watching cards have 16:9 aspect ratio | P1 | 1. Watch a channel briefly (triggers Continue Watching) 2. Return to Home 3. Look at Continue Watching section | Cards are wider than tall (16:9 ratio), show channel logo/image with gradient scrim, channel name below image, 8dp corners | None |
| 35 | B3.4 | Home top bar shows playlist dropdown only (no gear icon) | P1 | 1. Look at Home top bar | Search icon visible. Playlist dropdown visible. No separate gear/settings icon in the action area (playlist settings accessed via dropdown) | None |
| 36 | B3.5 | "View All" links on category sections | P2 | 1. Look at section headers in Home | Each group section has "View All" text in primary color on the right side | None |
| 37 | B3.6 | Cards have consistent 8dp corner radius | P2 | 1. Visually inspect card corners across Home screen | All cards (Continue Watching, category channels) have uniform rounded corners | None |

### B4. Favorites Screen

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 38 | B4.1 | Favorites uses 2-column grid layout | P1 | 1. Favorite 4+ channels from Channels tab 2. Go to Favorites tab | Channels displayed in a 2-column grid of square cards, not a vertical list | None |
| 39 | B4.2 | Favorite cards show filled star overlay | P1 | 1. Look at favorite cards in grid | Each card has a filled star icon (in primary/accent color) visible as an overlay on the card | None |
| 40 | B4.3 | Favorite card shows channel name and category | P1 | 1. Look at card details | Channel name visible on card. Category/group label visible (if available) | None |
| 41 | B4.4 | Filter chips visible below header | P1 | 1. Favorite channels from different groups (e.g., News, Sports) 2. Go to Favorites | Horizontal row of filter chips visible: "All" (selected by default), plus group names of favorited channels | None |
| 42 | B4.5 | Filter chip selection filters grid | P1 | 1. Tap a group chip (e.g., "News") | Grid filters to show only favorites in that group. Chip appears selected (filled/highlighted) | None |
| 43 | B4.6 | "All" chip resets filter | P1 | 1. Select a group chip 2. Tap "All" chip | All favorites shown again. "All" chip appears selected | None |
| 44 | B4.7 | Empty favorites with grid layout | P2 | 1. Clear all favorites (Settings > Clear Favorites) 2. Go to Favorites tab | Empty state shown: star icon, "You haven't added any favorite channels yet" message | None |
| 45 | B4.8 | Star color uses theme accent, not hardcoded cyan | P2 | 1. Look at star icons on favorite cards 2. Change accent color in Settings 3. Return to Favorites | Star icons use the current accent color (Teal/Blue/Red), not a fixed cyan color | None |
| 46 | B4.9 | No playlist dropdown in Favorites | P2 | 1. Look at Favorites screen layout | No playlist dropdown selector visible (design sync removed it in favor of filter chips) | None |

### B5. Channels Screen

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 47 | B5.1 | Grid mode cards use ChannelCardSquare style | P1 | 1. Set Channel View to Grid in Settings 2. Go to Channels tab | Cards are square with logo background, star overlay, channel name at bottom, 8dp corners | None |
| 48 | B5.2 | List mode cards use ChannelCardList style | P1 | 1. Set Channel View to List in Settings 2. Go to Channels tab | Cards are horizontal rows with logo thumbnail on left, name and subtitle, star toggle on right, 8dp corners | None |
| 49 | B5.3 | Grid and List modes are visually distinct | P1 | 1. Switch between Grid and List in Settings 2. Return to Channels each time | Clear visual difference between modes: Grid shows 2-column square cards, List shows full-width row items | None |
| 50 | B5.4 | Star icon color uses accent theme | P2 | 1. Look at star icons on channel cards 2. Change accent color in Settings 3. Return to Channels | Star icons for favorited channels use accent color, unfavorited use muted on-surface color | None |
| 51 | B5.5 | Dropdowns have improved visual styling | P2 | 1. Look at playlist and group dropdown selectors on Channels screen | Dropdowns have proper spacing, corner radius, and colors matching the theme (not raw OutlinedCard default) | None |
| 52 | B5.6 | Channel subtitle visible in List mode | P2 | 1. Set List mode 2. Look at channel list items | Channels with language or country metadata show a subtitle line below the name | None |

### B6. Dark/Light Theme with Design Sync

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 53 | B6.1 | Dark theme: card backgrounds distinct from surface | P1 | 1. Set Dark theme 2. Visit Home, Channels, Favorites | Card backgrounds are slightly lighter than the main dark background (surfaceContainer color), creating visual depth | None |
| 54 | B6.2 | Light theme: cards render correctly | P1 | 1. Set Light theme 2. Visit Home, Channels, Favorites | Cards have appropriate light surface colors, text is dark and readable | None |
| 55 | B6.3 | Theme switch does not lose state | P2 | 1. Favorite some channels 2. Switch theme in Settings 3. Check Favorites | Favorites preserved. Theme change does not clear app state | None |

---

## Section C: New Functional and Edge Case Tests

These tests cover flows and scenarios missing from the original 77-test suite.

### C1. Multiple Playlist Management

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 56 | C1.1 | Add second playlist via URL | P1 | 1. With demo playlist already imported 2. Find way to add a new playlist (dropdown "Add" or Settings) 3. Enter a second M3U URL (e.g., another public IPTV list) 4. Import | Second playlist imports successfully. Playlist dropdown shows both playlists | `ktor`, `Room` |
| 57 | C1.2 | Switch between playlists in dropdown | P1 | 1. Have 2 playlists imported 2. Open playlist dropdown on Home 3. Select second playlist | Home content updates to show channels from the selected playlist. Category sections reflect the new playlist's groups | None |
| 58 | C1.3 | "All" playlist selection shows merged channels | P1 | 1. Have 2 playlists 2. Select "All" in dropdown | All channels from both playlists shown in category sections | None |
| 59 | C1.4 | Delete one playlist, other remains | P1 | 1. Have 2 playlists 2. Select one playlist 3. Open playlist settings 4. Delete it | Deleted playlist removed. Other playlist becomes active. App stays on Home (does not go to Onboarding) | `Room` |
| 60 | C1.5 | Channels tab reflects playlist switch from Home | P1 | 1. Switch playlist on Home dropdown 2. Go to Channels tab | Channels tab shows channels from the same playlist selected on Home (CurrentPlaylistRepository sync) | None |
| 61 | C1.6 | Gear icon disabled for "All" selection | P2 | 1. Select "All" in dropdown 2. Look at gear/settings icon | Gear icon is disabled (greyed out) -- cannot open playlist settings for "All" | None |

### C2. Playlist Update / Refresh

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 62 | C2.1 | Update playlist re-downloads content | P1 | 1. Import demo playlist 2. Open playlist settings gear 3. Tap "Update Playlist" | Loading indicator shows. Playlist re-downloads. Channel count updates if source changed. Favorites preserved | `ktor`, `Room` |
| 63 | C2.2 | Update playlist preserves favorites | P1 | 1. Favorite 3 channels 2. Update playlist 3. Check Favorites tab | Previously favorited channels still appear in Favorites (if they still exist in the updated playlist) | `Room` |
| 64 | C2.3 | Update playlist during airplane mode | P2 | 1. Enable airplane mode 2. Try "Update Playlist" | Error message shown (network error). App does not crash. Existing data preserved | `ktor`, `AndroidRuntime:E` |

### C3. Settings Persistence

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 65 | C3.1 | Accent color persists across restart | P1 | 1. Set accent color to Blue 2. Force stop app 3. Relaunch | Accent color is Blue on all screens | None |
| 66 | C3.2 | Channel View mode persists across restart | P1 | 1. Set Channel View to List 2. Force stop app 3. Relaunch 4. Go to Channels | Channels tab shows List layout | None |
| 67 | C3.3 | Auto Update toggle persists across restart | P1 | 1. Enable Auto-Update Playlists 2. Force stop app 3. Relaunch 4. Go to Settings | Auto-Update toggle is ON | None |
| 68 | C3.4 | All settings persist together | P1 | 1. Set: Dark theme, Red accent, List view, Auto-Update ON 2. Force stop app 3. Relaunch 4. Go to Settings | All four settings reflect the saved values | None |
| 69 | C3.5 | Reset to Defaults restores ALL settings | P1 | 1. Change theme, accent, channel view, auto-update 2. Reset to Defaults 3. Verify | Theme=System, Accent=Teal, ChannelView=List (or default), AutoUpdate=Off | None |

### C4. Accent Color Propagation

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 70 | C4.1 | Teal accent: primary color on all screens | P1 | 1. Set accent to Teal 2. Visit Home, Channels, Favorites, Settings | Primary-colored elements (section headers, "View All" text, selected segmented buttons, filter chips, star icons) are all Teal-toned | None |
| 71 | C4.2 | Blue accent: primary color on all screens | P1 | 1. Set accent to Blue 2. Visit all screens | All primary-colored elements are Blue-toned | None |
| 72 | C4.3 | Red accent: primary color on all screens | P1 | 1. Set accent to Red 2. Visit all screens | All primary-colored elements are Red-toned | None |
| 73 | C4.4 | Accent change reflects immediately (no restart) | P2 | 1. Be on Home tab 2. Go to Settings 3. Change accent from Teal to Blue 4. Go back to Home | Home screen elements immediately show Blue accent color without app restart | None |

### C5. Channel View Mode Functional Difference

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 74 | C5.1 | Grid mode: 2-column square card layout | P1 | 1. Set Grid in Settings 2. Go to Channels | Channels displayed in a 2-column LazyVerticalGrid with square cards | None |
| 75 | C5.2 | List mode: single-column row layout | P1 | 1. Set List in Settings 2. Go to Channels | Channels displayed in a single-column LazyColumn with horizontal row items | None |
| 76 | C5.3 | Favorite toggle works in Grid mode | P1 | 1. Set Grid mode 2. Tap star on a channel card | Star toggles to filled/accent color. Channel appears in Favorites tab | None |
| 77 | C5.4 | Favorite toggle works in List mode | P1 | 1. Set List mode 2. Tap star on a channel row | Star toggles. Channel appears in Favorites | None |

### C6. Error Recovery Flows

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 78 | C6.1 | Import fails -> fix URL -> import succeeds | P1 | 1. On Onboarding, enter invalid URL "http://invalid.example.com/playlist.m3u" 2. Tap Import 3. See error 4. Clear URL, enter demo URL 5. Tap Import | First import shows error message. Second import succeeds, navigates to Home | `ktor`, `AndroidRuntime:E` |
| 79 | C6.2 | Player error -> back -> try different channel | P1 | 1. Open a channel that fails to play 2. See error message in player 3. Press back 4. Open a different channel | First channel shows error. Back returns to channel list. Second channel plays normally | `ExoPlayerImplInternal` |
| 80 | C6.3 | Airplane mode on -> try import -> airplane off -> retry | P2 | 1. Enable airplane mode 2. Try to import playlist URL 3. See error 4. Disable airplane mode 5. Tap Import again | Error on first attempt. Success on second attempt | `ktor` |
| 81 | C6.4 | Player error -> swipe to next channel | P1 | 1. Open player with a broken stream 2. See error 3. Swipe to next channel | Next channel loads and plays (if its stream is valid). Error clears | `ExoPlayerImplInternal` |

### C7. Edge Cases: Visual and Data

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 82 | C7.1 | Very long channel name truncation | P2 | 1. Find a channel with a very long name in the demo playlist (many exist) 2. View in Grid mode 3. View in List mode | Name truncates with ellipsis ("...") in both Grid and List modes. No layout overflow or crash | None |
| 83 | C7.2 | Channel with no logo shows placeholder | P2 | 1. Find a channel with no logo URL (many in demo playlist) 2. View in Grid and List modes | Card shows a solid background color (surface or surfaceContainer) instead of broken image icon. No crash | `RealImageLoader` |
| 84 | C7.3 | Channel with broken logo URL | P2 | 1. Find a channel whose logo URL returns 404 2. View the card | Placeholder/fallback shown gracefully. No crash, no blank white space | `RealImageLoader` |
| 85 | C7.4 | Very long group name in dropdown | P2 | 1. If a group has a long name, open the group dropdown on Channels | Group name truncates or wraps appropriately. Dropdown remains usable | None |
| 86 | C7.5 | Special characters in channel name | P2 | 1. Find channels with special characters (accents, CJK, RTL) in demo playlist | Names render correctly. No crash or garbled text | None |
| 87 | C7.6 | Empty group shows no channels state | P2 | 1. Select a group filter that has 0 matching channels (if possible) | "No channels found" empty state displayed. No crash | None |
| 88 | C7.7 | Scroll position preserved on tab switch | P2 | 1. Go to Channels tab 2. Scroll down significantly 3. Switch to Home tab 4. Switch back to Channels | Scroll position is preserved (or at least does not crash). Note: this may reset, document behavior | None |

### C8. Player Features (Deep Testing)

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 89 | C8.1 | Sleep timer: set 15 min timer | P2 | 1. Open player 2. Tap screen 3. Tap timer icon 4. Select 15 minutes | Timer countdown appears in bottom bar. Timer icon changes to accent color | None |
| 90 | C8.2 | Sleep timer: cancel active timer | P2 | 1. Set a sleep timer 2. Tap timer icon again 3. Tap cancel | Timer countdown disappears. Timer icon returns to white | None |
| 91 | C8.3 | PiP button visible (if supported) | P2 | 1. Open player on a device that supports PiP (API 26+) 2. Tap screen to show controls | PiP (Picture in Picture) icon visible in bottom bar | None |
| 92 | C8.4 | Controls show buffering indicator | P1 | 1. Open player with a stream 2. Observe during initial load | Circular progress indicator (white) shown at center during buffering | None |
| 93 | C8.5 | Multiple channel switches in sequence | P1 | 1. Open player 2. Swipe to next channel 3. Swipe to next again 4. Swipe to previous | Each switch loads the correct channel. No crash or freeze after rapid switching | `ExoPlayerImplInternal`, `AndroidRuntime:E` |
| 94 | C8.6 | Player keeps screen on | P2 | 1. Open player with a playing stream 2. Wait 1-2 minutes without touching screen | Screen does not turn off (KeepScreenOn composable active) | None |
| 95 | C8.7 | CastButton renders without crash | P0 | 1. Open player 2. Tap screen to show controls 3. Look at bottom bar | Cast button renders (or is removed if cast feature was dropped). No crash. This is the specific regression test for the original CastButton bug | `AndroidRuntime:E`, `MediaRouter` |

### C9. Search Deep Testing

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 96 | C9.1 | Search from Home: results show channel cards | P1 | 1. Tap search icon on Home 2. Type "BBC" | Search results appear as a list with channel logo, name. Tapping a result opens player | None |
| 97 | C9.2 | Search from Channels: results integrate with paging | P1 | 1. Tap search icon on Channels 2. Type "News" | Filtered results shown. Paging works for large result sets | None |
| 98 | C9.3 | Search clears when dismissed | P2 | 1. Open search 2. Type a query 3. Tap back/close 4. Open search again | Search field is empty on re-open | None |
| 99 | C9.4 | Search with single character | P2 | 1. Open search 2. Type "A" | Results shown (channels starting with "A"). No crash or excessive lag | None |
| 100 | C9.5 | Search result tap opens player | P1 | 1. Search for a channel 2. Tap a result | Player opens with the selected channel | `ExoPlayerImplInternal` |
| 101 | C9.6 | Empty search query shows all channels | P2 | 1. Open search 2. Leave query empty (or clear it) | All channels or empty state shown (document actual behavior) | None |

### C10. Onboarding Edge Cases

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 102 | C10.1 | Import button disabled when URL empty | P1 | 1. Fresh install or delete all playlists 2. On Onboarding, URL field is empty | "Import Playlist" button is disabled/greyed out | None |
| 103 | C10.2 | Import button enabled for valid URL | P1 | 1. Type "https://example.com/playlist.m3u" | Import button becomes enabled | None |
| 104 | C10.3 | Demo playlist link works | P0 | 1. Fresh install 2. Tap "Try demo playlist" link at bottom | Demo URL populates and imports. Navigates to Home with channels loaded | `ktor`, `Room` |
| 105 | C10.4 | File picker returns file and imports | P1 | 1. On Onboarding, tap "Choose File" 2. Select a local .m3u file (if available) | File name appears. Import button enables. Tapping Import processes the file | `Room` |
| 106 | C10.5 | Loading spinner during import | P1 | 1. Enter a URL 2. Tap Import 3. Observe during download | Spinner/progress indicator visible. Import button disabled. "Importing..." message shown | None |
| 107 | C10.6 | HTTP error codes show specific messages | P2 | 1. Enter a URL that returns 404 2. Tap Import | Error message specifically mentions "not found" or 404, not generic error | `ktor` |

### C11. Navigation Edge Cases

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 108 | C11.1 | Rapid tab switching (10 times) | P1 | 1. Tap Home, Channels, Favorites, Settings rapidly 10 times | No crash, no ANR. Final tab displays correctly | `AndroidRuntime:E` |
| 109 | C11.2 | System back from Home exits app | P0 | 1. Be on Home tab 2. Press system back | App exits (moves to background). Does not navigate to Splash or Onboarding | None |
| 110 | C11.3 | Deep link: Home -> Player -> Back -> Switch tab | P1 | 1. Home tab -> tap channel -> Player opens 2. Back -> Home 3. Switch to Channels tab | Each step works correctly. No stuck state or blank screen | None |
| 111 | C11.4 | Favorites tab -> Player -> Back -> still on Favorites | P1 | 1. Go to Favorites tab 2. Tap a favorited channel -> Player 3. Press back | Returns to Favorites tab, not Home | None |
| 112 | C11.5 | Player -> Home button -> reopen app | P2 | 1. Open player 2. Press Home button (minimize app) 3. Reopen app from recents | Player screen restored. Stream resumes or shows reconnection attempt | `ExoPlayerImplInternal` |

### C12. Process Death and State Restoration

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 113 | C12.1 | Process death on Home -> restore | P1 | 1. Open app, be on Home 2. Force stop via `adb shell am kill com.simplevideo.whiteiptv` 3. Reopen app | App relaunches from Splash, detects existing playlist, goes to Home/Main. Channels and favorites preserved in DB | `Room` |
| 114 | C12.2 | Process death on Player -> restore | P2 | 1. Open player 2. Force stop 3. Reopen | App relaunches from Splash, goes to Home. Player state is lost (acceptable) | None |
| 115 | C12.3 | Low memory: app survives background kill | P1 | 1. Open app 2. Open many other apps to trigger memory pressure 3. Return to app | App restores or relaunches gracefully. Data intact | `AndroidRuntime:E` |

### C13. Performance and Stability

| # | Test ID | Scenario | Priority | Steps | Expected Result | Log Focus |
|---|---------|----------|----------|-------|-----------------|-----------|
| 116 | C13.1 | Large playlist: scroll through 500+ channels | P0 | 1. Import demo playlist (~10k channels) 2. Channels tab 3. Scroll continuously for 30 seconds | Smooth scrolling, paging loads more items, no OOM crash | `AndroidRuntime:E` |
| 117 | C13.2 | Rapid favorite toggle (10 times on same channel) | P2 | 1. Go to Channels 2. Tap star on same channel 10 times rapidly | Final state is consistent (favorited or not). No crash, no DB errors | `Room`, `AndroidRuntime:E` |
| 118 | C13.3 | Import large playlist: memory stable | P0 | 1. Import demo playlist (10k+ channels) 2. Monitor memory via profiler or logcat | Import completes. No OOM. App remains responsive after import | `AndroidRuntime:E` |
| 119 | C13.4 | Image loading: many channels with logos | P2 | 1. Scroll through channels grid rapidly | Images load progressively. No excessive memory use. Some images may fail (404) but app remains stable | `RealImageLoader` |
| 120 | C13.5 | App cold start time under 3 seconds | P2 | 1. Force stop app 2. Launch app 3. Time until Home screen with content is visible | App reaches Home screen with channel content in under 3 seconds on modern hardware | None |

---

## Test Priority Summary

| Priority | Count | Description |
|----------|-------|-------------|
| P0 | 12 | Core flows: Player opens, playback works, CastButton regression, navigation, large playlist, demo import |
| P1 | 69 | Primary features: design sync visuals, settings icons/sections, card layouts, search, persistence, multi-playlist, error recovery |
| P2 | 39 | Edge cases: font rendering, long names, broken logos, PiP, sleep timer, scroll position, performance |
| **Total** | **120** | |

## Execution Order Recommendation

1. **Run P0 tests first (12 tests)** -- These are blockers. If any P0 fails, stop and report.
   - A-P6.1, A-P6.2, A-P6.3, A-P6.4 (player works at all)
   - A-H3.3, A-C4.6, A-F5.4 (player reachable from all screens)
   - C8.7 (CastButton regression)
   - C10.4 (demo import)
   - C11.2 (system back)
   - C13.1, C13.3 (large playlist)

2. **Run Section A remaining tests (13 tests)** -- Verify all previously blocked tests pass.

3. **Run Section B tests (35 tests)** -- Verify design sync visual changes.

4. **Run Section C remaining tests (60 tests)** -- New functional and edge case coverage.

## Known Issues to Watch For

From the original report, these issues were documented and may or may not be fixed:

| Issue | Original ID | Status | What to Check |
|-------|-------------|--------|---------------|
| Missing "Add new playlist" in dropdown | Issue 2 | May still exist | C1.1 -- can a second playlist be added? |
| Search uses substring matching not FTS4 prefix | Issue 3 | By design | Document behavior; not a bug |
| Channel View List/Grid no visual difference | Issue 4 | Should be fixed by design sync | B5.3, C5.1, C5.2 |
| Network error message not specific | Issue 5 | May be fixed by error-messages feature | C10.6, C6.3 |

## Reporting Template

For each test, record:

```
Test ID: [ID]
Result: PASS / FAIL / SKIP / BLOCKED
Notes: [Any observations]
Logcat: [Any relevant log output, especially for failures]
Screenshot: [filename if visual test]
```
