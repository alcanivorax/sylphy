# Sylphy Project Structure

A local music player Android app built with Jetpack Compose, MVVM + Clean Architecture, Hilt DI, Room database, and Media3 ExoPlayer.

---

## Quick Overview

```
app/src/main/java/io/sylphy/app/
├── MainActivity.kt           — App entry point
├── SylphyApplication.kt      — Application class (Hilt, Timber)
├── audio/                    — Audio hardware control (EQ, sleep timer)
├── core/                     — DI modules, extensions, utilities
├── data/                     — Storage (Room DB, DataStore, media scanning)
├── domain/                   — Business logic interfaces & use cases
├── service/                  — Foreground playback service, background workers
└── ui/                       — All UI: theme, navigation, screens, components
```

---

## Directory Map

### `audio/` — Low-level audio hardware

| File | What it does | Connects to |
|---|---|---|
| `SylphyEqualizer.kt` | Wraps Android's built-in 10-band equalizer. Turns dB sliders into real EQ. | `SylphyPlaybackService`, `AudioSettingsViewModel` |
| `SleepTimerController.kt` | Sleep timer that counts down and pauses playback. Reads settings from DataStore. | `PlayerViewModel`, `AudioSettingsViewModel`, `SettingsDataStore` |

### `core/di/` — Dependency injection modules

| File | What it does | Connects to |
|---|---|---|
| `AppModule.kt` | The big wiring file. Tells Hilt how to create: Room database, all DAOs, the ExoPlayer singleton, the TrackRepository. Every `@Inject` constructor depends on this. | Everything via Hilt |
| `MediaControllerProvider.kt` | Connects to the media playback service so the UI can talk to the player. Used by the shake-to-shuffle gesture. | `MainActivity` |

### `core/extension/` — Kotlin extension functions

| File | What it does | Connects to |
|---|---|---|
| `ContextExt.kt` | Shortcut: `context.hasPermission(perm)` — one-liner for permission checks. | `LibraryScreen` |
| `EntityMappers.kt` | Converts Room database objects (`TrackEntity`, `AlbumEntity`) into app models (`Track`, `Album`) and back. Also converts waveform JSON. | `TrackRepositoryImpl`, `LibraryViewModel`, `PlayerViewModel` |
| `FlowExt.kt` | Helpers for Kotlin coroutine flows: catch errors with logging, map lists. | Utility, used in repositories |
| `MediaItemExt.kt` | Converts a `Track` into a Media3 `MediaItem` (what ExoPlayer plays). | `PlayerViewModel`, `QueueViewModel`, `ScanLibraryUseCase` |

### `core/util/` — Standalone utility functions

| File | What it does | Connects to |
|---|---|---|
| `FormatUtil.kt` | Formatting: milliseconds → "m:ss", SHA-1 hashing, "1 track" vs "5 tracks" labels. | `PlayerScreen`, `StatsScreen` |
| `PermissionUtil.kt` | Returns the right permission string depending on Android version (API 33+ vs older). | `LibraryViewModel` |
| `ShuffleUtil.kt` | Shuffles a list but keeps the current item at position 0. | `PlayerViewModel` |

### `data/model/` — Plain data classes (no database logic)

| File | What it does | Connects to |
|---|---|---|
| `Track.kt` | A single audio track: title, artist, album, duration, artwork path, waveform, favorites, play count. | Every screen and ViewModel |
| `Album.kt` | Album with title, artist, year, artwork, list of tracks. | `LibraryScreen`, `AlbumDetailScreen` |
| `Artist.kt` | Artist with name, artwork, track/album counts. | `LibraryScreen`, `ArtistDetailScreen` |
| `Playlist.kt` | Playlist with name, description, list of tracks. | `LibraryScreen`, `PlaylistDetailScreen` |
| `PlayerState.kt` | `PlayerUiState` — the current state of the player (what track is playing, position, volume, shuffle mode, etc.). `PlaybackState` and `RepeatMode` enums. | `PlayerViewModel` → `PlayerScreen` |
| `Settings.kt` | User settings: theme, crossfade, speed, EQ, sleep timer, ambient mode. | `SettingsViewModel`, `PlayerViewModel` |
| `ThemeMode.kt` | Three theme options: `MONOCHROME_DARK`, `MONOCHROME_LIGHT`, `NOTHING_OS`. | `SettingsViewModel`, `SylphyNavGraph`, `Theme.kt` |

### `data/local/datastore/` — Settings persistence

| File | What it does | Connects to |
|---|---|---|
| `SettingsDataStore.kt` | Reads/writes settings to disk using Jetpack DataStore. Exposes a `Flow<Settings>` and setter methods. | `MainActivity`, `PlayerViewModel`, `AudioSettingsViewModel`, `SettingsViewModel`, `SleepTimerController` |

### `data/local/db/` — Room database

| File | What it does | Connects to |
|---|---|---|
| `SylphyDatabase.kt` | The Room database class (version 3). Ties all entities and DAOs together. Has a `WaveformConverter` for JSON. | `AppModule` (created via Hilt) |

**Entities** (= database tables):

| File | Table name | What it stores |
|---|---|---|
| `TrackEntity.kt` | `tracks` | Every audio track's metadata |
| `AlbumEntity.kt` | `albums` | Album info |
| `ArtistEntity.kt` | `artists` | Artist info |
| `PlaylistEntity.kt` | `playlists` + `playlist_tracks` | Playlists and which tracks belong to them |
| `ListeningSessionEntity.kt` | `listening_sessions` + `queue_snapshots` | When you listened to what, for stats |
| `TrackFtsEntity.kt` | `tracks_fts` | Full-text search index (FTS4) for fast search |

**DAOs** (= how you read/write tables):

| File | What it does | Connects to |
|---|---|---|
| `TrackDao.kt` | Query all tracks, by album, by artist, favorites, recent, search (FTS). Insert, update, delete. | `TrackRepositoryImpl` |
| `AlbumDao.kt` | Query albums, by artist, search. Insert/update/remove stale. | `TrackRepositoryImpl`, `LibraryDetailViewModel` |
| `ArtistDao.kt` | Same pattern for artists. | `TrackRepositoryImpl`, `LibraryDetailViewModel` |
| `PlaylistDao.kt` | CRUD for playlists, plus add/remove tracks, reorder. | `TrackRepositoryImpl`, `LibraryViewModel`, `LibraryDetailViewModel` |
| `SessionDao.kt` | Log listening sessions, get stats (total time, heatmap, top tracks). Also saves/restores queue snapshots. | `SylphyPlaybackService`, `StatsViewModel` |

### `data/local/scanner/` — Audio file scanning

| File | What it does | Connects to |
|---|---|---|
| `MediaScanner.kt` | Queries the Android `MediaStore` to find all audio files. Emits progress as it finds them. | `TrackRepositoryImpl` |
| `MetadataReader.kt` | Opens each audio file to read deeper metadata (genre, year, bitrate, album artist) using `MediaMetadataRetriever`. Also triggers artwork extraction. | `TrackRepositoryImpl` |
| `ArtworkExtractor.kt` | Extracts album art embedded in audio files and caches them as JPGs. | `MetadataReader` |
| `LibraryOrganizer.kt` | Takes scanned tracks, groups them into albums and artists, inserts/updates the album/artist tables. Removes stale entries. | `TrackRepositoryImpl` |

### `data/repository/` — Repository implementation

| File | What it does | Connects to |
|---|---|---|
| `TrackRepositoryImpl.kt` | The main data orchestrator. Ties together scanning, metadata reading, artwork, and database operations. Exposes `Flow<List<Track>>` for the UI. Implements the `TrackRepository` interface. | All DAOs, scanner classes, `WaveformScanWorker`, `EntityMappers` |

### `domain/repository/` — Repository interface

| File | What it does | Connects to |
|---|---|---|
| `TrackRepository.kt` | Interface = contract. Declares `getAllTracks()`, `scanLibrary()`, `searchTracks()`, etc. The actual work is in `TrackRepositoryImpl`. | ViewModels (they code to this interface) |

### `domain/usecase/` — Business logic actions

| File | What it does | Connects to |
|---|---|---|
| `ScanLibraryUseCase.kt` | Kicks off a media scan. Wraps `TrackRepository.scanLibrary()`. | `LibraryViewModel` |
| `PlayTrackUseCase.kt` | Converts a Track to a MediaItem and sets it on ExoPlayer with an optional queue. | `PlayerViewModel`, `LibraryViewModel`, `QueueViewModel` |

### `service/` — Background services

| File | What it does | Connects to |
|---|---|---|
| `SylphyPlaybackService.kt` | A foreground `MediaSessionService`. Runs playback in the background, manages the MediaSession, logs listening sessions, saves queue snapshots, sets up the equalizer. | `ExoPlayer`, `SessionDao`, `QueueDao`, `SleepTimerController`, `SylphyEqualizer` |
| `WaveformScanWorker.kt` | A WorkManager worker. Processes tracks in the background to generate waveform data (200 data points per track) for the visual seekbar. | `TrackRepositoryImpl` (enqueued after scan) |

---

## UI Layer

### `ui/theme/` — Design system

| File | What it does | Connects to |
|---|---|---|
| `Color.kt` | Defines the color palette (Black, White, Nothing Red, etc.), three Material3 color schemes (Dark/Light/Nothing OS), semantic color aliases (BgBase, FgPrimary, ActiveBackground, etc.), and Player-specific theme colors. | Every UI file |
| `Easing.kt` | Custom animation curves (`SylphyEasing`) and durations (`Duration` object). | Used in animations across screens |
| `Shape.kt` | Corner radiuses: chips (2dp), containers (4dp), bottom sheets (8dp). | Every composable with shapes |
| `Spacing.kt` | Spacing scale (px1=1dp to huge=96dp) and layout constants (topBarHeight=56dp, albumArtSize=280dp, etc.). | Every UI file that needs padding/sizes |
| `Theme.kt` | `SylphyTheme` composable — wraps MaterialTheme, applies color scheme with animation, configures status/nav bars. | `MainActivity` |
| `Type.kt` | Typography system: `SylphyType` object with styles (Heading, Body, Code, CodeSmall, Clock, etc.). Uses monospace and sans-serif font families. | Every screen/file that shows text |

### `ui/navigation/` — Screen routing

| File | What it does | Connects to |
|---|---|---|
| `Screen.kt` | A sealed class defining all route strings: `Player`, `Queue`, `Library`, `AlbumDetail`, `Settings`, `Eq`, etc. | `SylphyNavGraph`, all screens that navigate |
| `SylphyNavGraph.kt` | The main navigation composable. Three top-level screens (Library/Player/Queue) use a swipe pager. Detail/settings screens use a standard NavHost with slide animations. Also handles: bottom tab bar, auto-navigation to Ambient mode when charging + idle, player redirect when no track. | Every screen composable, `SwipePager`, `BottomNav`, `PlayerViewModel` |

### `ui/screens/` — Full-screen views

Each screen has a `*Screen.kt` (Compose UI) and a `*ViewModel.kt` (logic + state).

**Player** — The main now-playing screen:

| File | What it does | Connects to |
|---|---|---|
| `PlayerScreen.kt` | The main player UI: album art, track info, seekbar, transport controls, speed/volume. Uses a centered layout with width cap. | `PlayerViewModel` |
| `PlayerViewModel.kt` | The brain of the player. Manages play/pause/next/prev/seek/shuffle/repeat/favorite/volume/speed. Polls progress every 100ms. Syncs with ExoPlayer state. | Player data models, `PlayTrackUseCase`, `SleepTimerController`, `SettingsDataStore` |
| `PlayerComponents.kt` | All the small UI pieces that make up the player: `VinylArtwork`, `TrackInfoRow`, `QualityBadgeRow`, `ScrubberSection`, `ControlsRow`, `SecondaryRow`, `TopNav`, `BottomNav`, `SpeedChip`, `VolumeIndicator`, `EmptyPlayerState`, `BlurredArtBackground`, `GrainOverlay`. Also has `BottomNavTab` enum. | Used by `PlayerScreen` |

**Library** — Browse your music:

| File | What it does | Connects to |
|---|---|---|
| `LibraryScreen.kt` | Search bar + scan button + tabbed browser (Songs/Albums/Artists/Playlists). Lists with context menus. | `LibraryViewModel` |
| `LibraryViewModel.kt` | Manages scan flow, search (with 300ms debounce), tab state, track/album/artist/playlist lists. Combines 5 DAO flows into one UI state. | `TrackRepository`, `ScanLibraryUseCase`, `PlayTrackUseCase`, all DAOs |
| `AlbumDetailScreen.kt` | Shows album/artist/playlist detail page with header + track list. Plays tracks on tap. (All three detail screens in one file.) | `LibraryDetailViewModel` |
| `LibraryDetailViewModel.kt` | Fetches details for a single album/artist/playlist by ID, combines with track list. | DAOs, `PlayTrackUseCase`, `TrackRepository` |

**Queue** — Up-next list:

| File | What it does | Connects to |
|---|---|---|
| `QueueScreen.kt` | Drag-to-reorder queue list. Shows now-playing highlight. | `QueueViewModel` |
| `QueueViewModel.kt` | Reads current ExoPlayer queue, maps track IDs to Track objects, handles remove/reorder. | `Player`, `TrackRepository` |

**Settings** — App preferences:

| File | What it does | Connects to |
|---|---|---|
| `SettingsScreen.kt` | Theme picker, playback speed, crossfade, gapless toggle, EQ, sleep timer, ambient mode, stats button. | `SettingsViewModel` |
| `SettingsViewModel.kt` | Simple wrapper: reads settings from DataStore, exposes theme setter. | `SettingsDataStore` |
| `EqScreen.kt` | 10-band graphic equalizer with sliders and presets (Flat/Bass/Vocal/Pop). Also contains `SleepTimerScreen`. | `AudioSettingsViewModel` |
| `AudioSettingsViewModel.kt` | Manages EQ bands/presets and sleep timer start/cancel. Reads/writes DataStore. | `SettingsDataStore`, `SleepTimerController`, `SylphyEqualizer` |

**Stats** — Listening statistics:

| File | What it does | Connects to |
|---|---|---|
| `StatsScreen.kt` | Weekly listening time, monthly track count, listening heatmap, top tracks. | `StatsViewModel` |
| `StatsViewModel.kt` | Loads session data from Room: heatmap by day, top tracks, aggregates. | `SessionDao` |

**Ambient** — Full-screen clock mode:

| File | What it does | Connects to |
|---|---|---|
| `AmbientScreen.kt` | Large clock, scrolling track info, album art, touch-to-reveal. Hides system bars. For charging/idle display. | `AmbientViewModel` |
| `AmbientViewModel.kt` | Polls time every second, auto-hides cursor after 3s, tracks current playback. | `Player`, `TrackRepository` |

### `ui/components/shared/` — Reusable UI widgets

| File | What it does | Used by |
|---|---|---|
| `SwipePager.kt` | Horizontal swipe pager. Swipe between pages with 20% threshold. Scale+alpha animation on adjacent pages. | `SylphyNavGraph` |
| `SylphyTabBar.kt` | Tab bar with centered labels and a thin underline indicator. | `SylphyNavGraph` (imported) |
| `ContextMenuSheet.kt` | `ModalBottomSheet` with action items (play, add to playlist, favorite, etc.). | `LibraryScreen` |
| `SylphyButton.kt` | Press-animated custom button. | Shared components |
| `SylphyDivider.kt` | Themed horizontal line with optional label. | `LibraryScreen` |
| `SylphySearchBar.kt` | Animated search input with icon, clear button, focus highlight. | `LibraryScreen` |

### `ui/components/player/` — Player-specific visual components

| File | What it does | Used by |
|---|---|---|
| `AlbumArtwork.kt` | Loads album art image via Coil with rounded corners and fallback. | `PlayerScreen` components |
| `AmbientBackgroundGlow.kt` | Dynamic colored glow derived from album art's dominant colors. | `AmbientScreen` |
| `CDDisc.kt` | Animated spinning CD with grooves, center hole, reflection effect. | `PlayerScreen`, `EmptyPlayerState` |
| `OrganicSeekBar.kt` | Waveform-based seekbar — draws the track's waveform as bars, fill = played portion. | `PlayerScreen` |
| `ProgressRing.kt` | Circular progress arc around the album art with a playhead dot. | `PlayerScreen` |
| `SylphySeekBar.kt` | Simple linear seekbar with gradient fill and drag/tap gesture. | Player components |
| `TickerTape.kt` | Scrolling text marquee for long track titles that don't fit. | `PlayerScreen` |
| `TrackInfo.kt` | Title + artist display with animated content transitions. | `PlayerScreen` |
| `TransportControls.kt` | Full transport bar: shuffle, prev, play/pause, next, repeat — with press animations. | `PlayerScreen` |

---

## Data Flow Diagram

```
MediaStore (Android audio files)
    │
    ▼
MediaScanner ──► MetadataReader ──► ArtworkExtractor
    │                                      │
    ▼                                      ▼
TrackDao ◄── TrackRepositoryImpl ◄── EntityMappers
    │              │
    │              ▼
    │        TrackRepository (interface)
    │              │
    │         ┌────┴────┐
    │         ▼         ▼
    │   PlayerVM   LibraryVM
    │         │         │
    │         ▼         ▼
    │   PlayerScreen  LibraryScreen
    │
    └──► AlbumDao, ArtistDao, PlaylistDao
              │
              ▼
         LibraryDetailVM ──► AlbumDetailScreen
```

---

## Navigation Map

```
SylphyNavGraph
│
├── SwipePager (main tabs)
│   ├── 0: LibraryScreen
│   ├── 1: PlayerScreen  (redirects to Library if no track)
│   └── 2: QueueScreen
│
└── NavHost (detail screens)
    ├── album/{albumId}      → AlbumDetailScreen
    ├── artist/{artistId}    → ArtistDetailScreen
    ├── playlist/{playlistId}→ PlaylistDetailScreen
    ├── eq                   → EqScreen
    ├── sleep_timer          → SleepTimerScreen
    ├── stats                → StatsScreen
    ├── ambient              → AmbientScreen
    └── settings             → SettingsScreen
```

---

## Dependency Injection Map

```
AppModule (object)
├── MediaControllerProvider
│
DatabaseModule (object)
├── SylphyDatabase
│   ├── TrackDao
│   ├── AlbumDao
│   ├── ArtistDao
│   ├── PlaylistDao
│   ├── SessionDao
│   └── QueueDao
│
RepositoryModule (object)
└── TrackRepository (interface) ← TrackRepositoryImpl
    │
    PlayerModule (object)
    └── ExoPlayer (singleton)
```

---

## Key Interconnections

- **PlayerScreen** ← **PlayerViewModel** ← **ExoPlayer** + **TrackRepository** + **SettingsDataStore** + **SleepTimerController**
- **LibraryScreen** ← **LibraryViewModel** ← **TrackRepository** + **ScanLibraryUseCase** + **PlayTrackUseCase** + all DAOs
- **SylphyNavGraph** ← **PlayerViewModel** (to check active track for redirect) + all screens
- **SylphyPlaybackService** ← **ExoPlayer** + **SessionDao** + **QueueDao** + **SylphyEqualizer** + **SleepTimerController** + **SettingsDataStore**
- **TrackRepositoryImpl** ← **TrackDao** + **AlbumDao** + **ArtistDao** + **MediaScanner** + **MetadataReader** + **ArtworkExtractor** + **LibraryOrganizer** + **EntityMappers**
- **AppModule** → everything via Hilt (all DAOs, database, player, repository)
