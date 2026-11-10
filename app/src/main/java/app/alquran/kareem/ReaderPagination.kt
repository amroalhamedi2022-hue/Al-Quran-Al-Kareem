package app.alquran.kareem

/**
 * Immutable page sequence for Hafs Madinah print layout.
 * Landscape displays [left, right] in physical left-to-right order,
 * with page 1 on the RIGHT of the first spread.
 * No Quran characters or images are generated here.
 */
internal object ReaderPagination {
    fun clamp(page: Int, total: Int): Int {
        require(total > 0)
        return page.coerceIn(1, total)
    }

    fun rightPage(page: Int, total: Int): Int {
        val safe = clamp(page, total)
        return ((safe - 1) / 2) * 2 + 1
    }

    fun visible(page: Int, total: Int, landscape: Boolean): List<Int> {
        if (!landscape) return listOf(clamp(page, total))
        val right = rightPage(page, total)
        return listOf(right + 1, right).filter { it <= total }
    }

    fun next(page: Int, total: Int, landscape: Boolean): Int =
        if (landscape) (rightPage(page, total) + 2).coerceAtMost(total)
        else (clamp(page, total) + 1).coerceAtMost(total)

    fun previous(page: Int, total: Int, landscape: Boolean): Int =
        if (landscape) (rightPage(page, total) - 2).coerceAtLeast(1)
        else (clamp(page, total) - 1).coerceAtLeast(1)

    fun hasNext(page: Int, total: Int, landscape: Boolean): Boolean =
        next(page, total, landscape) > (if (landscape) rightPage(page, total) else clamp(page, total))

    fun hasPrevious(page: Int, total: Int, landscape: Boolean): Boolean =
        previous(page, total, landscape) < (if (landscape) rightPage(page, total) else clamp(page, total))
}
