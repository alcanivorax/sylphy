package io.sylphy.app.core.util

object MetadataCleaner {

    private val junkSuffixPatterns = listOf(
        Regex(
            """\s*\((?:official\s+)?(?:(?:music|lyric)\s+)?(?:video|audio|lyrics?|visualizer)\)""",
            RegexOption.IGNORE_CASE,
        ),
        Regex(
            """\s*\((?:hd|hq|4k|2160p|1080p|60fps|explicit|clean|official)\)""",
            RegexOption.IGNORE_CASE,
        ),
        Regex(
            """\s*\((?:prod\.?\s+.+?|produced\s+by\s+.+?)\)""",
            RegexOption.IGNORE_CASE,
        ),
        Regex(
            """\s*\((?:live|acoustic|remastered|radio\s+edit|single|cover\s+art|album\s+version)\)""",
            RegexOption.IGNORE_CASE,
        ),
        Regex(
            """\s*\[(?:official\s+)?(?:(?:music|lyric)\s+)?(?:video|audio|lyrics?|visualizer)\]""",
            RegexOption.IGNORE_CASE,
        ),
        Regex(
            """\s*\[(?:hd|hq|4k|2160p|1080p|60fps|explicit|clean|official)\]""",
            RegexOption.IGNORE_CASE,
        ),
        Regex("""\s*-\s*YouTube$""", RegexOption.IGNORE_CASE),
        Regex("""\s*\|.*$"""),
    )

    private val leadingPatterns = listOf(
        Regex("""^\d+\s*[.\-)]\s*"""),
        Regex("""^\d+\s+"""),
    )

    private val fileExtensionPattern =
        Regex("""\.(mp3|flac|wav|aac|ogg|wma|m4a|opus)$""", RegexOption.IGNORE_CASE)

    fun cleanTitle(title: String): String {
        var result = title.trim()

        for (pattern in leadingPatterns) {
            result = result.replaceFirst(pattern, "")
        }

        var previous: String
        do {
            previous = result
            for (pattern in junkSuffixPatterns) {
                result = result.replace(pattern, "")
            }
            result = result.trim()
        } while (result != previous)

        result = result.replace(fileExtensionPattern, "")

        result = result.replace(Regex("""\s+"""), " ").trim()

        return result.ifBlank { title.trim() }
    }

    fun cleanArtist(artist: String): String {
        val cleaned = artist.trim()
            .replace(Regex("""\s*\((?:official)\)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
        return cleaned.ifBlank { artist.trim() }
    }
}
