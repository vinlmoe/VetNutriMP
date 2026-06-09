package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExpressionEvaluatorTest {

    @Test
    fun evaluer_simpleAddition_returnsCorrectResult() {
        val result = ExpressionEvaluator.evaluer("2 + 3", emptyMap())
        assertEquals(5.0, result)
    }

    @Test
    fun evaluer_withVariable_substitutesCorrectly() {
        val result = ExpressionEvaluator.evaluer("BW * 2", mapOf("BW" to 10.0))
        assertEquals(20.0, result)
    }

    @Test
    fun evaluer_invalidExpression_returnsNull() {
        val result = ExpressionEvaluator.evaluer("BW +++ 2", mapOf("BW" to 5.0))
        assertNull(result)
    }

    @Test
    fun estExpressionValide_validExpr_returnsTrue() {
        assertTrue(ExpressionEvaluator.estExpressionValide("BW ^ 0.75", mapOf("BW" to 10.0)))
    }

    @Test
    fun estExpressionValide_emptyString_returnsFalse() {
        assertFalse(ExpressionEvaluator.estExpressionValide(""))
    }

    @Test
    fun extraireVariables_multipleVars_returnsAll() {
        val vars = ExpressionEvaluator.extraireVariables("BW + BEE * MW")
        assertTrue(vars.containsAll(listOf("BW", "BEE", "MW")))
        assertEquals(3, vars.size)
    }

    @Test
    fun extraireVariables_noVariables_returnsEmpty() {
        val vars = ExpressionEvaluator.extraireVariables("2 + 3 * 4")
        assertTrue(vars.isEmpty())
    }

    @Test
    fun validerVariables_allPresent_returnsEmpty() {
        val missing = ExpressionEvaluator.validerVariables("BW + BEE", setOf("BW", "BEE", "MW"))
        assertTrue(missing.isEmpty())
    }

    @Test
    fun validerVariables_missingVar_returnsMissing() {
        val missing = ExpressionEvaluator.validerVariables("BW + UNKNOWN", setOf("BW", "BEE"))
        assertTrue(missing.contains("UNKNOWN"))
        assertEquals(1, missing.size)
    }

    @Test
    fun testerExpression_validExpr_returnsResult() {
        val result = ExpressionEvaluator.testerExpression("BW * 2", valeurParDefaut = 5.0)
        assertEquals(10.0, result)
    }
}
