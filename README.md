# Sylphy

A minimal, privacy-first music player for Android. No ads, no tracking — just your music.

## Features

- **Local playback** — plays audio files stored on your device
- **Themes** — Nothing OS, Obsidian (dark mono), Chalk (light mono)
- **Vinyl disc UI** — animated spinning record with generative artwork
- **Queue management** — shuffle, repeat, reorder
- **Playback speed** — 0.5× to 2.0×
- **Favourites** — heart tracks for quick access
- **Search** — find songs, albums, and artists
- **Stats** — listen to your listening habits
- **Ambient mode** — minimal now-playing overlay

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Navigation | Jetpack Navigation |
| Database | Room |
| DI | Hilt |
| Media | Media3 (ExoPlayer) |
| Image loading | Coil |

## Building

```bash
git clone https://github.com/alcanivorax/sylphy.git
cd sylphy
./gradlew assembleDebug
```

Requires JDK 17 and Android SDK 34.

## License

MIT
