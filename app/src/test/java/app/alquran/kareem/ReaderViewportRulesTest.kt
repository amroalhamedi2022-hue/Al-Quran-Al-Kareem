package app.alquran.kareem

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderViewportRulesTest {
    @Test fun landscapeTabletGetsTwoPages() {
        assertTrue(ReaderViewportRules.twoPageSpread(true, 1280))
        assertTrue(ReaderViewportRules.twoPageSpread(true, 800))
    }

    @Test fun portraitTabletStillHasOnePage() {
        assertFalse(ReaderViewportRules.twoPageSpread(false, 1280))
    }

    @Test fun narrowLandscapePhoneGetsOnePage() {
        assertFalse(ReaderViewportRules.twoPageSpread(true, 699))
        assertFalse(ReaderViewportRules.twoPageSpread(true, 400))
    }

    @Test fun exactBoundaryIsDeterministic() {
        assertTrue(ReaderViewportRules.twoPageSpread(true, 700))
    }

    @Test fun invalidDimensionsDoNotEnableSpread() {
        assertFalse(ReaderViewportRules.twoPageSpread(true, 0))
        assertFalse(ReaderViewportRules.twoPageSpread(true, -1))
    }
}
