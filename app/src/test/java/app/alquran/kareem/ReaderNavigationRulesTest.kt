package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderNavigationRulesTest {
    @Test fun westernArabicIndicAndEasternArabicDigitsAreAccepted() {
        assertEquals(604, ReaderNavigationRules.parsePage("604", 604))
        assertEquals(604, ReaderNavigationRules.parsePage("\u0666\u0660\u0664", 604))
        assertEquals(604, ReaderNavigationRules.parsePage("\u06F6\u06F0\u06F4", 604))
    }

    @Test fun whitespaceIsIgnoredButNotInternalSeparators() {
        assertEquals(10, ReaderNavigationRules.parsePage("  10  ", 604))
        assertNull(ReaderNavigationRules.parsePage("1 0", 604))
    }

    @Test fun zeroNegativeAndOutOfRangeAreRejected() {
        assertNull(ReaderNavigationRules.parsePage("0", 604))
        assertNull(ReaderNavigationRules.parsePage("-1", 604))
        assertNull(ReaderNavigationRules.parsePage("605", 604))
    }

    @Test fun nonDecimalInputIsRejected() {
        for (input in listOf("", " ", "7.5", "1e2", "+3", "abc", "4/5")) {
            assertNull(ReaderNavigationRules.parsePage(input, 604))
        }
    }

    @Test fun giganticInputAndMissingBookAreRejected() {
        assertNull(ReaderNavigationRules.parsePage("999999999999999", 604))
        assertNull(ReaderNavigationRules.parsePage("1", 0))
    }

    @Test fun validPageBoundariesAreAccepted() {
        assertEquals(1, ReaderNavigationRules.parsePage("1", 604))
        assertEquals(604, ReaderNavigationRules.parsePage("0604", 604))
    }

    @Test fun bookmarkToggleAddsAndRemoves() {
        val first = ReaderNavigationRules.toggleBookmark(emptySet(), 25, 604)
        assertEquals(setOf(25), first)
        val last = ReaderNavigationRules.toggleBookmark(first, 25, 604)
        assertTrue(last.isEmpty())
    }

    @Test fun bookmarksAreDeduplicatedSortedAndBounded() {
        val result = ReaderNavigationRules.sanitizedPages(listOf(604, 1, 1, -5, 0, 605, 14), 604)
        assertEquals(listOf(1, 14, 604), result.toList())
    }

    @Test fun invalidBookmarkPageNeverPersists() {
        var threw = false
        try { ReaderNavigationRules.toggleBookmark(emptySet(), 605, 604) }
        catch (_: IllegalArgumentException) { threw = true }
        assertTrue(threw)
        assertFalse(ReaderNavigationRules.toggleBookmark(setOf(0, 1, 700), 3, 604).contains(700))
    }
}
