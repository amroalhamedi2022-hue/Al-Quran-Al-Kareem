package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranPageCachePolicyTest {
    @Test fun acceptsOnlyPinnedUnmodifiedPageDimensions() {
        assertTrue(QuranPageCachePolicy.acceptsDimensions(1600,2287))
        assertFalse(QuranPageCachePolicy.acceptsDimensions(1600,2286))
        assertFalse(QuranPageCachePolicy.acceptsDimensions(1599,2287))
        assertFalse(QuranPageCachePolicy.acceptsDimensions(2287,1600))
    }

    @Test fun lowMemoryDevicesHaveBoundedCache() {
        assertEquals(16*1024*1024, QuranPageCachePolicy.budgetBytes(64))
        assertEquals(16*1024*1024, QuranPageCachePolicy.budgetBytes(80))
    }

    @Test fun normalDevicesDoNotFillTheWholeHeap() {
        assertEquals(128*1024*1024/5, QuranPageCachePolicy.budgetBytes(128))
    }

    @Test fun highMemoryTabletsHaveMaximumBound() {
        assertEquals(44*1024*1024, QuranPageCachePolicy.budgetBytes(1024))
        assertEquals(44*1024*1024, QuranPageCachePolicy.budgetBytes(65536))
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroMemoryClassIsRejected() {
        QuranPageCachePolicy.budgetBytes(0)
    }
}
