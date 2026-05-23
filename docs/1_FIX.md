Files That Need Changes
1. ui/theme/Spacing.kt — Add one constant
QueueScreen.kt calls SylphyDivider with a start padding of:
kotlinModifier.padding(start = Spacing.md + Layout.albumArtSizeSm + Spacing.sm)
That arithmetic is fine at the call site, but Layout is missing a named constant for the inset divider start position. Not strictly required — the math works — but if you want to stay consistent with the comment in Spacing.kt ("change it HERE, not at the call site"), add:
kotlin// ── Queue Screen ──────────────────────────────────────────────────────────
/** Left inset for queue row dividers — aligns with text column start. */
val queueDividerInset = albumArtSizeSm + Spacing.md + Spacing.sm  // 48 + 16 + 8 = 72dp
Then the call site becomes Modifier.padding(start = Layout.queueDividerInset). Low priority — either way compiles and renders identically.

2. res/drawable/ic_remove.xml — Create
The QueueRowItem references R.drawable.ic_remove. You need a vector drawable. Minimal version:
xml<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:strokeColor="@color/white"
        android:strokeWidth="2"
        android:strokeLineCap="round"
        android:pathData="M18,6 L6,18 M6,6 L18,18"/>
</vector>
If you already have a close/dismiss icon under another name (ic_close, ic_dismiss, ic_cancel) — just swap the reference in QueueRowItem. No new file needed.

3. ui/components/shared/SylphyDivider.kt — Verify signature
QueueScreen.kt calls:
kotlinSylphyDivider(modifier = Modifier.padding(start = ...))
The original QueueScreen.kt called SylphyDivider() with no arguments. If your current SylphyDivider doesn't accept a modifier parameter, add it:
kotlin@Composable
fun SylphyDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = Layout.borderThin,
        color = BorderDefault,
    )
}
If it already accepts modifier, no change needed.

4. ui/components/shared/EmptyState.kt — Verify signature
QueueScreen.kt reuses the same EmptyState call from the original:
kotlinEmptyState(
    title = "Queue empty",
    description = "Play a track from Library to start a queue.",
    modifier = Modifier.align(Alignment.Center),
)
This was already in the original QueueScreen.kt so it almost certainly already matches. No change expected — just verify it exists with those three parameters.
