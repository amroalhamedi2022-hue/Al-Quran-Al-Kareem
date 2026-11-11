package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderTransitionEpochTest {
    @Test fun activePaperTurnCompletesOnlyIfItIsStillCurrent() {
        val epoch=ReaderTransitionEpoch()
        val token=epoch.begin()
        assertTrue(epoch.isCurrent(token))
    }

    @Test fun directIndexJumpInvalidatesOldPaperTurnCompletion() {
        val epoch=ReaderTransitionEpoch()
        val stale=epoch.begin()
        epoch.invalidate() // onJump: cancel animation and select chosen page
        assertFalse(epoch.isCurrent(stale))
    }

    @Test fun staleOldTurnCannotResetFreshNewTurn() {
        val epoch=ReaderTransitionEpoch()
        val first=epoch.begin()
        epoch.invalidate()
        val next=epoch.begin()
        assertFalse(epoch.isCurrent(first))
        assertTrue(epoch.isCurrent(next))
    }

    @Test fun twoQuickTurnsHaveDistinctGenerationIds() {
        val epoch=ReaderTransitionEpoch()
        val before=epoch.begin()
        val after=epoch.begin()
        assertTrue(after > before)
        assertFalse(epoch.isCurrent(before))
    }

    @Test fun orientationSwitchInvalidatesPendingAnimations() {
        val epoch=ReaderTransitionEpoch()
        val old=epoch.begin()
        epoch.invalidate()
        assertFalse(epoch.isCurrent(old))
    }

    @Test fun zoomedTwoPageBookDisablesImpossiblePreviousTurn() {
        // The zoom UI shows one page, but physical navigation stays by spread.
        assertFalse(ReaderPagination.hasPrevious(2,604,true))
        assertTrue(ReaderPagination.hasPrevious(2,604,false))
        assertEquals(3,ReaderTurnTargetRules.target(2,604,true,true))
    }
}
