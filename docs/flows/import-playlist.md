# Import Playlist

The flow of adding a new IPTV playlist to the app.

## Trigger

- **First launch**: Onboarding screen shown automatically when no playlists exist.
- **From Home**: User taps "+ Add new playlist" in the playlist dropdown.

## Input Methods

The user provides a playlist in one of three ways:

- **URL** — enter an HTTP/HTTPS link to an M3U/M3U8 file in the text field. URL is validated (must start with `http://` or `https://`).
- **Local file** — tap "Choose file" to open the device file picker. Accepts `.m3u` and `.m3u8` files.
- **Demo playlist** — tap "Use demo playlist" to import a public playlist from iptv-org. No user input required.

## Processing

After the user taps "Import playlist" (or selects a file/demo):

1. **Validation** — URL format check (for URL imports).
2. **Download** — HTTP request to fetch the M3U file (for URL imports).
3. **Parse** — Extract channels, groups, and metadata from M3U content.
4. **Deduplication** — If a playlist with the same URL already exists, update it instead of creating a duplicate.
5. **Save** — Atomic transaction: insert playlist + channels + groups + cross-references.

A loading indicator is shown during processing.

## Result

- **Success**: Navigate to Home screen. The new playlist is auto-selected.
- **First playlist**: Navigate from Onboarding to Main (clears back stack).
- **Additional playlist**: Return to Home with the new playlist selected.

## Error Scenarios

| Scenario | Behavior |
|----------|----------|
| Invalid URL format | Inline error: "Invalid playlist format" |
| Network error | Error message with suggestion to check connection |
| Parse error | Error: "Invalid playlist format. Please try again." |
| Empty playlist (no channels) | Error: "No channels found in this playlist" |
| Duplicate URL | Existing playlist is updated instead of creating a new one |

## UI Elements

- App logo and name (WhiteIPTV) at the top
- URL input field with placeholder
- "or" divider
- "Choose file" button
- "Import playlist" primary CTA button
- Error message area
- "Use demo playlist" link at the bottom
- Terms of Service link in the footer
