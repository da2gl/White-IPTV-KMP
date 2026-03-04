# Playlist

A collection of IPTV channels imported by the user. Playlist is the top-level container — all channels, groups, and metadata belong to a specific playlist.

## Properties

- **Name** — display name, auto-detected from M3U header or derived from URL/filename. User can rename.
- **Source URL** — original URL for URL-based playlists. Used for updates. Unique constraint — no duplicate URLs.
- **Icon** — optional icon URL from M3U header.
- **Channel count** — number of channels in the playlist.
- **Refresh interval** — auto-update period parsed from M3U `url-refresh` attribute. Disabled by default.
- **User-Agent** — custom HTTP User-Agent header from M3U metadata, passed to player for stream requests.
- **Last update** — timestamp of the most recent import or refresh.
- **Created at** — timestamp of the initial import.

## Import Sources

A playlist can be imported from one of three sources:

- **URL** — user enters an HTTP/HTTPS link to an M3U/M3U8 file. The app downloads and parses it.
- **Local file** — user picks a `.m3u` or `.m3u8` file from the device. The app reads and parses it.
- **Demo playlist** — built-in link to a public playlist (iptv-org) for users who want to try the app without their own source.

See [Import Playlist](../flows/import-playlist.md) for the full flow.

## Update Behavior

When a playlist is updated (manually or via auto-refresh):
- New channels are added.
- Removed channels are deleted.
- Existing channels are updated (name, URL, metadata).
- **Favorite status is preserved** — if a channel was marked as favorite before the update, it stays favorite after.

## Relationships

- A Playlist contains zero or more [Channels](./channel.md).
- A Playlist contains zero or more [Channel Groups](./channel-group.md).
- A Playlist belongs to the user (single-user, no accounts).

## Business Rules

- A playlist must have at least one channel after import (empty playlists are rejected).
- Source URL must be unique — importing a duplicate URL triggers an update of the existing playlist.
- Deleting a playlist removes all its channels, groups, and cross-references.
