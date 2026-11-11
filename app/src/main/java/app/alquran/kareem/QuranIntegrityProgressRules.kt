package app.alquran.kareem

/**
 * Limit UI progress events during the full 604-page publisher hash check.
 * This does not skip verification of even a single Quran source byte.
 */
internal object QuranIntegrityProgressRules {
    const val PAGE_COUNT = 604
    const val REPORT_EVERY = 16

    fun shouldReport(verifiedPage: Int, totalPages: Int = PAGE_COUNT): Boolean {
        if (totalPages <= 0 || verifiedPage !in 1..totalPages) return false
        return verifiedPage == 1 ||
            verifiedPage == totalPages ||
            verifiedPage % REPORT_EVERY == 0
    }
}
