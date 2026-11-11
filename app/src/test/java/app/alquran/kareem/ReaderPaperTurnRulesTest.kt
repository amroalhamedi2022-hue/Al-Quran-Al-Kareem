package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderPaperTurnRulesTest {
    @Test fun stationaryPagesNeverDistortQuran() {
        val pose = ReaderPaperTurnRules.pose(0f, 800f)
        assertEquals(0f, pose.rotationDegreesY)
        assertEquals(0f, pose.elevationFraction)
    }

    @Test fun rightwardRtlSwipeHingesAtTheRight() {
        val pose = ReaderPaperTurnRules.pose(400f, 800f)
        assertEquals(-39f, pose.rotationDegreesY)
        assertEquals(1f, pose.pivotX)
    }

    @Test fun leftwardSwipeUsesTheOppositeHinge() {
        val pose = ReaderPaperTurnRules.pose(-400f, 800f)
        assertEquals(39f, pose.rotationDegreesY)
        assertEquals(0f, pose.pivotX)
    }

    @Test fun neverFlipsQuranBackwardsOrMirrorsArabicLetters() {
        assertEquals(-78f, ReaderPaperTurnRules.pose(5000f, 800f).rotationDegreesY)
        assertEquals(78f, ReaderPaperTurnRules.pose(-5000f, 800f).rotationDegreesY)
    }

    @Test fun nonsensicalViewportOrGestureShowsUnmodifiedPage() {
        for (width in listOf(0f, -1f, Float.NaN)) {
            assertEquals(0f, ReaderPaperTurnRules.pose(500f, width).rotationDegreesY)
        }
        assertEquals(0f, ReaderPaperTurnRules.pose(Float.NaN, 800f).rotationDegreesY)
    }

    @Test fun landscapeTwoPageSpreadTiltsOnlyTurningSheet() {
        assertTrue(ReaderPaperTurnRules.shouldTiltPage(0, true, 200f))
        assertFalse(ReaderPaperTurnRules.shouldTiltPage(1, true, 200f))
        assertFalse(ReaderPaperTurnRules.shouldTiltPage(0, true, -200f))
        assertTrue(ReaderPaperTurnRules.shouldTiltPage(1, true, -200f))
    }

    @Test fun portraitPageAlwaysUsesTheMotionFeedback() {
        assertTrue(ReaderPaperTurnRules.shouldTiltPage(0, false, 100f))
        assertTrue(ReaderPaperTurnRules.shouldTiltPage(0, false, -100f))
    }
}
