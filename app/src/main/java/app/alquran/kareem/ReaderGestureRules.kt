package app.alquran.kareem

/**
 * Pointer input affects page NAVIGATION ONLY; it never transforms Quran pixels.
 * RTL Mushaf: a deliberate rightward gesture advances to the next page/spread.
 * One drag can select at most one navigation action.
 */
internal object ReaderGestureRules {
    enum class Turn { NEXT, PREVIOUS, NONE }

    fun classify(horizontalDeltaPx: Float, minimumDistancePx: Float): Turn {
        require(minimumDistancePx > 0f && minimumDistancePx.isFinite())
        if (!horizontalDeltaPx.isFinite()) return Turn.NONE
        return when {
            horizontalDeltaPx >= minimumDistancePx -> Turn.NEXT
            horizontalDeltaPx <= -minimumDistancePx -> Turn.PREVIOUS
            else -> Turn.NONE
        }
    }
}
