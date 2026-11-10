package app.alquran.kareem

/** Pure policy to keep full-size Quran artwork within a small memory budget. */
internal object QuranPageCachePolicy {
    const val PAGE_WIDTH = 1600
    const val PAGE_HEIGHT = 2287
    fun acceptsDimensions(width: Int, height: Int) =
        width == PAGE_WIDTH && height == PAGE_HEIGHT

    fun budgetBytes(heapMemoryClassMb: Int): Int {
        require(heapMemoryClassMb > 0)
        return (heapMemoryClassMb.toLong() * 1024L * 1024L / 5L)
            .coerceIn(16L * 1024 * 1024, 44L * 1024 * 1024)
            .toInt()
    }
}
