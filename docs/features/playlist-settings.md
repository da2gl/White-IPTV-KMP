# Playlist Settings

Per-playlist management accessible from the Home screen gear icon.

## Purpose

Let users rename, update, or delete a specific playlist without navigating to global Settings.

## Access

Gear icon (⚙) in the Home screen header, next to the search icon. Always operates on the currently selected playlist.

## Actions

| Action | Description |
|--------|-------------|
| Rename | Change the playlist display name |
| Update | Re-download and re-parse the playlist from its source URL. Preserves favorites. |
| Delete | Remove the playlist and all its channels, groups, and data. Confirmation required. |
| View URL | Show the source URL (read-only) for URL-based playlists |

## Behavior

- For local-file playlists, "Update" is not available (no source URL to re-download from).
- Deleting the last playlist navigates to [Onboarding](../flows/import-playlist.md).
- After a successful update, the channel list refreshes with the new data.

> [!NOTE]
> **Undefined — requires clarification:**
> - Whether this should be a bottom sheet, dialog, or a separate screen.
> - Whether auto-update interval can be configured per-playlist here.
