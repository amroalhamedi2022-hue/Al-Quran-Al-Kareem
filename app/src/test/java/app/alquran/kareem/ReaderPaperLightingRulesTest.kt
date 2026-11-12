package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderPaperLightingRulesTest {
    @Test fun stationaryPageHasNoCoveringOverlay() {
        assertEquals(ReaderPaperLightingRules.Light(0f, 0f, 0f),
            ReaderPaperLightingRules.light(0f, 800f))
    }

    @Test fun invalidGestureOrWidthNeverChangesPixels() {
        val rest = ReaderPaperLightingRules.Light(0f, 0f, 0f)
        assertEquals(rest, ReaderPaperLightingRules.light(Float.NaN, 800f))
        assertEquals(rest, ReaderPaperLightingRules.light(300f, 0f))
        assertEquals(rest, ReaderPaperLightingRules.light(300f, Float.POSITIVE_INFINITY))
    }

    @Test fun forwardAndBackwardTurnsHaveEqualLightingStrength() {
        assertEquals(
            ReaderPaperLightingRules.light(400f, 800f),
            ReaderPaperLightingRules.light(-400f, 800f)
        )
    }

    @Test fun maximumShadingIsCappedEvenForHugeGesture() {
        val light = ReaderPaperLightingRules.light(100000f, 800f)
        assertEquals(0.14f, light.bandFraction, 0.0001f)
        assertEquals(0.17f, light.shadowAlpha, 0.0001f)
        assertEquals(0.065f, light.highlightAlpha, 0.0001f)
    }

    @Test fun portraitPaintIsCenteredAndDoesNotExtendOutsidePaper() {
        val p = ReaderPaperLightingRules.fittedBounds(1080f, 2400f)!!
        assertEquals(0f, p.left, 0.001f)
        assertTrue(p.top > 0f)
        assertEquals(1080f, p.width, 0.001f)
        assertEquals(2400f, p.height + 2f * p.top, 0.001f)
    }

    @Test fun tabletPageIsCenteredInItsHalfOfLandscape() {
        val p = ReaderPaperLightingRules.fittedBounds(1280f, 1600f)!!
        assertTrue(p.left > 0f)
        assertEquals(0f, p.top, 0.001f)
        assertEquals(1280f, p.width + 2f * p.left, 0.001f)
    }

    @Test fun invalidViewportsRefuseToPaint() {
        assertNull(ReaderPaperLightingRules.fittedBounds(Float.NaN, 500f))
        assertNull(ReaderPaperLightingRules.fittedBounds(-1f, 500f))
        assertNull(ReaderPaperLightingRules.fittedBounds(500f, 0f))
    }
}
