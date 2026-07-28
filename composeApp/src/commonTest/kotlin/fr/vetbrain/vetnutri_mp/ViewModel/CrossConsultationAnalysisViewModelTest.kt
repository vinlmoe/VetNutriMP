package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class CrossConsultationAnalysisViewModelTest {

    private fun assertNear(expected: Double, actual: Double, tol: Double = 0.001) {
        assertTrue(abs(expected - actual) <= tol, "Expected $expected ± $tol but was $actual")
    }

    @Test
    fun energyTotalKcalFromDensity_multipliesDensityByQuantity() {
        // Régression directe du bug : l'ancien code divisait par 100 en trop.
        assertNear(1000.0, energyTotalKcalFromDensity(4.0, 250.0))
    }

    @Test
    fun energyTotalKcalFromDensity_zeroQuantity_returnsZero() {
        assertNear(0.0, energyTotalKcalFromDensity(4.0, 0.0))
    }

    @Test
    fun energyTotalKcalFromDensity_correspondALaConventionKcalParGramme() {
        // Garde-fou : Ration.getDensiteEnergetiqueMoyenne() renvoie des kcal/g (énergie totale déjà
        // mise à l'échelle par les quantités, divisée par la quantité totale) — pas une densité
        // "pour 100g". energyTotalKcalFromDensity ne doit donc PAS diviser par 100.
        val ration = Ration(alimentMutableList = mutableListOf(
                AlimentRation(quantite = 100.0, weight = 1.0, densiteEnergetique = 4.0),
                AlimentRation(quantite = 100.0, weight = 1.0, densiteEnergetique = 4.0)
        ))

        val densite = ration.getDensiteEnergetiqueMoyenne() // kcal/g
        val quantiteTotale = ration.getQuantiteTotale()

        // 4 kcal/g * 100g + 4 kcal/g * 100g = 800 kcal au total
        assertNear(800.0, energyTotalKcalFromDensity(densite, quantiteTotale))
    }
}
