package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderZoomRulesTest {
    @Test fun originalQuranPageIsNeverScaledAtRest() {
        assertEquals(1f, ReaderZoomRules.normalize(1f))
        assertFalse(ReaderZoomRules.isZoomed(1f))
        assertEquals(ReaderZoomRules.Pan(0f, 0f),
            ReaderZoomRules.clampPan(400f, 800f, 900f, 1200f, 1f))
    }

    @Test fun zoomNeverExceedsThreeTimesOrShrinksBelowOne() {
        assertEquals(3f, ReaderZoomRules.zoomIn(3f))
        assertEquals(1f, ReaderZoomRules.zoomOut(1f))
        assertEquals(1f, ReaderZoomRules.normalize(Float.NaN))
        assertEquals(1f, ReaderZoomRules.normalize(Float.POSITIVE_INFINITY))
    }

    @Test fun zoomStepsAreExactlyHalfOfOriginalScale() {
        assertEquals(1.5f, ReaderZoomRules.zoomIn(1f))
        assertEquals(2f, ReaderZoomRules.zoomIn(1.5f))
        assertEquals(1.5f, ReaderZoomRules.zoomOut(2f))
    }

    @Test fun doubleTapRestoresOriginalUnmodifiedImage() {
        assertEquals(2f, ReaderZoomRules.toggleDoubleTap(1f))
        assertEquals(1f, ReaderZoomRules.toggleDoubleTap(2f))
        assertEquals(1f, ReaderZoomRules.toggleDoubleTap(3f))
    }

    @Test fun panIsLimitedToVisibleImageAndCannotLoseThePage() {
        val pan = ReaderZoomRules.clampPan(5000f, -5000f, 600f, 1000f, 2f)
        assertEquals(300f, pan.x)
        assertEquals(-500f, pan.y)
    }

    @Test fun panBoundariesMatchZoomFactor() {
        val pan = ReaderZoomRules.clampPan(-300f, 300f, 1000f, 1000f, 1.5f)
        assertEquals(-250f, pan.x)
        assertEquals(250f, pan.y)
    }

    @Test fun invalidDimensionsOrMotionNeverShiftQuran() {
        assertEquals(ReaderZoomRules.Pan(0f, 0f),
            ReaderZoomRules.clampPan(Float.NaN, 3f, 0f, 500f, 2f))
        val safe = ReaderZoomRules.clampPan(Float.NaN, Float.POSITIVE_INFINITY, 500f, 800f, 3f)
        assertEquals(0f, safe.x)
        assertEquals(0f, safe.y)
    }

    @Test fun zoomStateExactlyGatesPageTurnGestures() {
        assertTrue(ReaderZoomRules.isZoomed(1.5f))
        assertFalse(ReaderZoomRules.isZoomed(0.2f))
    }
}
