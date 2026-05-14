# Fonts Required - Geist by Vercel

The Android font resource directory must contain the following Geist font files
before the app can build.

## Download

https://vercel.com/font (click "Download Geist")

## Required Files

| File name (after renaming)       | Original name           |
|----------------------------------|-------------------------|
| `geist_mono_regular.ttf`         | `GeistMono-Regular.ttf` |
| `geist_mono_medium.ttf`          | `GeistMono-Medium.ttf`  |
| `geist_mono_bold.ttf`            | `GeistMono-Bold.ttf`    |
| `geist_sans_regular.ttf`         | `GeistSans-Regular.ttf` |
| `geist_sans_medium.ttf`          | `GeistSans-Medium.ttf`  |

## Naming Rule

Android resource names must be lowercase with underscores only. Rename every
downloaded file exactly as shown in the table above before placing it in
`app/src/main/res/font`.

## Why These Two Families?

- **Geist Mono** - all player UI, data labels, timestamps, counters, tab labels.
  It is the primary display face of Sylphy. Geometric, neutral, high-legibility at
  small sizes.
- **Geist Sans** - body copy, descriptions, secondary metadata, artist names in
  track rows, genre, and empty-state prose. Used wherever a proportional
  sans-serif reads better than a monospaced face.

## Verification

After placing the files, add a temporary composable in MainActivity and confirm
the font renders visibly differently from the system monospace:

```kotlin
Text(
    "Sylphy 01:23",
    style = TextStyle(
        fontFamily = GeistMono,
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
    ),
    color = Color.White,
)
```

Remove the test composable before committing.

## Agent Note

If `R.font.geist_mono_regular` fails to resolve at compile time, the `.ttf` files
are missing from `app/src/main/res/font`. The build will not proceed until all
five files are present and correctly named.
