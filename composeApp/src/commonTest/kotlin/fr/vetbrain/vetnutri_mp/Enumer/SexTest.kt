package fr.vetbrain.vetnutri_mp.Enumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SexTest {

    // ── fromId ────────────────────────────────────────────────────────────────

    @Test
    fun fromId_zero_returnsMaleEntier() {
        assertEquals(Sex.MALE_ENTIER, Sex.fromId(0))
    }

    @Test
    fun fromId_one_returnsMaleCastre() {
        assertEquals(Sex.MALE_CASTRE, Sex.fromId(1))
    }

    @Test
    fun fromId_two_returnsFemelleEntiere() {
        assertEquals(Sex.FEMELLE_ENTIERE, Sex.fromId(2))
    }

    @Test
    fun fromId_three_returnsFemelleSterilisee() {
        assertEquals(Sex.FEMELLE_STERILISEE, Sex.fromId(3))
    }

    @Test
    fun fromId_unknown_defaultsToMaleEntier() {
        assertEquals(Sex.MALE_ENTIER, Sex.fromId(999))
    }

    @Test
    fun fromId_negative_defaultsToMaleEntier() {
        assertEquals(Sex.MALE_ENTIER, Sex.fromId(-1))
    }

    @Test
    fun fromId_allKnownIds_roundTrip() {
        Sex.values().forEach { sex ->
            assertEquals(sex, Sex.fromId(sex.id), "fromId round-trip failed for $sex")
        }
    }

    // ── getSimpleSex ──────────────────────────────────────────────────────────

    @Test
    fun getSimpleSex_maleEntier_returnsMale() {
        assertEquals("Mâle", Sex.getSimpleSex(0))
    }

    @Test
    fun getSimpleSex_maleCastre_returnsMale() {
        assertEquals("Mâle", Sex.getSimpleSex(1))
    }

    @Test
    fun getSimpleSex_femelleEntiere_returnsFemelle() {
        assertEquals("Femelle", Sex.getSimpleSex(2))
    }

    @Test
    fun getSimpleSex_femelleSterilisee_returnsFemelle() {
        assertEquals("Femelle", Sex.getSimpleSex(3))
    }

    @Test
    fun getSimpleSex_unknown_returnsNonSpecifie() {
        assertEquals("Non spécifié", Sex.getSimpleSex(99))
    }

    // ── coef values ───────────────────────────────────────────────────────────

    @Test
    fun maleEntier_coef_isOne() {
        assertEquals(1.0, Sex.MALE_ENTIER.coef)
    }

    @Test
    fun maleCastre_coef_isPointEight() {
        assertEquals(0.8, Sex.MALE_CASTRE.coef)
    }

    @Test
    fun femelleEntiere_coef_isOne() {
        assertEquals(1.0, Sex.FEMELLE_ENTIERE.coef)
    }

    @Test
    fun femelleSterilisee_coef_isPointEight() {
        assertEquals(0.8, Sex.FEMELLE_STERILISEE.coef)
    }

    // ── id values ─────────────────────────────────────────────────────────────

    @Test
    fun allSexValues_haveUniqueIds() {
        val ids = Sex.values().map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    // ── displayName ───────────────────────────────────────────────────────────

    @Test
    fun maleEntier_displayName_isNotBlank() {
        assertTrue(Sex.MALE_ENTIER.displayName.isNotBlank())
    }

    @Test
    fun femelleSterilisee_displayName_isNotBlank() {
        assertTrue(Sex.FEMELLE_STERILISEE.displayName.isNotBlank())
    }
}
