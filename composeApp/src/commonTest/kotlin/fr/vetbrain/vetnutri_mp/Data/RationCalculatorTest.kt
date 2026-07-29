package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RationCalculatorTest {

    private fun assertNear(expected: Double, actual: Double, tol: Double = 0.001) {
        assertTrue(
            abs(expected - actual) <= tol,
            "Expected $expected ± $tol but was $actual"
        )
    }

    @Test
    fun beforeCalculer_BEE_isZero() {
        val calc = RationCalculator(10.0, 10.0, Espece.CHIEN)
        assertEquals(0.0, calc.getBEE())
    }

    @Test
    fun beforeCalculer_PM_isZero() {
        val calc = RationCalculator(10.0, 10.0, Espece.CHIEN)
        assertEquals(0.0, calc.getPM())
    }

    @Test
    fun calculer_PM_equals_BW_pow_0_75() {
        val calc = RationCalculator(10.0, 10.0, Espece.CHIEN)
        calc.calculer(emptyList())
        // 10^0.75 ≈ 5.6234
        assertNear(5.6234, calc.getPM())
    }

    @Test
    fun calculer_BEE_equals_70_times_PM() {
        val calc = RationCalculator(10.0, 10.0, Espece.CHIEN)
        calc.calculer(emptyList())
        assertNear(70.0 * calc.getPM(), calc.getBEE())
    }

    @Test
    fun calculer_cat_PM_uses_correct_weight() {
        val calc = RationCalculator(4.0, 4.0, Espece.CHAT)
        calc.calculer(emptyList())
        // 4^0.75 ≈ 2.8284
        assertNear(2.8284, calc.getPM(), tol = 0.001)
    }

    @Test
    fun clone_producesIndependentCopy() {
        val original = RationCalculator(20.0, 18.0, Espece.CHAT)
        original.calculer(emptyList())
        val clone = original.clone()

        assertEquals(original.getPoids(), clone.getPoids())
        assertEquals(original.getEspece(), clone.getEspece())
        assertNear(original.getBEE(), clone.getBEE())
        assertNear(original.getPM(), clone.getPM())
    }

    @Test
    fun setters_updateValues() {
        val calc = RationCalculator()
        calc.setPoids(25.0)
        calc.setEspece(Espece.LAPIN)
        assertEquals(25.0, calc.getPoids())
        assertEquals(Espece.LAPIN, calc.getEspece())
    }

    @Test
    fun defaultConstructor_poids_isZero() {
        val calc = RationCalculator()
        assertEquals(0.0, calc.getPoids())
    }
}
