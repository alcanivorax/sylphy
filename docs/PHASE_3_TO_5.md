# Phase 3 — Library, Playlists, Search, Room

## Goal
Full library experience. SONGS / ALBUMS / ARTISTS / PLAYLISTS tabs. FTS search. Playlist management. Artwork enrichment. Everything in monochrome.

**Phase 3 complete when:** Library is fully populated from device, album art shows, search returns results in < 300ms, playlists can be created and played.

---

## Task 3.1 — MediaMetadataRetriever Enrichment

Full implementation from `architecture/STATE_MANAGEMENT.md` (v1 Phase 3.1).

Run after initial scan in a background coroutine — one track at a time, `delay(30)` between each.

Fields to enrich: `title`, `artist`, `album`, `albumArtist`, `genre`, `year`, `trackNumber`, `bitRate`, `sampleRate`.

No BPM field — skip entirely.

```kotlin
// data/local/scanner/MetadataReader.kt
class MetadataReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun read(contentUri: String): TrackMetadata = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, Uri.parse(contentUri))
            TrackMetadata(
                title       = retriever.extract(METADATA_KEY_TITLE),
                artist      = retriever.extract(METADATA_KEY_ARTIST),
                album       = retriever.extract(METADATA_KEY_ALBUM),
                albumArtist = retriever.extract(METADATA_KEY_ALBUMARTIST),
                genre       = retriever.extract(METADATA_KEY_GENRE),
                year        = retriever.extract(METADATA_KEY_YEAR)?.toIntOrNull(),
                trackNumber = retriever.extract(METADATA_KEY_CD_TRACK_NUMBER)
                                ?.split("/")?.firstOrNull()?.toIntOrNull(),
                bitRate     = retriever.extract(METADATA_KEY_BITRATE)?.toIntOrNull(),
            )
        } catch (e: Exception) {
            Timber.w(e, "Metadata read failed: $contentUri")
            TrackMetadata()
        } finally {
            retriever.release()
        }
    }

    private fun MediaMetadataRetriever.extract(key: Int) =
        extractMetadata(key)?.takeIf { it.isNotBlank() }
}
```

**Acceptance criteria:**
- [ ] After enrichment pass: most tracks show correct artist and album
- [ ] Enrichment runs in background without freezing player
- [ ] No crash on tracks with no metadata

---

## Task 3.2 — Library Organizer

```kotlin
// data/local/scanner/LibraryOrganizer.kt

@Singleton
class LibraryOrganizer @Inject constructor(
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
) {
    suspend fun organize(tracks: List<TrackEntity>) = withContext(Dispatchers.IO) {
        // Build albums
        val albumMap = tracks.groupBy { t ->
            "${(t.albumArtist ?: t.artist).lowercase()}::${t.album.lowercase()}"
        }
        val albums = albumMap.entries.map { (key, tracks) ->
            val first = tracks.first()
            AlbumEntity(
                id          = key.sha1(),
                title       = first.album,
                artist      = first.albumArtist ?: first.artist,
                year        = tracks.mapNotNull { it.year }.firstOrNull(),
                artworkPath = tracks.firstNotNullOfOrNull { it.artworkPath },
                trackCount  = tracks.size,
                durationMs  = tracks.sumOf { it.durationMs },
                addedAt     = tracks.minOf { it.addedAt },
            )
        }
        albumDao.upsertAlbums(albums)

        // Build artists
        val artistMap = albums.groupBy { it.artist.lowercase() }
        val artists = artistMap.entries.map { (_, artistAlbums) ->
            ArtistEntity(
                id          = artistAlbums.first().artist.lowercase().sha1(),
                name        = artistAlbums.first().artist,
                artworkPath = artistAlbums.firstNotNullOfOrNull { it.artworkPath },
                albumCount  = artistAlbums.size,
                trackCount  = artistAlbums.sumOf { it.trackCount },
            )
        }
        artistDao.upsertArtists(artists)
    }
}
```

---

## Task 3.3 — Full Library Screen

Implement from `components/QUEUE_LIBRARY_SCREENS.md`.

**Sub-task breakdown:**

**3.3a — SylphySearchBar:**
```kotlin
// Focus state: border weight 1.dp → 2.dp (animateDpAsState)
val borderWidth by animateDpAsState(
    targetValue   = if (focused) Layout.borderThick else Layout.borderThin,
    animationSpec = tween(Duration.Fast),
    label         = "search_border",
)
```

**3.3b — LibrarySubTabs (underline indicator):**
```kotlin
// Different from main SylphyTabBar (which uses inverted pill)
// Sub-tabs use a sliding 2.dp underline — FgPrimary color
// Same animation pattern as the pill, just a thin line instead

val underlineOffset = remember { Animatable(0f) }
LaunchedEffect(selected.ordinal) {
    underlineOffset.animateTo(
        targetValue   = selected.ordinal * tabWidthPx,
        animationSpec = tween(Duration.Normal, easing = SylphyEasing.Standard),
    )
}
```

**3.3c — Songs tab with sticky section headers:**
```kotlin
LazyColumn {
    stickyHeader(key = "recent") {
        if (recentlyPlayed.isNotEmpty()) {
            RecentlyPlayedStrip(
                tracks = recentlyPlayed,
                onTrackClick = { viewModel.playTrack(it, tracks) },
            )
        }
    }

    sectioned.forEach { (letter, sectionTracks) ->
        stickyHeader(key = "header_$letter") {
            SectionHeader(title = letter)
        }
        items(sectionTracks, key = { it.id }) { track ->
            TrackRow(
                track       = track,
                isActive    = track.id == activeTrackId,
                onClick     = { viewModel.playTrack(track, tracks) },
                onLongClick = { showContextMenu = track },
                modifier    = Modifier.animateItem(),
            )
        }
    }
}
```

**3.3d — Albums tab (2-column grid):**
```kotlin
LazyVerticalGrid(
    columns  = GridCells.Fixed(2),
    contentPadding = PaddingValues(Spacing.md),
    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    verticalArrangement   = Arrangement.spacedBy(Spacing.md),
) {
    items(albums, key = { it.id }) { album ->
        AlbumCard(album = album, onClick = { navController.navigate("album/${album.id}") })
    }
}
```

**3.3e — Artists / Playlists tabs:** follow same pattern.

**Acceptance criteria:**
- [ ] All four sub-tabs populated with real data
- [ ] Section headers sticky while scrolling songs list
- [ ] Recently played strip at top of Songs tab
- [ ] Search bar always visible, auto-focuses on tap
- [ ] Scan progress bar shows during active scan

---

## Task 3.4 — FTS Search

Migration from `architecture/DATABASE_SCHEMA.md`:

```kotlin
// Add to SylphyDatabase migrations
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS tracks_fts
            USING fts4(trackId, title, artist, album, genre, content='tracks')
        """)
        db.execSQL("""
            INSERT INTO tracks_fts(trackId, title, artist, album, genre)
            SELECT id, title, artist, album, COALESCE(genre,'') FROM tracks
        """)
        db.execSQL("""
            CREATE TRIGGER tracks_fts_insert AFTER INSERT ON tracks BEGIN
              INSERT INTO tracks_fts(trackId,title,artist,album,genre)
              VALUES(new.id,new.title,new.artist,new.album,COALESCE(new.genre,''));
            END
        """)
        db.execSQL("""
            CREATE TRIGGER tracks_fts_delete AFTER DELETE ON tracks BEGIN
              DELETE FROM tracks_fts WHERE trackId=old.id;
            END
        """)
        db.execSQL("""
            CREATE TRIGGER tracks_fts_update AFTER UPDATE ON tracks BEGIN
              DELETE FROM tracks_fts WHERE trackId=old.id;
              INSERT INTO tracks_fts(trackId,title,artist,album,genre)
              VALUES(new.id,new.title,new.artist,new.album,COALESCE(new.genre,''));
            END
        """)
    }
}
```

Search use case:
```kotlin
class SearchTracksUseCase @Inject constructor(private val trackDao: TrackDao) {
    suspend operator fun invoke(query: String): SearchResults {
        val q = query.trim().split(" ").joinToString(" ") { "$it*" }
        val tracks  = trackDao.searchTracks(q)
        val albums  = /* in-memory filter on libraryStore */
        val artists = /* in-memory filter on libraryStore */
        return SearchResults(tracks.map { it.toDomain() }, albums, artists)
    }
}
```

Debounce in `LibraryViewModel`:
```kotlin
_searchQuery
    .debounce(300)
    .distinctUntilChanged()
    .filter { it.length >= 2 || it.isEmpty() }
    .collect { query ->
        _uiState.update {
            it.copy(searchResults = if (query.length >= 2) searchUseCase(query) else null)
        }
    }
```

**Acceptance criteria:**
- [ ] Search results appear within 300ms
- [ ] Tracks, albums, artists in separate sections
- [ ] Empty query: returns to normal library view
- [ ] FTS handles partial words: "wkn" matches "The Weeknd"

---

## Task 3.5 — Playlist Management

```kotlin
// Context menu → "Add to playlist..." → secondary sheet shows all playlists
// Tap a playlist → adds track → toast: "Added to Playlist Name"

// Playlist detail screen:
//   - Header: auto-generated 2×2 cover art grid
//   - "Play all" + "Shuffle" buttons
//   - Reorderable track list (same DraggableQueueList pattern)
//   - Swipe to remove from playlist

// Playlist create:
//   - ModalBottomSheet with a single TextField
//   - "Create" button (Solid variant)
//   - Keyboard shown on open
```

Context menu implementation:
```kotlin
// Show/hide via state in LibraryScreen
var contextMenuTrack by remember { mutableStateOf<Track?>(null) }

contextMenuTrack?.let { track ->
    TrackContextMenuSheet(
        track         = track,
        playlists     = uiState.playlists,
        onDismiss     = { contextMenuTrack = null },
        onPlayNext    = { viewModel.playNext(track); contextMenuTrack = null },
        onAddToQueue  = { viewModel.addToQueue(track); contextMenuTrack = null },
        onAddToPlaylist = { playlist ->
            viewModel.addToPlaylist(playlist.id, track.id)
            contextMenuTrack = null
        },
        onToggleFav   = { viewModel.toggleFavorite(track) },
    )
}
```

**Acceptance criteria:**
- [ ] Create playlist from empty state prompt
- [ ] Add tracks via context menu
- [ ] Playlist detail shows correct tracks
- [ ] Reorder tracks within playlist (writes to Room)
- [ ] Delete playlist (confirmation via second tap: label changes to "Tap to confirm")

---

## Task 3.6 — Detail Screens

**Album detail** from `components/QUEUE_LIBRARY_SCREENS.md`:
- Full-width header artwork
- Metadata row: artist · year · track count · total duration
- `AlbumTrackRow` — numbered, no artwork thumbnail
- "Play all" (Solid) + "Shuffle" (Outline) action buttons

**Artist detail:**
```kotlin
// Layout:
// LazyColumn {
//   item { ArtistHeader(name, albumCount, trackCount) }
//   item { HorizontalAlbumScroll(albums) }
//   stickyHeader { SectionHeader("All tracks") }
//   items(tracks) { TrackRow(...) }
// }

// HorizontalAlbumScroll:
// LazyRow of AlbumCard — 160.dp width each
// gap: Spacing.md
// paddingStart: Spacing.md
```

**Acceptance criteria:**
- [ ] Album detail shows all tracks in correct order
- [ ] "Play all" loads album queue and starts playing
- [ ] Artist detail shows albums horizontally + all tracks vertically
- [ ] Navigation back works (system gesture)

---

## Task 3.7 — Queue Persistence

On app restart: restore queue from `QueueSnapshotEntity`:
```kotlin
// In SylphyPlaybackService.onCreate():
lifecycleScope.launch {
    queueRepository.restoreQueueSnapshot(/* all tracks from Room */)
}
// Does NOT auto-play — user must press play
```

Save queue:
- On every `onMediaItemTransition`
- On `SylphyPlaybackService.onDestroy()`

---

## Phase 3 Definition of Done

- [ ] All 4 sub-tabs show real data
- [ ] Album art in track rows and album grid
- [ ] Sticky section headers in Songs tab
- [ ] Search results in < 300ms
- [ ] Playlist create, add tracks, delete
- [ ] Album and artist detail screens functional
- [ ] Queue persists across restarts
- [ ] Context menu: all actions working
- [ ] Track info sheet: correct metadata
- [ ] All monochrome — zero color violations
- [ ] 0 Kotlin errors

---
---

# Phase 4 — EQ, Sleep Timer, Crossfade, Gapless

## Goal
Complete audio polish. 10-band EQ using Android's native `Equalizer`. Sleep timer with smooth 30s fade. Crossfade. Gapless playback verified.

---

## Task 4.1 — Equalizer

Use `android.media.audiofx.Equalizer`:
```kotlin
// audio/eq/SylphyEqualizer.kt
@Singleton
class SylphyEqualizer @Inject constructor() {
    private var eq: Equalizer? = null

    fun attach(audioSessionId: Int) {
        eq?.release()
        eq = Equalizer(0, audioSessionId).apply { enabled = true }
    }

    fun setBand(band: Short, gainDb: Float) {
        eq?.setBandLevel(band, (gainDb * 100).toInt().toShort())  // dB → millibels
    }

    fun setEnabled(enabled: Boolean) { eq?.enabled = enabled }
    fun release() { eq?.release(); eq = null }
}
```

Attach after ExoPlayer is built in `SylphyPlaybackService`:
```kotlin
val audioSessionId = player.audioSessionId
if (audioSessionId != AudioManager.AUDIO_SESSION_ID_GENERATE) {
    equalizer.attach(audioSessionId)
}
```

---

## Task 4.2 — EQ Screen Design

**Monochrome EQ screen — distinct from the Nothing OS version:**

Layout:
```
┌──────────────────────────────────────────┐
│  ← EQ                               ON   │  ← header + toggle
├──────────────────────────────────────────┤
│                                          │
│  [ Flat ] [ Bass ] [ Vocal ] [ Pop ] →   │  ← preset chips (horizontal scroll)
│                                          │
├──────────────────────────────────────────┤
│                                          │
│  +12 ─────────────────────────────────   │  ← top label
│       │     │     │     │     │          │
│       │   ▐ │     │   ▐ │     │          │  ← band sliders
│     ▐ │   ▐ │ ▐   │   ▐ │   ▐ │          │
│     ▐ │   ▐ │ ▐   │   ▐ │   ▐ │          │
│  ──── │ ── │ ── │ ── │ ── │ ──           │  ← 0dB line
│       │     │     │     │     │          │
│ -12 ─────────────────────────────────    │
│                                          │
│  32  64  125  250  500  1k  2k  4k  8k 16k│  ← freq labels
│                                          │
│                      [ Reset to flat ]   │
└──────────────────────────────────────────┘
```

Slider styling (monochrome):
```kotlin
// Vertical slider (Slider rotated -90°)
// Track: 2.dp, BgElevated
// Active fill: FgPrimary above 0dB, FgSecondary below 0dB
// Thumb: 10×10dp, FgPrimary, RectangleShape (square thumb)
// 0dB line: 1.dp FgSubtle across all sliders

// On/Off toggle (top right):
// Text toggle: "ON" / "OFF" SylphyType.Code
// Active: inverted chip (White bg, Black text)
// Inactive: bordered chip (1.dp BorderDefault, FgMuted text)
```

Preset chips:
```kotlin
// Horizontal LazyRow of chips
// Each: border 1.dp BorderDefault, ChipCorner, padding (Spacing.sm × Spacing.xs)
// Selected: ActiveBackground fill, ActiveForeground text
// Font: SylphyType.CodeSmall
```

**Acceptance criteria:**
- [ ] EQ accessible from player screen via settings icon or long press
- [ ] 10 sliders interactive and produce audible effect
- [ ] Presets animate sliders to correct positions (300ms)
- [ ] On/Off toggle bypasses EQ in real-time
- [ ] Monochrome — zero color violations

---

## Task 4.3 — Sleep Timer Screen

**Monochrome timer screen:**
```
┌──────────────────────────────────────────┐
│  ← Sleep timer                           │
├──────────────────────────────────────────┤
│                                          │
│  15 min    30 min    45 min              │  ← option grid
│  60 min    90 min    End of track        │
│                                          │
│  ──────────────────────────────────────  │
│                                          │
│  Custom duration                         │
│  ┌──────────────────────────────────┐    │
│  │  30                              │  min│
│  └──────────────────────────────────┘    │
│                                          │
│  [ Set timer ]                           │  ← Solid button
│                                          │
│  Active: stops in  43:21               × │  ← shown when timer running
└──────────────────────────────────────────┘
```

Duration option chips:
```kotlin
// Same chip pattern as EQ presets
// Selected chip: ActiveBackground (inverted)
// 2-column grid layout for the 6 presets
```

Countdown display:
```kotlin
// When timer active — shown at bottom of screen:
// "Stops in  43:21" — SylphyType.Code, FgPrimary
// Cancel ×: FgMuted, right-aligned
// Updates every second

// On player screen: small indicator near speed control
// "⬛ 43:21" — CodeSmall, FgMuted (timer icon + remaining)
// Tap: navigate to sleep timer screen
```

Sleep timer logic from `architecture/AUDIO_PIPELINE.md` — unchanged from v1 except no color references.

**Acceptance criteria:**
- [ ] 6 preset options + custom input
- [ ] Countdown visible on player screen when active
- [ ] 30s fade confirmed (volume decreases smoothly)
- [ ] Cancel works mid-fade (volume restored to 1.0)

---

## Task 4.4 — Crossfade

Same implementation as v1 Phase 4.4 (volume ramp approach).

Settings access:
```kotlin
// Add to player screen: small settings icon (gear) top-right
// Opens a compact bottom sheet:
//
//  Audio settings
//  ──────────────
//  Crossfade     [ 0s ▸ 3s ▸ 6s ▸ 12s ]   (tap cycles)
//  Speed         (existing SpeedControl chip)
//  EQ            → navigate to EQ screen
//  Sleep timer   → navigate to sleep timer

// DataStore persistence for crossfadeDurationMs
```

---

## Task 4.5 — Gapless Verification

Same test matrix as v1 — MP3→MP3, FLAC→FLAC, MP3→FLAC.

**Acceptance criteria:**
- [ ] No audible gap: MP3 consecutive tracks from same album
- [ ] No audible gap: FLAC consecutive tracks
- [ ] Test results documented

---

## Phase 4 Definition of Done

- [ ] EQ: 10 sliders, presets, on/off toggle, persisted
- [ ] EQ audibly affects playback in real-time
- [ ] Sleep timer: countdown, 30s fade, cancel
- [ ] Crossfade: smooth transition at 3s, 6s, 12s
- [ ] Gapless: confirmed for MP3 and FLAC
- [ ] All screens monochrome — zero color violations
- [ ] 0 Kotlin errors, no regressions

---
---

# Phase 5 — Stats Dashboard, Gestures, Ambient Mode

## Goal
The final experience layer. Listening stats with precision monochrome data visualization. Gesture controls. Ambient clock mode.

**BPM feature is removed entirely from Phase 5.** No BPM detection, no smart playlists by tempo.

**Phase 5 complete when:** Stats show real listening data in a clean grid heatmap. Two-finger swipe controls volume. Shake toggles shuffle. Ambient mode activates on charger with a minimal black clock screen.

---

## Task 5.1 — Waveform Generation

The waveform data is used only for seek bar visualization — it modulates line thickness. This is a visual enhancement, not a music analysis feature.

```kotlin
// domain/usecase/GenerateWaveformUseCase.kt

class GenerateWaveformUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(track: Track): List<Float>? = withContext(Dispatchers.IO) {
        val NUM_BUCKETS = 200
        val extractor   = MediaExtractor()

        try {
            extractor.setDataSource(context, Uri.parse(track.contentUri), null)
            val audioTrack = (0 until extractor.trackCount)
                .map { extractor.getTrackFormat(it) }
                .indexOfFirst { it.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true }

            if (audioTrack < 0) return@withContext null
            extractor.selectTrack(audioTrack)

            val format    = extractor.getTrackFormat(audioTrack)
            val duration  = format.getLong(MediaFormat.KEY_DURATION)  // microseconds
            val codec     = MediaCodec.createDecoderByType(
                format.getString(MediaFormat.KEY_MIME)!!
            )

            codec.configure(format, null, null, 0)
            codec.start()

            val bucketDuration = duration / NUM_BUCKETS
            val rmsValues      = FloatArray(NUM_BUCKETS)
            val bufferInfo     = MediaCodec.BufferInfo()
            var bucketIndex    = 0
            var bucketSumSq    = 0f
            var bucketSamples  = 0

            // Decode loop
            var inputDone  = false
            var outputDone = false
            while (!outputDone && bucketIndex < NUM_BUCKETS) {
                if (!inputDone) {
                    val inIndex = codec.dequeueInputBuffer(10_000)
                    if (inIndex >= 0) {
                        val buf   = codec.getInputBuffer(inIndex)!!
                        val size  = extractor.readSampleData(buf, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(inIndex, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outIndex >= 0) {
                    val buf     = codec.getOutputBuffer(outIndex)!!
                    val samples = buf.remaining() / 2  // 16-bit PCM

                    repeat(samples) {
                        val sample = buf.getShort().toFloat() / Short.MAX_VALUE
                        bucketSumSq  += sample * sample
                        bucketSamples++
                    }

                    val currentTime  = bufferInfo.presentationTimeUs
                    val targetBucket = (currentTime / bucketDuration).toInt().coerceAtMost(NUM_BUCKETS - 1)

                    while (bucketIndex < targetBucket) {
                        rmsValues[bucketIndex] = if (bucketSamples > 0)
                            sqrt(bucketSumSq / bucketSamples) else 0f
                        bucketIndex++
                        bucketSumSq   = 0f
                        bucketSamples = 0
                    }

                    codec.releaseOutputBuffer(outIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputDone = true
                    }
                }
            }

            codec.stop()
            codec.release()
            extractor.release()

            // Normalize
            val max = rmsValues.maxOrNull()?.coerceAtLeast(0.001f) ?: return@withContext null
            rmsValues.map { it / max }

        } catch (e: Exception) {
            Timber.w(e, "Waveform generation failed: ${track.title}")
            null
        }
    }
}
```

Schedule via WorkManager after scan completes (same pattern as v1 BPM scan, repurposed for waveform):
```kotlin
class WaveformScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val generateWaveformUseCase: GenerateWaveformUseCase,
    private val trackDao: TrackDao,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val tracks = trackDao.getTracksWithoutWaveform()
        tracks.forEach { entity ->
            val waveform = generateWaveformUseCase(entity.toDomain()) ?: return@forEach
            val json = Json.encodeToString(waveform)
            trackDao.updateWaveform(entity.id, json)
        }
        return Result.success()
    }
}
```

**Acceptance criteria:**
- [ ] Waveform data generated for tracks after scan
- [ ] Seek bar thickness varies visibly between loud/quiet sections
- [ ] Generation runs in background without degrading playback
- [ ] Null gracefully handled (flat line seek bar)

---

## Task 5.2 — Listening Stats

**StatsViewModel:**
```kotlin
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val sessionDao: SessionDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadStats()
        }
    }

    private suspend fun loadStats() {
        val now   = System.currentTimeMillis()
        val week  = now - 7L * 24 * 3600 * 1000
        val month = now - 30L * 24 * 3600 * 1000
        val weeks12 = now - 84L * 24 * 3600 * 1000

        _uiState.update {
            it.copy(
                heatmapData     = sessionDao.getListeningByDay(weeks12),
                topTracks       = sessionDao.getTopTracks(month, 10),
                weeklyMinutes   = (sessionDao.getTotalListeningMs(week) ?: 0L) / 60_000L,
                monthlyTracks   = sessionDao.getTopTracks(month, 1000).size,
            )
        }
    }
}

data class StatsUiState(
    val heatmapData: List<DayListening> = emptyList(),
    val topTracks: List<TrackWithStats> = emptyList(),
    val weeklyMinutes: Long = 0L,
    val monthlyTracks: Int  = 0,
)
```

**Stats screen layout:**
```kotlin
@Composable
fun StatsScreen() {
    LazyColumn(
        Modifier.fillMaxSize().background(BgBase),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        // Header
        item {
            Text("Listening stats", style = SylphyType.Display, color = FgPrimary)
            Spacer(Modifier.height(Spacing.xs))
            Text("Your last 12 weeks", style = SylphyType.Caption, color = FgMuted)
        }

        // Summary cards
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement   = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.height(240.dp),  // fixed height inside LazyColumn
            ) {
                item { StatCard(value = weeklyMinutes.toInt(), unit = "min", label = "This week") }
                item { StatCard(value = monthlyTracks, unit = "tracks", label = "This month") }
            }
        }

        // Heatmap
        item {
            Text("Daily listening", style = SylphyType.Heading, color = FgPrimary)
            Spacer(Modifier.height(Spacing.md))
            StatsHeatmap(data = heatmapData)
        }

        // Top tracks
        item {
            Text("Top tracks (30 days)", style = SylphyType.Heading, color = FgPrimary)
            Spacer(Modifier.height(Spacing.md))
        }
        items(topTracks, key = { it.track.id }) { item ->
            TopTrackRow(item = item, maxCount = topTracks.firstOrNull()?.sessionCount ?: 1)
        }

        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}
```

Stats accessible from: Library screen → stats icon in top-right corner (or dedicated nav entry).

**Acceptance criteria:**
- [ ] Heatmap shows real data for last 12 weeks
- [ ] Cells: opacity-only scale (no color) from FgSubtle to FgPrimary
- [ ] Top tracks sorted by play count, rank 1 = FgPrimary Medium weight
- [ ] Summary cards: animated count-up on screen enter
- [ ] Empty state when no listening history

---

## Task 5.3 — Gesture Controls

```kotlin
// ui/screens/player/PlayerScreen.kt — add gesture layer

val volumeGestureModifier = Modifier.pointerInput(Unit) {
    var accumulatedDelta = 0f
    detectTransformGestures(
        panZoomLock = true,
    ) { _, pan, _, _ ->
        // Multi-touch only (two fingers) — filter single-finger pan
        // detectTransformGestures fires for pinch + pan
        // Check pointer count via custom approach:
        accumulatedDelta -= pan.y / 400f   // negative y = up = louder
        val clampedVolume = accumulatedDelta.coerceIn(0f, 1f)
        viewModel.setVolume(clampedVolume)
        // Show VolumeIndicator overlay
    }
}
```

**Note:** `detectTransformGestures` in Compose fires for multi-touch. For two-finger-only filtering, use `PointerInputScope.awaitPointerEventScope` and check `currentEvent.changes.size == 2`.

```kotlin
// More precise two-finger detection:
Modifier.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            if (event.changes.size == 2 && event.changes.all { it.pressed }) {
                val dy = event.changes.map { it.position.y - it.previousPosition.y }.average()
                val volumeDelta = (-dy / 400f).toFloat()
                viewModel.adjustVolume(volumeDelta)
            }
        }
    }
}
```

**Shake detection:**
```kotlin
// MainActivity.kt — SensorEventListener

private val shakeListener = object : SensorEventListener {
    private var lastShakeMs = 0L
    override fun onSensorChanged(event: SensorEvent) {
        val (x, y, z) = event.values
        val magnitude = sqrt(x * x + y * y + z * z)
        val now = System.currentTimeMillis()
        if (magnitude > 15f && now - lastShakeMs > 2000L) {
            lastShakeMs = now
            mediaControllerProvider.controller.value?.let { ctrl ->
                ctrl.shuffleModeEnabled = !ctrl.shuffleModeEnabled
            }
            vibrate()
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
```

**VolumeIndicator** from `design/COMPONENT_LIBRARY.md`:
```kotlin
// Shown as overlay in PlayerScreen Box when volume gesture is active
// Auto-dismiss via coroutine delay:

var volumeIndicatorVisible by remember { mutableStateOf(false) }
var dismissJob by remember { mutableStateOf<Job?>(null) }

fun onVolumeGestureUpdate(volume: Float) {
    viewModel.setVolume(volume)
    volumeIndicatorVisible = true
    dismissJob?.cancel()
    dismissJob = scope.launch {
        delay(1500)
        volumeIndicatorVisible = false
    }
}

AnimatedVisibility(
    visible = volumeIndicatorVisible,
    enter   = fadeIn(tween(Duration.Fast)),
    exit    = fadeOut(tween(Duration.Normal)),
) {
    VolumeIndicator(
        volume   = uiState.volume,
        modifier = Modifier.align(Alignment.TopCenter).padding(top = Spacing.xxl),
    )
}
```

**Acceptance criteria:**
- [ ] Two-finger vertical swipe changes volume (tested with headphones)
- [ ] Volume indicator appears and auto-dismisses
- [ ] Shake triggers shuffle toggle with vibration feedback
- [ ] Shake debounced to 2s
- [ ] Single-finger gestures on player screen are unaffected

---

## Task 5.4 — Ambient Mode

```kotlin
// ui/screens/ambient/AmbientScreen.kt

@Composable
fun AmbientScreen(viewModel: AmbientViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view    = LocalView.current

    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Pure black background — AMOLED exception to #0A0A0A rule
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
            ) { viewModel.dismiss() },
    ) {
        Column(
            modifier            = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.Start,   // left-aligned — editorial
        ) {
            // Clock
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text  = uiState.time,
                    style = SylphyType.Clock,
                    color = FgPrimary,
                )
                // Blinking cursor — simple underscore, not a block cursor
                AnimatedVisibility(
                    visible = uiState.cursorVisible,
                    enter   = fadeIn(tween(0)),
                    exit    = fadeOut(tween(0)),
                ) {
                    Text(
                        "_",
                        style = SylphyType.Clock,
                        color = FgSecondary,
                        modifier = Modifier.padding(start = 2.dp),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            // Track info (if playing)
            uiState.activeTrack?.let { track ->
                Text(
                    text     = track.title,
                    style    = SylphyType.Heading,
                    color    = FgSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text     = track.artist,
                    style    = SylphyType.Body,
                    color    = FgMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Minimal progress indicator — thin line at very bottom
        uiState.progress?.let { progress ->
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(progress)
                    .height(1.dp)
                    .background(FgSubtle),
            )
        }
    }
}
```

**Ambient trigger logic:**
```kotlin
// AmbientViewModel.kt

@HiltViewModel
class AmbientViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val mediaControllerProvider: MediaControllerProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AmbientUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Clock tick
        viewModelScope.launch {
            while (true) {
                val now = LocalTime.now()
                _uiState.update {
                    it.copy(time = "%02d:%02d".format(now.hour, now.minute))
                }
                delay(1000)
            }
        }
        // Cursor blink
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(cursorVisible = !it.cursorVisible) }
                delay(1000)
            }
        }
        // Sync active track
        viewModelScope.launch {
            mediaControllerProvider.controller.collect { ctrl ->
                ctrl?.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                        // update activeTrack in uiState
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        // update progress
                    }
                })
            }
        }
    }

    fun dismiss() {
        // Handled by NavController in MainActivity — pop back
    }
}

data class AmbientUiState(
    val time: String = "00:00",
    val cursorVisible: Boolean = true,
    val activeTrack: Track? = null,
    val progress: Float? = null,
)
```

**Ambient trigger in MainActivity:**
```kotlin
// Register BroadcastReceiver for charging state
private val chargingReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                  || status == BatteryManager.BATTERY_STATUS_FULL
    }
}

// Track idle time — reset on any touch in GestureDetector root
// After 60s idle + charging: navigate to ambient
```

**Acceptance criteria:**
- [ ] Ambient activates when charger connected + 60s no touch
- [ ] Clock shows correct time, updates every second
- [ ] Underscore cursor blinks at 1Hz
- [ ] Track title and artist visible below clock
- [ ] Thin progress line at bottom of screen
- [ ] Any tap: navigate back to player
- [ ] Screen stays on (`keepScreenOn = true`)
- [ ] Pure black background (`#000000`)

---

## Phase 5 Definition of Done

- [ ] Waveform generated for all tracks, seek bar uses it
- [ ] Stats heatmap shows real data (12 weeks, opacity-only scale)
- [ ] Top tracks list sorted by play count
- [ ] Animated count-up on stat cards
- [ ] Two-finger volume gesture working
- [ ] Volume indicator shows and auto-dismisses
- [ ] Shake shuffles with haptic
- [ ] Ambient mode activates on charger + 60s idle
- [ ] Ambient: clock, track info, progress line, any-tap dismiss
- [ ] Screen stays on in ambient
- [ ] All phases 1–4 still passing (regression)
- [ ] `./gradlew assembleRelease` → success
- [ ] Zero color violations across entire app
- [ ] Zero Kotlin compilation errors
