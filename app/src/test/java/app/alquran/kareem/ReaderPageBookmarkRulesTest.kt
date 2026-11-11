package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderPageBookmarkRulesTest {
    @Test fun tabletLeftPageLongPressMarksOnlyPageTwo() {
        val saved=ReaderPageBookmarkRules.toggleAtTouch(
            emptySet(), listOf(2,1), 150f, 800f, 604
        )
        assertEquals(setOf(2),saved)
    }

    @Test fun tabletRightPageLongPressMarksOnlyPageOne() {
        val saved=ReaderPageBookmarkRules.toggleAtTouch(
            emptySet(), listOf(2,1), 700f, 800f, 604
        )
        assertEquals(setOf(1),saved)
    }

    @Test fun secondLongPressRemovesBookmarkWithoutTouchingOtherPages() {
        val first=setOf(1,2,54)
        val result=ReaderPageBookmarkRules.toggleAtTouch(
            first, listOf(2,1), 170f, 800f, 604
        )
        assertEquals(setOf(1,54),result)
    }

    @Test fun portraitPhoneLongPressAlwaysTargetsOnlyDisplayedPage() {
        val saved=ReaderPageBookmarkRules.toggleAtTouch(
            setOf(10), listOf(307), 450f, 900f, 604
        )
        assertEquals(setOf(10,307),saved)
    }

    @Test fun touchesOutsidePageViewportCannotChangeSavedBookmarks() {
        val existing=setOf(1,51,603)
        for (x in listOf(-1f,801f,Float.NaN)) {
            assertEquals(existing,ReaderPageBookmarkRules.toggleAtTouch(
                existing,listOf(2,1),x,800f,604
            ))
        }
    }

    @Test fun invalidOrOutOfRangePageCannotBeSaved() {
        val original=setOf(4)
        assertEquals(original,ReaderPageBookmarkRules.toggleAtTouch(
            original,listOf(605),200f,800f,604
        ))
        assertTrue(ReaderPageBookmarkRules.toggleAtTouch(
            emptySet(),listOf(604,603),700f,800f,604
        ).contains(603))
    }
}
