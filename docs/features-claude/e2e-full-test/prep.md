# Full App E2E Test Suite -- Implementation Plan

## Summary

Comprehensive end-to-end test plan for all existing features of the WhiteIPTV app, executed via `claude-in-mobile` CLI against a physical or emulated Android device. The plan covers every screen and user flow documented in `docs/features/` and `docs/flows/`, organized by screen with prioritized test scenarios.

## Decisions Made

- **Decision**: Test on Android only. iOS requires Xcode and a separate test harness; `claude-in-mobile` targets Android via ADB.
- **Rationale**: The shared KMP UI layer means Android testing covers the Compose UI logic. iOS-specific behavior (AVPlayer, file picker) is out of scope for this CLI-driven plan.

- **Decision**: Use the demo playlist (`https://iptv-org.github.io/iptv/index.m3u`) as the primary test data source.
- **Rationale**: It is large (~10k channels), publicly available, and already wired into the app's "Use demo playlist" flow.

- **Decision**: Tests are manual-interactive via `claude-in-mobile` CLI, not automated JUnit/instrumented tests.
- **Rationale**: The request specifies E2E test scenarios using the CLI tool, not code-level test infrastructure.

- **Decision**: EPG tests are excluded (EPG data layer is not yet implemented per `docs/features-claude/epg/prep.md`).
- **Rationale**: Testing non-existent features would produce only failures. EPG scenarios are listed as P2 future stubs.

- **Decision**: PiP, Cast, and Sleep Timer tests are excluded (listed as "Planned Features" in `docs/features/player.md`).
- **Rationale**: These features are not implemented.

## Prerequisites

Before running any test scenario:

1. **Device**: Android device or emulator connected via ADB, API 24+.
2. **APK**: Build and install the debug APK.
3. **Network**: Device has internet access (required for demo playlist download).
4. **Clean state**: For full regression, clear app data before starting.

```bash
# Build and install
./gradlew :androidApp:assembleDebug
claude-in-mobile install androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Clear app data for clean state
adb shell pm clear com.simplevideo.whiteiptv

# Verify device
claude-in-mobile devices
claude-in-mobile system-info
```

---

## 1. Splash Screen

### S1.1 -- Fresh install navigates to Onboarding
- **Priority**: P0
- **Preconditions**: App data cleared (no playlists in DB).
- **Steps**:
  ```
  claude-in-mobile launch com.simplevideo.whiteiptv
  claude-in-mobile wait 3000
  claude-in-mobile screenshot
  ```
- **Expected result**: Onboarding screen is displayed with the WhiteIPTV logo, URL input field, "Choose file" button, and "Use demo playlist" link. No splash screen remains visible.

### S1.2 -- Returning user navigates to Main
- **Priority**: P0
- **Preconditions**: At least one playlist has been imported previously.
- **Steps**:
  ```
  claude-in-mobile stop com.simplevideo.whiteiptv
  claude-in-mobile launch com.simplevideo.whiteiptv
  claude-in-mobile wait 3000
  claude-in-mobile screenshot
  ```
- **Expected result**: Main screen (Home tab) is displayed with the imported playlist's channels. Bottom navigation bar is visible with Home, Favorites, Channels, Settings tabs.

---

## 2. Onboarding -- Playlist Import

### O2.1 -- Import demo playlist
- **Priority**: P0
- **Preconditions**: App data cleared (fresh install). Onboarding screen visible.
- **Steps**:
  ```
  claude-in-mobile launch com.simplevideo.whiteiptv
  claude-in-mobile wait 3000
  claude-in-mobile tap-text "Use demo playlist"
  claude-in-mobile wait 15000
  claude-in-mobile screenshot
  ```
- **Expected result**: Loading indicator appears during download/parse. After completion, app navigates to Home screen showing channels from the demo playlist. Back button does NOT return to Onboarding (back stack is cleared).

### O2.2 -- Import playlist via URL
- **Priority**: P0
- **Preconditions**: Onboarding screen visible.
- **Steps**:
  ```
  claude-in-mobile launch com.simplevideo.whiteiptv
  claude-in-mobile wait 3000
  claude-in-mobile tap-text "Enter playlist URL"
  claude-in-mobile input "https://iptv-org.github.io/iptv/index.m3u"
  claude-in-mobile tap-text "Import playlist"
  claude-in-mobile wait 15000
  claude-in-mobile screenshot
  ```
- **Expected result**: Playlist downloads, parses, and saves. App navigates to Home screen with channels displayed.

### O2.3 -- Invalid URL shows error
- **Priority**: P1
- **Preconditions**: Onboarding screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "Enter playlist URL"
  claude-in-mobile input "not-a-url"
  claude-in-mobile tap-text "Import playlist"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Error message displayed: "Invalid playlist format" or similar. App stays on Onboarding screen.

### O2.4 -- Empty URL field shows validation
- **Priority**: P1
- **Preconditions**: Onboarding screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "Import playlist"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Error or no action -- the import button should not trigger a download with an empty URL field.

### O2.5 -- Network error during import
- **Priority**: P1
- **Preconditions**: Onboarding screen visible. Device in airplane mode.
- **Steps**:
  ```
  adb shell cmd connectivity airplane-mode enable
  claude-in-mobile tap-text "Enter playlist URL"
  claude-in-mobile input "https://iptv-org.github.io/iptv/index.m3u"
  claude-in-mobile tap-text "Import playlist"
  claude-in-mobile wait 5000
  claude-in-mobile screenshot
  adb shell cmd connectivity airplane-mode disable
  ```
- **Expected result**: Error message about network connectivity displayed. App stays on Onboarding.

### O2.6 -- File picker opens
- **Priority**: P1
- **Preconditions**: Onboarding screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "Choose file"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: System file picker dialog opens, showing files. (Cannot fully automate file selection via CLI; verify picker launches.)

### O2.7 -- Duplicate URL updates existing playlist
- **Priority**: P2
- **Preconditions**: Demo playlist already imported. Navigate back to add another playlist via Home's "+ Add new playlist".
- **Steps**:
  ```
  # Import demo playlist again via URL input on the add-playlist flow
  # (access via Home playlist dropdown -> "+ Add new playlist")
  claude-in-mobile wait 15000
  claude-in-mobile screenshot
  ```
- **Expected result**: Existing playlist is updated (not duplicated). Only one playlist entry appears in the playlist selector.

---

## 3. Home Screen

### H3.1 -- Home displays category sections
- **Priority**: P0
- **Preconditions**: Demo playlist imported.
- **Steps**:
  ```
  claude-in-mobile launch com.simplevideo.whiteiptv
  claude-in-mobile wait 5000
  claude-in-mobile screenshot
  ```
- **Expected result**: Home screen shows category sections (e.g., News, Sports, Music, General or other top groups from the demo playlist). Each section has a group name header with "View All" link and channel cards.

### H3.2 -- Playlist selector dropdown
- **Priority**: P0
- **Preconditions**: Demo playlist imported. Home screen visible.
- **Steps**:
  ```
  claude-in-mobile find --text "iptv"
  claude-in-mobile tap-text "iptv"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Dropdown menu appears showing the imported playlist with a checkmark, and "+ Add new playlist" action at the bottom.

### H3.3 -- Tap channel card navigates to Player
- **Priority**: P0
- **Preconditions**: Demo playlist imported. Home screen visible with channel cards.
- **Steps**:
  ```
  claude-in-mobile screenshot
  # Identify a channel card from the screenshot and tap it
  claude-in-mobile tap-text "<channel_name_from_screenshot>"
  claude-in-mobile wait 5000
  claude-in-mobile screenshot
  ```
- **Expected result**: Player screen opens. Video player is visible (may be buffering or playing). Channel name displayed in player controls.

### H3.4 -- "View All" on category navigates to Channels tab with group filter
- **Priority**: P1
- **Preconditions**: Demo playlist imported. Home screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "View All"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Channels tab opens with the group filter pre-selected to the category that was tapped. Title shows the group name instead of "All Channels".

### H3.5 -- Continue Watching section hidden when no history
- **Priority**: P1
- **Preconditions**: Fresh demo playlist import (no channels watched yet).
- **Steps**:
  ```
  claude-in-mobile screenshot
  claude-in-mobile find --text "Continue Watching"
  ```
- **Expected result**: "Continue Watching" section is NOT visible on the Home screen.

### H3.6 -- Continue Watching section appears after watching a channel
- **Priority**: P1
- **Preconditions**: Demo playlist imported. Watch at least one channel and return to Home.
- **Steps**:
  ```
  # Tap a channel to watch it
  claude-in-mobile tap-text "<channel_name>"
  claude-in-mobile wait 5000
  claude-in-mobile key back
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  claude-in-mobile find --text "Continue Watching"
  ```
- **Expected result**: "Continue Watching" section appears on Home with the recently watched channel visible.

### H3.7 -- Favorites section hidden when no favorites
- **Priority**: P1
- **Preconditions**: Fresh demo playlist import (no favorites set).
- **Steps**:
  ```
  claude-in-mobile screenshot
  claude-in-mobile find --text "Favorites"
  ```
- **Expected result**: Favorites section is NOT visible on the Home screen (only the Favorites tab in bottom nav).

### H3.8 -- Search icon opens search overlay
- **Priority**: P1
- **Preconditions**: Demo playlist imported. Home screen visible.
- **Steps**:
  ```
  claude-in-mobile annotate
  # Identify search icon from annotated screenshot and tap it
  claude-in-mobile tap <search_icon_x> <search_icon_y>
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Full-screen search overlay opens with text input field and "Search channels..." placeholder.

### H3.9 -- Playlist settings gear icon opens bottom sheet
- **Priority**: P1
- **Preconditions**: Demo playlist imported. A specific playlist selected (not "All Playlists").
- **Steps**:
  ```
  claude-in-mobile annotate
  # Identify gear icon and tap it
  claude-in-mobile tap <gear_icon_x> <gear_icon_y>
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Bottom sheet appears with playlist name as title and actions: Rename, Update, Delete, View URL.

### H3.10 -- Add new playlist from Home
- **Priority**: P1
- **Preconditions**: Demo playlist imported. Home screen visible.
- **Steps**:
  ```
  # Open playlist dropdown
  claude-in-mobile tap-text "iptv"
  claude-in-mobile wait 1000
  claude-in-mobile tap-text "Add new playlist"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Onboarding/import screen appears allowing the user to add another playlist.

### H3.11 -- Scroll through Home sections
- **Priority**: P2
- **Preconditions**: Demo playlist imported.
- **Steps**:
  ```
  claude-in-mobile swipe 540 1800 540 400
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  claude-in-mobile swipe 540 1800 540 400
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Home screen scrolls vertically showing more category sections below the fold.

---

## 4. Channels Screen

### C4.1 -- Channels tab shows channel grid
- **Priority**: P0
- **Preconditions**: Demo playlist imported.
- **Steps**:
  ```
  claude-in-mobile tap-text "Channels"
  claude-in-mobile wait 3000
  claude-in-mobile screenshot
  ```
- **Expected result**: 2-column grid of channel cards displayed. Each card shows channel logo, name, group label, and star toggle. Title shows "All Channels".

### C4.2 -- Scroll triggers paged loading
- **Priority**: P0
- **Preconditions**: Channels screen visible with demo playlist (10k+ channels).
- **Steps**:
  ```
  claude-in-mobile swipe 540 1800 540 200
  claude-in-mobile wait 1000
  claude-in-mobile swipe 540 1800 540 200
  claude-in-mobile wait 1000
  claude-in-mobile swipe 540 1800 540 200
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: More channels load as the user scrolls. No crash or hang with 10k+ channels. Loading spinner may appear briefly at the bottom during page fetch.

### C4.3 -- Group filter dropdown
- **Priority**: P0
- **Preconditions**: Channels screen visible.
- **Steps**:
  ```
  claude-in-mobile find --text "All"
  claude-in-mobile tap-text "All"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Group dropdown appears listing available channel groups from the playlist.

### C4.4 -- Select a group filter narrows channel list
- **Priority**: P0
- **Preconditions**: Channels screen with group dropdown open.
- **Steps**:
  ```
  # Select a specific group (e.g., "News")
  claude-in-mobile tap-text "News"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Channel grid shows only channels from the "News" group. Title changes to "News".

### C4.5 -- Toggle favorite on channel card
- **Priority**: P0
- **Preconditions**: Channels screen visible with channel cards.
- **Steps**:
  ```
  claude-in-mobile annotate
  # Identify star icon on first channel card and tap it
  claude-in-mobile tap <star_x> <star_y>
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Star icon toggles to filled state. Channel is now a favorite.

### C4.6 -- Tap channel navigates to Player
- **Priority**: P0
- **Preconditions**: Channels screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "<channel_name_visible>"
  claude-in-mobile wait 5000
  claude-in-mobile screenshot
  ```
- **Expected result**: Player screen opens with the selected channel loading/playing.

### C4.7 -- Search from Channels screen
- **Priority**: P1
- **Preconditions**: Channels screen visible.
- **Steps**:
  ```
  claude-in-mobile annotate
  # Tap search icon
  claude-in-mobile tap <search_icon_x> <search_icon_y>
  claude-in-mobile wait 1000
  claude-in-mobile input "BBC"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Search overlay opens. Typing "BBC" shows matching channels (FTS4 prefix match: "BBC" is a word prefix). Results include channels like "BBC World News", "BBC Arabic", etc.

### C4.8 -- Search with no results
- **Priority**: P1
- **Preconditions**: Channels search overlay open.
- **Steps**:
  ```
  claude-in-mobile input "xyznonexistent123"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Empty state shown: "No channels found" message.

### C4.9 -- Empty state when no channels match filter
- **Priority**: P2
- **Preconditions**: Channels screen visible. Select a group that may have no channels after a playlist update.
- **Steps**:
  ```
  # This is hard to trigger with demo playlist; verify the empty state text exists in code
  claude-in-mobile screenshot
  ```
- **Expected result**: If triggered, "No channels found" message is displayed.

### C4.10 -- Initial loading spinner
- **Priority**: P2
- **Preconditions**: Channels screen navigated to (first load).
- **Steps**:
  ```
  claude-in-mobile tap-text "Channels"
  claude-in-mobile screenshot
  ```
- **Expected result**: A centered spinner is briefly shown while the first page of channels loads (may be too fast to capture on fast devices).

---

## 5. Favorites Screen

### F5.1 -- Favorites tab shows empty state
- **Priority**: P0
- **Preconditions**: Demo playlist imported. No favorites set.
- **Steps**:
  ```
  claude-in-mobile tap-text "Favorites"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Empty state displayed: star icon, "No favorites yet" heading, "Add channels to Favorites by tapping the star icon" hint.

### F5.2 -- Favorites tab shows favorited channels
- **Priority**: P0
- **Preconditions**: At least one channel has been favorited (via Channels screen star toggle).
- **Steps**:
  ```
  claude-in-mobile tap-text "Favorites"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: List of favorite channels displayed. Each row shows channel logo, name, playlist/group label, and filled star icon.

### F5.3 -- Remove favorite from Favorites screen
- **Priority**: P0
- **Preconditions**: Favorites screen visible with at least one favorite.
- **Steps**:
  ```
  claude-in-mobile annotate
  # Tap star icon on first favorite to unfavorite
  claude-in-mobile tap <star_x> <star_y>
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Channel removed from favorites list immediately. If it was the last favorite, empty state appears.

### F5.4 -- Tap favorite channel navigates to Player
- **Priority**: P0
- **Preconditions**: Favorites screen with favorites listed.
- **Steps**:
  ```
  claude-in-mobile tap-text "<favorite_channel_name>"
  claude-in-mobile wait 5000
  claude-in-mobile screenshot
  ```
- **Expected result**: Player opens with the selected channel.

### F5.5 -- Search within Favorites
- **Priority**: P1
- **Preconditions**: Favorites screen with multiple favorites.
- **Steps**:
  ```
  claude-in-mobile annotate
  # Tap search icon
  claude-in-mobile tap <search_icon_x> <search_icon_y>
  claude-in-mobile wait 1000
  claude-in-mobile input "<partial_name>"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Favorites list filters to show only matching channels.

### F5.6 -- Playlist filter on Favorites
- **Priority**: P2
- **Preconditions**: Favorites from multiple playlists (requires importing two playlists and favoriting channels from each).
- **Steps**:
  ```
  claude-in-mobile find --text "All"
  claude-in-mobile tap-text "All"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Dropdown shows available playlists. Selecting one filters favorites to only that playlist's channels.

---

## 6. Player Screen

### P6.1 -- Player loads and plays stream
- **Priority**: P0
- **Preconditions**: Demo playlist imported. Navigate to any channel.
- **Steps**:
  ```
  # Navigate to Home, tap a channel
  claude-in-mobile tap-text "Channels"
  claude-in-mobile wait 3000
  claude-in-mobile tap-text "<first_visible_channel>"
  claude-in-mobile wait 8000
  claude-in-mobile screenshot
  ```
- **Expected result**: Player screen shows video content or buffering indicator. No crash.

### P6.2 -- Player controls appear and auto-hide
- **Priority**: P0
- **Preconditions**: Player screen with video playing.
- **Steps**:
  ```
  # Tap screen to show controls
  claude-in-mobile tap 540 1200
  claude-in-mobile wait 500
  claude-in-mobile screenshot
  # Wait for auto-hide
  claude-in-mobile wait 4000
  claude-in-mobile screenshot
  ```
- **Expected result**: First screenshot shows controls (play/pause, channel name, back button). Second screenshot (after 4s) shows controls hidden.

### P6.3 -- Back button returns to previous screen
- **Priority**: P0
- **Preconditions**: Player screen visible.
- **Steps**:
  ```
  # Show controls
  claude-in-mobile tap 540 1200
  claude-in-mobile wait 500
  # Tap back button
  claude-in-mobile annotate
  claude-in-mobile tap <back_button_x> <back_button_y>
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Returns to the screen from which the player was opened (Home, Channels, or Favorites).

### P6.4 -- System back returns from Player
- **Priority**: P0
- **Preconditions**: Player screen visible.
- **Steps**:
  ```
  claude-in-mobile key back
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Returns to the previous screen. Player resources are released.

### P6.5 -- Channel info displayed in controls
- **Priority**: P1
- **Preconditions**: Player screen with controls visible.
- **Steps**:
  ```
  claude-in-mobile tap 540 1200
  claude-in-mobile wait 500
  claude-in-mobile screenshot
  ```
- **Expected result**: Channel name and group name are visible in the player controls overlay.

### P6.6 -- Stream error shows retry option
- **Priority**: P1
- **Preconditions**: Play a channel with a known broken/offline stream.
- **Steps**:
  ```
  # Navigate to a channel that is likely offline
  claude-in-mobile wait 10000
  claude-in-mobile screenshot
  ```
- **Expected result**: Error message displayed with a retry option. No app crash.

### P6.7 -- Channel navigation via swipe (next channel)
- **Priority**: P1
- **Preconditions**: Player screen visible, opened from a list with multiple channels.
- **Steps**:
  ```
  # Swipe up in center area to go to next channel
  claude-in-mobile swipe 540 1200 540 400
  claude-in-mobile wait 5000
  claude-in-mobile tap 540 1200
  claude-in-mobile wait 500
  claude-in-mobile screenshot
  ```
- **Expected result**: Player switches to the next channel in the list. Channel name in controls changes.

### P6.8 -- Channel navigation via swipe (previous channel)
- **Priority**: P1
- **Preconditions**: Player has been swiped to at least the second channel.
- **Steps**:
  ```
  # Swipe down in center area to go to previous channel
  claude-in-mobile swipe 540 400 540 1200
  claude-in-mobile wait 5000
  claude-in-mobile tap 540 1200
  claude-in-mobile wait 500
  claude-in-mobile screenshot
  ```
- **Expected result**: Player switches to the previous channel.

### P6.9 -- Track selection bottom sheet
- **Priority**: P2
- **Preconditions**: Player with a stream that has multiple audio/subtitle tracks.
- **Steps**:
  ```
  # Show controls, tap tracks button (if available)
  claude-in-mobile tap 540 1200
  claude-in-mobile wait 500
  claude-in-mobile annotate
  # Tap tracks/quality button if visible
  claude-in-mobile tap <tracks_button_x> <tracks_button_y>
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Bottom sheet appears showing available audio tracks, subtitle tracks, and video quality options.

### P6.10 -- Volume gesture (right side vertical swipe)
- **Priority**: P2
- **Preconditions**: Player screen visible with video playing.
- **Steps**:
  ```
  # Swipe up on right side to increase volume
  claude-in-mobile swipe 900 1200 900 800
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Volume increases. A visual volume indicator may appear.

### P6.11 -- Brightness gesture (left side vertical swipe)
- **Priority**: P2
- **Preconditions**: Player screen visible with video playing.
- **Steps**:
  ```
  # Swipe up on left side to increase brightness
  claude-in-mobile swipe 180 1200 180 800
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Screen brightness increases. A visual brightness indicator may appear.

---

## 7. Settings Screen

### ST7.1 -- Settings tab displays all sections
- **Priority**: P0
- **Preconditions**: App launched with a playlist imported.
- **Steps**:
  ```
  claude-in-mobile tap-text "Settings"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Settings screen visible with sections: Appearance (Theme, Accent Color, Channel View), App Behavior (Language, Auto Update Playlists), Data & Storage, About.

### ST7.2 -- Theme toggle: System / Light / Dark
- **Priority**: P0
- **Preconditions**: Settings screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "Theme"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  # Select "Light"
  claude-in-mobile tap-text "Light"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Theme picker dialog/options appear. After selecting "Light", the app switches to light theme (light background, dark text). Setting persists.

### ST7.3 -- Theme toggle: switch to Dark
- **Priority**: P0
- **Preconditions**: Settings screen, currently in Light theme.
- **Steps**:
  ```
  claude-in-mobile tap-text "Theme"
  claude-in-mobile wait 1000
  claude-in-mobile tap-text "Dark"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: App switches to dark theme (dark background, light text).

### ST7.4 -- Theme persists across app restart
- **Priority**: P1
- **Preconditions**: Theme set to "Light".
- **Steps**:
  ```
  claude-in-mobile stop com.simplevideo.whiteiptv
  claude-in-mobile launch com.simplevideo.whiteiptv
  claude-in-mobile wait 5000
  claude-in-mobile tap-text "Settings"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: App launches in Light theme. Settings screen shows "Light" as the selected theme.

### ST7.5 -- Accent Color selection
- **Priority**: P1
- **Preconditions**: Settings screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "Accent Color"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  claude-in-mobile tap-text "Blue"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Color picker shows Teal, Blue, Red options. Selecting "Blue" persists the preference. Note: per current limitations, the accent color is saved but may NOT visually change the theme colors yet.

### ST7.6 -- Channel View mode toggle
- **Priority**: P1
- **Preconditions**: Settings screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "Channel View"
  claude-in-mobile wait 1000
  claude-in-mobile tap-text "Grid"
  claude-in-mobile wait 1000
  # Navigate to Channels to verify
  claude-in-mobile tap-text "Channels"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Channel view preference changes to Grid. Channels screen displays in grid layout.

### ST7.7 -- Auto Update Playlists toggle
- **Priority**: P1
- **Preconditions**: Settings screen visible. Auto Update is Off by default.
- **Steps**:
  ```
  claude-in-mobile tap-text "Auto Update Playlists"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Toggle switches to On. Playlist auto-refresh scheduler activates (coroutine-based, foreground only). Toggle state persists.

### ST7.8 -- Clear Cache action
- **Priority**: P2
- **Preconditions**: Settings screen visible.
- **Steps**:
  ```
  claude-in-mobile swipe 540 1800 540 800
  claude-in-mobile wait 500
  claude-in-mobile tap-text "Clear Cache"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Success message displayed. Note: per current limitations, shows "0 MB" and does not actually clear caches.

### ST7.9 -- Clear Favorites with confirmation
- **Priority**: P1
- **Preconditions**: Settings screen visible. At least one favorite exists.
- **Steps**:
  ```
  claude-in-mobile swipe 540 1800 540 800
  claude-in-mobile wait 500
  claude-in-mobile tap-text "Clear Favorites"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  # Confirm
  claude-in-mobile tap-text "Confirm"
  claude-in-mobile wait 1000
  # Navigate to Favorites tab to verify
  claude-in-mobile tap-text "Favorites"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Confirmation dialog appears. After confirming, all favorites are removed. Favorites tab shows empty state.

### ST7.10 -- Reset to Defaults with confirmation
- **Priority**: P1
- **Preconditions**: Settings screen visible. Some settings changed (e.g., theme set to Light).
- **Steps**:
  ```
  claude-in-mobile swipe 540 1800 540 800
  claude-in-mobile wait 500
  claude-in-mobile tap-text "Reset to Defaults"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  # Confirm
  claude-in-mobile tap-text "Confirm"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Confirmation dialog appears (destructive action in red). After confirming, all settings reset to defaults: Theme=System, Accent Color=Teal, Channel View=List, Auto Update=Off. Playlists and channels are NOT deleted.

### ST7.11 -- About section displays app version
- **Priority**: P2
- **Preconditions**: Settings screen visible.
- **Steps**:
  ```
  claude-in-mobile swipe 540 1800 540 400
  claude-in-mobile wait 500
  claude-in-mobile screenshot
  claude-in-mobile find --text "Version"
  ```
- **Expected result**: About section visible with app version number.

### ST7.12 -- Language shows "System" only
- **Priority**: P2
- **Preconditions**: Settings screen visible.
- **Steps**:
  ```
  claude-in-mobile find --text "Language"
  claude-in-mobile screenshot
  ```
- **Expected result**: Language setting shows "System" as the only option (per current limitations).

---

## 8. Playlist Settings (Bottom Sheet from Home)

### PS8.1 -- View URL action
- **Priority**: P1
- **Preconditions**: Demo playlist imported (URL-based). Home screen, playlist settings bottom sheet open.
- **Steps**:
  ```
  claude-in-mobile tap-text "Home"
  claude-in-mobile wait 2000
  # Open gear icon
  claude-in-mobile annotate
  claude-in-mobile tap <gear_x> <gear_y>
  claude-in-mobile wait 1000
  claude-in-mobile tap-text "View URL"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Dialog shows the playlist source URL (https://iptv-org.github.io/iptv/index.m3u) in read-only format.

### PS8.2 -- Rename playlist
- **Priority**: P1
- **Preconditions**: Playlist settings bottom sheet open.
- **Steps**:
  ```
  claude-in-mobile tap-text "Rename"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  # Type new name
  claude-in-mobile input "My Test Playlist"
  claude-in-mobile tap-text "OK"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Rename dialog appears with current name. After confirming, playlist name updates in the Home header.

### PS8.3 -- Update playlist (re-download)
- **Priority**: P1
- **Preconditions**: URL-based playlist imported. Playlist settings bottom sheet open.
- **Steps**:
  ```
  claude-in-mobile tap-text "Update"
  claude-in-mobile wait 15000
  claude-in-mobile screenshot
  ```
- **Expected result**: Playlist re-downloads and re-parses. Favorites are preserved. Channel list refreshes. Loading indicator shown during update.

### PS8.4 -- Delete playlist (last playlist navigates to Onboarding)
- **Priority**: P0
- **Preconditions**: Only one playlist exists. Playlist settings bottom sheet open.
- **Steps**:
  ```
  claude-in-mobile tap-text "Delete"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  # Confirm deletion
  claude-in-mobile tap-text "Confirm"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Confirmation dialog appears. After confirming, playlist is deleted and app navigates to Onboarding screen (no playlists remain).

### PS8.5 -- Delete playlist (with other playlists remaining)
- **Priority**: P1
- **Preconditions**: Two or more playlists imported. Playlist settings open for one of them.
- **Steps**:
  ```
  claude-in-mobile tap-text "Delete"
  claude-in-mobile wait 1000
  claude-in-mobile tap-text "Confirm"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Playlist deleted. Home screen shows another remaining playlist as active.

### PS8.6 -- Gear icon disabled for "All Playlists"
- **Priority**: P2
- **Preconditions**: Multiple playlists imported. "All Playlists" selected in dropdown.
- **Steps**:
  ```
  # Select "All Playlists" from dropdown
  claude-in-mobile tap-text "All"
  claude-in-mobile wait 1000
  claude-in-mobile annotate
  # Check gear icon state
  ```
- **Expected result**: Gear icon is disabled/not tappable when "All Playlists" is selected.

---

## 9. Search (Cross-Screen)

### SR9.1 -- FTS4 word prefix search
- **Priority**: P0
- **Preconditions**: Demo playlist imported. Search overlay open (from any screen).
- **Steps**:
  ```
  claude-in-mobile input "BB"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Channels starting with "BB" prefix appear (e.g., "BBC World News", "BBC Arabic"). FTS4 tokenizer matches word prefixes.

### SR9.2 -- FTS4 mid-word substring does NOT match
- **Priority**: P1
- **Preconditions**: Search overlay open.
- **Steps**:
  ```
  claude-in-mobile input "NN"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: "CNN" does NOT appear in results (per FTS4 limitation -- "NN" is not a word prefix). This is expected behavior documented in `docs/features/search.md`.

### SR9.3 -- Search results update as user types
- **Priority**: P1
- **Preconditions**: Search overlay open.
- **Steps**:
  ```
  claude-in-mobile input "N"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  claude-in-mobile input "ew"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Results update incrementally. First shows channels with words starting with "N", then narrows to channels with words starting with "New".

### SR9.4 -- Dismiss search returns to parent screen
- **Priority**: P1
- **Preconditions**: Search overlay open.
- **Steps**:
  ```
  claude-in-mobile key back
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Search overlay closes. Parent screen (Home/Channels/Favorites) is visible.

### SR9.5 -- Empty query shows all channels in scope
- **Priority**: P2
- **Preconditions**: Search overlay open with text entered.
- **Steps**:
  ```
  # Clear the search field (select all + delete)
  claude-in-mobile tap <clear_icon_x> <clear_icon_y>
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: All channels in the current scope are displayed (same as before search was triggered).

---

## 10. Navigation and Tab Switching

### N10.1 -- Bottom navigation tabs work
- **Priority**: P0
- **Preconditions**: Main screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "Home"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  claude-in-mobile tap-text "Favorites"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  claude-in-mobile tap-text "Channels"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  claude-in-mobile tap-text "Settings"
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: Each tab displays the correct screen. Active tab is highlighted in the bottom nav bar.

### N10.2 -- System back from Main does not return to Splash
- **Priority**: P0
- **Preconditions**: Main screen visible (navigated from Splash).
- **Steps**:
  ```
  claude-in-mobile key back
  claude-in-mobile wait 1000
  claude-in-mobile screenshot
  ```
- **Expected result**: App exits or goes to system home. Does NOT navigate back to Splash screen (back stack was cleared).

### N10.3 -- Deep navigation: Home -> Channel -> Player -> Back -> Home
- **Priority**: P1
- **Preconditions**: Demo playlist imported.
- **Steps**:
  ```
  claude-in-mobile tap-text "Home"
  claude-in-mobile wait 2000
  # Tap a channel card
  claude-in-mobile tap-text "<channel_name>"
  claude-in-mobile wait 5000
  # Return from player
  claude-in-mobile key back
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: After returning from Player, the Home screen is displayed (not Channels or another tab).

---

## 11. Edge Cases and Stress Tests

### E11.1 -- App handles large playlist without OOM
- **Priority**: P0
- **Preconditions**: Demo playlist (10k+ channels) imported.
- **Steps**:
  ```
  claude-in-mobile tap-text "Channels"
  claude-in-mobile wait 3000
  # Rapid scrolling
  claude-in-mobile swipe 540 1800 540 200
  claude-in-mobile swipe 540 1800 540 200
  claude-in-mobile swipe 540 1800 540 200
  claude-in-mobile swipe 540 1800 540 200
  claude-in-mobile swipe 540 1800 540 200
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  claude-in-mobile logs --tag "AndroidRuntime" --lines 10
  ```
- **Expected result**: No crash, no ANR, no OOM. Channels continue to load via paging.

### E11.2 -- Rapid tab switching
- **Priority**: P1
- **Preconditions**: Main screen visible.
- **Steps**:
  ```
  claude-in-mobile tap-text "Channels"
  claude-in-mobile tap-text "Home"
  claude-in-mobile tap-text "Favorites"
  claude-in-mobile tap-text "Settings"
  claude-in-mobile tap-text "Home"
  claude-in-mobile tap-text "Channels"
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  claude-in-mobile logs --tag "AndroidRuntime" --lines 10
  ```
- **Expected result**: No crash. Final screen shows Channels tab correctly.

### E11.3 -- App survives process death
- **Priority**: P1
- **Preconditions**: Demo playlist imported. App on Home screen.
- **Steps**:
  ```
  claude-in-mobile stop com.simplevideo.whiteiptv
  claude-in-mobile launch com.simplevideo.whiteiptv
  claude-in-mobile wait 5000
  claude-in-mobile screenshot
  ```
- **Expected result**: App relaunches to Home screen with playlist data intact (persisted in Room DB and DataStore).

### E11.4 -- Orientation change during playback
- **Priority**: P2
- **Preconditions**: Player screen with video playing.
- **Steps**:
  ```
  adb shell settings put system accelerometer_rotation 1
  adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:1
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:0
  claude-in-mobile wait 2000
  claude-in-mobile screenshot
  ```
- **Expected result**: Player handles orientation change without crash. Video continues playing.

---

## 12. Playlist Auto-Refresh (Background)

### AR12.1 -- Auto-refresh triggers when enabled
- **Priority**: P2
- **Preconditions**: URL-based playlist imported. Auto Update enabled in Settings.
- **Steps**:
  ```
  # Enable auto-update
  claude-in-mobile tap-text "Settings"
  claude-in-mobile wait 1000
  claude-in-mobile tap-text "Auto Update Playlists"
  claude-in-mobile wait 1000
  # Check logs for refresh scheduler
  claude-in-mobile logs --tag "PlaylistAutoRefresh" --lines 20
  ```
- **Expected result**: Logs indicate the auto-refresh scheduler is active. (Full verification requires waiting for the refresh interval, which defaults to 6 hours -- impractical for E2E testing.)

---

## Execution Order

For a full regression run, execute test groups in this order:

1. **S1.1** -- Fresh install splash to onboarding (clean state)
2. **O2.1** -- Import demo playlist (establishes test data)
3. **H3.1-H3.11** -- Home screen tests
4. **C4.1-C4.10** -- Channels screen tests
5. **C4.5** then **F5.2-F5.5** -- Favorite a channel, then test Favorites screen
6. **P6.1-P6.11** -- Player tests
7. **SR9.1-SR9.5** -- Search tests
8. **ST7.1-ST7.12** -- Settings tests
9. **PS8.1-PS8.3** -- Playlist settings (non-destructive)
10. **N10.1-N10.3** -- Navigation tests
11. **E11.1-E11.4** -- Edge cases
12. **PS8.4** -- Delete last playlist (destructive, run last)

## Priority Summary

| Priority | Count | Description |
|----------|-------|-------------|
| P0 | 19 | Critical path -- app must not crash, core flows must work |
| P1 | 27 | Important -- key features that users rely on daily |
| P2 | 14 | Nice to have -- edge cases, placeholders, advanced gestures |
| **Total** | **60** | |

## Known Limitations Affecting Tests

1. **Accent Color**: Setting is persisted but does not visually change the theme (ST7.5 will show preference saved but no visual change).
2. **Clear Cache**: Shows "0 MB" placeholder (ST7.8 will see success message but no actual cache clearing).
3. **Language**: Only shows "System" (ST7.12 will confirm this limitation).
4. **FTS4 word-boundary**: Partial substring search does not work (SR9.2 validates this expected behavior).
5. **EPG**: Not implemented -- no EPG-related assertions in player tests.
6. **PiP / Sleep Timer / Cast**: Not implemented -- no test scenarios included.

## Build and Test Commands

```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Install on device
claude-in-mobile install androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Clear app data for clean state
adb shell pm clear com.simplevideo.whiteiptv

# Launch app
claude-in-mobile launch com.simplevideo.whiteiptv

# Check for crashes after any test
claude-in-mobile logs --tag "AndroidRuntime" --lines 20
```
