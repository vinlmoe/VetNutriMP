package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NutrientQuantityTest {

    // ── propriétés de base ────────────────────────────────────────────────────

    @Test
    fun unit_aliasesNut() {
        val nq = NutrientQuantity(5.0, "g")
        assertEquals("g", nq.unit)
    }

    @Test
    fun quantity_aliasesValue() {
        val nq = NutrientQuantity(7.5, "g")
        assertEquals(7.5, nq.quantity)
    }

    // ── times ─────────────────────────────────────────────────────────────────

    @Test
    fun times_doublesValue() {
        val result = NutrientQuantity(5.0, "g").times(2.0)
        assertEquals(10.0, result.value)
    }

    @Test
    fun times_preservesUnit() {
        val result = NutrientQuantity(5.0, "mg").times(3.0)
        assertEquals("mg", result.nut)
    }

    @Test
    fun times_byZero_returnsZero() {
        val result = NutrientQuantity(5.0, "g").times(0.0)
        assertEquals(0.0, result.value)
    }

    @Test
    fun times_byFraction_reducesValue() {
        val result = NutrientQuantity(10.0, "g").times(0.5)
        assertEquals(5.0, result.value)
    }

    @Test
    fun times_doesNotMutateOriginal() {
        val original = NutrientQuantity(5.0, "g")
        original.times(2.0)
        assertEquals(5.0, original.value)
    }

    // ── plus ──────────────────────────────────────────────────────────────────

    @Test
    fun plus_sameUnit_sumsValues() {
        val a = NutrientQuantity(3.0, "g")
        val b = NutrientQuantity(4.0, "g")
        val result = a.plus(b)
        assertEquals(7.0, result?.value)
    }

    @Test
    fun plus_sameUnit_preservesUnit() {
        val result = NutrientQuantity(3.0, "mg").plus(NutrientQuantity(2.0, "mg"))
        assertEquals("mg", result?.nut)
    }

    @Test
    fun plus_differentUnits_returnsNull() {
        val a = NutrientQuantity(3.0, "g")
        val b = NutrientQuantity(4.0, "mg")
        assertNull(a.plus(b))
    }

    @Test
    fun plus_withZero_returnsSameValue() {
        val result = NutrientQuantity(5.0, "g").plus(NutrientQuantity(0.0, "g"))
        assertEquals(5.0, result?.value)
    }

    @Test
    fun plus_doesNotMutateOriginals() {
        val a = NutrientQuantity(3.0, "g")
        val b = NutrientQuantity(4.0, "g")
        a.plus(b)
        assertEquals(3.0, a.value)
        assertEquals(4.0, b.value)
    }

    // ── égalité data class ────────────────────────────────────────────────────

    @Test
    fun equality_sameValueAndUnit_areEqual() {
        assertEquals(NutrientQuantity(5.0, "g"), NutrientQuantity(5.0, "g"))
    }

    @Test
    fun equality_differentValue_notEqual() {
        val a = NutrientQuantity(5.0, "g")
        val b = NutrientQuantity(6.0, "g")
        assertTrue(a != b)
    }
}
