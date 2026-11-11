package app.alquran.kareem

/**
 * Prefetch only if the device can keep visible full-resolution pages AND one
 * neighbor in its bounded LRU cache. The source artwork itself stays untouched.
 */
internal object ReaderPrefetchRules {
    const val PAGE_BYTES: Long =
        QuranPageCachePolicy.PAGE_WIDTH.toLong() * QuranPageCachePolicy.PAGE_HEIGHT * 4L

    fun target(
        currentPage: Int,
        pageCount: Int,
        twoPageSpread: Boolean,
        cacheBudgetBytes: Int
    ): Int? {
        if (pageCount < 1 || currentPage !in 1..pageCount) return null
        val visible = ReaderPagination.visible(currentPage, pageCount, twoPageSpread)
        val required = (visible.size + 1L) * PAGE_BYTES
        if (cacheBudgetBytes.toLong() < required) return null
        val forward = ReaderPagination.next(currentPage, pageCount, twoPageSpread)
        if (forward !in visible) return forward
        val backward = ReaderPagination.previous(currentPage, pageCount, twoPageSpread)
        return backward.takeUnless { it in visible }
    }
}
