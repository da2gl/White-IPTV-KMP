# Favorites

Central access to all favorite channels across playlists.

## Purpose

Let users quickly find and play channels they've saved, without navigating through categories.

## Layout

List layout — each row shows:
- Channel logo/icon
- Channel name
- Playlist/group label
- Star toggle to remove from favorites

## Filtering

- **Playlist selector** — tapping the playlist name opens a bottom sheet to filter favorites by a specific playlist or show all.
- **Search** — text search within favorites, scoped to the current playlist filter. See [Search](./search.md).

## Behavior

- Favorites are global — collected from all playlists.
- When accessed from Home "View All", the active playlist filter is auto-applied.
- Removing a favorite (toggling the star) removes it immediately with animation.
- Tapping a channel navigates to [Player](./player.md).

## Empty State

When no favorites exist:
- Star icon
- "No favorites yet" heading
- "Add channels to Favorites by tapping the star icon" hint
