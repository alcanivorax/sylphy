# Phase 1 — Scaffold + Media3 + Navigation + Library Scan

## Goal
A running Kotlin Jetpack Compose app with:
- Monochrome brutalist theme applied globally
- Geist Mono + Geist Sans fonts loaded
- Media3 `ExoPlayer` initialized and playing local audio
- `MediaStore` scanning local audio files and persisting to Room
- Three-tab navigation: Player / Queue / Library
- Background playback confirmed on physical device

**Phase 1 is complete when:** You can tap a scanned track in Library tab → audio plays in background → lock screen controls work → the entire app renders in black, white, and correct typography. Zero red pixels anywhere.

---

## Task 1.1 — Create Android Project

In Android Studio:
- **New Project → Empty Activity**
- **Package:** `com.sylphy.player`
- **Language:** Kotlin
- **Min SDK:** API 26 (Android 8.0)
- **Build config language:** Kotlin DSL

After creation:
1. Delete `res/layout/` directory entirely
2. Replace `build.gradle.kts` files with content from `architecture/TECH_STACK.md`
3. Create `gradle/libs.versions.toml` from `architecture/TECH_STACK.md`
4. Update `gradle.properties` from `architecture/TECH_STACK.md`

```bash
./gradlew assembleDebug
# Must succeed with 0 errors before proceeding
```

**Acceptance criteria:**
- [ ] Project builds with 0 errors
- [ ] All version catalog entries resolve
- [ ] No "Could not resolve" Gradle errors

---

## Task 1.2 — Fonts

Download from https://vercel.com/font:
- `GeistMono-Regular.ttf`
- `GeistMono-Medium.ttf`
- `GeistMono-Bold.ttf`
- `GeistSans-Regular.ttf`
- `GeistSans-Medium.ttf`

Rename to lowercase with underscores (Android resource naming requirement):
```
app/src/main/res/font/
├── geist_mono_regular.ttf
├── geist_mono_medium.ttf
├── geist_mono_bold.ttf
├── geist_sans_regular.ttf
└── geist_sans_medium.ttf
```

Verify font files load — add a temporary test composable:
```kotlin
Text(
    "Sylphy 01:23",
    style = TextStyle(
        fontFamily = GeistMono,
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
    ),
    color = Color.White,
)
```

**Acceptance criteria:**
- [ ] `R.font.geist_mono_regular` resolves without error
- [ ] Rendered text is visibly Geist Mono (geometric, not system monospace)
- [ ] Three weights (Regular/Medium/Bold) visibly distinct

---

## Task 1.3 — Theme Setup

Create all files from `design/DESIGN_TOKENS.md`:

```
ui/theme/
├── Color.kt         ← full monochrome token system
├── Type.kt          ← GeistMono + GeistSans families + SylphyType.*
├── Shape.kt         ← ContainerCorner=4dp, ChipCorner=2dp, SharpCorner=0dp
├── Spacing.kt       ← Spacing.* and Layout.* objects
├── Easing.kt        ← SylphyEasing.* and Duration.*
└── SylphyTheme.kt   ← MaterialTheme wrapper
```

Apply theme in `MainActivity`:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var mediaControllerProvider: MediaControllerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        mediaControllerProvider.connect()

        setContent {
            SylphyTheme {
                SylphyNavGraph()
            }
        }
    }

    override fun onDestroy() {
        mediaControllerProvider.disconnect()
        super.onDestroy()
    }
}
```

**`SylphyApplication.kt`:**
```kotlin
@HiltAndroidApp
class SylphyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
```

**`res/values/themes.xml`** — minimal, only for splash and system bars:
```xml
<resources>
    <style name="Theme.Sylphy" parent="android:Theme.Material.NoTitleBar.Fullscreen">
        <item name="android:windowBackground">@color/bg_base</item>
        <item name="android:statusBarColor">@color/bg_base</item>
        <item name="android:navigationBarColor">@color/bg_base</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
</resources>
```

**`res/values/colors.xml`:**
```xml
<resources>
    <color name="bg_base">#0A0A0A</color>
</resources>
```

**Acceptance criteria:**
- [ ] App background is `#0A0A0A` — not `#000000`, not white
- [ ] Status bar icons are white (light on dark)
- [ ] Navigation bar background matches app background
- [ ] No default Compose purple/teal anywhere

---

## Task 1.4 — AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:name=".SylphyApplication"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="Sylphy"
        android:theme="@style/Theme.Sylphy"
        android:supportsRtl="true">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.SylphyPlaybackService"
            android:foregroundServiceType="mediaPlayback"
            android:exported="true">
            <intent-filter>
                <action android:name="androidx.media3.session.MediaSessionService" />
                <action android:name="android.media.browse.MediaBrowserService" />
            </intent-filter>
        </service>

    </application>
</manifest>
```

**Acceptance criteria:**
- [ ] App installs without manifest rejection
- [ ] No "Missing permission" crash on launch

---

## Task 1.5 — Domain Models

Create all data classes in `data/model/`:

```kotlin
// data/model/Track.kt
data class Track(
    val id: String,
    val contentUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val durationMs: Long,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val sampleRate: Int? = null,
    val bitRate: Int? = null,
    val artworkPath: String? = null,
    val waveformData: List<Float>? = null,  // 200 values for seek bar modulation
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null,
    val addedAt: Long,
    val isAvailable: Boolean = true,
    val isFavorite: Boolean = false,
)

// data/model/Album.kt
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtist: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    val artworkPath: String? = null,
    val trackCount: Int = 0,
    val durationMs: Long = 0L,
    val addedAt: Long,
    val tracks: List<Track> = emptyList(),
)

// data/model/Artist.kt
data class Artist(
    val id: String,
    val name: String,
    val artworkPath: String? = null,
    val albumCount: Int = 0,
    val trackCount: Int = 0,
)

// data/model/Playlist.kt
data class Playlist(
    val id: String,
    val name: String,
    val description: String? = null,
    val artworkPath: String? = null,
    val isAutoGenerated: Boolean = false,
    val trackCount: Int = 0,
    val durationMs: Long = 0L,
    val createdAt: Long,
    val updatedAt: Long,
    val tracks: List<Track> = emptyList(),
)

// data/model/PlayerState.kt
data class PlayerUiState(
    val activeTrack: Track? = null,
    val isPlaying: Boolean = false,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val position: Long = 0L,
    val duration: Long = 0L,
    val buffered: Long = 0L,
    val speed: Float = 1.0f,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
)

enum class PlaybackState { IDLE, BUFFERING, READY, ENDED }
enum class RepeatMode    { OFF, ONE, ALL }

// data/model/Settings.kt
data class Settings(
    val crossfadeDurationMs: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val gaplessEnabled: Boolean = true,
    val eqEnabled: Boolean = false,
    val eqPreset: String = "flat",
    val eqBands: List<Float> = List(10) { 0f },
    val sleepTimerEnabled: Boolean = false,
    val sleepTimerEndTime: Long? = null,
    val ambientModeEnabled: Boolean = true,
)
```

**Acceptance criteria:**
- [ ] All data classes compile without error
- [ ] No nullable fields that should be non-null (check each field)

---

## Task 1.6 — Hilt Setup

```kotlin
// SylphyApplication.kt — @HiltAndroidApp annotation
// MainActivity.kt — @AndroidEntryPoint annotation

// core/di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMediaControllerProvider(
        @ApplicationContext ctx: Context,
    ): MediaControllerProvider = MediaControllerProvider(ctx)
}
```

Create empty stub modules (implementation added as dependencies built):
- `core/di/DatabaseModule.kt`
- `core/di/RepositoryModule.kt`

**Acceptance criteria:**
- [ ] App compiles with Hilt — no "Hilt component not found" error
- [ ] `@AndroidEntryPoint` on MainActivity works

---

## Task 1.7 — Room Database

Implement from `architecture/DATABASE_SCHEMA.md`.

**Note: BPM column removed.** `TrackEntity` has no `bpm` field in v2.

```kotlin
// data/local/db/entity/TrackEntity.kt
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val contentUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val durationMs: Long,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val sampleRate: Int? = null,
    val bitRate: Int? = null,
    val artworkPath: String? = null,
    val waveformJson: String? = null,   // JSON of 200 Float values
    // NO bpm field
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null,
    val addedAt: Long,
    val isAvailable: Boolean = true,
    val isFavorite: Boolean = false,
)
```

Create all other entities (Album, Artist, Playlist, PlaylistTrack, ListeningSession, QueueSnapshot) from `architecture/DATABASE_SCHEMA.md` — all unchanged from v1 except no BPM references.

Create all DAOs. Remove `getTracksWithoutBpm()` and `updateBpm()` from `TrackDao` — those don't exist in v2.

```kotlin
// core/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SylphyDatabase =
        Room.databaseBuilder(ctx, SylphyDatabase::class.java, "sylphy.db")
            .build()

    @Provides fun provideTrackDao(db: SylphyDatabase)    = db.trackDao()
    @Provides fun provideAlbumDao(db: SylphyDatabase)    = db.albumDao()
    @Provides fun provideArtistDao(db: SylphyDatabase)   = db.artistDao()
    @Provides fun providePlaylistDao(db: SylphyDatabase) = db.playlistDao()
    @Provides fun provideSessionDao(db: SylphyDatabase)  = db.sessionDao()
    @Provides fun provideQueueDao(db: SylphyDatabase)    = db.queueDao()
}
```

**Acceptance criteria:**
- [ ] `sylphy.db` created on first launch
- [ ] `adb shell run-as com.sylphy.player ls databases/` shows `sylphy.db`
- [ ] No Room "schema changed" crash

---

## Task 1.8 — MediaStore Scanner

Implement `MediaScanner.kt` from `architecture/STATE_MANAGEMENT.md` (v1 Phase 1, Task 1.8).

The scanner is identical to v1 — no BPM-related changes needed here.

Key points:
- Uses `MediaStore.Audio.Media` ContentResolver query
- Filters by `IS_MUSIC != 0` and supported MIME types
- Batch inserts in groups of 100 (performance)
- Emits `ScanProgress` flow consumed by `LibraryViewModel`

Add `MetadataReader.kt` for enriched metadata via `MediaMetadataRetriever`:
```kotlin
// data/local/scanner/MetadataReader.kt
// Reads: title, artist, album, albumArtist, genre, year, trackNumber, bitRate
// Called per-track after initial scan pass (background, throttled)
```

Add `ArtworkExtractor.kt`:
```kotlin
// data/local/scanner/ArtworkExtractor.kt
// MediaMetadataRetriever.embeddedPicture → write to cacheDir/artwork/{trackId}.jpg
// Check cache before extracting
// Return absolute file path string
```

**Acceptance criteria:**
- [ ] Permission requested on first launch (API 33+: READ_MEDIA_AUDIO)
- [ ] After grant, MediaStore query returns audio files
- [ ] Tracks inserted into Room `tracks` table
- [ ] `trackDao.getAllTracks()` Flow emits list after scan

---

## Task 1.9 — Media3 Service

Implement from `architecture/AUDIO_PIPELINE.md`:

- `service/SylphyPlaybackService.kt`
- `core/di/MediaControllerProvider.kt`

No changes from v1 — the audio pipeline is theme-independent.

Connect in `MainActivity.onCreate()` / `onDestroy()`.

**Acceptance criteria:**
- [ ] Service starts without crash
- [ ] `adb shell dumpsys media_session` shows a Sylphy session after launch
- [ ] `MediaController` connects within 2 seconds of app launch

---

## Task 1.10 — Navigation Shell

```kotlin
// ui/navigation/SylphyNavGraph.kt

@Composable
fun SylphyNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val topLevelRoutes = listOf(
        Screen.Player.route,
        Screen.Queue.route,
        Screen.Library.route,
    )
    val showTabBar = currentRoute in topLevelRoutes

    Scaffold(
        containerColor = BgBase,
        topBar = {
            if (showTabBar) {
                SylphyTabBar(
                    tabs = listOf("Player", "Queue", "Library"),
                    selectedIndex = topLevelRoutes.indexOf(currentRoute).coerceAtLeast(0),
                    onTabSelected = { index ->
                        navController.navigate(topLevelRoutes[index]) {
                            popUpTo(Screen.Player.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Player.route,
            modifier         = Modifier.padding(padding),
            enterTransition  = {
                fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) +
                slideInHorizontally(tween(Duration.Normal, easing = SylphyEasing.Enter)) { it / 12 }
            },
            exitTransition = { fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit)) },
            popEnterTransition = { fadeIn(tween(Duration.Normal, easing = SylphyEasing.Enter)) },
            popExitTransition  = {
                fadeOut(tween(Duration.Fast, easing = SylphyEasing.Exit)) +
                slideOutHorizontally(tween(Duration.Normal, easing = SylphyEasing.Exit)) { it / 12 }
            },
        ) {
            composable(Screen.Player.route)  { PlayerScreen() }
            composable(Screen.Queue.route)   { QueueScreen() }
            composable(Screen.Library.route) { LibraryScreen(navController) }

            composable(
                Screen.AlbumDetail.ROUTE,
                arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
            ) { AlbumDetailScreen(it.arguments!!.getString("albumId")!!, navController) }

            composable(
                Screen.ArtistDetail.ROUTE,
                arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
            ) { ArtistDetailScreen(it.arguments!!.getString("artistId")!!, navController) }

            composable(
                Screen.PlaylistDetail.ROUTE,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
            ) { PlaylistDetailScreen(it.arguments!!.getString("playlistId")!!, navController) }

            composable(Screen.Eq.route)          { EqScreen() }
            composable(Screen.SleepTimer.route)  { SleepTimerScreen() }
            composable(Screen.Stats.route)       { StatsScreen() }
            composable(Screen.Ambient.route)     { AmbientScreen() }
        }
    }
}
```

**Tab bar implementation** from `design/ANIMATION_SPECS.md §4`.

**Placeholder screens** (replaced in Phase 2/3):
```kotlin
@Composable
fun PlayerScreen() {
    Box(Modifier.fillMaxSize().background(BgBase), Alignment.Center) {
        Text("Player", style = SylphyType.Heading, color = FgMuted)
    }
}
// Same for Queue, Library
```

**Acceptance criteria:**
- [ ] Three tabs at top with inverted pill indicator
- [ ] Pill animates smoothly between tabs
- [ ] Active tab label is black-on-white (inverted)
- [ ] Inactive tab labels are FgMuted
- [ ] Background on all tabs is `BgBase` (`#0A0A0A`)
- [ ] Screen transitions: subtle fade + 1/12 slide

---

## Task 1.11 — Shared Components (Phase 1 subset)

Implement these from `design/COMPONENT_LIBRARY.md`:

**`SylphyText.kt`** — font enforcer wrapper

**`SylphyDivider.kt`:**
```kotlin
@Composable
fun SylphyDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = Separator, thickness = Layout.borderThin)
}
```

**`EmptyState.kt`** — used in Library when no tracks

**`LoadingDots.kt`** — 3 FgMuted squares with staggered fade

**`SylphyButton.kt`** — Solid and Outline variants

**Acceptance criteria:**
- [ ] All components render in correct monochrome palette
- [ ] No color besides black, white, and their opacity variants

---

## Task 1.12 — Playback Verification

Wire a bare-minimum Library placeholder that scans and plays:

```kotlin
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val permission   = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    )

    Column(
        Modifier.fillMaxSize().background(BgBase)
            .padding(Spacing.lg)
    ) {
        // Scan button
        SylphyButton(
            text    = if (permission.status.isGranted) "Scan library" else "Grant permission",
            variant = ButtonVariant.Outline,
            onClick = {
                if (permission.status.isGranted) viewModel.scanLibrary()
                else permission.launchPermissionRequest()
            },
        )

        Spacer(Modifier.height(Spacing.md))

        // Progress
        AnimatedVisibility(uiState.scanStatus is ScanProgress.Scanning) {
            val p = (uiState.scanStatus as? ScanProgress.Scanning)?.progress ?: 0f
            Column {
                LinearProgressIndicator(
                    progress   = { p },
                    modifier   = Modifier.fillMaxWidth().height(2.dp),
                    color      = FgPrimary,
                    trackColor = FgSubtle,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Scanning  ${(p * 100).toInt()}%",
                    style = SylphyType.CodeSmall,
                    color = FgMuted,
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        // Track list
        LazyColumn {
            items(uiState.tracks.take(100)) { track ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playTrack(track, uiState.tracks) }
                        .padding(vertical = Spacing.sm)
                ) {
                    Text(track.title,  style = SylphyType.Code,      color = FgPrimary)
                    Text(track.artist, style = SylphyType.BodySmall,  color = FgSecondary)
                }
                SylphyDivider()
            }
        }
    }
}
```

**Manual test checklist:**
```
Test 1: Font rendering
  → Every text element uses Geist Mono or Geist Sans (no system font)

Test 2: Color discipline
  → Screenshot the app — zero non-monochrome pixels

Test 3: Scan + Play
  1. Tap "Scan library", grant permission
  2. Wait for scan
  3. Tap a track → audio starts

Test 4: Background playback
  1. Play a track
  2. Press Home
  → Audio continues

Test 5: Lock screen
  1. Play a track, lock phone
  → Lock screen shows track title + controls

Test 6: Notification
  → Pull down: Sylphy notification with title, play/pause, skip
```

**Acceptance criteria:**
- [ ] All 6 tests pass on physical Android device
- [ ] `./gradlew assembleDebug` succeeds
- [ ] Zero non-monochrome pixels in any screenshot

---

## Phase 1 Definition of Done

- [ ] `./gradlew assembleDebug` → success, 0 errors
- [ ] Geist Mono renders — visibly distinct from system monospace
- [ ] Three-tab nav with inverted pill indicator works
- [ ] Pill animation smooth at 60fps
- [ ] All screens background is `#0A0A0A`
- [ ] Hilt DI working, no manual instantiation
- [ ] Room database created on first launch
- [ ] MediaStore scan populates Room
- [ ] Tapping a track starts audio playback
- [ ] Background playback confirmed
- [ ] Lock screen controls confirmed
- [ ] Zero Kotlin compilation errors
- [ ] Zero color violations (screenshot audit)
