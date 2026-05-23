package io.sylphy.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    primary              = Color(0xFF1A1814),
    onPrimary            = Color(0xFFF2F0EB),
    primaryContainer     = Color(0xFFE8E5DE),
    onPrimaryContainer   = Charcoal,
    secondary            = Color(0x99111111),
    onSecondary          = PaperWhite,
    background           = Color(0xFFF2F0EB),
    onBackground         = Color(0xFF1A1814),
    surface              = Color(0xFFE8E5DE),
    onSurface            = Color(0xFF1A1814),
    surfaceVariant       = Color(0xFFDEDAD2),
    onSurfaceVariant     = Color(0x99111111),
    outline              = Color(0xFFCCC8BF),
    outlineVariant       = Color(0xFFB8B3A8),
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

// ─── Player Specific Tokens ──────────────────────────────────────────────────

object PlayerTheme {
    val Black: Color @Composable get() = MaterialTheme.colorScheme.background
    val OffBlack: Color @Composable get() = if (isLight) Color(0xFFF0F0F0) else Color(0xFF0D0D0D)
    val Surface: Color @Composable get() = MaterialTheme.colorScheme.surface
    val Surface2: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val Border: Color @Composable get() = MaterialTheme.colorScheme.outline
    val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val White: Color @Composable get() = MaterialTheme.colorScheme.onBackground
    val WhiteDim: Color @Composable get() = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
    val Red: Color @Composable get() = if (isNothingOS) Color(0xFFFF3B3B) else MaterialTheme.colorScheme.primary
    val RedDim: Color @Composable get() = Red.copy(alpha = 0.18f)

    private val isLight: Boolean @Composable get() = MaterialTheme.colorScheme.background.luminance() > 0.5f
    private val isNothingOS: Boolean @Composable get() = MaterialTheme.colorScheme.primary == NothingRed
}

data class PlayerChromeColors(
    val bg: Color,
    val border2: Color,
    val muted: Color,
    val muted2: Color,
    val fg: Color,
    val fgDim: Color,
    val accent: Color,
    val progress: Color,
    val playBg: Color,
    val playFg: Color,
    val discOuter: Color,
    val groove: Color,
)

fun playerChromeColors(mode: ThemeMode): PlayerChromeColors {
    return when (mode) {
        ThemeMode.MONOCHROME_DARK -> PlayerChromeColors(
            bg = Color(0xFF0A0A0A),
            border2 = Color(0xFF2E2E2E),
            muted = Color(0xFF4A4A4A),
            muted2 = Color(0xFF666666),
            fg = Color(0xFFE8E8E8),
            fgDim = Color(0x80E8E8E8),
            accent = Color(0xFFE8E8E8),
            progress = Color(0xFFE8E8E8),
            playBg = Color(0xFFE8E8E8),
            playFg = Color(0xFF0A0A0A),
            discOuter = Color(0xFF0F0F0F),
            groove = Color(0x05FFFFFF),
        )
        ThemeMode.MONOCHROME_LIGHT -> PlayerChromeColors(
            bg = Color(0xFFF2F0EB),
            border2 = Color(0xFFB8B3A8),
            muted = Color(0xFF9B9690),
            muted2 = Color(0xFF7A756E),
            fg = Color(0xFF1A1814),
            fgDim = Color(0x731A1814),
            accent = Color(0xFF1A1814),
            progress = Color(0xFF1A1814),
            playBg = Color(0xFF1A1814),
            playFg = Color(0xFFF2F0EB),
            discOuter = Color(0xFFDEDAD2),
            groove = Color(0x0A1A1814),
        )
        ThemeMode.NOTHING_OS -> PlayerChromeColors(
            bg = Color(0xFF000000),
            border2 = Color(0xFF2A2A2A),
            muted = Color(0xFF555555),
            muted2 = Color(0xFF555555),
            fg = Color(0xFFF0F0F0),
            fgDim = Color(0x8CF0F0F0),
            accent = Color(0xFFFF3B3B),
            progress = Color(0xFFFF3B3B),
            playBg = Color(0xFFF0F0F0),
            playFg = Color(0xFF000000),
            discOuter = Color(0xFF141414),
            groove = Color(0x06FFFFFF),
        )
    }
}

data class QueueChromeColors(
    val bg: Color,
    val surface2: Color,
    val border: Color,
    val border2: Color,
    val muted: Color,
    val muted2: Color,
    val fg: Color,
    val fgDim: Color,
    val accent: Color,
    val accentDim: Color,
    val playingBg: Color,
    val playingBorder: Color,
    val dragHandle: Color,
    val removeColor: Color,
    val progressFill: Color,
)

fun queueChromeColors(mode: ThemeMode): QueueChromeColors {
    return when (mode) {
        ThemeMode.NOTHING_OS -> QueueChromeColors(
            bg = Color(0xFF000000),
            surface2 = Color(0xFF161616),
            border = Color(0xFF222222),
            border2 = Color(0xFF2C2C2C),
            muted = Color(0xFF444444),
            muted2 = Color(0xFF666666),
            fg = Color(0xFFF0F0F0),
            fgDim = Color(0x73F0F0F0),
            accent = Color(0xFFFF3B3B),
            accentDim = Color(0x1FFF3B3B),
            playingBg = Color(0x12FF3B3B),
            playingBorder = Color(0x38FF3B3B),
            dragHandle = Color(0xFF333333),
            removeColor = Color(0xFF444444),
            progressFill = Color(0xFFFF3B3B),
        )
        ThemeMode.MONOCHROME_DARK -> QueueChromeColors(
            bg = Color(0xFF0A0A0A),
            surface2 = Color(0xFF181818),
            border = Color(0xFF1E1E1E),
            border2 = Color(0xFF272727),
            muted = Color(0xFF3A3A3A),
            muted2 = Color(0xFF5A5A5A),
            fg = Color(0xFFE8E8E8),
            fgDim = Color(0x66E8E8E8),
            accent = Color(0xFFE8E8E8),
            accentDim = Color(0x0FE8E8E8),
            playingBg = Color(0x0DE8E8E8),
            playingBorder = Color(0x24E8E8E8),
            dragHandle = Color(0xFF2E2E2E),
            removeColor = Color(0xFF3A3A3A),
            progressFill = Color(0xFFE8E8E8),
        )
        ThemeMode.MONOCHROME_LIGHT -> QueueChromeColors(
            bg = Color(0xFFF2F0EB),
            surface2 = Color(0xFFE0DDD6),
            border = Color(0xFFCCC8BF),
            border2 = Color(0xFFB8B3A8),
            muted = Color(0xFFA09A92),
            muted2 = Color(0xFF7A756E),
            fg = Color(0xFF1A1814),
            fgDim = Color(0x661A1814),
            accent = Color(0xFF1A1814),
            accentDim = Color(0x0F1A1814),
            playingBg = Color(0x0D1A1814),
            playingBorder = Color(0x2E1A1814),
            dragHandle = Color(0xFFC0BBB3),
            removeColor = Color(0xFFB0ABA3),
            progressFill = Color(0xFF1A1814),
        )
    }
}

data class LibraryChromeColors(
    val bg: Color,
    val surface2: Color,
    val border: Color,
    val border2: Color,
    val muted: Color,
    val muted2: Color,
    val fg: Color,
    val fgDim: Color,
    val accent: Color,
    val accentDim: Color,
    val searchBg: Color,
    val searchBorder: Color,
    val rowHover: Color,
    val sectionLetter: Color,
    val playIndicator: Color,
)

fun libraryChromeColors(mode: ThemeMode): LibraryChromeColors {
    return when (mode) {
        ThemeMode.NOTHING_OS -> LibraryChromeColors(
            bg = Color(0xFF000000),
            surface2 = Color(0xFF1A1A1A),
            border = Color(0xFF242424),
            border2 = Color(0xFF2E2E2E),
            muted = Color(0xFF444444),
            muted2 = Color(0xFF666666),
            fg = Color(0xFFF0F0F0),
            fgDim = Color(0x80F0F0F0),
            accent = Color(0xFFFF3B3B),
            accentDim = Color(0x24FF3B3B),
            searchBg = Color(0xFF141414),
            searchBorder = Color(0xFF2A2A2A),
            rowHover = Color(0x08FFFFFF),
            sectionLetter = Color(0xFF333333),
            playIndicator = Color(0xFFFF3B3B),
        )
        ThemeMode.MONOCHROME_DARK -> LibraryChromeColors(
            bg = Color(0xFF0A0A0A),
            surface2 = Color(0xFF191919),
            border = Color(0xFF1E1E1E),
            border2 = Color(0xFF282828),
            muted = Color(0xFF3E3E3E),
            muted2 = Color(0xFF5E5E5E),
            fg = Color(0xFFE8E8E8),
            fgDim = Color(0x73E8E8E8),
            accent = Color(0xFFE8E8E8),
            accentDim = Color(0x12E8E8E8),
            searchBg = Color(0xFF141414),
            searchBorder = Color(0xFF252525),
            rowHover = Color(0x06FFFFFF),
            sectionLetter = Color(0xFF2A2A2A),
            playIndicator = Color(0xFFE8E8E8),
        )
        ThemeMode.MONOCHROME_LIGHT -> LibraryChromeColors(
            bg = Color(0xFFF2F0EB),
            surface2 = Color(0xFFE0DDD6),
            border = Color(0xFFCCC8BF),
            border2 = Color(0xFFB8B3A8),
            muted = Color(0xFFA09A92),
            muted2 = Color(0xFF7A756E),
            fg = Color(0xFF1A1814),
            fgDim = Color(0x661A1814),
            accent = Color(0xFF1A1814),
            accentDim = Color(0x121A1814),
            searchBg = Color(0xFFE8E5DE),
            searchBorder = Color(0xFFCCC8BF),
            rowHover = Color(0x0A1A1814),
            sectionLetter = Color(0xFFCCC8BF),
            playIndicator = Color(0xFF1A1814),
        )
    }
}
