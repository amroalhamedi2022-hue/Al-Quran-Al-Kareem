package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderGestureRulesTest {
    private val NEXT = ReaderGestureRules.Turn.NEXT
    private val PREVIOUS = ReaderGestureRules.Turn.PREVIOUS
    private val NONE = ReaderGestureRules.Turn.NONE

    @Test fun rtlRightwardSwipeAdvancesOnePage() {
        assertEquals(NEXT, ReaderGestureRules.classify(100f, 64f))
    }

    @Test fun rtlLeftwardSwipeReturnsOnePage() {
        assertEquals(PREVIOUS, ReaderGestureRules.classify(-100f, 64f))
    }

    @Test fun shortMotionDoesNotTurnPages() {
        assertEquals(NONE, ReaderGestureRules.classify(63.9f, 64f))
        assertEquals(NONE, ReaderGestureRules.classify(-63.9f, 64f))
        assertEquals(NONE, ReaderGestureRules.classify(0f, 64f))
    }

    @Test fun thresholdBoundaryIsExact() {
        assertEquals(NEXT, ReaderGestureRules.classify(64f, 64f))
        assertEquals(PREVIOUS, ReaderGestureRules.classify(-64f, 64f))
    }

    @Test fun nonFiniteGesturesAreIgnored() {
        assertEquals(NONE, ReaderGestureRules.classify(Float.NaN, 64f))
        assertEquals(NONE, ReaderGestureRules.classify(Float.POSITIVE_INFINITY, 64f))
    }

    @Test(expected=IllegalArgumentException::class)
    fun invalidThresholdIsRefused() {
        ReaderGestureRules.classify(120f, 0f)
    }
}
