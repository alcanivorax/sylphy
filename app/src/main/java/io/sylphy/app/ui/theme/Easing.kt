package io.sylphy.app.ui.theme

// =============================================================================
// Sylphy — ui/theme/Easing.kt
//
// Motion philosophy: purposeful and restrained.
// Motion communicates state change and spatial orientation — it never entertains.
//
// Rules:
//   - Fast out of the gate, slow and deliberate at the end.
//   - Nothing overshoots or bounces — Standard is the workhorse.
//   - Duration scales with distance: small movements are fast, screen transitions slow.
//   - All animations must respect LocalReduceMotion (see note below).
//   - Use tween() with these easings for most transitions.
//   - Use spring() only for press/release feedback (PlayButton scale) — never for layout.
//
// Agent notes:
//   - CubicBezierEasing(0.16, 1, 0.3, 1) is modelled after Vercel's "ease-out-expo".
//     It accelerates instantly and decelerates smoothly — the opposite of ease-in-out.
//   - Never use LinearEasing for visible UI transitions (only for progress bar interpolation).
//   - Never use BounceEasing or OvershootEasing — this is editorial, not playful.
//   - When in doubt about which easing to use: Standard.
//   - Duration.Instant (100ms) is for micro-feedback like button press scale.
//     Duration.Fast (150ms) is for icon swaps and state changes.
//     Duration.Normal (250ms) is for most composable enter/exit transitions.
//     Duration.Slow (400ms) is for full-screen transitions and sheet reveals.
//     Duration.Deliberate (600ms) is for stat number count-up animations.
// =============================================================================

import androidx.compose.animation.core.CubicBezierEasing

// ─── Easing Curves ────────────────────────────────────────────────────────────

object SylphyEasing {

    /**
     * Standard — the default easing for all UI transitions in Sylphy.
     *
     * Modelled after Vercel's motion curve. Accelerates immediately, then
     * decelerates into a slow, deliberate settle. Nothing overshoots.
     *
     * Use for: tab-bar pill animation, screen enter transitions,
     *          seek bar position updates, most animateXAsState calls.
     *
     * Bezier: fast out (steep start), slow settle (nearly flat end).
     * Equivalent to CSS: cubic-bezier(0.16, 1, 0.3, 1)
     */
    val Standard = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    /**
     * Enter — for elements arriving on screen.
     *
     * Starts at zero velocity and accelerates toward a hard stop.
     * Creates a feeling of the element "landing" rather than flying in.
     *
     * Use for: NavHost enterTransition, LazyColumn item appearance,
     *          bottom sheet reveal, album art crossfade in.
     *
     * Equivalent to CSS: cubic-bezier(0, 0, 0.2, 1)  ("ease-out")
     */
    val Enter = CubicBezierEasing(0f, 0f, 0.2f, 1f)

    /**
     * Exit — for elements leaving screen. Slightly faster than Enter.
     *
     * Accelerates immediately and exits at high velocity — the element
     * "disappears" rather than fading leisurely.
     *
     * Use for: NavHost exitTransition, dialog dismiss, sheet drag dismiss.
     *
     * Equivalent to CSS: cubic-bezier(0.4, 0, 1, 1)  ("ease-in")
     */
    val Exit = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    /**
     * Inert — slow, deliberate curve for long content transitions.
     *
     * Neither fast nor dramatically decelerating. Used when the
     * transition itself carries meaning and the user should observe it.
     *
     * Use for: stat number count-up (Duration.Deliberate),
     *          waveform reveal, heatmap fill animation.
     *
     * Equivalent to CSS: cubic-bezier(0.65, 0, 0.35, 1)  ("ease-in-out" variant)
     */
    val Inert = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
}

// ─── Duration Constants ───────────────────────────────────────────────────────
// Integer millisecond values — used as tween(durationMillis = Duration.Normal, …).
// Agent: use these constants, never raw integers like tween(250).
// That way a global timing adjustment only requires changing one file.

object Duration {

    /**
     * Instant — 100ms.
     * Use for: play button press scale, icon state swap on tap,
     *          any micro-interaction that is felt more than seen.
     */
    const val Instant = 100

    /**
     * Fast — 150ms.
     * Use for: exitTransition in NavHost, icon crossfade,
     *          seek bar position snap after drag release.
     */
    const val Fast = 150

    /**
     * Normal — 250ms.
     * Use for: most composable enter transitions, tab-bar pill slide,
     *          animateFloatAsState for progress ring, modal fade-in.
     * This is the default duration — when unsure, use Normal.
     */
    const val Normal = 250

    /**
     * Slow — 400ms.
     * Use for: full-screen screen transitions, bottom sheet slide-up,
     *          album art crossfade, content list swap.
     */
    const val Slow = 400

    /**
     * Deliberate — 600ms.
     * Use for: stat number count-up animations (AnimatedStatNumber),
     *          waveform bar fill on track load.
     * Only use when the animation itself conveys a sense of scale or passage of time.
     */
    const val Deliberate = 600
}
