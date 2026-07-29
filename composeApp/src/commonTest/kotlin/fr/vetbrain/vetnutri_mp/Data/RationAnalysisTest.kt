package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RationAnalysisTest {
    private fun assertNear(expected: Double, actual: Double, tolerance: Double = 0.0001) {
        assertTrue(
            abs(expected - actual) <= tolerance,
            "Expected $expected +/- $tolerance but was $actual"
        )
    }

    private fun aliment(
        name: String,
        quantity: Double,
        energyDensity: Double = 0.0,
        nutrients: Map<Nutrient, Double> = emptyMap()
    ): AlimentRation {
        val food = AlimentEv(nom = name)
        nutrients.forEach { (nutrient, value) -> food.setNutrient(nutrient, value) }
        return AlimentRation(
            quantite = quantity,
            densiteEnergetique = energyDensity,
            aliment = food
        )
    }

    private fun completeBalancedRation(multiplier: Double = 1.0): Ration {
        val nutrients = mapOf<Nutrient, Double>(
            NutrientMain.PROTEINE to 30.0,
            NutrientMain.LIPIDE to 12.0,
            NutrientMain.GLUCIDE to 40.0,
            NutrientMacro.CAL to 2.0,
            NutrientMacro.PHOS to 1.5,
            NutrientMacro.NA to 1.0,
            NutrientMacro.K to 3.0,
            NutrientMin.ZN to 8.0,
            NutrientMin.CU to 1.0,
            NutrientLipid.O3 to 1.0,
            NutrientLipid.O6 to 5.0,
            NutrientVitam.VITA to 1.0,
            NutrientVitam.VITD to 1.0,
            NutrientVitam.VITE to 1.0
        )
        return Ration(
            uuid = "ration-$multiplier",
            name = "Ration $multiplier",
            alimentMutableList = mutableListOf(
                aliment(
                    name = "Aliment complet",
                    quantity = 1000.0 * multiplier,
                    energyDensity = 3.5,
                    nutrients = nutrients
                )
            )
        )
    }

    @Test
    fun getQuantiteTotale_sumsAllIngredients() {
        val ration = Ration(
            alimentMutableList = mutableListOf(
                aliment("A", 120.0),
                aliment("B", 80.0)
            )
        )

        assertEquals(200.0, ration.getQuantiteTotale())
    }

    @Test
    fun getNutrient_weightsValuesByIngredientQuantity() {
        val ration = Ration(
            alimentMutableList = mutableListOf(
                aliment("A", 100.0, nutrients = mapOf(NutrientMain.PROTEINE to 20.0)),
                aliment("B", 50.0, nutrients = mapOf(NutrientMain.PROTEINE to 40.0))
            )
        )

        assertNear(40.0, ration.getNutrient(NutrientMain.PROTEINE)!!)
    }

    @Test
    fun getNutrient_absentFromEveryIngredient_returnsNull() {
        val ration = Ration(
            alimentMutableList = mutableListOf(aliment("A", 100.0))
        )

        assertNull(ration.getNutrient(NutrientMacro.CAL))
    }

    @Test
    fun getDensiteEnergetiqueMoyenne_isQuantityWeighted() {
        val ration = Ration(
            alimentMutableList = mutableListOf(
                aliment("A", 100.0, energyDensity = 4.0),
                aliment("B", 300.0, energyDensity = 2.0)
            )
        )

        assertNear(2.5, ration.getDensiteEnergetiqueMoyenne())
    }

    @Test
    fun getDensiteEnergetiqueMoyenne_emptyRation_returnsZero() {
        assertEquals(0.0, Ration().getDensiteEnergetiqueMoyenne())
    }

    @Test
    fun getDensiteEnergetiqueMoyenne_suspendFallback_matchesStoredDensity() = runTest {
        val ration = Ration(
            alimentMutableList = mutableListOf(
                aliment("A", 100.0, energyDensity = 4.0),
                aliment("B", 300.0, energyDensity = 2.0)
            )
        )

        assertNear(2.5, ration.getDensiteEnergetiqueMoyenne(null, null))
    }

    @Test
    fun analyserRation_emptyRation_reportsMissingEssentials() {
        val result = RationAnalyzer().analyserRation(Ration(uuid = "empty", name = "Vide"))

        assertEquals(0.0, result.quantiteTotale)
        assertEquals(0.0, result.completude)
        assertEquals(0.0, result.equilibre)
        assertEquals(8, result.alertes.count { it.startsWith("Nutriment essentiel manquant:") })
    }

    @Test
    fun analyserRation_completeBalancedRation_hasExpectedRatiosAndScores() {
        val result = RationAnalyzer().analyserRation(completeBalancedRation())

        assertEquals(100.0, result.completude)
        assertEquals(100.0, result.equilibre)
        assertNear(5.0, result.ratios.getValue("Oméga-6/Oméga-3"))
        assertNear(2.0 / 1.5, result.ratios.getValue("Calcium/Phosphore"))
        assertNear(3.0, result.ratios.getValue("Potassium/Sodium"))
        assertNear(8.0, result.ratios.getValue("Zinc/Cuivre"))
        assertNear(20.0, result.ratios.getValue("Protéines/Phosphore"))
        assertTrue(result.alertes.isEmpty())
    }

    @Test
    fun analyserRation_unbalancedCalciumPhosphorus_addsAlertAndReducesBalance() {
        val ration = completeBalancedRation()
        ration.alimentMutableList.single().aliment?.setNutrient(NutrientMacro.CAL, 4.0)

        val result = RationAnalyzer().analyserRation(ration)

        assertTrue(result.alertes.any { it.contains("Calcium/Phosphore") })
        assertTrue(result.equilibre < 100.0)
    }

    @Test
    fun analyserValeursNutritionnellesRation_missingValue_marksResultIncomplete() {
        val ration = Ration(
            alimentMutableList = mutableListOf(
                aliment("Avec protéines", 100.0, nutrients = mapOf(NutrientMain.PROTEINE to 20.0)),
                aliment("Sans protéines", 100.0)
            )
        )

        val protein = analyserValeursNutritionnellesRation(ration)
            .getValue(NutrientMain.PROTEINE.label)

        assertNear(20.0, protein.valeur)
        assertFalse(protein.complete)
        assertTrue(protein.description.contains("Sans protéines: NA"))
    }

    @Test
    fun comparerRations_doubledQuantity_reportsHundredPercentIncrease() {
        val comparison = RationAnalyzer()
            .comparerRations(completeBalancedRation(1.0), completeBalancedRation(2.0))
            .getValue(NutrientMain.PROTEINE.label)

        assertNear(comparison.valeur1, comparison.difference)
        assertNear(100.0, comparison.pourcentageDifference)
    }
}
