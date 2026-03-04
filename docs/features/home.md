# Home

Content hub showing personalized channel sections for the active playlist.

## Purpose

Give the user a quick overview of their channels — what they were watching, what's favorite, and what's available by category — so they can start playback with one tap.

## Header

- **Playlist selector** — dropdown showing all imported playlists with a checkmark on the active one. Includes "+ Add new playlist" action at the bottom, separated by a divider.
- **Search icon** — opens [search](./search.md) scoped to the current playlist.
- **Playlist settings icon** (gear) — opens settings for the active playlist: rename, delete, update, view URL. See [Playlist Settings](./playlist-settings.md).

## Sections

### Continue Watching

Horizontal scrollable row of recently watched channels. Each card shows:
- Channel logo/thumbnail
- Channel name
- Time indicator (e.g., "24m left", "1h 15m left")

Hidden when no watch history exists.

### Favorites

Horizontal scrollable row of favorite channels from the active playlist. Each card shows:
- Channel logo
- Channel name
- LIVE badge (when applicable)
- "View All" link navigates to the [Favorites](./favorites.md) tab.

Hidden when no favorites exist for the active playlist.

### Category Sections

Multiple sections, one per top [channel group](../domain/channel-group.md). Each section shows:
- Group name as section header
- "View All" link navigates to [Channels](./channel-browsing.md) tab filtered by that group
- Sample channels from the group

Group selection prioritizes common categories (News, Sports, Music, General), then fills with the largest groups by channel count. Groups with invalid names are excluded.

## Navigation

- Tapping a channel card navigates to [Player](./player.md).
- "View All" on Favorites navigates to Favorites tab.
- "View All" on a category navigates to Channels tab with that group pre-selected.
