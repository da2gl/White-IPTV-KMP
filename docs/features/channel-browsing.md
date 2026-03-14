# Channel Browsing

Full catalog of channels with filtering by playlist and group.

## Purpose

Let users browse all available channels, filter by category, and manage favorites.

## Header

- **Title**: "All Channels" or the selected group name.
- **Search icon** — opens [search](./search.md) scoped to the current filters.

## Filtering

- **Playlist dropdown** — select a specific playlist or "All".
- **Group dropdown** — select a specific [channel group](../domain/channel-group.md) or "All". Groups are loaded based on the selected playlist.

Filters are combinable: selecting a playlist narrows the group list, and selecting a group narrows the channel list.

## Channel Grid

2-column grid of channel cards with paged loading for performance with large playlists (10k+ channels). Channels are loaded incrementally in pages of 50, with the next page prefetched as the user scrolls.

Each card shows:
- Channel logo/icon
- Channel name
- Group label
- Star toggle (filled = favorite, outline = not favorite)

Tapping a card navigates to [Player](./player.md).
Tapping the star toggles [favorite](./favorites.md) status.

## Navigation Context

When navigated from Home "View All" on a category section, the group filter is pre-selected to that category.

## Loading and Empty States

**Initial load**: A centered spinner is shown while the first page of channels is being fetched.

**Append load**: When the user scrolls near the end of the loaded channels, a spinner appears at the bottom of the grid while the next page loads.

**Empty state**: When no channels match the current filters:
- "No channels found" message
- If search is active, a search-specific empty state with the query is shown
