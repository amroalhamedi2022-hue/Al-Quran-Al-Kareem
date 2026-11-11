package app.alquran.kareem

/**
 * Manual visual page concealment without any OCR or invented verse boundaries.
 * Every underlying Quran image stays bit-for-bit unchanged.
 * The equal-height reveals are NOT asserted to be individual ayah locations.
 */
internal object VisualRevealRules {
    const val REVEAL_STEPS = 15

    fun fraction(step: Int): Float {
        require(step in 0..REVEAL_STEPS)
        return step.toFloat() / REVEAL_STEPS
    }

    fun next(step: Int): Int = step.coerceIn(0, REVEAL_STEPS)
        .plus(1).coerceAtMost(REVEAL_STEPS)

    fun previous(step: Int): Int = step.coerceIn(0, REVEAL_STEPS)
        .minus(1).coerceAtLeast(0)
}
