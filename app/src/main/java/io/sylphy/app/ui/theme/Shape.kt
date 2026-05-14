package io.sylphy.app.ui.theme

// =============================================================================
// Sylphy — ui/theme/Shape.kt
//
// Shape philosophy:
//   0dp  — structural chrome: dividers, progress tracks, drag handles,
//           any element that should read as a hard edge or line
//   2dp  — data chips: format badges (FLAC, HI-RES), section labels,
//           pill tags, the tab-bar inverted pill indicator
//   4dp  — containers: cards, bottom sheets, buttons, search bar,
//           album art, context menu rows
//   999dp — reserved for toggle switches only; never use for buttons,
//           cards, or any element in the player UI
//
// The 4dp cap distinguishes Sylphy from pure brutalism while staying
// far from the "rounded everything" trend. It reads as "considered"
// rather than "aggressively square."
//
// Agent rules:
//   - Never set cornerRadius > 4dp on any card, button, or container.
//   - Never use RoundedCornerShape(50%) or CircleShape for buttons.
//   - The only 999dp shape is for toggle switches (not yet implemented in Phase 1).
//   - Bottom sheets use RoundedCornerShape(topStart=8.dp, topEnd=8.dp) as an
//     intentional exception — see ContextMenuSheet.kt for the rationale.
//   - Album art corners: ContainerCorner (4dp). Not circular, not square.
//   - Use the direct constants (SharpCorner, ChipCorner, ContainerCorner) in
//     Modifier.background() and Modifier.border() calls — they are more readable
//     than reaching into SylphyShapes.*.
// =============================================================================

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ─── Material3 Shapes integration ────────────────────────────────────────────
// SylphyShapes is passed to MaterialTheme(shapes = SylphyShapes) in SylphyTheme.kt.
// This ensures any Material3 component that internally uses MaterialTheme.shapes.*
// respects the Sylphy corner radius system rather than Material's opinionated defaults.
//
// All size buckets map to 4dp because the design system uses a single container
// corner radius. The only exceptions (0dp and 2dp) are handled via direct constants
// below rather than via the Material shape system.

val SylphyShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),   // chips, badges, tab-pill indicator
    small      = RoundedCornerShape(4.dp),   // buttons, input fields, search bar
    medium     = RoundedCornerShape(4.dp),   // cards, album art, track rows
    large      = RoundedCornerShape(4.dp),   // bottom sheets (sides), popovers
    extraLarge = RoundedCornerShape(4.dp),   // full-screen modal surfaces
)

// ─── Direct Constants ─────────────────────────────────────────────────────────
// Use these in Modifier.background(color, shape) and Modifier.border(w, c, shape)
// rather than referencing SylphyShapes.* — intent is clearer at the call site.

/**
 * SharpCorner — 0dp, fully square.
 * Use for: dividers, progress track backgrounds, drag handles,
 *          any structural chrome that must read as a hard edge.
 */
val SharpCorner = RoundedCornerShape(0.dp)

/**
 * ChipCorner — 2dp, subtly softened square.
 * Use for: format badges ("FLAC", "HI-RES"), section pill labels,
 *          the sliding tab-bar indicator pill, small data tags.
 */
val ChipCorner = RoundedCornerShape(2.dp)

/**
 * ContainerCorner — 4dp, the standard Sylphy container corner.
 * Use for: buttons, search bar, album artwork, cards, bottom sheets
 *          (non-top corners), any surface that holds content.
 * This is the most-used shape constant in the codebase.
 */
val ContainerCorner = RoundedCornerShape(4.dp)

/**
 * BottomSheetCorner — 8dp top corners only, 0dp bottom corners.
 * Use for: ContextMenuSheet and any ModalBottomSheet surface.
 * The larger radius on top reads naturally as a "lifted" tray.
 * Bottom corners are flush with the screen edge — do not round them.
 */
val BottomSheetCorner = RoundedCornerShape(
    topStart    = 8.dp,
    topEnd      = 8.dp,
    bottomStart = 0.dp,
    bottomEnd   = 0.dp,
)
