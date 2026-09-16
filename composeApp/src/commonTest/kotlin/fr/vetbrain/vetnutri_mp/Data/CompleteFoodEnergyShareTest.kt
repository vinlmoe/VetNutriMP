package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.View.AnalNut.AlimentAdjustmentData
import fr.vetbrain.vetnutri_mp.View.AnalNut.RationAdjustmentResult
import fr.vetbrain.vetnutri_mp.View.AnalNut.calculerAjustement
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class CompleteFoodEnergyShareTest {
    private val reference = ReferenceEv()

    private fun food(kind: FoodKind, quantity: Double): AlimentRation {
        val food = AlimentEv(nom = kind.name, typeAliment = kind)
        food.setNutrient(NutrientMain.ENERGIE, 400.0)
        return AlimentRation(aliment = food, quantite = quantity, weight = 1.0)
    }

    private suspend fun adjust(
        lp: Boolean,
        foods: List<AlimentRation>,
        percentage: Double?,
        locked: Set<String> = emptySet()
    ): RationAdjustmentResult {
        val ration = Ration(alimentMutableList = foods.toMutableList())
        val data = foods.map {
            AlimentAdjustmentData(it, selectedNutrient = "ENERGIE", isLocked = it.uuid in locked)
        }
        return if (lp) {
            val result = adjustRationByConstraints(ration, data, reference, 1000.0, 1000.0,
                null, null, null, percentage)
            RationAdjustmentResult(result.success, result.message, result.adjustedAliments)
        } else {
            calculerAjustement(ration, data, reference, 1000.0, 1000.0,
                null, null, null, percentage)
        }
    }

    @Test
    fun fixedShare_isRespectedByBothMethods_includingZeroAndHundredAndFractionalQuantities() = runTest {
        for (lp in listOf(false, true)) {
            for (percentage in listOf(0.0, 33.3, 50.0, 100.0)) {
                val result = adjust(lp, listOf(food(FoodKind.COMPLET, 250.0), food(FoodKind.MEN, 0.0)), percentage)
                assertTrue(result.success, "lp=$lp percentage=$percentage: ${result.message}")
                val adjusted = result.adjustedAliments!!
                val completeEnergy = adjusted.first().getEnergie(reference, null)
                val otherEnergy = adjusted.last().getEnergie(reference, null)
                assertEquals(percentage * 10.0, completeEnergy, 0.001)
                assertEquals(1000.0, completeEnergy + otherEnergy, 0.001)
            }
        }
    }

    @Test
    fun lockedCompleteFood_isIncludedInTheFixedShare() = runTest {
        for (lp in listOf(false, true)) {
            val locked = food(FoodKind.COMPLET, 40.0)
            val result = adjust(lp, listOf(locked, food(FoodKind.COMPLET, 250.0), food(FoodKind.MEN, 0.0)),
                50.0, setOf(locked.uuid))
            assertTrue(result.success, result.message)
            val adjusted = result.adjustedAliments!!
            assertEquals(40.0, adjusted.first().quantite)
            assertEquals(500.0, adjusted[0].getEnergie(reference, null) + adjusted[1].getEnergie(reference, null), 0.001)
        }
    }

    @Test
    fun impossibleShare_returnsFailure() = runTest {
        for (lp in listOf(false, true)) {
            val locked = food(FoodKind.COMPLET, 150.0)
            assertFalse(adjust(lp, listOf(locked, food(FoodKind.MEN, 0.0)), 50.0, setOf(locked.uuid)).success)
            assertFalse(adjust(lp, listOf(food(FoodKind.COMPLET, 250.0)), 50.0).success)
            assertFalse(adjust(lp, listOf(food(FoodKind.MEN, 250.0)), 50.0).success)
        }
    }

    @Test
    fun invalidPercentage_returnsFailure() = runTest {
        for (lp in listOf(false, true)) {
            for (percentage in listOf(-1.0, 101.0, Double.NaN, Double.POSITIVE_INFINITY)) {
                assertFalse(adjust(lp, listOf(food(FoodKind.COMPLET, 250.0), food(FoodKind.MEN, 0.0)), percentage).success)
            }
        }
    }

    @Test
    fun emptyPercentage_keepsCompleteFoodAvailableForAllEnergy() = runTest {
        for (lp in listOf(false, true)) {
            val result = adjust(lp, listOf(food(FoodKind.COMPLET, 250.0)), null)
            assertTrue(result.success, result.message)
            assertEquals(1000.0, result.adjustedAliments!!.single().getEnergie(reference, null), 0.001)
        }
    }
}
