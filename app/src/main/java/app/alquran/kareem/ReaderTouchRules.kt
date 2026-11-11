package app.alquran.kareem

/**
 * Reader tap areas are outside the immutable Quran artwork. Touch mapping
 * never crops, reconstructs, or edits any letter of the Mushaf.
 *
 * The reader is right-to-left: tap on right edge = next, left edge = previous.
 * Tap in the center = show/hide the reading controls.
 */
internal object ReaderTouchRules {
    enum class Action { NEXT, PREVIOUS, TOGGLE_CONTROLS, NONE }

    fun classify(x: Float, width: Float): Action {
        if (!x.isFinite() || !width.isFinite() || width <= 0f || x < 0f || x > width) {
            return Action.NONE
        }
        val fraction = x / width
        return when {
            fraction >= 0.80f -> Action.NEXT
            fraction <= 0.20f -> Action.PREVIOUS
            else -> Action.TOGGLE_CONTROLS
        }
    }
}
