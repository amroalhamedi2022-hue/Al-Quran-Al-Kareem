package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderPaginationTest {
    @Test fun firstLandscapeSpreadIsRightToLeft() {
        assertEquals(listOf(2, 1), ReaderPagination.visible(1, 604, true))
        assertEquals(listOf(2, 1), ReaderPagination.visible(2, 604, true))
    }

    @Test fun middleSpreadFollowsPrintedPageOrder() {
        assertEquals(listOf(54, 53), ReaderPagination.visible(54, 604, true))
        assertEquals(55, ReaderPagination.next(54, 604, true))
        assertEquals(listOf(56, 55), ReaderPagination.visible(55, 604, true))
    }

    @Test fun previousDoesNotRepeatFirstSpread() {
        assertFalse(ReaderPagination.hasPrevious(1, 604, true))
        assertFalse(ReaderPagination.hasPrevious(2, 604, true))
        assertEquals(1, ReaderPagination.previous(2, 604, true))
    }

    @Test fun lastLandscapeSpreadHasNoPhantomPage() {
        assertEquals(listOf(604, 603), ReaderPagination.visible(604, 604, true))
        assertFalse(ReaderPagination.hasNext(603, 604, true))
        assertFalse(ReaderPagination.hasNext(604, 604, true))
        assertEquals(603, ReaderPagination.next(604, 604, true))
        assertEquals(601, ReaderPagination.previous(604, 604, true))
    }

    @Test fun portraitMovesOnePageAndClampsBounds() {
        assertEquals(listOf(604), ReaderPagination.visible(604, 604, false))
        assertEquals(2, ReaderPagination.next(1, 604, false))
        assertEquals(604, ReaderPagination.next(604, 604, false))
        assertEquals(1, ReaderPagination.previous(1, 604, false))
        assertFalse(ReaderPagination.hasNext(604, 604, false))
    }

    @Test fun loneOddFinalPageNeverInventsPage() {
        assertEquals(listOf(5), ReaderPagination.visible(5, 5, true))
        assertFalse(ReaderPagination.hasNext(5, 5, true))
        assertEquals(3, ReaderPagination.previous(5, 5, true))
    }

    @Test fun restoredStateIsClampedWithinApprovedPages() {
        assertEquals(1, ReaderPagination.clamp(-7, 604))
        assertEquals(604, ReaderPagination.clamp(10000, 604))
        assertTrue(ReaderPagination.hasNext(603, 604, false))
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotNavigateWithoutApprovedPages() {
        ReaderPagination.clamp(1, 0)
    }
}
