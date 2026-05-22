# Sylphy — Project Architecture & Structure

Sylphy is a modern, high-fidelity music player for Android, built with a focus on minimalist design (Nothing OS inspired) and robust performance. It follows **Clean Architecture** principles and utilizes the latest Android development stack.

---

## 🏗 High-Level Architecture

Sylphy is organized into three primary layers to ensure separation of concerns, testability, and maintainability:

1.  **Data Layer**: Responsible for data retrieval and persistence.
2.  **Domain Layer**: Contains the business logic and defines the "what" of the application.
3.  **UI Layer**: Handles the presentation and user interaction using Jetpack Compose.

---

## 📂 Directory Structure & Responsibilities

### 1. `core/` — The Foundation
Contains cross-cutting concerns used throughout the application.
*   **`di/`**: Hilt modules defining how dependencies (like the Database, Repositories, and MediaController) are provided.
*   **`extension/`**: Kotlin extension functions for cleaner syntax (e.g., `Context.ext`, `MediaItem.ext`).
*   **`util/`**: General-purpose utilities for formatting, permissions, and shuffle logic.

### 2. `domain/` — Business Logic
The pure Kotlin layer that defines the core functionality of the app.
*   **`model/`**: Pure data classes representing business entities (e.g., `Track`, `Album`, `Artist`).
*   **`repository/`**: Interfaces defining the contract for data operations. The UI and UseCases depend on these interfaces, not their implementations.
*   **`usecase/`**: Specific business rules (e.g., `ScanLibraryUseCase`) that orchestrate data flow.

### 3. `data/` — Data Implementation
Implements the repositories defined in the Domain layer.
*   **`local/`**:
    *   **`db/`**: Room database definition, entities, and DAOs for track metadata.
    *   **`datastore/`**: Jetpack DataStore for persisting user preferences (theme mode, playback speed).
    *   **`scanner/`**: Logic for scanning the device's storage for music files and extracting metadata/artwork.
*   **`repository/`**: Concrete implementations of Domain interfaces (e.g., `TrackRepositoryImpl`), coordinating between the DB, Scanner, and MediaStore.

### 4. `ui/` — Presentation Layer
Built entirely with Jetpack Compose.
*   **`screens/`**: High-level composables representing entire screens (Player, Library, Settings, etc.).
*   **`components/`**: Reusable UI building blocks, organized by screen context (e.g., `player/VinylArtwork.kt`).
*   **`theme/`**: Design tokens, color schemes (Nothing OS, Monochrome), typography, and shapes.
*   **`navigation/`**: Navigation graph definition and screen routing logic.
*   **`viewmodel/`**: Screen-specific ViewModels that manage UI state and interact with the Domain layer.

### 5. `service/` — Background Operations
*   **`SylphyPlaybackService.kt`**: A Media3 `MediaSessionService` that manages the ExoPlayer instance and handles playback in the background.
*   **`WaveformScanWorker.kt`**: A WorkManager worker that generates waveform data for tracks in the background.

### 6. `audio/` — Audio Processing
*   **`SylphyEqualizer.kt`**: Manages the system equalizer and audio effects.
*   **`SleepTimerController.kt`**: Logic for automatically pausing playback after a set duration.

---

## 🔄 Key Interconnections

### 💉 Dependency Injection (Hilt)
Hilt acts as the "glue" that connects the layers. It injects Repository implementations into ViewModels and provides the singleton `Player` instance to both the `PlaybackService` and the UI.

### 🎵 Media3 (ExoPlayer + MediaSession)
*   The **Service** owns the `ExoPlayer` instance.
*   The **UI** communicates with the player via a `MediaController`.
*   This separation ensures music continues playing even if the UI process is killed or the user navigates away.

### 📊 Reactive Data Flow
1.  **Data Layer** exposes data as `Flow<T>` from Room or DataStore.
2.  **Repositories** transform these flows if necessary.
3.  **ViewModels** collect these flows and convert them into `StateFlow<UiState>`.
4.  **Compose Screens** observe the `UiState` and recompose automatically when data changes.

### 🎨 Theme Management
The `SylphyTheme` handles dynamic switching between **Nothing OS**, **Monochrome Dark**, and **Monochrome Light** modes based on user settings persisted in `SettingsDataStore`.

---

## 🚀 Entry Points

*   **`SylphyApplication.kt`**: The application class. Initializes Hilt and sets up Timber for structured logging.
*   **`MainActivity.kt`**: The single activity of the app. It enables edge-to-edge display, connects the `MediaController`, and hosts the `SylphyNavGraph` within the `SylphyTheme`.
