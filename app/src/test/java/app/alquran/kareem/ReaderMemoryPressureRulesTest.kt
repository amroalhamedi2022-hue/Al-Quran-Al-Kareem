package app.alquran.kareem

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderMemoryPressureRulesTest {
    @Test fun healthySystemKeepsDecodedPagesCached() {
        assertFalse(ReaderMemoryPressureRules.shouldTrim(0))
        assertFalse(ReaderMemoryPressureRules.shouldTrim(
            ReaderMemoryPressureRules.RUNNING_MODERATE
        ))
    }

    @Test fun runningLowMemoryImmediatelyReleasesPageCache() {
        assertTrue(ReaderMemoryPressureRules.shouldTrim(
            ReaderMemoryPressureRules.RUNNING_LOW
        ))
    }

    @Test fun hiddenApplicationReleasesCachedFullSizePages() {
        assertTrue(ReaderMemoryPressureRules.shouldTrim(20))
    }

    @Test fun severeMemoryPressureAlwaysEvictsExtraCache() {
        for (level in listOf(15,40,60,80,100)) {
            assertTrue(ReaderMemoryPressureRules.shouldTrim(level))
        }
    }

    @Test fun invalidNegativeLevelsAreNotTreatedAsPressure() {
        assertFalse(ReaderMemoryPressureRules.shouldTrim(-1))
    }
}
