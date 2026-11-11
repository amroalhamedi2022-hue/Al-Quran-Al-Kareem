package app.alquran.kareem

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranSectionRulesTest {
    private val juz = (1..30).map { number ->
        QuranSectionStart(
            number,
            page = if (number == 30) 582 else 1 + (number - 1) * 20,
            surah = number,
            ayah = 1
        )
    }
    private val hizb = (1..60).map { number ->
        QuranSectionStart(
            number,
            page = 1 + (number - 1) * 10,
            surah = 1 + (number - 1) / 2,
            ayah = if (number % 2 == 1) 1 else 2
        )
    }

    @Test fun acceptsFullOrderedSections() {
        assertTrue(QuranSectionRules.isValid(juz, 30))
        assertTrue(QuranSectionRules.isValid(hizb, 60))
    }

    @Test fun refusesMissingSections() {
        assertFalse(QuranSectionRules.isValid(juz.dropLast(1), 30))
        assertFalse(QuranSectionRules.isValid(hizb.dropLast(1), 60))
    }

    @Test fun refusesWrongJuzThirtyStart() {
        assertFalse(QuranSectionRules.isValid(juz.dropLast(1) +
            QuranSectionStart(30, 581, 30, 1), 30))
    }

    @Test fun refusesOutOfRangePage() {
        assertFalse(QuranSectionRules.isValid(
            juz.map { if (it.number == 30) it.copy(page = 605) else it }, 30))
    }

    @Test fun refusesUnorderedSectionNumbers() {
        assertFalse(QuranSectionRules.isValid(
            hizb.map { if (it.number == 12) it.copy(number = 13) else it }, 60))
    }

    @Test fun refusesDecreasingVerses() {
        assertFalse(QuranSectionRules.isValid(
            hizb.map { if (it.number == 2) it.copy(ayah = 0) else it }, 60))
    }
}
