package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyWirdRulesTest {
    @Test fun newKhatmaStartsAtFirstPageWithoutAutomaticallyMarkingIt() {
        val finished = emptySet<Int>()
        assertEquals(listOf(1,2,3,4,5),
            DailyWirdRules.suggestedNextPages(finished))
        assertTrue(finished.isEmpty())
    }

    @Test fun nextPagesSkipAlreadyManuallyReadOnes() {
        val finished = setOf(1,2,4,6)
        assertEquals(listOf(3,5,7),
            DailyWirdRules.suggestedNextPages(finished,604,3))
    }

    @Test fun readingPlanHandlesBookGapsRatherThanInventingProgress() {
        assertEquals(listOf(1,3,5),
            DailyWirdRules.suggestedNextPages(setOf(2,4,6),604,3))
    }

    @Test fun completedKhatmaSuggestsNothing() {
        assertTrue(DailyWirdRules.suggestedNextPages((1..604).toSet()).isEmpty())
        assertEquals(0, DailyWirdRules.estimatedDaysRemaining((1..604).toSet()))
    }

    @Test fun invalidOrDuplicateMarksNeverCountTowardKhatma() {
        val next = DailyWirdRules.suggestedNextPages(setOf(-2,0,1,605),604,2)
        assertEquals(listOf(2,3),next)
    }

    @Test fun dailyGoalNeverExceedsMaximumOrFallsBelowOne() {
        assertEquals(1,DailyWirdRules.normalizeTarget(-100))
        assertEquals(30,DailyWirdRules.normalizeTarget(500))
        assertEquals(5,DailyWirdRules.normalizeTarget(5))
        assertEquals(1,DailyWirdRules.suggestedNextPages(emptySet(),604,-1).size)
    }

    @Test fun remainingDaysAreOnlyMathematicalEstimate() {
        assertEquals(121,DailyWirdRules.estimatedDaysRemaining(emptySet(),604,5))
        assertEquals(2,DailyWirdRules.estimatedDaysRemaining((1..600).toSet(),604,3))
        assertEquals(1,DailyWirdRules.estimatedDaysRemaining((1..603).toSet(),604,30))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidMushafSizeIsRejected() {
        DailyWirdRules.suggestedNextPages(emptySet(),0,3)
    }
}
