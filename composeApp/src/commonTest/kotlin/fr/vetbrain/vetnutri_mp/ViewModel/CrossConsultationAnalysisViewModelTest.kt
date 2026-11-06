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

    private fun ration(actuelle: Boolean, proteines: Double?) =
            CrossConsultationAnalysisViewModel.RationSummary(
                    rationId = "r$proteines", name = "R", actual = actuelle, quantity = 100.0,
                    energyTotalKcal = 400.0, energyDensity = 4.0, beeKcal = null,
                    animalWeightKg = 10.0, animalMetabolicWeightKg = 5.6, proteins = proteines ?: 0.0,
                    lipids = 0.0, ratioCaP = 0.0, ratioOmega6Omega3 = 0.0,
                    nutrientValues = if (proteines == null) emptyMap() else mapOf("PROTEINE" to proteines),
                    ratioValues = emptyMap(), ingredients = emptyList(), animalName = "A",
                    consultationDate = "", referenceLabel = null, speciesLabel = ""
            )

    @Test
    fun statistiquesDescriptives_ecarteLesValeursNonRenseignees() {
        val s = statistiquesDescriptives("PROTEINE", listOf(10.0, 0.0, 20.0, 30.0))!!
        assertNear(20.0, s.mean)
        assertNear(20.0, s.median)
        assertNear(10.0, s.min)
        assertNear(30.0, s.max)
        assertTrue(s.count == 3)
        assertTrue(statistiquesDescriptives("X", listOf(0.0)) == null)
    }

    @Test
    fun comparerStatistiques_ecartRelatifDesMoyennes() {
        val comparaison = comparerStatistiques(
                listOf(ration(true, 40.0), ration(true, 60.0)),
                listOf(ration(false, 75.0))
        ).single()
        assertNear(50.0, comparaison.actual!!.mean)
        assertNear(75.0, comparaison.proposed!!.mean)
        assertNear(50.0, comparaison.ecartPourcent!!)
        // Aucune ration proposée : pas d'écart
        assertTrue(comparerStatistiques(listOf(ration(true, 40.0)), emptyList()).single().ecartPourcent == null)
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
