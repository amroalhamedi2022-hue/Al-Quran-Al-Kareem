package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderTurnTargetRulesTest {
    @Test fun rightToLeftTwoPageBookAdvancesToNewSpreadAfterLeavingZoom() {
        assertEquals(3, ReaderTurnTargetRules.target(1,604,true,true))
        assertEquals(3, ReaderTurnTargetRules.target(2,604,true,true))
        assertEquals(1, ReaderTurnTargetRules.target(3,604,true,false))
    }

    @Test fun portraitSinglePageStillAdvancesExactlyOnePage() {
        assertEquals(2, ReaderTurnTargetRules.target(1,604,false,true))
        assertEquals(1, ReaderTurnTargetRules.target(2,604,false,false))
        assertEquals(604, ReaderTurnTargetRules.target(603,604,false,true))
    }

    @Test fun firstAndLastBookBoundaryNeverOverflow() {
        assertEquals(1, ReaderTurnTargetRules.target(1,604,true,false))
        assertEquals(603, ReaderTurnTargetRules.target(604,604,true,true))
        assertEquals(604, ReaderTurnTargetRules.target(604,604,false,true))
    }

    @Test fun navigatingFromMagnifiedLeftHandPageStillReturnsCorrectBookSpread() {
        // Page 2 is the LEFT of the [2,1] spread. A turn must move to [4,3],
        // not redraw the same spread after zoom returns to 100%.
        assertEquals(3, ReaderTurnTargetRules.target(2,604,true,true))
    }
}
