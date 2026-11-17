package fr.vetbrain.vetnutri_mp.Utils

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EquationEvaluatorTest {

    private fun assertNear(expected: Double, actual: Double, tol: Double = 0.01) {
        assertTrue(abs(expected - actual) <= tol, "Expected $expected ± $tol but was $actual")
    }

    // ── evaluerPourAnimal ──────────────────────────────────────────────────────

    @Test
    fun evaluerPourAnimal_multiplication_returnsProduct() {
        val result = EquationEvaluator.evaluerPourAnimal("BW * 2", 10.0)
        assertEquals(20.0, result)
    }

    @Test
    fun evaluerPourAnimal_powerExpression_returnsMetabolicWeight() {
        val result = EquationEvaluator.evaluerPourAnimal("BW ^ 0.75", 10.0)
        assertNotNull(result)
        assertNear(5.623, result!!)
    }

    @Test
    fun evaluerPourAnimal_emptyExpression_returnsNull() {
        assertNull(EquationEvaluator.evaluerPourAnimal("", 10.0))
    }

    @Test
    fun evaluerPourAnimal_zeroWeight_returnsZero() {
        val result = EquationEvaluator.evaluerPourAnimal("BW * 5", 0.0)
        assertEquals(0.0, result)
    }

    @Test
    fun evaluerPourAnimal_constantExpression_ignoresWeight() {
        val result = EquationEvaluator.evaluerPourAnimal("42", 999.0)
        assertEquals(42.0, result)
    }

    // ── calculerPoidsMetabolique ───────────────────────────────────────────────

    @Test
    fun calculerPoidsMetabolique_10kg_returns5_623() {
        assertNear(5.623, EquationEvaluator.calculerPoidsMetabolique(10.0))
    }

    @Test
    fun calculerPoidsMetabolique_0kg_returnsZero() {
        assertEquals(0.0, EquationEvaluator.calculerPoidsMetabolique(0.0))
    }

    @Test
    fun calculerPoidsMetabolique_1kg_returns1() {
        assertNear(1.0, EquationEvaluator.calculerPoidsMetabolique(1.0))
    }

    @Test
    fun calculerPoidsMetabolique_4kg_returns2_828() {
        // 4^0.75 ≈ 2.828
        assertNear(2.828, EquationEvaluator.calculerPoidsMetabolique(4.0))
    }

    // ── calculerBesoinEnergetiqueBase ─────────────────────────────────────────

    @Test
    fun calculerBesoinEnergetiqueBase_10kg_defaultFactor_returns730() {
        // BEE = 130 * 10^0.75 ≈ 130 * 5.623 ≈ 730.9
        assertNear(730.9, EquationEvaluator.calculerBesoinEnergetiqueBase(10.0), tol = 1.0)
    }

    @Test
    fun calculerBesoinEnergetiqueBase_10kg_factor70_returns393() {
        // BEE = 70 * 10^0.75 ≈ 70 * 5.623 ≈ 393.6
        assertNear(393.6, EquationEvaluator.calculerBesoinEnergetiqueBase(10.0, 70.0), tol = 1.0)
    }

    @Test
    fun calculerBesoinEnergetiqueBase_consistentWithPM() {
        val pm = EquationEvaluator.calculerPoidsMetabolique(25.0)
        val bee = EquationEvaluator.calculerBesoinEnergetiqueBase(25.0)
        assertNear(130.0 * pm, bee, tol = 0.5)
    }

    // ── validerExpression ──────────────────────────────────────────────────────

    @Test
    fun validerExpression_validBWExpression_isValid() {
        val result = EquationEvaluator.validerExpression("BW ^ 0.75", TypeEquationValidation.BESOIN_ENERGETIQUE)
        assertTrue(result.estValide)
        assertTrue(result.variablesManquantes.isEmpty())
    }

    @Test
    fun validerExpression_emptyExpression_isInvalid() {
        val result = EquationEvaluator.validerExpression("")
        assertFalse(result.estValide)
    }

    @Test
    fun validerExpression_unknownVariable_reportsItMissing() {
        val result = EquationEvaluator.validerExpression("UNKNOWNVAR * 2", TypeEquationValidation.GENERALE)
        assertFalse(result.estValide)
        assertTrue(result.variablesManquantes.contains("UNKNOWNVAR"))
    }

    @Test
    fun validerExpression_constantExpression_isValid() {
        val result = EquationEvaluator.validerExpression("130 * BW ^ 0.75", TypeEquationValidation.BESOIN_ENERGETIQUE)
        assertTrue(result.estValide)
    }

    // ── testerExpression ───────────────────────────────────────────────────────

    @Test
    fun testerExpression_validBWExpr_returnsNonNull() {
        val result = EquationEvaluator.testerExpression("BW * 2", TypeEquationValidation.GENERALE)
        assertNotNull(result)
        // BW defaults to 25.0 → 50.0
        assertNear(50.0, result!!)
    }

    @Test
    fun testerExpression_emptyExpression_returnsNull() {
        assertNull(EquationEvaluator.testerExpression(""))
    }
}
