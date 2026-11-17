package fr.vetbrain.vetnutri_mp.Enumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NutrientMainTest {

    // ── getByCoef ─────────────────────────────────────────────────────────────

    @Test
    fun getByCoef_proteineCoef_returnsProteine() {
        assertEquals(NutrientMain.PROTEINE, NutrientMain.getByCoef(NutrientMain.PROTEINE.coef))
    }

    @Test
    fun getByCoef_humiditeCoef_returnsHumidite() {
        assertEquals(NutrientMain.HUMIDITE, NutrientMain.getByCoef(NutrientMain.HUMIDITE.coef))
    }

    @Test
    fun getByCoef_unknownCoef_returnsNull() {
        assertNull(NutrientMain.getByCoef(9999))
    }

    @Test
    fun getByCoef_allEntries_roundTrip() {
        NutrientMain.entries.forEach { entry ->
            assertEquals(entry, NutrientMain.getByCoef(entry.coef), "Round-trip failed for $entry")
        }
    }

    // ── getByLabel ────────────────────────────────────────────────────────────

    @Test
    fun getByLabel_exactLabel_returnsEnum() {
        assertEquals(NutrientMain.PROTEINE, NutrientMain.getByLabel("PROTEINE"))
    }

    @Test
    fun getByLabel_lowercaseLabel_returnsEnum() {
        assertEquals(NutrientMain.LIPIDE, NutrientMain.getByLabel("lipide"))
    }

    @Test
    fun getByLabel_mixedCaseLabel_returnsEnum() {
        assertEquals(NutrientMain.HUMIDITE, NutrientMain.getByLabel("HuMiDiTe"))
    }

    @Test
    fun getByLabel_unknownLabel_returnsNull() {
        assertNull(NutrientMain.getByLabel("NOT_A_NUTRIENT_LABEL"))
    }

    @Test
    fun getByLabel_allEntries_roundTrip() {
        NutrientMain.entries.forEach { entry ->
            assertNotNull(NutrientMain.getByLabel(entry.label), "Round-trip failed for $entry")
        }
    }

    // ── isByLabel ─────────────────────────────────────────────────────────────

    @Test
    fun isByLabel_knownLabel_returnsTrue() {
        assertTrue(NutrientMain.isByLabel("GLUCIDE"))
    }

    @Test
    fun isByLabel_knownLabelLowercase_returnsTrue() {
        assertTrue(NutrientMain.isByLabel("glucide"))
    }

    @Test
    fun isByLabel_unknownLabel_returnsFalse() {
        assertFalse(NutrientMain.isByLabel("UNKNOWN_LABEL_XYZ"))
    }

    // ── size ──────────────────────────────────────────────────────────────────

    @Test
    fun size_isPositive() {
        assertTrue(NutrientMain.size() > 0)
    }

    @Test
    fun size_matchesEntries() {
        assertEquals(NutrientMain.entries.size, NutrientMain.size())
    }

    // ── getMNE ────────────────────────────────────────────────────────────────

    @Test
    fun getMNE_allEntries_returnsBASE() {
        NutrientMain.entries.forEach { entry ->
            assertEquals(MainNutrientEnum.BASE, entry.getMNE(), "getMNE failed for $entry")
        }
    }

    // ── coef uniqueness ───────────────────────────────────────────────────────

    @Test
    fun allEntries_haveUniqueCoefs() {
        val coefs = NutrientMain.entries.map { it.coef }
        assertEquals(coefs.size, coefs.toSet().size)
    }

    // ── label non-blank ───────────────────────────────────────────────────────

    @Test
    fun allEntries_labelIsNotBlank() {
        NutrientMain.entries.forEach { entry ->
            assertTrue(entry.label.isNotBlank(), "Label blank for $entry")
        }
    }
}
