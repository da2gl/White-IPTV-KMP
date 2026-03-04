# Channel

A single IPTV stream that the user can watch. Channel is the central entity — browsing, favorites, search, and playback all revolve around channels.

## Properties

- **Name** — display name parsed from M3U `tvg-name` or `#EXTINF` title.
- **Stream URL** — HTTP/HTTPS URL of the live stream.
- **Logo URL** — optional channel logo image URL from `tvg-logo`.
- **Group** — category/genre assignment. A channel can belong to multiple [Channel Groups](./channel-group.md) via a many-to-many relationship.
- **Is Favorite** — user-set flag. Persists across playlist updates.
- **TVG ID** — EPG identifier for matching with [TV guide](../features/epg.md) data.
- **TVG Language / Country** — optional metadata for future filtering.
- **Catchup** — archive/timeshift metadata (`catchup-days`, `catchup-type`, `catchup-source`) for EPG-based replay.
- **User-Agent / Referer** — per-channel HTTP headers for stream requests, overriding playlist-level defaults.
- **Extended metadata** — less common M3U attributes stored as JSON for forward compatibility.

## Relationships

- A Channel belongs to one [Playlist](./playlist.md).
- A Channel can belong to zero or more [Channel Groups](./channel-group.md).
- A Channel can appear in [Favorites](../features/favorites.md).
- A Channel can appear in [Continue Watching](../features/home.md#continue-watching).

## Business Rules

- A Channel must have a stream URL.
- Deleting a channel removes it from all groups and favorites.
- Favorite status is a local user preference — not part of the M3U data.
- When the parent playlist is deleted, all its channels are cascade-deleted.
