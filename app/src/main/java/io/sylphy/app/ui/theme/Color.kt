package io.sylphy.app.ui.theme

import androidx.compose.ui.graphics.Color

// Base values — near-black/white to reduce OLED harshness
val Black = Color(0xFF0A0A0A)
val White = Color(0xFFFAFAFA)

// Foreground opacity scale
val FgPrimary   = Color(0xFFFAFAFA) // 100%
val FgSecondary = Color(0xA8FAFAFA) // 66%
val FgMuted     = Color(0x66FAFAFA) // 40%
val FgSubtle    = Color(0x33FAFAFA) // 20%
val FgGhost     = Color(0x14FAFAFA) // 8%

// Background surfaces
val BgBase     = Color(0xFF0A0A0A)
val BgElevated = Color(0xFF111111)
val BgSunken   = Color(0xFF060606)
val BgInverted = Color(0xFFFAFAFA)

// Borders — weight carries meaning: 1dp standard, 2dp active/focused
val BorderDefault  = Color(0x1FFAFAFA) // 12%
val BorderStrong   = Color(0x3DFAFAFA) // 24%
val BorderInverted = Color(0xFFFAFAFA) // 100%

// Semantic aliases
val Background    = BgBase
val Surface       = BgElevated
val TextPrimary   = FgPrimary
val TextSecondary = FgSecondary
val TextMuted     = FgMuted
val Accent        = FgPrimary
val Separator     = FgGhost

// Seek bar / progress ring
val ProgressFilled   = FgPrimary
val ProgressEmpty    = FgSubtle
val ProgressPlayhead = FgPrimary

// Active state — inversion is the only selection language; no color ever
val ActiveBackground = White
val ActiveForeground = Black
