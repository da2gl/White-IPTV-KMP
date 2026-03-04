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

2-column grid of channel cards. Each card shows:
- Channel logo/icon
- Channel name
- Group label
- Star toggle (filled = favorite, outline = not favorite)

Tapping a card navigates to [Player](./player.md).
Tapping the star toggles [favorite](./favorites.md) status.

## Navigation Context

When navigated from Home "View All" on a category section, the group filter is pre-selected to that category.

## Empty State

When no channels match the current filters:
- "No channels found" message
- "Try adjusting your search or category filter" hint
- "Reload Playlist" button
