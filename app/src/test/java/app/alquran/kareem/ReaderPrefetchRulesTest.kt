package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderPrefetchRulesTest {
    private val enough = (ReaderPrefetchRules.PAGE_BYTES * 3).toInt()

    @Test fun noExtraPagesAreDecodedOnSmallMemoryDevices() {
        assertNull(ReaderPrefetchRules.target(4,604,false,16*1024*1024))
        assertNull(ReaderPrefetchRules.target(4,604,true,16*1024*1024))
    }

    @Test fun portraitNextIsPrefetchedWhenMemoryAllows() {
        assertEquals(5,ReaderPrefetchRules.target(4,604,false,enough))
    }

    @Test fun landscapePrefetchIsNextSpreadRightPage() {
        assertEquals(5,ReaderPrefetchRules.target(3,604,true,enough))
        assertEquals(3,ReaderPrefetchRules.target(1,604,true,enough))
    }

    @Test fun lastPageSelectsPreviousRatherThanOutOfBounds() {
        assertEquals(603,ReaderPrefetchRules.target(604,604,false,enough))
        assertEquals(601,ReaderPrefetchRules.target(604,604,true,enough))
    }

    @Test fun invalidPageCannotBePrefetched() {
        assertNull(ReaderPrefetchRules.target(0,604,false,enough))
        assertNull(ReaderPrefetchRules.target(605,604,false,enough))
        assertNull(ReaderPrefetchRules.target(1,0,false,enough))
    }

    @Test fun requirementIsExactlyOneAdditionalFullResolutionBitmap() {
        val two = ReaderPrefetchRules.PAGE_BYTES * 2
        assertEquals(2,ReaderPrefetchRules.target(1,604,false,two.toInt()))
        assertNull(ReaderPrefetchRules.target(1,604,true,two.toInt()))
        assertTrue(ReaderPrefetchRules.PAGE_BYTES > 14_000_000L)
    }
}
