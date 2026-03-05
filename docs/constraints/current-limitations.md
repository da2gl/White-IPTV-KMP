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

