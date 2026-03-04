# Current Limitations

Known gaps between the product specification and the current implementation.

---

## Settings screen is a placeholder

The Settings screen currently shows only `Text("Settings Screen")`. No ViewModel, no state management, no configurable options exist.

**Target behavior**: Full settings with Appearance, App Behavior, Data & Storage, and About sections. See [Settings](../features/settings.md).

---

## iOS video player not implemented

The iOS player (`IOSVideoPlayerFactory`) is a stub returning a non-functional dummy player. No AVPlayer integration exists.

**Target behavior**: AVPlayer-based implementation matching the Android ExoPlayer feature set (playback, track selection, gestures).

---

## Continue Watching returns empty

`GetContinueWatchingUseCase` returns an empty Flow. No watch history tracking, no database table for watch events.

**Target behavior**: Record channel plays with timestamps, display recent channels on Home. See [Home](../features/home.md#continue-watching).

---

## Search not database-driven

Favorites screen implements search as in-memory filtering. Channels screen has a search icon with a TODO comment but no implementation.

**Target behavior**: Context-aware search across all screens, potentially database-driven for large playlists. See [Search](../features/search.md).

---

## Playlist auto-refresh not implemented

`PlaylistEntity` has a `refreshInterval` field parsed from M3U, but no background job or periodic update logic exists.

**Target behavior**: When auto-update is enabled in Settings, playlists refresh automatically based on their interval. See [Settings](../features/settings.md).

---

## EPG not implemented

No XMLTV parser, no EPG data model, no program guide UI. The `urlTvg` field is stored in `PlaylistEntity` but unused.

**Target behavior**: Parse XMLTV, match programs to channels via tvg-id, show current/next program in player. See [EPG](../features/epg.md).

---

## PiP, Sleep Timer, Chromecast/AirPlay not implemented

Player features exist only in the specification. No code for picture-in-picture, sleep timer countdown, or cast functionality.

**Target behavior**: See [Player](../features/player.md#planned-features).

---

## Playlist management actions not implemented

No UI for renaming, deleting, or manually updating existing playlists. The gear icon on Home and playlist management flows do not exist.

**Target behavior**: See [Playlist Settings](../features/playlist-settings.md).

---

## Light theme not implemented

The app uses a dark-only theme. `Theme.kt` does not define light color scheme variants.

**Target behavior**: Dark + Light + System theme support, selectable in Settings.
