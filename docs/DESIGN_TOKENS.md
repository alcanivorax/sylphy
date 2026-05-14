# Design Tokens — Monochrome Brutalist System

## The Constraint

Two base values. Everything else is derived from them through opacity, weight, and inversion. If you find yourself reaching for a third color, stop — the answer is always opacity, inversion, or typography.

---

## File: `ui/theme/Color.kt`

```kotlin
package com.sylphy.player.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Base Values ─────────────────────────────────────────────────────────────
// These are the only two "real" colors in the entire application.
// #0A0A0A instead of pure black: avoids OLED harshness, maintains warmth.
// #FAFAFA instead of pure white: same reason, reduces contrast fatigue.

val Black = Color(0xFF0A0A0A)
val White = Color(0xFFFAFAFA)

// ─── Foreground Scale (white on black, varying opacity) ──────────────────────
// Used for text, icons, and borders at different hierarchy levels.
// Named after their primary use — not their opacity value.

val FgPrimary    = Color(0xFFFAFAFA)  // 100% — headings, active labels, primary content
val FgSecondary  = Color(0xA8FAFAFA)  // 66%  — supporting text, inactive labels
val FgMuted      = Color(0x66FAFAFA)  // 40%  — placeholders, disabled states, captions
val FgSubtle     = Color(0x33FAFAFA)  // 20%  — very faint hints, skeleton states
val FgGhost      = Color(0x14FAFAFA)  // 8%   — barely-there separators, hover states

// ─── Background Scale (black surfaces at varying lightness) ──────────────────
val BgBase       = Color(0xFF0A0A0A)  // primary background — all screens
val BgElevated   = Color(0xFF111111)  // slightly elevated: sheets, cards
val BgSunken     = Color(0xFF060606)  // slightly depressed: input fields, troughs
val BgInverted   = Color(0xFFFAFAFA)  // fully inverted — active/selected states

// ─── Border Scale ─────────────────────────────────────────────────────────────
// Borders are structural, not decorative.
// Weight (1dp vs 2dp) carries meaning — do not mix arbitrarily.

val BorderDefault  = Color(0x1FFAFAFA)  // 12% — standard container border
val BorderStrong   = Color(0x3DFAFAFA)  // 24% — focused/active container border
val BorderInverted = Color(0xFFFAFAFA)  // 100% — fully inverted border (selected items)

// ─── Semantic Aliases (map to the above — never add new values here) ──────────
val Background   = BgBase
val Surface      = BgElevated
val TextPrimary  = FgPrimary
val TextSecondary= FgSecondary
val TextMuted    = FgMuted
val Accent       = FgPrimary     // "accent" is just primary white — no color
val Separator    = FgGhost

// ─── Progress / Waveform ──────────────────────────────────────────────────────
// Seek bar and progress ring use two weights of the same white.
val ProgressFilled   = FgPrimary   // played portion — full white
val ProgressEmpty    = FgSubtle    // remaining portion — ghost white
val ProgressPlayhead = FgPrimary   // dot at current position

// ─── Inversion (active state system) ──────────────────────────────────────────
// When something is selected/active/playing, the background and text invert.
// This is the ONLY way to express "selected" — no color, no underline, no icon change.
val ActiveBackground = White
val ActiveForeground = Black
```

---

## File: `ui/theme/Type.kt`

```kotlin
package com.sylphy.player.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sylphy.player.R

// ─── Font Families ────────────────────────────────────────────────────────────

val GeistMono = FontFamily(
    Font(R.font.geist_mono_regular, FontWeight.Normal),
    Font(R.font.geist_mono_medium,  FontWeight.Medium),
    Font(R.font.geist_mono_bold,    FontWeight.Bold),
)

val GeistSans = FontFamily(
    Font(R.font.geist_sans_regular, FontWeight.Normal),
    Font(R.font.geist_sans_medium,  FontWeight.Medium),
)

// ─── Type Scale ───────────────────────────────────────────────────────────────
//
// Rule: Geist Mono for anything that is data, UI chrome, or player controls.
//       Geist Sans for anything that is descriptive prose or secondary information.
//
// The scale is tightly controlled — do not introduce new font sizes.
// If something doesn't fit, adjust weight or letter-spacing before changing size.

object SylphyType {

    // ── Mono Scale ─────────────────────────────────────────────────────────────

    /** Hero display: track title on player, large time display */
    val DisplayLarge = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 34.sp,
        letterSpacing = (-0.5).sp,
    )

    /** Section title, album name in detail view */
    val Display = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Medium,
        fontSize      = 20.sp,
        lineHeight    = 26.sp,
        letterSpacing = (-0.3).sp,
    )

    /** Screen headers, tab labels */
    val Heading = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.8.sp,
    )

    /** Data labels, timestamps, durations, counters */
    val Code = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    )

    /** Metadata badges, keyboard shortcuts, micro-labels */
    val CodeSmall = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 15.sp,
        letterSpacing = 0.2.sp,
    )

    /** Ambient mode clock */
    val Clock = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Bold,
        fontSize      = 72.sp,
        lineHeight    = 80.sp,
        letterSpacing = (-3).sp,
    )

    /** Stats dashboard large numbers */
    val Stat = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Bold,
        fontSize      = 40.sp,
        lineHeight    = 46.sp,
        letterSpacing = (-1).sp,
    )

    // ── Sans Scale ──────────────────────────────────────────────────────────────

    /** Track artist name, description text */
    val Body = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = (-0.1).sp,
    )

    /** Secondary info: album name in track row, genre */
    val BodySmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 17.sp,
        letterSpacing = 0.sp,
    )

    /** Captions, empty state descriptions */
    val Caption = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 15.sp,
        letterSpacing = 0.sp,
    )
}
```

---

## File: `ui/theme/Shape.kt`

```kotlin
package com.sylphy.player.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Shape philosophy:
//   0dp  — structural chrome: dividers, progress tracks, drag handles
//   2dp  — data chips: format badges, section labels, pill tags
//   4dp  — containers: cards, sheets, buttons, search bar
//   999dp — only for toggle switches (never for buttons or cards)
//
// The 4dp cap on containers distinguishes Sylphy from pure brutalism
// while keeping far away from the rounded-everything trend.
// It reads as "considered" rather than "aggressively square."

val SylphyShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),   // chips, badges
    small      = RoundedCornerShape(4.dp),   // buttons, inputs
    medium     = RoundedCornerShape(4.dp),   // cards, sheets
    large      = RoundedCornerShape(4.dp),   // bottom sheets
    extraLarge = RoundedCornerShape(4.dp),
)

// Direct access constants — use these instead of SylphyShapes.* in modifiers
val SharpCorner     = RoundedCornerShape(0.dp)
val ChipCorner      = RoundedCornerShape(2.dp)
val ContainerCorner = RoundedCornerShape(4.dp)
```

---

## File: `ui/theme/Spacing.kt`

```kotlin
package com.sylphy.player.ui.theme

import androidx.compose.ui.unit.dp

// Spacing scale: based on a 4dp base unit.
// Spacing is generous throughout — Vercel/shadcn-style negative space.

object Spacing {
    val px1  = 1.dp
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 16.dp
    val lg   = 24.dp
    val xl   = 32.dp
    val xxl  = 48.dp
    val xxxl = 64.dp
    val huge = 96.dp
}

// Layout constants
object Layout {
    // Navigation
    val topBarHeight      = 56.dp

    // Player
    val albumArtSize      = 280.dp
    val albumArtSizeSm    = 48.dp
    val progressRingStroke = 2.dp        // arc progress ring stroke width
    val seekBarHeight     = 44.dp        // tap target height
    val seekDotRadius     = 4.dp         // playhead dot
    val playButtonSize    = 64.dp        // square play/pause button
    val transportIconSize = 24.dp        // skip, shuffle, repeat icons
    val transportTapTarget = 44.dp       // minimum touch target

    // Library
    val trackRowHeight    = 64.dp
    val albumGridColumns  = 2            // album grid columns
    val sectionHeaderHeight = 32.dp

    // Border weights
    val borderThin   = 1.dp             // default structural border
    val borderThick  = 2.dp             // active/focus border

    // Queue
    val queueItemHeight = 64.dp
}
```

---

## File: `ui/theme/Easing.kt`

```kotlin
package com.sylphy.player.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

// Custom easing curves inspired by Vercel's motion design.
// Fast out of the gate, slow and deliberate at the end.
// Nothing overshoots or bounces — this is editorial, not playful.

object SylphyEasing {
    /** Default: fast entry, slow settle. Use for most transitions. */
    val Standard = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    /** Enter: elements coming onto screen */
    val Enter = CubicBezierEasing(0f, 0f, 0.2f, 1f)

    /** Exit: elements leaving screen — slightly faster */
    val Exit = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    /** Inert: slow, deliberate — for long content transitions */
    val Inert = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
}

// Duration constants (ms)
object Duration {
    const val Instant   = 100
    const val Fast      = 150
    const val Normal    = 250
    const val Slow      = 400
    const val Deliberate = 600
}
```

---

## File: `ui/theme/SylphyTheme.kt`

```kotlin
package com.sylphy.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SylphyColorScheme = darkColorScheme(
    primary          = White,
    onPrimary        = Black,
    primaryContainer = BgElevated,
    secondary        = FgSecondary,
    onSecondary      = Black,
    background       = BgBase,
    onBackground     = FgPrimary,
    surface          = BgElevated,
    onSurface        = FgPrimary,
    surfaceVariant   = BgElevated,
    onSurfaceVariant = FgSecondary,
    outline          = BorderDefault,
    outlineVariant   = FgGhost,
    error            = FgPrimary,     // errors are white — no red allowed
    onError          = Black,
)

@Composable
fun SylphyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SylphyColorScheme,
        shapes      = SylphyShapes,
        content     = content,
    )
}
```

---

## Active State System

The single most important pattern to understand:

```
INACTIVE state:
  background = BgBase (black)
  text       = FgPrimary (white)
  border     = BorderDefault (faint white)

ACTIVE / SELECTED state:
  background = White (fully inverted)
  text       = Black (fully inverted)
  border     = none (inversion provides enough contrast)
```

Implementation:
```kotlin
// Example: tab label
Text(
    text  = label,
    color = if (isSelected) ActiveForeground else FgSecondary,
    modifier = Modifier
        .background(
            color = if (isSelected) ActiveBackground else Color.Transparent,
            shape = ChipCorner,
        )
        .padding(horizontal = Spacing.md, vertical = Spacing.xs),
)

// Example: track row (currently playing)
Row(
    modifier = Modifier
        .background(if (isActive) ActiveBackground else Color.Transparent)
        .border(if (isActive) 0.dp else Layout.borderThin, BorderDefault)
) {
    Text(track.title, color = if (isActive) ActiveForeground else FgPrimary)
    Text(track.artist, color = if (isActive) Black.copy(alpha = 0.6f) else FgSecondary)
}
```

---

## Typography Hierarchy Rules

When two pieces of text share a row/column, create hierarchy **only** through:

1. **Size** — one size step difference is enough
2. **Weight** — Medium vs Normal is clearly legible
3. **Opacity** — FgPrimary vs FgSecondary vs FgMuted
4. **Font family** — GeistMono vs GeistSans (mono for data, sans for description)

Never create hierarchy through color. Never use uppercase for everything — uppercase is reserved for section headers and navigation labels only.

---

## Border Usage Rules

```kotlin
// Container (card, sheet, input) — thin default border
Modifier.border(Layout.borderThin, BorderDefault, ContainerCorner)

// Focused / hovered container — thicker border, no color change
Modifier.border(Layout.borderThick, BorderStrong, ContainerCorner)

// Selected item — full inversion (no border needed)
Modifier.background(ActiveBackground, ContainerCorner)

// Divider between list items — use Box or HorizontalDivider
HorizontalDivider(color = Separator, thickness = Layout.borderThin)

// Section separator — slightly more prominent
HorizontalDivider(color = BorderDefault, thickness = Layout.borderThin)
```

---

## Do / Do Not

```kotlin
// ✅ Express state through inversion
Box(Modifier.background(if (active) ActiveBackground else Color.Transparent))

// ❌ Never add color for state
Box(Modifier.background(if (active) Color(0xFFFF3B30) else Color.Transparent))

// ✅ Create depth through border weight
Modifier.border(if (focused) 2.dp else 1.dp, BorderDefault)

// ❌ Never create depth through shadow
Modifier.shadow(elevation = 8.dp, ...)  // NEVER

// ✅ Use opacity for hierarchy
Text(text, color = FgSecondary)  // 66% opacity

// ❌ Never use a new color for secondary text
Text(text, color = Color(0xFF888888))  // NEVER — use FgSecondary

// ✅ Generous whitespace
Modifier.padding(Spacing.xl)

// ❌ Cramped layout
Modifier.padding(Spacing.xs)
```
