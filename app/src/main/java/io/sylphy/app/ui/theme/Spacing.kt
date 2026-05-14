package io.sylphy.app.ui.theme

// =============================================================================
// Sylphy — ui/theme/Spacing.kt
//
// Spacing system based on a 4dp base unit.
// Philosophy: generous whitespace is the design. When in doubt, add more.
// Vercel/shadcn-style negative space — breathing room is intentional.
//
// Usage rules:
//   - Always use Spacing.* tokens in padding/margin modifiers.
//   - NEVER hardcode a dp value like Modifier.padding(12.dp) — use the nearest token.
//   - If nothing fits exactly, go larger (not smaller) to maintain the airy feel.
//   - Spacing.px1 is for borders and dividers only — not for padding.
//
// Layout constants (Layout.*) are fixed measurements for recurring UI slots.
// They exist so that every screen that renders the same element (e.g., a track row)
// uses an identical height — no drift between screens over time.
//
// Agent notes:
//   - Do not add new Spacing.* values between existing steps.
//     The scale is intentionally sparse — gaps between values force generous layouts.
//   - Do not add new Layout.* values without updating this comment block.
//   - All Dp values here are read-only at compile time — no allocation at runtime.
// =============================================================================

import androidx.compose.ui.unit.dp

// ─── Spacing Scale ────────────────────────────────────────────────────────────
// Based on a 4dp base unit. Steps: 1, 4, 8, 16, 24, 32, 48, 64, 96.
// There is intentionally no 12dp or 20dp step — use the next step up.

object Spacing {
    /** 1dp — borders and hairline dividers only. Not for padding. */
    val px1  = 1.dp

    /** 4dp — minimum internal padding for very compact elements (chips, badges). */
    val xs   = 4.dp

    /** 8dp — tight padding: icon margins, small gaps between stacked elements. */
    val sm   = 8.dp

    /** 16dp — standard content padding: screen horizontal inset, list item vertical padding. */
    val md   = 16.dp

    /** 24dp — comfortable section spacing: gap between player controls rows. */
    val lg   = 24.dp

    /** 32dp — generous block spacing: gap between album art and track info. */
    val xl   = 32.dp

    /** 48dp — large section breaks: empty-state vertical centering padding. */
    val xxl  = 48.dp

    /** 64dp — extra-large: top/bottom padding for hero areas (player screen). */
    val xxxl = 64.dp

    /** 96dp — maximum: full-bleed hero spacing, ambient mode padding. */
    val huge = 96.dp
}

// ─── Layout Constants ─────────────────────────────────────────────────────────
// Fixed dimensions for named UI slots. Using named constants instead of raw dp
// values ensures all screens that render the same slot stay in sync.
//
// Agent: if you need to change a dimension, change it HERE — not at the call site.

object Layout {

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Height of the SylphyTabBar at the top of every primary screen. */
    val topBarHeight = 56.dp

    // ── Player Screen ─────────────────────────────────────────────────────────

    /** Square album art size on the player screen (main, not mini). */
    val albumArtSize = 280.dp

    /** Square album art thumbnail used in track rows, queue items, context menu header. */
    val albumArtSizeSm = 48.dp

    /** Stroke width for the arc progress ring around album art. */
    val progressRingStroke = 2.dp

    /**
     * Total tap-target height of the seek bar.
     * The visible line is much thinner; this extra height makes dragging comfortable.
     */
    val seekBarHeight = 44.dp

    /** Radius of the playhead dot on the seek bar and progress ring. */
    val seekDotRadius = 4.dp

    /** Width and height of the square play/pause button. */
    val playButtonSize = 64.dp

    /** Icon size inside skip, shuffle, and repeat buttons. */
    val transportIconSize = 24.dp

    /**
     * Minimum touch target for transport buttons (skip, shuffle, repeat).
     * The visible icon is transportIconSize; the tap target is always this value.
     */
    val transportTapTarget = 44.dp

    // ── Library Screen ────────────────────────────────────────────────────────

    /** Fixed height of a single track row (title + artist + duration). */
    val trackRowHeight = 64.dp

    /** Number of columns in the album grid view. */
    const val albumGridColumns = 2

    /** Fixed height of a sticky section header (alphabetical letter or group name). */
    val sectionHeaderHeight = 32.dp

    // ── Queue Screen ──────────────────────────────────────────────────────────

    /** Fixed height of a single item in the draggable queue list. */
    val queueItemHeight = 64.dp

    // ── Border Weights ────────────────────────────────────────────────────────
    // Border WEIGHT carries semantic meaning — do not mix these arbitrarily.
    //
    //   borderThin  (1dp) → standard structural container border, list separators
    //   borderThick (2dp) → focused / active container border
    //
    // Never use shadow for depth. These two weights are the only depth signals.

    /** 1dp — default structural border for containers, cards, and list separators. */
    val borderThin = 1.dp

    /** 2dp — focused or active container border (stronger visual weight). */
    val borderThick = 2.dp
}
