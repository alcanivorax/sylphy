package io.sylphy.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.sylphy.app.data.model.ThemeMode

// ─── Base Palette ───────────────────────────────────────────────────────────

// Monochrome Dark / Nothing OS Base
val Black       = Color(0xFF0A0A0A)
val OLEDBlack   = Color(0xFF000000)
val White       = Color(0xFFFAFAFA)
val PureWhite   = Color(0xFFFFFFFF)

// Monochrome Light Base
val PaperWhite  = Color(0xFFFDFDFD)
val Charcoal    = Color(0xFF111111)

// Nothing OS Accent
val NothingRed  = Color(0xFFFF0031)

// ─── Theme Schemes ──────────────────────────────────────────────────────────

/**
 * Monochrome Dark Scheme
 * Deep charcoal black, clean grayscale hierarchy.
 */
val MonochromeDarkScheme = darkColorScheme(
    primary              = White,
    onPrimary            = Black,
    primaryContainer     = Color(0xFF1A1A1A),
    onPrimaryContainer   = White,
    secondary            = Color(0xFFA8FAFAFA),
    onSecondary          = Black,
    background           = Black,
    onBackground         = White,
    surface              = Color(0xFF111111),
    onSurface            = White,
    surfaceVariant       = Color(0xFF1A1A1A),
    onSurfaceVariant     = Color(0xFFA8FAFAFA),
    outline              = Color(0xFF222222),
    outlineVariant       = Color(0xFF1A1A1A),
    scrim                = Color.Black,
)

/**
 * Monochrome Light Scheme
 * Warm white, airy, dark charcoal text.
 */
val MonochromeLightScheme = lightColorScheme(
    primary              = Charcoal,
    onPrimary            = PaperWhite,
    primaryContainer     = Color(0xFFF0F0F0),
    onPrimaryContainer   = Charcoal,
    secondary            = Color(0x99111111),
    onSecondary          = PaperWhite,
    background           = PaperWhite,
    onBackground         = Charcoal,
    surface              = Color(0xFFF5F5F5),
    onSurface            = Charcoal,
    surfaceVariant       = Color(0xFFEEEEEE),
    onSurfaceVariant     = Color(0x99111111),
    outline              = Color(0xFFDDDDDD),
    outlineVariant       = Color(0xFFEEEEEE),
    scrim                = Color.Black,
)

/**
 * Nothing OS Scheme
 * Pure OLED black, industrial red accents, white typography.
 */
val NothingOSScheme = darkColorScheme(
    primary              = NothingRed,
    onPrimary            = OLEDBlack,
    primaryContainer     = Color(0xFF111111),
    onPrimaryContainer   = PureWhite,
    secondary            = PureWhite,
    onSecondary          = OLEDBlack,
    background           = OLEDBlack,
    onBackground         = PureWhite,
    surface              = Color(0xFF080808),
    onSurface            = PureWhite,
    surfaceVariant       = Color(0xFF111111),
    onSurfaceVariant     = PureWhite,
    outline              = Color(0xFF1A1A1A),
    outlineVariant       = Color(0xFF080808),
    scrim                = Color.Black,
)

// ─── Theme Resolver ─────────────────────────────────────────────────────────

fun getSylphyColorScheme(mode: ThemeMode): ColorScheme {
    return when (mode) {
        ThemeMode.MONOCHROME_DARK  -> MonochromeDarkScheme
        ThemeMode.MONOCHROME_LIGHT -> MonochromeLightScheme
        ThemeMode.NOTHING_OS       -> NothingOSScheme
    }
}

// ─── Semantic Aliases ───────────────────────────────────────────────────────

val BgBase: Color @Composable get() = MaterialTheme.colorScheme.background
val BgElevated: Color @Composable get() = MaterialTheme.colorScheme.surface
val BgSunken: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val BgInverted: Color @Composable get() = MaterialTheme.colorScheme.primary

val FgPrimary: Color @Composable get() = MaterialTheme.colorScheme.onBackground
val FgSecondary: Color @Composable get() = MaterialTheme.colorScheme.secondary
val FgMuted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val FgSubtle: Color @Composable get() = MaterialTheme.colorScheme.outline
val FgGhost: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant

val BorderDefault: Color @Composable get() = MaterialTheme.colorScheme.outline
val BorderStrong: Color @Composable get() = MaterialTheme.colorScheme.primary

val ActiveBackground: Color @Composable get() = MaterialTheme.colorScheme.primary
val ActiveForeground: Color @Composable get() = MaterialTheme.colorScheme.onPrimary

// Semantic aliases for components
val ProgressFilled: Color @Composable get() = MaterialTheme.colorScheme.primary
val ProgressEmpty: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val ProgressPlayhead: Color @Composable get() = MaterialTheme.colorScheme.primary
