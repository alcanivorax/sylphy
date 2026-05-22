// =============================================================================
// Sylphy — app/build.gradle.kts
//
// Agent notes:
//   - KSP is used for Room and Hilt annotation processing (replaces kapt in Kotlin 2.x).
//   - All library versions come from gradle/libs.versions.toml — never hardcode versions here.
//   - media3-exoplayer, media3-session, media3-ui MUST stay on the same version.
//   - compileSdk uses AGP 9.x preview-SDK syntax; targetSdk stays at stable API 35.
//   - buildConfig is enabled so BuildConfig.DEBUG is available for Timber setup.
// =============================================================================

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace  = "io.sylphy.app"
    compileSdk = 36

    defaultConfig {
        applicationId   = "io.sylphy.app"
        minSdk          = 26           // Android 8.0 — required by Media3 AudioOffload
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room schema export directory — useful for migration auditing.
        // Agent: do not remove. Migrations live in app/schemas/.
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // Required for kotlinx-coroutines desugaring on API < 26 (not strictly needed at minSdk 26,
        // but keeps the build consistent if minSdk is ever lowered).
        isCoreLibraryDesugaringEnabled = false
    }

    buildFeatures {
        compose      = true
        buildConfig  = true    // needed for BuildConfig.DEBUG → Timber conditional planting
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        // Opt-in to experimental Compose APIs used by animateItem, etc.
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        )
    }
}

dependencies {

    // ── AndroidX Core ─────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)


    // ── Activity ──────────────────────────────────────────────────────────────
    implementation(libs.androidx.activity.compose)

    // ── Jetpack Compose (BOM pins all Compose artifact versions) ──────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    // Material Icons Extended — provides standard playback/media icons as Compose vectors.
    // Agent: do not remove; TransportControls and Navigation rely on it.
    implementation(libs.androidx.compose.material.icons)

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)     // collectAsStateWithLifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)   // hiltViewModel()

    // ── Navigation ────────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── Hilt (Dependency Injection) ───────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // Bridges Hilt with Navigation Compose → hiltViewModel() inside NavHost
    implementation(libs.hilt.navigation.compose)
    // Bridges Hilt with WorkManager → @HiltWorker annotation
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // ── Room (Local Database) ─────────────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)        // suspend + Flow extensions
    ksp(libs.room.compiler)             // generates DAOs at compile time
    implementation(libs.reorderable)

    // ── DataStore (Settings Persistence) ──────────────────────────────────────
    implementation(libs.datastore.preferences)

    // ── Media3 (Audio playback + media session) ───────────────────────────────
    // ExoPlayer: core audio/video engine
    implementation(libs.media3.exoplayer)
    // Session: MediaSession + MediaController + lock-screen / notification integration
    implementation(libs.media3.session)
    // UI: PlayerView (used only if we ever embed a surface; mostly here for completeness)
    implementation(libs.media3.ui)
    // Common: shared data classes (MediaItem, MediaMetadata, etc.) used across all layers
    implementation(libs.media3.common)

    // ── WorkManager (Background waveform scan) ────────────────────────────────
    implementation(libs.work.runtime.ktx)

    // ── Coroutines ────────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)
    // Guava bridge — needed for ListenableFuture → Deferred conversions in MediaController
    implementation(libs.kotlinx.coroutines.guava)

    // ── Coil (Album artwork loading) ──────────────────────────────────────────
    // Handles content:// URIs, file paths, and HTTP URLs with built-in disk cache.
    implementation(libs.coil.compose)

    // ── Accompanist ───────────────────────────────────────────────────────────
    implementation(libs.accompanist.systemuicontroller)

    // ── Timber (Structured logging) ───────────────────────────────────────────
    implementation(libs.timber)

    // ── Gson (Waveform JSON serialisation) ────────────────────────────────────
    // Used exclusively in TrackEntity.waveformJson ↔ List<Float> type converter.
    implementation(libs.gson)

    // ── Testing ───────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
