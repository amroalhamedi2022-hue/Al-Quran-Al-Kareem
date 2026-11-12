package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderAccessibilityRulesTest {
    @Test fun firstPortraitPageReadsMetadataOnly() {
        val spoken = ReaderAccessibilityRules.status(listOf(1), 604, emptySet(), 1f)
        assertEquals("الصفحة 1 من 604. لا توجد علامة على الصفحات المعروضة. التكبير 100 بالمئة.", spoken)
    }

    @Test fun rTLTabletSpreadSpeaksAscendingPageNumbers() {
        val spoken = ReaderAccessibilityRules.status(listOf(2, 1), 604, setOf(2), 1f)
        assertTrue(spoken.startsWith("الصفحتان 1 و2 من 604."))
        assertTrue(spoken.contains("علامة محفوظة للصفحة 2"))
    }

    @Test fun bothBookmarkedPagesAreDistinguished() {
        val spoken = ReaderAccessibilityRules.status(listOf(604, 603), 604, setOf(603, 604), 3f)
        assertTrue(spoken.contains("الصفحتان 603 و604 من 604"))
        assertTrue(spoken.contains("الصفحتان محفوظتان في العلامات"))
        assertTrue(spoken.endsWith("التكبير 300 بالمئة."))
    }

    @Test fun invalidOrRepeatedPageNumbersAreNeverAnnouncedAsQuran() {
        val spoken = ReaderAccessibilityRules.status(listOf(0, 1, 1, 605), 604, setOf(605), 1f)
        assertTrue(spoken.startsWith("الصفحة 1 من 604"))
        assertFalse(spoken.contains("605"))
    }

    @Test fun missingPageNeverInventsQuranicContent() {
        assertTrue(
            ReaderAccessibilityRules.status(emptyList(), 604, emptySet(), 1f)
                .startsWith("لا توجد صفحة معروضة")
        )
    }

    @Test fun invalidZoomFallsBackToOneHundredPercent() {
        assertTrue(
            ReaderAccessibilityRules.status(listOf(10), 604, emptySet(), Float.NaN)
                .endsWith("التكبير 100 بالمئة.")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonexistentMushafIsRejected() {
        ReaderAccessibilityRules.status(listOf(1), 0, emptySet(), 1f)
    }
}
