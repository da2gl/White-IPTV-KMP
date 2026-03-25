# Code Report: Stable Entities

## Files Modified
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/model/ChannelEntity.kt` -- added `@Stable` annotation
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/model/PlaylistEntity.kt` -- added `@Stable` annotation
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/model/ChannelGroupEntity.kt` -- added `@Stable` annotation
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/model/ChannelGroup.kt` -- added `@Immutable` annotation
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/ContinueWatchingItem.kt` -- added `@Immutable` annotation

## Deviations from Plan
None. Room `@Entity` classes use `@Stable` (weaker contract, safer with Room annotation processing). Domain model `ChannelGroup` and presentation model `ContinueWatchingItem` use `@Immutable` (stronger contract, safe since they are pure data classes with only val properties).

## Build Status
Build and tests pass.

## Notes
- `@Stable` on Room entities tells Compose the class follows the stable contract (equals is consistent, mutations are notified via Snapshot system). Since these are data classes with only val properties, they are inherently stable.
- `@Immutable` on domain/presentation models is the stronger guarantee that instances never change after construction, enabling Compose to skip recomposition when the reference is unchanged.
- All three entity classes (`ChannelEntity`, `PlaylistEntity`, `ChannelGroupEntity`) are used directly in LazyColumn/LazyRow items across Home, Channels, and Favorites screens.
