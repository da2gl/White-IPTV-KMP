# Player

Full-screen video player for live IPTV streams.

## Purpose

Provide smooth, uninterrupted playback with quick access to controls, track selection, and channel navigation.

## Controls

Controls auto-hide after 3 seconds of inactivity. Tapping the screen toggles visibility.

### Playback Controls
- Play/Pause
- Channel info (name, group)
- Back button (return to previous screen)

### Gesture Controls
- **Vertical swipe (left side)** — adjust brightness
- **Vertical swipe (right side)** — adjust volume
- **Horizontal swipe** — seek (for catchup/archive content)
- **Swipe up/down (center)** — next/previous channel

### Track Selection
- **Audio tracks** — select audio language/track when multiple are available
- **Subtitles** — enable/disable subtitle tracks
- **Video quality** — select resolution/bitrate (Auto, HD, SD, etc.)

Presented as a bottom sheet dialog.

## Channel Navigation

Users can switch channels without leaving the player:
- Swipe up — next channel in the current list
- Swipe down — previous channel
- Channel order follows the list from which the player was opened (Home category, Favorites, Channels screen)

## EPG Integration

When [EPG data](./epg.md) is available for the current channel:
- Show current program name and time
- Show next program name

See [EPG](./epg.md) for details.

## Planned Features

### Picture-in-Picture (PiP)
Watch in a floating window while using other apps. Platform-specific implementation (Android PiP API, iOS AVPictureInPictureController).

### Sleep Timer
Auto-stop playback after a user-selected duration (15m, 30m, 45m, 1h, 2h).

### Chromecast / AirPlay
Cast the stream to a TV. Platform-specific: Chromecast on Android, AirPlay on iOS.

## Keep Screen On

Screen stays awake during active playback. Releases when player is paused or closed.

## Error Handling

| Scenario | Behavior |
|----------|----------|
| Stream fails to load | Show error message with retry option |
| Behind-live-window error | Automatically seek to live edge |
| Network lost | Show buffering indicator, retry when network returns |
| Unsupported format | Show "Format not supported" error |

## Platform Implementation

- **Android**: ExoPlayer (Media3) with Cronet network stack, IPTV-optimized buffer configuration.
- **iOS**: AVPlayer implementation (planned).

See [Platform](../constraints/platform.md) for details.
