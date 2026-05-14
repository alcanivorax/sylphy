# Sylphy — Monochrome Brutalist Music Player

## Vision

Sylphy is a native Kotlin Android music player built around a single design thesis: **that total restraint is its own form of luxury**. 

No color. No gradients. No decoration. Only pure typographic hierarchy, surgical spacing, and motion that earns its place. Every pixel either carries information or creates breathing room — nothing exists for ornamentation.

The aesthetic draws from:
- **Geist** — Vercel's typeface system: geometric, neutral, uncompromising negative space
- **Vercel's dashboard UI** — borders as structure, whitespace as hierarchy, zero decorative chrome  
- **shadcn/ui** — component precision, consistent density, systematic token usage
- **Swiss/International Typographic Style** — grid discipline, type-led layouts

The result is a player that looks like it was designed by someone who deeply studied both Dieter Rams and Massimo Vignelli, then built it entirely in black and white.

---

## Design Philosophy vs Nothing OS

| Axis | Nothing OS (old) | Monochrome Brutalist (new) |
|---|---|---|
| **Accent color** | Red `#FF3B30` | None — black/white only |
| **Corners** | Square (0dp) everywhere | Systematic: 0dp for hard elements, 4dp for containers |
| **Typography** | ShareTechMono only | Geist Mono (display) + Geist Sans (body) |
| **Texture** | Dot grid + CRT scanlines | None — pure flat surfaces |
| **Active state** | Red highlight | Inverted (white bg, black text) |
| **Borders** | Minimal | Structural — borders define space, not decoration |
| **Spacing** | Tight | Generous — whitespace is the design |
| **Motion** | Sine wave animations | Precision easing — fast in, slow settle |
| **Personality** | Hardware aesthetic | Editorial — like a premium print publication |

---

## Color System

**Two values. That is all.**

```
Background:  #0A0A0A   (near-black, not pure black — avoids harshness)
Foreground:  #FAFAFA   (near-white, not pure white — avoids harshness)
```

All UI states are expressed through:
- **Opacity** of foreground on background
- **Inversion** (swap fg/bg for active/selected states)
- **Border weight** (1dp vs 2dp — heavier = more important)
- **Typography weight and size** — hierarchy through type, not color

No color is ever added. Not for errors, not for playback state, not for anything.

---

## Typography System

**Geist Mono** — all display, player UI, data, numbers, labels  
**Geist Sans** — body copy, descriptions, secondary labels

Geist is Vercel's open-source typeface. Download from: https://vercel.com/font

```
res/font/geist_mono_regular.ttf
res/font/geist_mono_medium.ttf
res/font/geist_mono_bold.ttf
res/font/geist_sans_regular.ttf
res/font/geist_sans_medium.ttf
```

---

## Document Map

```
sylphy-v2-docs/
├── README.md                        ← This file
│
├── architecture/
│   ├── TECH_STACK.md               ← Gradle deps (unchanged from v1 except font)
│   ├── FOLDER_STRUCTURE.md         ← Updated package structure
│   ├── STATE_MANAGEMENT.md         ← Same pattern, BPM removed
│   ├── DATABASE_SCHEMA.md          ← BPM column removed, schema updated
│   └── AUDIO_PIPELINE.md           ← Unchanged from v1
│
├── design/
│   ├── DESIGN_TOKENS.md            ← Full monochrome token system
│   ├── COMPONENT_LIBRARY.md        ← All composables redesigned
│   └── ANIMATION_SPECS.md          ← New motion language
│
├── components/
│   ├── PLAYER_SCREEN.md            ← Full player redesign
│   ├── QUEUE_SCREEN.md             ← Queue redesign
│   └── LIBRARY_SCREEN.md          ← Library redesign
│
└── phases/
    ├── PHASE_1.md                  ← Scaffold (BPM removed)
    ├── PHASE_2.md                  ← Player screen
    ├── PHASE_3.md                  ← Library + Playlists
    ├── PHASE_4.md                  ← EQ + Sleep + Crossfade
    └── PHASE_5.md                  ← Stats + Gestures + Ambient
```

---

## What Changed vs v1

### Removed
- BPM detection, BPM scan worker, BPM smart playlists, BPM pulse dot
- `tracks.bpm` column from Room schema
- `DetectBpmUseCase`, `GenerateBpmPlaylistsUseCase`
- Red color everywhere
- Dot grid background
- CRT scanline overlay
- Corner bracket frames
- Sine wave ring (replaced with precision arc progress ring)
- Nothing OS visual language

### Changed
- All colors: red removed, system is now pure monochrome
- Typography: ShareTechMono → Geist Mono + Geist Sans
- Progress ring: sine wave → clean geometric arc (like Apple Watch Activity rings)
- Seek bar: sine wave → flat precision line with dot playhead
- Play button: red glow → sharp inversion (black bg on white)
- Tab bar: red underline → inverted pill indicator
- Active states: red text → inverted block (white bg, black text)
- Borders: structural borders throughout (shadcn-style)
- Spacing: significantly more generous — Vercel-style negative space

### Added
- `GeistMono` and `GeistSans` font families
- Systematic border tokens (border weights carry meaning)
- Elevation via border, never shadow
- Precise easing curves for all animations

---

## Agent Instructions

1. Read `design/DESIGN_TOKENS.md` before any code — the monochrome system is strict
2. No color may be added under any circumstance — not even "just a hint" of anything
3. Active/selected states use **inversion** (swap background and foreground)
4. Borders are structural — use them to define containers and hierarchy
5. Whitespace is generous — when in doubt, add 4dp more padding
6. Typography does all the work color used to do — size, weight, and opacity create hierarchy
7. `BpmPulseDot` does not exist — do not implement it
8. `waveformData` field is kept in the schema for waveform seek visualisation — **that is not BPM**

---

## Phase Status

| Phase | Name | Status |
|---|---|---|
| 1 | Scaffold + Media3 + Nav + Scan | ⬜ Not Started |
| 2 | Player Screen | ⬜ Not Started |
| 3 | Library + Playlists + Room | ⬜ Not Started |
| 4 | EQ + Sleep Timer + Crossfade | ⬜ Not Started |
| 5 | Stats + Gestures + Ambient | ⬜ Not Started |
