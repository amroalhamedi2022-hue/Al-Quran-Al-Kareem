package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Test

class VersePageRulesTest {
    private val anchors = listOf(
        PageVerseAnchor(1,1,1),
        PageVerseAnchor(2,2,1),
        PageVerseAnchor(3,2,6),
        PageVerseAnchor(4,2,17),
        PageVerseAnchor(5,2,25),
        PageVerseAnchor(6,2,30)
    )
    private val surahs = (1..114).map {
        SurahIndexEntry(it, "الاسم", 1, if(it==2)286 else 7)
    }

    @Test fun jumpsToThePageContainingVerseStart() {
        assertEquals(1,VersePageRules.findPage(anchors,1,7))
        assertEquals(2,VersePageRules.findPage(anchors,2,5))
        assertEquals(3,VersePageRules.findPage(anchors,2,6))
        assertEquals(4,VersePageRules.findPage(anchors,2,24))
    }

    @Test fun cannotJumpToUnknownVerse() {
        assertNull(VersePageRules.findPage(anchors,0,5))
        assertNull(VersePageRules.findPage(anchors,2,300))
    }

    @Test fun cannotTrustIncompletePageIndexAsRealMushaf() {
        assertFalse(VersePageRules.isComplete(anchors))
    }

    @Test fun colonReferenceAcceptsArabicDigits() {
        assertEquals(2 to 255,VersePageRules.parseReference("٢:٢٥٥",surahs))
        assertEquals(2 to 255,VersePageRules.parseReference("2：255",surahs))
    }

    @Test fun outOfRangeReferenceIsRejected() {
        assertNull(VersePageRules.parseReference("2:287",surahs))
        assertNull(VersePageRules.parseReference("0:1",surahs))
        assertNull(VersePageRules.parseReference("2:0",surahs))
    }

    @Test fun invalidCharactersAreRejected() {
        assertNull(VersePageRules.parseReference("2/255",surahs))
        assertNull(VersePageRules.parseReference("2:255:10",surahs))
    }
}
