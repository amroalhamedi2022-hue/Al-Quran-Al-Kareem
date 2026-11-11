package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KhatmaProgressRulesTest {
    @Test fun readingIsNeverInferredFromOpeningPages() {
        assertEquals(0f, KhatmaProgressRules.fraction(emptySet(), 604))
    }
    @Test fun manualMarkSinglePage() {
        assertEquals(setOf(19), KhatmaProgressRules.toggleVisible(emptySet(),listOf(19),604))
    }
    @Test fun manualMarkLandscapePairExactly() {
        assertEquals(setOf(23,24),KhatmaProgressRules.toggleVisible(emptySet(),listOf(24,23),604))
    }
    @Test fun togglingSamePairAgainUnmarksIt() {
        val first=KhatmaProgressRules.toggleVisible(emptySet(),listOf(2,1),604)
        assertTrue(KhatmaProgressRules.toggleVisible(first,listOf(2,1),604).isEmpty())
    }
    @Test fun partialPairBecomesBothMarked() {
        assertEquals(setOf(1,2),KhatmaProgressRules.toggleVisible(setOf(1),listOf(2,1),604))
    }
    @Test fun outOfRangeMarksCannotInflateCompletion() {
        assertEquals(1f/604f,KhatmaProgressRules.fraction(setOf(-1,604,999),604))
        assertEquals(setOf(604),KhatmaProgressRules.sanitize(listOf(604,0,-5,999),604))
    }
    @Test fun percentageIsClampedToAllValidPages() {
        assertEquals(1f,KhatmaProgressRules.fraction((1..604).toList()+1000,604))
    }
    @Test(expected=IllegalArgumentException::class)
    fun invalidVisiblePageIsRejected() {
        KhatmaProgressRules.toggleVisible(emptySet(),listOf(605),604)
    }
    @Test(expected=IllegalArgumentException::class)
    fun emptyVisiblePageListIsRejected() {
        KhatmaProgressRules.toggleVisible(emptySet(),emptyList(),604)
    }
    @Test(expected=IllegalArgumentException::class)
    fun duplicateVisiblePagesAreRejected() {
        KhatmaProgressRules.toggleVisible(emptySet(),listOf(1,1),604)
    }
}
