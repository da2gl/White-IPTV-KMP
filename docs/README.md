# WhiteIPTV

WhiteIPTV is a mobile application for watching IPTV channels. It helps users solve two everyday problems:

**"What's on?"** — Users import their M3U playlists from IPTV providers or public sources, and the app organizes channels into browsable categories. One tap starts playback.

**"Where's my channel?"** — With hundreds or thousands of channels across multiple playlists, finding the right one needs to be fast. [Search](./features/search.md) and [filtering](./features/channel-browsing.md) by category, playlist, and favorites let users locate any channel instantly.

The app targets mass-market users who receive an M3U link from their IPTV provider and want to start watching with minimal friction. No technical knowledge required.

## How It Works

- **Playlist import** — add playlists via URL, local file, or a built-in demo. The app [parses M3U/M3U8](./domain/playlist.md) format and extracts channels, groups, and metadata.
- **Channel browsing** — browse channels organized by [groups](./domain/channel-group.md) parsed from the playlist. The [Home](./features/home.md) screen shows favorites, continue watching, and top categories at a glance.
- **Favorites** — save channels to [favorites](./features/favorites.md) for quick access across all playlists.
- **Video player** — watch live streams with gesture controls, track selection (audio, subtitles, quality), and channel navigation. See [Player](./features/player.md).
- **EPG** — view current and upcoming programs via [TV guide](./features/epg.md) data from the playlist's XMLTV source.

## Design Principles

- One-tap playback
- Dark-first design with light theme support
- Minimal visual noise
- Context-aware filtering and search
- Consistent card layout across screens
