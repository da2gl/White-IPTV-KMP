# Feature Implementation Review

Аудит реализации фич и выбора библиотек. Для каждой фичи — соответствие документации, корректность библиотек, и рекомендации по best practices.

---

## 1. Settings (Настройки)

### Хранение настроек

| Аспект | Текущее | Рекомендация | Критичность |
|--------|---------|--------------|-------------|
| Библиотека | `multiplatform-settings` 1.3.0 (russhwolf) | **androidx.datastore:datastore-preferences** | ВЫСОКАЯ |

**Проблема:** `multiplatform-settings` — обёртка над `SharedPreferences` (Android) / `UserDefaults` (iOS). SharedPreferences имеют известные проблемы:
- Синхронное чтение с диска на main thread при первом обращении
- Нет type safety (строковые ключи)
- Нет поддержки транзакций
- Google официально рекомендует DataStore как замену

**Рекомендация:** Jetpack DataStore Preferences — официальная замена от Google:
- Асинхронный API на coroutines/Flow (нативная интеграция с MVI)
- Type-safe (Proto DataStore для сложных структур)
- Есть KMP-версия: `androidx.datastore:datastore-preferences-core` (multiplatform с 1.1.0+)
- Атомарные операции записи
- Обработка ошибок через coroutines exceptions

```kotlin
// Вместо
val settings = Settings()
settings.putBoolean("auto_update", true)

// Рекомендуется
val AUTO_UPDATE = booleanPreferencesKey("auto_update")
dataStore.edit { it[AUTO_UPDATE] = true }
val flow: Flow<Boolean> = dataStore.data.map { it[AUTO_UPDATE] ?: false }
```

### Auto-Update плейлистов

| Аспект | Текущее | Рекомендация | Критичность |
|--------|---------|--------------|-------------|
| Механизм | Coroutine loop в foreground | **WorkManager** (Android) / **BGTaskScheduler** (iOS) | КРИТИЧЕСКАЯ |

**Проблема:** `PlaylistAutoRefreshScheduler` использует `while(true) { delay(interval) }` в корутине, привязанной к lifecycle App.kt:
- Работает ТОЛЬКО пока приложение в foreground
- При сворачивании/убийстве процесса — обновление прекращается
- Пользователь ожидает, что "Auto Update = ON" значит обновление происходит в фоне
- Нарушает Android best practice для периодических background tasks

**Рекомендация:**

**Android — WorkManager:**
```kotlin
// Официальный способ для периодических background tasks
class PlaylistRefreshWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        // refresh playlists
        return Result.success()
    }
}

// Регистрация
val request = PeriodicWorkRequestBuilder<PlaylistRefreshWorker>(6, TimeUnit.HOURS)
    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
    .build()
WorkManager.getInstance(context).enqueueUniquePeriodicWork("playlist_refresh", KEEP, request)
```

Преимущества WorkManager:
- Гарантированное выполнение даже после перезагрузки устройства
- Battery-friendly (учитывает Doze mode, App Standby)
- Constraints (только при наличии сети)
- Наблюдение за статусом через LiveData/Flow
- Retry policy из коробки

**iOS — BGTaskScheduler:**
```swift
BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.simplevideo.whiteiptv.refresh", using: nil) { task in
    // refresh playlists
    task.setTaskCompleted(success: true)
}
```

**KMP подход:** expect/actual для `BackgroundScheduler` — WorkManager на Android, BGTaskScheduler на iOS.

---

## 2. Player (Плеер)

### ExoPlayer / Media3

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Библиотека | `media3-exoplayer` 1.8.0 | КОРРЕКТНО |
| HLS | `media3-exoplayer-hls` | КОРРЕКТНО |
| Сеть | `media3-datasource-cronet` (HTTP/2, QUIC) | ОТЛИЧНО |
| Compose UI | `media3-ui-compose` | КОРРЕКТНО |

**Замечание по версии:** Текущая 1.8.0, доступна 1.9.2. Рекомендуется обновить — содержит фиксы для HLS и улучшения Compose-интеграции.

### Picture-in-Picture

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Android | `Activity.enterPictureInPictureMode()` | КОРРЕКТНО |
| iOS | `AVPictureInPictureController` | КОРРЕКТНО |

Реализация соответствует платформенным API. Без замечаний.

### Chromecast / AirPlay

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Chromecast | `play-services-cast-framework` 22.0.0 + `media3-cast` + `mediarouter` 1.7.0 | КОРРЕКТНО |
| AirPlay | `AVRoutePickerView` (нативный iOS) | КОРРЕКТНО |

**Замечание по версиям:**
- `play-services-cast-framework`: текущая 22.0.0, доступна 22.3.0
- `mediarouter`: текущая 1.7.0, доступна 1.8.1
- Рекомендуется обновить для совместимости с новыми Cast SDK

### Sleep Timer

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Механизм | Coroutine с `delay()` + tick каждую секунду | ПРИЕМЛЕМО |

Для sleep timer в foreground — корутины вполне подходят (таймер нужен только пока плеер активен). Это корректный подход, в отличие от auto-update.

### Gesture Controls

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Реализация | `detectVerticalDragGestures()` + зоны экрана | КОРРЕКТНО |
| Brightness | `Window.attributes.screenBrightness` (Android), `UIScreen.mainScreen.brightness` (iOS) | КОРРЕКТНО |
| Volume | `AudioManager.STREAM_MUSIC` (Android), фиксированное значение (iOS) | ПРИЕМЛЕМО |

**Замечание iOS Volume:** iOS не позволяет программно менять громкость без `MPVolumeView`. Текущий workaround (фиксированное 0.5f) — единственное легальное решение без отображения системного слайдера.

### Keep Screen On

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Android | `FLAG_KEEP_SCREEN_ON` через `DisposableEffect` | КОРРЕКТНО |
| iOS | `UIApplication.sharedApplication.idleTimerDisabled` | КОРРЕКТНО |

Стандартный подход, корректная очистка при dispose.

---

## 3. Home (Главный экран)

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Playlist Selector | Dropdown с CurrentPlaylistRepository | КОРРЕКТНО |
| Continue Watching | Последние 10, горизонтальный скролл | КОРРЕКТНО |
| Favorites Section | Flow-based, реактивно обновляется | КОРРЕКТНО |
| Category Sections | Priority-based (news, sports, music, general) + по количеству | КОРРЕКТНО |
| Playlist Settings | ModalBottomSheet (rename, update, delete, view URL) | КОРРЕКТНО |

**Без критических замечаний.** Реализация полностью соответствует документации. MVI паттерн с Flow-orchestration работает правильно.

---

## 4. Channel Browsing (Просмотр каналов)

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Фильтрация | 3 уровня: playlist → group → search | КОРРЕКТНО |
| Layout | 2-column LazyVerticalGrid | КОРРЕКТНО |
| Favorite toggle | Inline star icon | КОРРЕКТНО |
| Group persistence | SavedStateHandle | КОРРЕКТНО |

**Замечание:** Нет пагинации — все каналы загружаются сразу. Для плейлистов с 10k+ каналами это потенциальная проблема производительности.

**Рекомендация:** Использовать `androidx.paging:paging-compose-common` (KMP) для lazy loading из Room:
```kotlin
@Query("SELECT * FROM channels WHERE playlistId = :id ORDER BY name")
fun getChannelsPaged(id: Long): PagingSource<Int, ChannelEntity>
```

| Критичность | СРЕДНЯЯ (при текущих объёмах работает, но не масштабируется) |
|---|---|

---

## 5. Favorites (Избранное)

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Global favorites | Across all playlists | КОРРЕКТНО |
| Playlist filtering | Dropdown + Flow | КОРРЕКТНО |
| Toggle animation | Immediate removal | КОРРЕКТНО |
| Empty state | Star icon + hint text | КОРРЕКТНО |

**Полностью соответствует документации.** Без замечаний.

---

## 6. Search (Поиск)

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Контекстность | Respects active filters on parent screen | КОРРЕКТНО |
| Debounce | 300ms | КОРРЕКТНО |
| Database queries | `LIKE '%query%' COLLATE NOCASE` | КОРРЕКТНО |
| Performance | Database-driven (не in-memory) | КОРРЕКТНО |

**Соответствует документации.** Контекстный поиск реализован через UseCase layer, который принимает и фильтр, и query одновременно.

**Мелкое замечание:** FTS (Full-Text Search) в Room дал бы лучшую производительность для 10k+ каналов:
```kotlin
@Fts4(contentEntity = ChannelEntity::class)
@Entity(tableName = "channels_fts")
data class ChannelFts(val name: String)
```
| Критичность | НИЗКАЯ (LIKE работает для текущих объёмов) |
|---|---|

---

## 7. EPG (Телепрограмма)

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| В плеере | Current + next program, refresh каждые 60с | КОРРЕКТНО |
| В карточках каналов | Не реализовано | ПО ДОКУМЕНТАЦИИ (отмечено как undefined) |
| Кэширование | Не определено | ТРЕБУЕТСЯ РЕШЕНИЕ |
| XMLTV парсинг | Реализован | ТРЕБУЕТСЯ ПРОВЕРКА производительности для 50MB+ файлов |

Реализация в плеере соответствует документации. Вопросы из docs (кэширование, отображение в карточках) остаются открытыми.

---

## 8. Playlist Settings (Настройки плейлиста)

| Аспект | Текущее | Оценка |
|--------|---------|--------|
| Presentation | ModalBottomSheet | КОРРЕКТНО |
| Rename | AlertDialog с подтверждением | КОРРЕКТНО |
| Update | Re-download + re-parse, preserves favorites | КОРРЕКТНО |
| Delete | Confirmation dialog, navigates to Onboarding if last | КОРРЕКТНО |
| View URL | Read-only dialog | КОРРЕКТНО |

**Полностью соответствует документации.** Без замечаний.

---

## Сводная таблица проблем

| # | Проблема | Фича | Критичность | Рекомендуемая библиотека |
|---|----------|------|-------------|-------------------------|
| 1 | Auto-Update работает только в foreground | Settings | КРИТИЧЕСКАЯ | `androidx.work:work-runtime-ktx` (Android) + `BGTaskScheduler` (iOS) |
| 2 | SharedPreferences вместо DataStore | Settings | ВЫСОКАЯ | `androidx.datastore:datastore-preferences-core` (KMP) |
| 3 | Нет пагинации для больших плейлистов | Channels | СРЕДНЯЯ | `androidx.paging:paging-compose-common` (KMP) |
| 4 | Устаревшие версии Media3 и Cast | Player | СРЕДНЯЯ | Обновить media3 → 1.9.2, cast → 22.3.0, mediarouter → 1.8.1 |
| 5 | LIKE вместо FTS для поиска | Search | НИЗКАЯ | Room FTS4 |

---

## Что реализовано хорошо

- **MVI паттерн** — единообразно во всех фичах, чистое разделение State/Event/Action
- **Flow-based реактивность** — `combine()` + `flatMapLatest()` для каскадной фильтрации
- **Expect/actual** — грамотное разделение платформенного кода (PiP, Cast, SystemControls)
- **ExoPlayer/Media3** с Cronet — оптимальный выбор для IPTV (HTTP/2, QUIC, HLS)
- **Room** для KMP — правильный выбор для структурированных данных
- **Koin DI** — корректные scope (single/factory/viewModel)
- **Debounced search** (300ms) — предотвращает избыточные запросы к БД
- **Clean Architecture** — чёткие слои domain/data/presentation
- **Chromecast + AirPlay** — правильные нативные API на каждой платформе
