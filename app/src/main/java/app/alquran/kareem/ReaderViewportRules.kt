package app.alquran.kareem

/**
 * Only determines layout; it never resizes or edits the original Quran page data.
 * A double-page spread is useful on a landscape tablet, but unreadable
 * on a narrow phone held horizontally.
 */
internal object ReaderViewportRules {
    fun twoPageSpread(isLandscape: Boolean, widthDp: Int): Boolean =
        isLandscape && widthDp >= 700
}
