package app.alquran.kareem

/**
 * A reader long-press must bookmark the EXACT printed page under the finger,
 * even when two numbered Quran pages share a landscape tablet spread.
 *
 * This changes only the device's saved page numbers, not any Quran content.
 */
internal object ReaderPageBookmarkRules {
    fun toggleAtTouch(
        existing: Set<Int>,
        visible: List<Int>,
        x: Float,
        viewportWidth: Float,
        totalPages: Int
    ): Set<Int> {
        val target = ReaderSpreadTapRules.tappedPage(x, viewportWidth, visible)
            ?: return existing
        if (target !in 1..totalPages) return existing
        return ReaderNavigationRules.toggleBookmark(existing, target, totalPages)
    }
}
