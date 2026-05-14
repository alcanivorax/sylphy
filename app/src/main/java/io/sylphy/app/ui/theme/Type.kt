package io.sylphy.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: replace with R.font.* once Geist TTF files are placed in res/font/
// See app/src/main/res/font/FONTS_REQUIRED.md for download instructions.

//val GeistMono = FontFamily(
//    Font(R.font.geist_mono_regular, FontWeight.Normal),
//    Font(R.font.geist_mono_medium,  FontWeight.Medium),
//    Font(R.font.geist_mono_bold,    FontWeight.Bold),
//)
//
//val GeistSans = FontFamily(
//    Font(R.font.geist_sans_regular, FontWeight.Normal),
//    Font(R.font.geist_sans_medium,  FontWeight.Medium),
//)

val GeistMono = FontFamily.Monospace
val GeistSans = FontFamily.SansSerif

object SylphyType {

    val DisplayLarge = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 34.sp,
        letterSpacing = (-0.5).sp,
    )

    val Display = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Medium,
        fontSize      = 20.sp,
        lineHeight    = 26.sp,
        letterSpacing = (-0.3).sp,
    )

    // Screen headers, tab labels, button text
    val Heading = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.8.sp,
    )

    // Timestamps, durations, counters
    val Code = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp,
    )

    // Format badges, micro-labels, seek bar timestamps
    val CodeSmall = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 15.sp,
        letterSpacing = 0.2.sp,
    )

    val Clock = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Bold,
        fontSize      = 72.sp,
        lineHeight    = 80.sp,
        letterSpacing = (-3).sp,
    )

    val Stat = TextStyle(
        fontFamily    = GeistMono,
        fontWeight    = FontWeight.Bold,
        fontSize      = 40.sp,
        lineHeight    = 46.sp,
        letterSpacing = (-1).sp,
    )

    val Body = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = (-0.1).sp,
    )

    val BodySmall = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 17.sp,
        letterSpacing = 0.sp,
    )

    val Caption = TextStyle(
        fontFamily    = GeistSans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 15.sp,
        letterSpacing = 0.sp,
    )
}
