package app.alquran.kareem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderAppearanceTest {
    @Test fun userStartsWithQuietPearlStyle() {
        assertEquals(ReaderThemeMode.PEARL, ReaderDisplaySettings().appearance)
    }

    @Test fun changingThemeKeepsUnrelatedReadingPreferencesIntact() {
        val original=ReaderDisplaySettings(
            keepScreenAwake=true, reduceMotion=true
        )
        val updated=original.copy(appearance=ReaderThemeMode.ROYAL_DARK)
        assertTrue(updated.keepScreenAwake)
        assertTrue(updated.reduceMotion)
        assertEquals(ReaderThemeMode.ROYAL_DARK, updated.appearance)
    }

    @Test fun allThreeThemeIdsAreStableAndDistinctForFutureUpdates() {
        val themes=ReaderThemeMode.entries
        assertEquals(3,themes.size)
        assertEquals(3,themes.map { it.storageId }.toSet().size)
        assertTrue(themes.all { it.title.isNotBlank() })
    }

    @Test fun storedOptionsRoundTripWithoutAmbiguity() {
        for (mode in ReaderThemeMode.entries) {
            assertEquals(mode,ReaderThemeMode.fromStorage(mode.storageId))
        }
    }

    @Test fun corruptOrFutureUnknownPreferenceUsesSafePearlFallback() {
        assertEquals(ReaderThemeMode.PEARL,ReaderThemeMode.fromStorage(null))
        assertEquals(ReaderThemeMode.PEARL,ReaderThemeMode.fromStorage(""))
        assertEquals(ReaderThemeMode.PEARL,ReaderThemeMode.fromStorage("malformed"))
    }

    @Test fun originalSettingsDefaultsRemainUnchanged() {
        val original=ReaderDisplaySettings()
        assertFalse(original.keepScreenAwake)
        assertFalse(original.reduceMotion)
        assertNotEquals(ReaderThemeMode.ROYAL_DARK,original.appearance)
    }
}
