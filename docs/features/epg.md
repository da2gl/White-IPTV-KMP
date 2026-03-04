# EPG (TV Guide)

Electronic Program Guide showing current and upcoming TV programs.

## Purpose

Let users see what's currently playing and what's coming next, so they can decide whether to watch or switch channels.

## Data Source

EPG data comes from the `url-tvg` attribute in the M3U playlist header. This points to an XMLTV file containing program schedules.

Channels are matched to EPG data via the `tvg-id` attribute on each channel.

## Display

### In Player
- Current program: name and time range
- Next program: name and start time

### In Channel Cards
> [!NOTE]
> **Undefined — requires clarification:**
> - Whether EPG info should appear in channel cards on Home/Channels/Favorites screens.
> - How to handle timezones for EPG data.

## Catchup / Archive

Some channels support timeshift/catchup, indicated by M3U attributes:
- `catchup-days` — how many days of archive are available
- `catchup-type` — archive format (shift, append, default)
- `catchup-source` — URL template for archive streams

When available, users can watch past programs from the EPG.

> [!NOTE]
> **Undefined — requires clarification:**
> - UI for browsing past programs and triggering catchup playback.
> - Whether EPG data should be cached locally and for how long.
> - XMLTV parsing performance for large EPG files (can be 50MB+).

## Error Handling

| Scenario | Behavior |
|----------|----------|
| No url-tvg in playlist | EPG features hidden |
| XMLTV download fails | EPG features hidden, no error shown to user |
| Channel has no tvg-id match | No program info shown for that channel |
