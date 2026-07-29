package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RationAnalysisCalculationsTest {
    private fun assertNear(expected: Double, actual: Double, tolerance: Double = 0.0001) {
        assertTrue(
            abs(expected - actual) <= tolerance,
            "Expected $expected +/- $tolerance but was $actual"
        )
    }

    @Test
    fun calculerCoefficientGlobal_nullConsultation_returnsNeutralCoefficient() {
        assertEquals(1.0, RationAnalysisCalculations.calculerCoefficientGlobal(null))
    }

    @Test
    fun calculerCoefficientGlobal_missingCoefficients_usesOne() {
        val consultation = ConsultationEv(
            k1Value = 1.2,
            k3Value = 0.8,
            coefficientAjustement = 1.1
        )

        assertNear(
            1.2 * 0.8 * 1.1,
            RationAnalysisCalculations.calculerCoefficientGlobal(consultation)
        )
    }

    @Test
    fun calculerCoefficientGlobal_allCoefficients_multipliesEveryFactor() {
        val consultation = ConsultationEv(
            k1Value = 1.1,
            k2Value = 0.9,
            k3Value = 1.2,
            k4Value = 0.8,
            k5Value = 1.05,
            coefficientAjustement = 1.15
        )

        assertNear(
            1.1 * 0.9 * 1.2 * 0.8 * 1.05 * 1.15,
            RationAnalysisCalculations.calculerCoefficientGlobal(consultation)
        )
    }

    @Test
    fun calculerBesoinApresK_nullStandard_returnsNull() {
        assertNull(RationAnalysisCalculations.calculerBesoinApresK(null, 1.5))
    }

    @Test
    fun calculerBesoinTotal_addsDiseaseEnergy() {
        assertEquals(650.0, RationAnalysisCalculations.calculerBesoinTotal(600.0, 50.0))
    }

    @Test
    fun calculerPourcentageCouverture_validNeed_returnsPercentage() {
        assertNear(
            80.0,
            RationAnalysisCalculations.calculerPourcentageCouverture(400.0, 500.0)
        )
    }

    @Test
    fun calculerPourcentageCouverture_nonPositiveOrMissingNeed_returnsZero() {
        assertEquals(0.0, RationAnalysisCalculations.calculerPourcentageCouverture(400.0, null))
        assertEquals(0.0, RationAnalysisCalculations.calculerPourcentageCouverture(400.0, 0.0))
        assertEquals(0.0, RationAnalysisCalculations.calculerPourcentageCouverture(400.0, -1.0))
    }

    @Test
    fun calculerCoefficientObserve_validStandard_returnsRatio() {
        assertNear(
            1.25,
            RationAnalysisCalculations.calculerCoefficientObserve(500.0, 400.0)
        )
    }

    @Test
    fun calculerCoefficientObserve_nonPositiveOrMissingStandard_returnsZero() {
        assertEquals(0.0, RationAnalysisCalculations.calculerCoefficientObserve(500.0, null))
        assertEquals(0.0, RationAnalysisCalculations.calculerCoefficientObserve(500.0, 0.0))
        assertEquals(0.0, RationAnalysisCalculations.calculerCoefficientObserve(500.0, -1.0))
    }
}
