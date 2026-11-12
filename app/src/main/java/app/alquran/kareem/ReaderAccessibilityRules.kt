package app.alquran.kareem

/**
 * TalkBack speaks page metadata, not reconstructed Quran text.
 * Screen reader descriptions are composed only from verified page numbers,
 * local bookmark metadata and the reader's current zoom.
 */
internal object ReaderAccessibilityRules {
    fun status(
        visiblePages: List<Int>,
        totalPages: Int,
        bookmarks: Set<Int>,
        zoomScale: Float
    ): String {
        require(totalPages > 0)
        val pages = visiblePages.filter { it in 1..totalPages }.distinct().sorted()
        val location = when (pages.size) {
            0 -> "لا توجد صفحة معروضة"
            1 -> "الصفحة ${pages.first()} من ${totalPages}"
            else -> "الصفحتان ${pages.first()} و${pages.last()} من ${totalPages}"
        }
        val marked = pages.filter { it in bookmarks }
        val bookmarkStatus = when (marked.size) {
            0 -> "لا توجد علامة على الصفحات المعروضة"
            1 -> "علامة محفوظة للصفحة ${marked.first()}"
            else -> "الصفحتان محفوظتان في العلامات"
        }
        val zoomPercent = (ReaderZoomRules.normalize(zoomScale) * 100).toInt()
        return "${location}. ${bookmarkStatus}. التكبير ${zoomPercent} بالمئة."
    }
}
