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

## Presentation

Uses a **Material3 ModalBottomSheet** overlay on the Home screen. The bottom sheet shows the playlist name as title and lists available actions as tappable rows with icons.

- Rename and Delete show confirmation dialogs (AlertDialog) before executing.
- View URL shows a read-only dialog with the source URL.

## Behavior

- For local-file playlists, "Update" is not available (no source URL to re-download from).
- Deleting the last playlist navigates to [Onboarding](../flows/import-playlist.md).
- After a successful update, the channel list refreshes with the new data.
- The gear icon is disabled when "All Playlists" is selected (no single playlist to manage).
- Per-playlist auto-update interval is not configurable here; that belongs in global [Settings](./settings.md).
