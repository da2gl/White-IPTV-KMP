# ast-index Rules

## Mandatory Search Rules

1. **ALWAYS use ast-index FIRST** for any code search task
2. **NEVER duplicate results** — if ast-index found usages/implementations, that IS the complete answer
3. **DO NOT run grep "for completeness"** after ast-index returns results
4. **Use grep/Grep ONLY when:**
   - ast-index returns empty results
   - Searching for regex patterns (ast-index uses literal match)
   - Searching for string literals inside code (`"some text"`)
   - Searching in comments content

## Why ast-index

ast-index is 17-69x faster than grep (1-10ms vs 200ms-3s) and returns structured, accurate results.

## Command Reference

| Task | Command | Time |
|------|---------|------|
| Universal search | `ast-index search "query"` | ~10ms |
| Find class | `ast-index class "ClassName"` | ~1ms |
| Find usages | `ast-index usages "SymbolName"` | ~8ms |
| Find implementations | `ast-index implementations "Interface"` | ~5ms |
| Call hierarchy | `ast-index call-tree "function" --depth 3` | ~1s |
| Class hierarchy | `ast-index hierarchy "ClassName"` | ~5ms |
| Find callers | `ast-index callers "functionName"` | ~1s |
| Module deps | `ast-index deps "module-name"` | ~10ms |
| File outline | `ast-index outline "File.kt"` | ~1ms |

## Android/KMP Commands

| Task | Command |
|------|---------|
| Composables | `ast-index composables` |
| Composable by name | `ast-index composables "ScreenName"` |
| Suspend functions | `ast-index suspend` |
| Flows | `ast-index flows` |
| Find @Inject | `ast-index inject "Type"` |

## Index Management

- `ast-index rebuild` — Full reindex (after major restructuring)
- `ast-index update` — After git pull/merge/checkout
- `ast-index stats` — Show index statistics
