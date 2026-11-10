package app.alquran.kareem

/**
 * Pure, locally testable navigation rules. These never read, write,
 * reconstruct or transform Quran artwork or any Quranic text.
 */
internal object ReaderNavigationRules {
    /** Accepts Western and Arabic-Indic decimal page numbers only. */
    fun parsePage(input: String, pageCount: Int): Int? {
        if (pageCount < 1) return null
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed.length > 6) return null
        val digits = buildString(trimmed.length) {
            for (char in trimmed) {
                val value = when (char) {
                    in '0'..'9' -> char - '0'
                    in '\u0660'..'\u0669' -> char - '\u0660'
                    in '\u06F0'..'\u06F9' -> char - '\u06F0'
                    else -> return null
                }
                append(('0'.code + value).toChar())
            }
        }
        return digits.toIntOrNull()?.takeIf { it in 1..pageCount }
    }

    /** All persistent bookmark pages must be within the verified source count. */
    fun sanitizedPages(candidates: Iterable<Int>, pageCount: Int): Set<Int> {
        require(pageCount > 0)
        return candidates.filter { it in 1..pageCount }.toSortedSet()
    }

    fun toggleBookmark(existing: Set<Int>, page: Int, pageCount: Int): Set<Int> {
        require(page in 1..pageCount)
        val clean = sanitizedPages(existing, pageCount).toMutableSet()
        if (!clean.add(page)) clean.remove(page)
        return clean.toSortedSet()
    }
}
