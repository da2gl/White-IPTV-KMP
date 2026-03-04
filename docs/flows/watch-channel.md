# Watch Channel

The flow of selecting and watching a channel.

## Trigger

User taps a channel card from any screen: Home, Favorites, or Channels.

## Steps

1. **Navigate to Player** — full-screen player opens with the selected channel.
2. **Load stream** — player initializes with the channel's stream URL, applying any custom User-Agent or Referer headers.
3. **Playback starts** — stream begins playing. Buffering indicator shown while loading.
4. **Controls visible** — playback controls shown briefly, then auto-hide after 3 seconds.

## During Playback

- User can switch channels via swipe gestures (up/down).
- User can select audio tracks, subtitles, or video quality.
- User can toggle favorite status.
- Screen stays on during active playback.

## Channel Context

The player maintains the channel list context from the source screen:
- From Home category section → channels in that group
- From Favorites → favorite channels
- From Channels → channels matching current filters

Swipe up/down navigates through this contextual list.

## Exit

User taps the back button to return to the previous screen. Playback stops and player resources are released.

## Watch History

When a channel is played, it is recorded in watch history for the [Continue Watching](../features/home.md#continue-watching) section on Home.

## Error Scenarios

| Scenario | Behavior |
|----------|----------|
| Stream fails | Error message with retry option |
| Network lost during playback | Buffering indicator, auto-retry when network returns |
| Behind live window | Auto-seek to live edge |
