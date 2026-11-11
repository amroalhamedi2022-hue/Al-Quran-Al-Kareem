package app.alquran.kareem

/** Pure manual khatma-progress rules: never infer reading from opening a page. */
internal object KhatmaProgressRules {
    fun sanitize(marks: Iterable<Int>, count: Int): Set<Int> {
        require(count > 0)
        return marks.filter { it in 1..count }.toSortedSet()
    }

    /** Toggle precisely the pages currently shown, only on an explicit tap. */
    fun toggleVisible(existing: Set<Int>, visiblePages: List<Int>, total: Int): Set<Int> {
        require(total > 0)
        require(visiblePages.isNotEmpty())
        require(visiblePages.distinct().size == visiblePages.size)
        require(visiblePages.all { it in 1..total })
        val result = sanitize(existing, total).toMutableSet()
        if (visiblePages.all { it in result }) {
            result.removeAll(visiblePages.toSet())
        } else {
            result.addAll(visiblePages)
        }
        return result.toSortedSet()
    }

    fun fraction(marks: Iterable<Int>, total: Int): Float {
        require(total > 0)
        return sanitize(marks, total).size.toFloat() / total
    }
}
