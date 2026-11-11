package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderMotionRulesTest {
    @Test fun usualReadingUsesShortPaperAnimation() {
        assertTrue(ReaderMotionRules.animateTurn(false))
        assertEquals(190, ReaderMotionRules.turnDurationMs(false))
    }

    @Test fun reducedMotionRemovesPageTiltAndTransitionDelay() {
        assertFalse(ReaderMotionRules.animateTurn(true))
        assertEquals(0, ReaderMotionRules.turnDurationMs(true))
    }

    @Test fun screenAwakeIsNeverEnabledWithoutTheReaderChoosingIt() {
        val settings = ReaderDisplaySettings()
        assertFalse(settings.keepScreenAwake)
        assertFalse(settings.reduceMotion)
    }

    @Test fun settingsAreIndependent() {
        val a = ReaderDisplaySettings(keepScreenAwake = true, reduceMotion = false)
        val b = ReaderDisplaySettings(keepScreenAwake = false, reduceMotion = true)
        assertTrue(a.keepScreenAwake)
        assertFalse(a.reduceMotion)
        assertFalse(b.keepScreenAwake)
        assertTrue(b.reduceMotion)
    }
}
