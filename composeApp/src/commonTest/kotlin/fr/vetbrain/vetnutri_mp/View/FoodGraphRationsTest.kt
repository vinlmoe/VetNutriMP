package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class FoodGraphRationsTest {
    private fun ration(id: String, protein: Double, energy: Double, coefficient: Double) : Ration {
        val food = AlimentEv(uuid = id, nom = id)
        food.setNutrient(NutrientMain.PROTEINE, protein)
        food.setNutrient(NutrientMain.ENERGIE, energy)
        food.setNutrient(NutrientMain.HUMIDITE, 50.0)
        food.setNutrient(NutrientVitam.VITE, 2.0)
        return Ration(uuid = id, actual = true, coef = coefficient,
            alimentMutableList = mutableListOf(AlimentRation(aliment = food, uuidUnif = id, quantite = 100.0)))
    }

    @Test
    fun sumUsesWeightedIngredientsBeforeNormalizingAndKeepsEnergyPercentagesInDryMatter() = runTest {
        val sum = RationAggregator.agregerRations(
            listOf(ration("a", 10.0, 200.0, 1.0), ration("b", 30.0, 300.0, 3.0)),
            RationAnalysisScope.GROUPE_ACTUELLES
        )!!
        val reference = ReferenceEv()
        val perEnergy = rationFoodGraphData(sum, 0, true, reference, null, false)!!
        val dry = rationFoodGraphData(sum, 0, true, reference, null, true)!!
        assertEquals(275.0, perEnergy.densiteEnergetique, 0.0001)
        assertEquals(25.0 / 275.0 * 1000, perEnergy.proteinePer1000Kcal, 0.0001)
        assertEquals(550.0, dry.densiteEnergetique, 0.0001)
        assertEquals(50.0, dry.proteinePer1000Kcal, 0.0001)
        assertEquals(perEnergy.pourcentageProteines, dry.pourcentageProteines, 0.0001)
        assertEquals(4.0, dry.getNutrimentValue("vitamine_e", reference, null, true), 0.0001)
        assertEquals(2.0 / 275.0 * 1000, perEnergy.getNutrimentValue("vitamine_e", reference, null), 0.0001)
        assertEquals("Σ", perEnergy.graphLabel)
        assertEquals(true, perEnergy.rationActual)
    }

    @Test
    fun emptyRationsAreOmittedAndIndividualRationsHaveDistinctLabels() = runTest {
        assertNull(rationFoodGraphData(Ration(), 1, false, ReferenceEv(), null, false))
        val individual = rationFoodGraphData(ration("a", 10.0, 200.0, 1.0), 1, false, ReferenceEv(), null, false)!!
        assertEquals("R1", individual.graphLabel)
        assertEquals(100.0, individual.aliment.getNutrient(NutrientMain.HUMIDITE)!! + 50.0)
    }
}
