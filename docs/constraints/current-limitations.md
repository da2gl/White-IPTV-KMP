# Current Limitations

Known gaps between the product specification and the current implementation.

---

## Settings: Language switching not implemented

The Settings screen Language option shows "System" only. No locale override or translated string resources exist yet.

**Target behavior**: Allow users to select a language from a list, overriding the system default.

---

## Settings: Clear Cache is a placeholder

The Clear Cache option shows "0 MB" and displays a success message, but does not actually clear platform caches (Coil image cache, Ktor HTTP cache). Requires platform-specific expect/actual implementation.

**Target behavior**: Calculate and display actual cache size, clear all caches when tapped.

---

## Settings: Accent Color is persisted but not applied to theme

The Accent Color preference (Teal/Blue/Red) is saved in settings but does not yet change the Material theme color scheme. The `AppTheme` always uses the default Teal primary color.

**Target behavior**: Switching accent color changes the primary color throughout the app.

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

