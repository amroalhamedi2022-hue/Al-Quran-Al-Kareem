package app.alquran.kareem

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranIntegrityProgressRulesTest {
    @Test fun firstVerifiedPageIsReported() {
        assertTrue(QuranIntegrityProgressRules.shouldReport(1))
    }

    @Test fun regularProgressIsReportedWithoutFloodingCompose() {
        assertTrue(QuranIntegrityProgressRules.shouldReport(16))
        assertTrue(QuranIntegrityProgressRules.shouldReport(32))
        assertTrue(QuranIntegrityProgressRules.shouldReport(592))
        assertFalse(QuranIntegrityProgressRules.shouldReport(15))
        assertFalse(QuranIntegrityProgressRules.shouldReport(33))
    }

    @Test fun finalPageAlwaysReportsFullVerification() {
        assertTrue(QuranIntegrityProgressRules.shouldReport(604))
    }

    @Test fun noInvalidPageClaimsCanBeReported() {
        assertFalse(QuranIntegrityProgressRules.shouldReport(0))
        assertFalse(QuranIntegrityProgressRules.shouldReport(-1))
        assertFalse(QuranIntegrityProgressRules.shouldReport(605))
        assertFalse(QuranIntegrityProgressRules.shouldReport(1,0))
    }

    @Test fun noProgressSequenceCanExceed604() {
        for (page in 1..604) {
            if (QuranIntegrityProgressRules.shouldReport(page)) {
                assertTrue(page in 1..604)
            }
        }
    }
}
