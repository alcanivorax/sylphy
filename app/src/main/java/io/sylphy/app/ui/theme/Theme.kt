package io.sylphy.app.ui.theme

// =============================================================================
// Sylphy — ui/theme/Theme.kt  (alias: SylphyTheme.kt)
//
// Wraps Material3's MaterialTheme with the Sylphy monochrome color scheme,
// Geist-based typography stub, and systematic shape tokens.
//
// Design contract:
//   - The color scheme is ALWAYS the dark monochrome scheme defined below.
//     There is no light variant, no dynamic color, no system-color override.
//   - Dynamic color (Android 12+ Monet) is explicitly disabled — it would
//     introduce chromatic hues that violate the monochrome constraint.
//   - MaterialTheme.typography is intentionally left as the default Material
//     typography because the app always uses SylphyType.* directly in Text()
//     calls. Wiring GeistMono into Material's named slots (titleLarge, etc.)
//     would be misleading since those slots are not used.
//   - MaterialTheme.shapes maps to SylphyShapes so that any Material3 component
//     (Button, Card, TextField, BottomSheet) that internally reads MaterialTheme.shapes
//     respects the 4dp ContainerCorner cap.
//
// Agent rules:
//   - Do NOT add a lightColorScheme branch. Ever.
//   - Do NOT enable dynamicColor. Ever.
//   - Do NOT add chromatic colors to SylphyColorScheme. Not for errors, not for
//     "just a subtle tint." White and black. That is all.
//   - The error color maps to FgPrimary (white). Errors are expressed through
//     typography hierarchy and descriptive text, not red pixels.
//   - If a Material3 component renders with unexpected color, override its
//     individual color parameters at the call site — do not change this scheme.
// =============================================================================

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Monochrome Color Scheme ──────────────────────────────────────────────────
// Every slot that Material3 defines is explicitly mapped to a monochrome token.
// Nothing is left at the Material default (which would introduce purple/teal).
//
// Mapping rationale:
//   primary / onPrimary       → White / Black  (the inverted action state)
//   primaryContainer          → BgElevated     (slightly lifted surface)
//   secondary / onSecondary   → FgSecondary / Black  (supporting content)
//   background / onBackground → BgBase / FgPrimary   (root screen)
//   surface / onSurface       → BgElevated / FgPrimary  (elevated surfaces)
//   surfaceVariant            → BgElevated  (same — no chromatic variant)
//   outline                   → BorderDefault  (structural borders)
//   outlineVariant            → FgGhost        (subtle separators)
//   error / onError           → FgPrimary / Black  (errors = same white text)
//   scrim                     → Black at 70%   (modal backdrop)

private val SylphyColorScheme = darkColorScheme(
    // ── Primary (the "action" role in Material3) ──────────────────────────────
    // In Sylphy there is no chromatic accent. The "action" color is white — the
    // inverted state. Any Material component that reads primary will render white.
    primary                = White,
    onPrimary              = Black,
    primaryContainer       = BgElevated,
    onPrimaryContainer     = FgPrimary,
    inversePrimary         = Black,

    // ── Secondary (supporting / metadata role) ────────────────────────────────
    secondary              = FgSecondary,
    onSecondary            = Black,
    secondaryContainer     = BgElevated,
    onSecondaryContainer   = FgSecondary,

    // ── Tertiary (intentionally same as secondary — no third hue) ────────────
    tertiary               = FgMuted,
    onTertiary             = Black,
    tertiaryContainer      = BgElevated,
    onTertiaryContainer    = FgMuted,

    // ── Error (no red — errors expressed through text hierarchy) ─────────────
    // If a Material3 component tries to render an error state in red, this
    // mapping ensures it stays white instead.
    error                  = FgPrimary,
    onError                = Black,
    errorContainer         = BgElevated,
    onErrorContainer       = FgPrimary,

    // ── Background / Surface ──────────────────────────────────────────────────
    background             = BgBase,
    onBackground           = FgPrimary,
    surface                = BgElevated,
    onSurface              = FgPrimary,
    surfaceVariant         = BgElevated,
    onSurfaceVariant       = FgSecondary,
    surfaceTint            = Color.Transparent,  // disable Material3 surface tinting

    // ── Inverse (used by Snackbar, tooltip surfaces) ──────────────────────────
    inverseSurface         = White,
    inverseOnSurface       = Black,

    // ── Outline (borders) ─────────────────────────────────────────────────────
    outline                = BorderDefault,      // standard structural border
    outlineVariant         = FgGhost,            // subtle list divider

    // ── Scrim (modal backdrop) ────────────────────────────────────────────────
    scrim                  = Black,              // opacity applied at usage site (.copy(0.7f))
)

// ─── SylphyTheme ─────────────────────────────────────────────────────────────

/**
 * Root theme composable for the entire Sylphy app.
 *
 * Apply at the top of the composition tree (in MainActivity.setContent).
 * Every screen and composable below this call has access to:
 *   - MaterialTheme.colorScheme  → SylphyColorScheme (monochrome)
 *   - MaterialTheme.shapes       → SylphyShapes (4dp cap)
 *   - SylphyType.*               → direct access (not via MaterialTheme.typography)
 *
 * Edge-to-edge insets are handled by MainActivity.enableEdgeToEdge() — this
 * composable does not set window insets. The status bar icon colour is
 * controlled by the XML theme (windowLightStatusBar = false).
 *
 * @param content The composable content subtree to theme.
 */
@Composable
fun SylphyTheme(content: @Composable () -> Unit) {
    // Apply status bar appearance after the view is attached.
    // WindowCompat.getInsetsController is the modern API (no deprecated WindowInsetsController).
    // We force light icons (isAppearanceLightStatusBars = false) on the near-black background.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            // Ensure the status bar icon tint is white (light icons on dark background).
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false   // white icons
                isAppearanceLightNavigationBars = false   // white nav icons
            }
        }
    }

    MaterialTheme(
        colorScheme = SylphyColorScheme,
        shapes      = SylphyShapes,
        // Note: typography is intentionally NOT overridden here.
        // All text in the app uses SylphyType.* directly to avoid any ambiguity
        // about which Material typography slot maps to which Geist style.
        content     = content,
    )
}
