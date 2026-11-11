package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReaderSpreadTapRulesTest {
    @Test fun doubleTapLeftSheetZoomsLeftQuranPage() {
        assertEquals(2, ReaderSpreadTapRules.tappedPage(99f, 800f, listOf(2,1)))
        assertEquals(604, ReaderSpreadTapRules.tappedPage(0f, 800f, listOf(604,603)))
    }

    @Test fun doubleTapRightSheetZoomsRightQuranPage() {
        assertEquals(1, ReaderSpreadTapRules.tappedPage(701f, 800f, listOf(2,1)))
        assertEquals(603, ReaderSpreadTapRules.tappedPage(800f, 800f, listOf(604,603)))
    }

    @Test fun centerFoldBelongsToRightSheetByConvention() {
        assertEquals(1, ReaderSpreadTapRules.tappedPage(400f, 800f, listOf(2,1)))
    }

    @Test fun singlePhonePageAlwaysFocusesTheOnlyVisiblePage() {
        assertEquals(152, ReaderSpreadTapRules.tappedPage(5f, 800f, listOf(152)))
        assertEquals(152, ReaderSpreadTapRules.tappedPage(799f, 800f, listOf(152)))
    }

    @Test fun invalidPointersNeverSelectAnyPage() {
        assertNull(ReaderSpreadTapRules.tappedPage(Float.NaN, 800f, listOf(2,1)))
        assertNull(ReaderSpreadTapRules.tappedPage(-1f, 800f, listOf(2,1)))
        assertNull(ReaderSpreadTapRules.tappedPage(801f, 800f, listOf(2,1)))
        assertNull(ReaderSpreadTapRules.tappedPage(3f, 0f, listOf(2,1)))
    }

    @Test fun untrustedOrEmptySpreadsAreRejected() {
        assertNull(ReaderSpreadTapRules.tappedPage(20f, 800f, emptyList()))
        assertNull(ReaderSpreadTapRules.tappedPage(20f, 800f, listOf(0)))
        assertNull(ReaderSpreadTapRules.tappedPage(20f, 800f, listOf(605)))
        assertNull(ReaderSpreadTapRules.tappedPage(20f, 800f, listOf(1,1)))
        assertNull(ReaderSpreadTapRules.tappedPage(20f, 800f, listOf(1,2,3)))
    }
}
