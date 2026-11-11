package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderTouchRulesTest {
    @Test fun rightHandEdgeTurnsForwardInRtl() {
        assertEquals(ReaderTouchRules.Action.NEXT, ReaderTouchRules.classify(1000f, 1000f))
        assertEquals(ReaderTouchRules.Action.NEXT, ReaderTouchRules.classify(800f, 1000f))
    }

    @Test fun leftHandEdgeTurnsBackwardInRtl() {
        assertEquals(ReaderTouchRules.Action.PREVIOUS, ReaderTouchRules.classify(0f, 1000f))
        assertEquals(ReaderTouchRules.Action.PREVIOUS, ReaderTouchRules.classify(200f, 1000f))
    }

    @Test fun centralPageShowsAndHidesControls() {
        assertEquals(ReaderTouchRules.Action.TOGGLE_CONTROLS, ReaderTouchRules.classify(500f, 1000f))
        assertEquals(ReaderTouchRules.Action.TOGGLE_CONTROLS, ReaderTouchRules.classify(201f, 1000f))
        assertEquals(ReaderTouchRules.Action.TOGGLE_CONTROLS, ReaderTouchRules.classify(799f, 1000f))
    }

    @Test fun invalidCoordinatesNeverNavigate() {
        for (x in listOf(-1f, 1001f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertEquals(ReaderTouchRules.Action.NONE, ReaderTouchRules.classify(x, 1000f))
        }
        assertEquals(ReaderTouchRules.Action.NONE, ReaderTouchRules.classify(20f, 0f))
        assertEquals(ReaderTouchRules.Action.NONE, ReaderTouchRules.classify(20f, Float.NaN))
    }

    @Test fun proportionalOnSmallDevices() {
        assertEquals(ReaderTouchRules.Action.NEXT, ReaderTouchRules.classify(320f,400f))
        assertEquals(ReaderTouchRules.Action.PREVIOUS, ReaderTouchRules.classify(80f,400f))
        assertEquals(ReaderTouchRules.Action.TOGGLE_CONTROLS, ReaderTouchRules.classify(200f,400f))
    }
}
