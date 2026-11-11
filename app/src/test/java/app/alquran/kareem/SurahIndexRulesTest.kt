package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SurahIndexRulesTest {
    private val entries = listOf(
        SurahIndexEntry(1, "الفاتحة", 1, 7),
        SurahIndexEntry(2, "البقرة", 2, 286),
        SurahIndexEntry(3, "آل عمران", 50, 200),
        SurahIndexEntry(4, "النساء", 77, 176),
        SurahIndexEntry(5, "المائدة", 106, 120),
        SurahIndexEntry(6, "الأنعام", 128, 165)
    )

    @Test fun exactSurahNameSearchFindsPage() {
        assertEquals(2, SurahIndexRules.search(entries,"البقرة").single().startPage)
        assertEquals(50, SurahIndexRules.search(entries,"آل عمران").single().startPage)
    }

    @Test fun diacriticsAndHamzaVariationsDoNotBreakSearch() {
        assertEquals(2, SurahIndexRules.search(entries,"البقره").single().startPage)
        assertEquals(50, SurahIndexRules.search(entries,"آل عمران").single().startPage)
    }

    @Test fun blankQueryReturnsIndexInOriginalOrder() {
        assertEquals(entries, SurahIndexRules.search(entries,"   "))
    }

    @Test fun numericSurahAndPageSearch() {
        assertEquals(77, SurahIndexRules.search(entries,"4").single().startPage)
        assertTrue(SurahIndexRules.search(entries,"200").isEmpty())
    }

    @Test fun incompleteIndexIsNeverTreatedAsComplete() {
        assertFalse(SurahIndexRules.isComplete(entries))
        assertFalse(SurahIndexRules.isComplete(emptyList()))
    }

    @Test fun normalizationDoesNotTurnEveryInputIntoAMatch() {
        assertTrue(SurahIndexRules.search(entries,"سورة غير موجودة").isEmpty())
    }
}
