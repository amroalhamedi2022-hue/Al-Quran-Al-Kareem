package app.alquran.kareem

/**
 * Main-thread navigation transaction ID.
 *
 * When a user jumps to a specific page or changes orientation while a paper
 * animation is running, stale animation completion callbacks MUST NOT reset
 * the next navigation. The ID is deliberately not a Compose State: it should
 * not trigger re-composition on every animation start/end.
 */
internal class ReaderTransitionEpoch {
    private var current: Long = 0

    fun begin(): Long {
        current += 1
        return current
    }

    fun invalidate() {
        current += 1
    }

    fun isCurrent(token: Long): Boolean = token == current
}
