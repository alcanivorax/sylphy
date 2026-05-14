# Folder Structure — Sylphy v2

## Full Directory Layout

```
Sylphy/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/sylphy/player/
│   │   │   │
│   │   │   ├── SylphyApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   │
│   │   │   ├── core/
│   │   │   │   ├── di/
│   │   │   │   │   ├── AppModule.kt
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   ├── MediaModule.kt
│   │   │   │   │   └── RepositoryModule.kt
│   │   │   │   ├── util/
│   │   │   │   │   ├── FormatUtil.kt          ← toMmSs(), toHhMm(), sha1()
│   │   │   │   │   ├── PermissionUtil.kt
│   │   │   │   │   └── ShuffleUtil.kt
│   │   │   │   └── extension/
│   │   │   │       ├── FlowExt.kt
│   │   │   │       ├── ContextExt.kt
│   │   │   │       └── MediaItemExt.kt        ← Track ↔ MediaItem mapping
│   │   │   │
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── SylphyDatabase.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── TrackDao.kt
│   │   │   │   │   │   │   ├── AlbumDao.kt
│   │   │   │   │   │   │   ├── ArtistDao.kt
│   │   │   │   │   │   │   ├── PlaylistDao.kt
│   │   │   │   │   │   │   ├── SessionDao.kt
│   │   │   │   │   │   │   └── QueueDao.kt
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       ├── TrackEntity.kt      ← NO bpm field
│   │   │   │   │   │       ├── AlbumEntity.kt
│   │   │   │   │   │       ├── ArtistEntity.kt
│   │   │   │   │   │       ├── PlaylistEntity.kt
│   │   │   │   │   │       ├── PlaylistTrackEntity.kt
│   │   │   │   │   │       ├── ListeningSessionEntity.kt
│   │   │   │   │   │       └── QueueSnapshotEntity.kt
│   │   │   │   │   ├── datastore/
│   │   │   │   │   │   └── SettingsDataStore.kt
│   │   │   │   │   └── scanner/
│   │   │   │   │       ├── MediaScanner.kt
│   │   │   │   │       ├── MetadataReader.kt
│   │   │   │   │       ├── ArtworkExtractor.kt
│   │   │   │   │       └── LibraryOrganizer.kt
│   │   │   │   ├── repository/
│   │   │   │   │   ├── TrackRepositoryImpl.kt
│   │   │   │   │   ├── PlaylistRepositoryImpl.kt
│   │   │   │   │   ├── StatsRepositoryImpl.kt
│   │   │   │   │   └── SettingsRepositoryImpl.kt
│   │   │   │   └── model/
│   │   │   │       ├── Track.kt
│   │   │   │       ├── Album.kt
│   │   │   │       ├── Artist.kt
│   │   │   │       ├── Playlist.kt
│   │   │   │       ├── PlayerState.kt
│   │   │   │       └── Settings.kt
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── repository/
│   │   │   │   │   ├── TrackRepository.kt
│   │   │   │   │   ├── PlaylistRepository.kt
│   │   │   │   │   ├── StatsRepository.kt
│   │   │   │   │   └── SettingsRepository.kt
│   │   │   │   └── usecase/
│   │   │   │       ├── ScanLibraryUseCase.kt
│   │   │   │       ├── PlayTrackUseCase.kt
│   │   │   │       ├── GetLibraryUseCase.kt
│   │   │   │       ├── SearchTracksUseCase.kt
│   │   │   │       ├── CreatePlaylistUseCase.kt
│   │   │   │       └── GenerateWaveformUseCase.kt  ← no BPM usecase
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── SylphyPlaybackService.kt
│   │   │   │   └── WaveformScanWorker.kt           ← replaces BpmScanWorker
│   │   │   │
│   │   │   └── ui/
│   │   │       ├── navigation/
│   │   │       │   ├── SylphyNavGraph.kt
│   │   │       │   └── Screen.kt
│   │   │       ├── theme/
│   │   │       │   ├── Color.kt
│   │   │       │   ├── Type.kt
│   │   │       │   ├── Shape.kt
│   │   │       │   ├── Spacing.kt
│   │   │       │   ├── Easing.kt
│   │   │       │   └── SylphyTheme.kt
│   │   │       ├── components/
│   │   │       │   ├── shared/
│   │   │       │   │   ├── SylphyText.kt
│   │   │       │   │   ├── SylphyDivider.kt
│   │   │       │   │   ├── SylphyButton.kt
│   │   │       │   │   ├── SylphySearchBar.kt
│   │   │       │   │   ├── SylphyTabBar.kt
│   │   │       │   │   ├── SectionHeader.kt
│   │   │       │   │   ├── EmptyState.kt
│   │   │       │   │   ├── LoadingDots.kt
│   │   │       │   │   └── ContextMenuSheet.kt
│   │   │       │   ├── player/
│   │   │       │   │   ├── AlbumArtwork.kt
│   │   │       │   │   ├── ProgressRing.kt         ← replaces SineWaveRing
│   │   │       │   │   ├── SylphySeekBar.kt        ← replaces SineWaveSeekBar
│   │   │       │   │   ├── PlayButton.kt
│   │   │       │   │   ├── TransportControls.kt
│   │   │       │   │   ├── TrackInfo.kt
│   │   │       │   │   ├── TickerTape.kt
│   │   │       │   │   ├── SpeedControl.kt
│   │   │       │   │   └── VolumeIndicator.kt
│   │   │       │   │   # NO BpmPulseDot
│   │   │       │   ├── queue/
│   │   │       │   │   ├── QueueItem.kt
│   │   │       │   │   ├── DraggableQueueList.kt
│   │   │       │   │   └── QueueHeader.kt
│   │   │       │   ├── library/
│   │   │       │   │   ├── TrackRow.kt
│   │   │       │   │   ├── AlbumCard.kt
│   │   │       │   │   ├── ArtistRow.kt
│   │   │       │   │   ├── PlaylistCard.kt
│   │   │       │   │   ├── LibraryTabs.kt
│   │   │       │   │   ├── RecentlyPlayedStrip.kt
│   │   │       │   │   └── ScanProgressBar.kt
│   │   │       │   └── stats/
│   │   │       │       ├── StatsHeatmap.kt
│   │   │       │       ├── TopTracks.kt
│   │   │       │       └── StatCard.kt
│   │   │       └── screens/
│   │   │           ├── player/
│   │   │           │   ├── PlayerScreen.kt
│   │   │           │   └── PlayerViewModel.kt
│   │   │           ├── queue/
│   │   │           │   ├── QueueScreen.kt
│   │   │           │   └── QueueViewModel.kt
│   │   │           ├── library/
│   │   │           │   ├── LibraryScreen.kt
│   │   │           │   ├── LibraryViewModel.kt
│   │   │           │   ├── AlbumDetailScreen.kt
│   │   │           │   ├── ArtistDetailScreen.kt
│   │   │           │   └── PlaylistDetailScreen.kt
│   │   │           ├── settings/
│   │   │           │   ├── EqScreen.kt
│   │   │           │   ├── EqViewModel.kt
│   │   │           │   ├── SleepTimerScreen.kt
│   │   │           │   └── SettingsViewModel.kt
│   │   │           ├── stats/
│   │   │           │   ├── StatsScreen.kt
│   │   │           │   └── StatsViewModel.kt
│   │   │           └── ambient/
│   │   │               ├── AmbientScreen.kt
│   │   │               └── AmbientViewModel.kt
│   │   │
│   │   └── res/
│   │       ├── font/
│   │       │   ├── geist_mono_regular.ttf
│   │       │   ├── geist_mono_medium.ttf
│   │       │   ├── geist_mono_bold.ttf
│   │       │   ├── geist_sans_regular.ttf
│   │       │   └── geist_sans_medium.ttf
│   │       ├── drawable/
│   │       │   ├── ic_play.xml
│   │       │   ├── ic_pause.xml
│   │       │   ├── ic_skip_next.xml
│   │       │   ├── ic_skip_prev.xml
│   │       │   ├── ic_shuffle.xml
│   │       │   ├── ic_repeat.xml
│   │       │   ├── ic_repeat_one.xml
│   │       │   └── ic_drag_handle.xml
│   │       ├── values/
│   │       │   ├── colors.xml
│   │       │   └── themes.xml
│   │       └── xml/
│   │           └── backup_rules.xml
│   └── build.gradle.kts
│
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## What Was Removed vs v1 Folder Structure

| v1 file | v2 status | Reason |
|---|---|---|
| `ui/components/player/BpmPulseDot.kt` | **Deleted** | BPM feature removed |
| `ui/components/player/SineWaveRing.kt` | **Renamed** → `ProgressRing.kt` | New visual language |
| `ui/components/player/SineWaveSeekBar.kt` | **Renamed** → `SylphySeekBar.kt` | New visual language |
| `ui/components/shared/DotGridBackground.kt` | **Deleted** | Theme removed |
| `ui/components/shared/ScanlineOverlay.kt` | **Deleted** | Theme removed |
| `ui/components/shared/CornerBracket.kt` | **Deleted** | Theme removed |
| `service/BpmScanService.kt` | **Renamed** → `WaveformScanWorker.kt` | Repurposed |
| `domain/usecase/DetectBpmUseCase.kt` | **Deleted** | BPM feature removed |
| `domain/usecase/GenerateBpmPlaylistsUseCase.kt` | **Deleted** | BPM feature removed |

---

## Agent Execution Checklist

Before writing any code in any session:

```
□ Read README.md — confirm active phase
□ Read design/DESIGN_TOKENS.md — load all color/font tokens into context
□ Read design/COMPONENT_LIBRARY.md for the screen being built
□ Read the active phase file
□ THEN write code
```

Before committing any task:

```
□ Screenshot the screen — are there any non-monochrome pixels?
□ Does any text use a font other than Geist Mono or Geist Sans?
□ Does any container have borderRadius > 4.dp?
□ Does any shadow/elevation exist (should be zero everywhere)?
□ Does the active state use inversion (not a color highlight)?
□ npx tsc --noEmit equivalent: ./gradlew compileDebugKotlin → 0 errors
□ Physical device test: does it actually look like the design?
```

Code style rules:

```kotlin
// ✅ CORRECT — token from design system
color = FgSecondary

// ❌ WRONG — hardcoded value
color = Color(0xFF888888)

// ✅ CORRECT — inversion for active state
background = if (active) ActiveBackground else Color.Transparent
color      = if (active) ActiveForeground else FgPrimary

// ❌ WRONG — color for active state
color = if (active) Color(0xFFFF3B30) else FgPrimary

// ✅ CORRECT — border as structure
Modifier.border(Layout.borderThin, BorderDefault, ContainerCorner)

// ❌ WRONG — shadow for depth
Modifier.shadow(8.dp)

// ✅ CORRECT — generous spacing
Modifier.padding(Spacing.xl)

// ❌ WRONG — cramped
Modifier.padding(4.dp)
```
