package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertTrue

class EquationEvaluatorVariablesTest {

    /** Lue avant toute validation, la liste plantait (`!!` sur une variable encore nulle). */
    @Test
    fun variablesDisponiblesSansValidationPrealable() {
        val variables = EquationEvaluator.toutesLesVariables
        assertTrue("BW" in variables && "BEE" in variables && "MW" in variables && "iBW" in variables)
    }
}
