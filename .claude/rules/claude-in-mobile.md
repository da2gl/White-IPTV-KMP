# claude-in-mobile Rules

## Mandatory Usage

1. **ALWAYS use `claude-in-mobile` CLI** for any mobile device interaction
2. **NEVER use MCP tools** (`mcp__mobile__*`) — use CLI instead
3. **Prefer `tap-text`** over coordinate-based `tap` — works across screen sizes
4. **Always `wait`** after navigation actions before asserting

## Command Reference

| Task | Command |
|------|---------|
| List devices | `claude-in-mobile devices` |
| Screenshot | `claude-in-mobile screenshot` |
| Annotated screenshot | `claude-in-mobile annotate` |
| UI hierarchy | `claude-in-mobile ui-dump` |
| Find element | `claude-in-mobile find --text "Label"` |
| Tap by text | `claude-in-mobile tap-text "Button"` |
| Tap coordinates | `claude-in-mobile tap <x> <y>` |
| Long press | `claude-in-mobile long-press <x> <y>` |
| Swipe | `claude-in-mobile swipe <x1> <y1> <x2> <y2>` |
| Input text | `claude-in-mobile input "text"` |
| Press key | `claude-in-mobile key back/home/enter` |
| Wait | `claude-in-mobile wait <ms>` |
| Launch app | `claude-in-mobile launch <package>` |
| Stop app | `claude-in-mobile stop <package>` |
| Install APK | `claude-in-mobile install <path>` |
| Get logs | `claude-in-mobile logs --tag "Tag" --lines 50` |
| Clear logs | `claude-in-mobile clear-logs` |
| System info | `claude-in-mobile system-info` |
| Current app | `claude-in-mobile current-activity` |

## App Package

`com.simplevideo.whiteiptv`
