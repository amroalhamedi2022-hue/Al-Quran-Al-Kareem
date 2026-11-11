package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Test

class VisualRevealRulesTest {
    @Test fun firstStepCompletelyConcealsOriginalImage() {
        assertEquals(0f, VisualRevealRules.fraction(0))
    }
    @Test fun lastStepShowsOriginalImageUnchanged() {
        assertEquals(1f, VisualRevealRules.fraction(15))
    }
    @Test fun revealMovesInFifteenEqualFractions() {
        assertEquals(8f/15f, VisualRevealRules.fraction(8))
    }
    @Test fun nextRevealNeverExceedsFullPage() {
        assertEquals(15,VisualRevealRules.next(15))
        assertEquals(1,VisualRevealRules.next(0))
    }
    @Test fun previousRevealCannotGoBelowZero() {
        assertEquals(0,VisualRevealRules.previous(0))
        assertEquals(14,VisualRevealRules.previous(15))
    }
    @Test(expected = IllegalArgumentException::class)
    fun invalidRevealStateIsRejected() {
        VisualRevealRules.fraction(16)
    }
}
