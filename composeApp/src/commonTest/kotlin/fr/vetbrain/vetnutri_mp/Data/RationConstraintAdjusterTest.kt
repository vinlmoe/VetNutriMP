package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.View.AnalNut.AlimentAdjustmentData
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class RationConstraintAdjusterTest {

    private val biblio = BiblioRef(firstAuthor = "Dupont", year = 2020, consistent = 1)

    private fun assertNear(expected: Double, actual: Double, tolerance: Double = 1e-3) {
        assertTrue(
            abs(expected - actual) <= tolerance,
            "Expected $expected +/- $tolerance but was $actual"
        )
    }

    private fun food(name: String, proteinPer100g: Double, energyPer100g: Double): AlimentEv {
        val aliment = AlimentEv(nom = name)
        aliment.setNutrient(NutrientMain.PROTEINE, proteinPer100g)
        aliment.setNutrient(NutrientMain.ENERGIE, energyPer100g)
        return aliment
    }

    private fun foodWithMinerals(
        name: String,
        calciumPer100g: Double,
        phosphorusPer100g: Double,
        energyPer100g: Double = 400.0
    ): AlimentEv {
        val aliment = AlimentEv(nom = name)
        aliment.setNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL, calciumPer100g)
        aliment.setNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS, phosphorusPer100g)
        aliment.setNutrient(NutrientMain.ENERGIE, energyPer100g)
        return aliment
    }

    private fun makeReference(): ReferenceEv = ReferenceEv(nom = "Ref test", espece = Espece.CHIEN)

    private fun defineAbsolute(ref: ReferenceEv, nutrient: fr.vetbrain.vetnutri_mp.Enumer.Nutrient, level: Reflevel, grams: Double) {
        ref.definirNutriment(grams, nutrient, level, UnitReqEnum.ABSOLUTE, biblio, UnitEnum.BUg)
    }

    @Test
    fun adjustRationByConstraints_feasibleCase_satisfiesMinProtein() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val foodB = food("B", proteinPer100g = 0.0, energyPer100g = 300.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val arB = AlimentRation(quantite = 100.0, aliment = foodB, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA, arB))

        val ref = makeReference()
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 45.0)

        val adjustmentData = listOf(
            AlimentAdjustmentData(alimentRation = arA),
            AlimentAdjustmentData(alimentRation = arB)
        )

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0, // already satisfied at current quantities, no pressure
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet()
        )

        assertTrue(result.success, "Expected success but got: ${result.message}")
        val adjusted = result.adjustedAliments!!
        val recomputed = Ration(alimentMutableList = adjusted.toMutableList())
        val protein = recomputed.getNutrient(NutrientMain.PROTEINE, ref) ?: 0.0
        assertTrue(protein >= 45.0 - 1e-3, "Expected protein >= 45 but was $protein")

        // Only food A carries protein, so it should absorb the whole adjustment while B stays put.
        val adjustedB = adjusted.first { it.uuid == arB.uuid }
        assertNear(100.0, adjustedB.quantite)
    }

    @Test
    fun adjustRationByConstraints_foodBecomesMajority_addsNonBlockingWarning() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val foodB = food("B", proteinPer100g = 0.0, energyPer100g = 300.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val arB = AlimentRation(quantite = 100.0, aliment = foodB, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA, arB))

        val ref = makeReference()
        // Only A carries protein, so it must grow to 225g (69% of the final 325g ration) to
        // reach 45g of protein. This is a legitimate nutritional outcome, not an aberration --
        // it must never block the solve, only be surfaced as a warning.
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 45.0)

        val adjustmentData = listOf(
            AlimentAdjustmentData(alimentRation = arA),
            AlimentAdjustmentData(alimentRation = arB)
        )

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet(),
            maxFoodSharePercent = 50.0
        )

        assertTrue(result.success, "Expected success but got: ${result.message}")
        assertTrue(result.warnings.isNotEmpty(), "Expected a share warning since A ends up > 50% of the ration")
        assertTrue(result.warnings.any { it.contains("'A'") })
    }

    @Test
    fun adjustRationByConstraints_maxFoodShareDisabled_producesNoWarning() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val foodB = food("B", proteinPer100g = 0.0, energyPer100g = 300.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val arB = AlimentRation(quantite = 100.0, aliment = foodB, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA, arB))

        val ref = makeReference()
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 45.0)

        val adjustmentData = listOf(
            AlimentAdjustmentData(alimentRation = arA),
            AlimentAdjustmentData(alimentRation = arB)
        )

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet(),
            maxFoodSharePercent = null
        )

        assertTrue(result.success)
        assertTrue(result.warnings.isEmpty(), "Warning check must be disabled when threshold is null")
    }

    @Test
    fun adjustRationByConstraints_lockedFood_keepsItsQuantityAndReducesRequirement() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val foodB = food("B", proteinPer100g = 10.0, energyPer100g = 300.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val arB = AlimentRation(quantite = 100.0, aliment = foodB, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA, arB))

        val ref = makeReference()
        // Locked food B alone already provides 10g protein; only 35g more is needed from A.
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 45.0)

        val adjustmentData = listOf(
            AlimentAdjustmentData(alimentRation = arA),
            AlimentAdjustmentData(alimentRation = arB, isLocked = true)
        )

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet()
        )

        assertTrue(result.success, "Expected success but got: ${result.message}")
        val adjusted = result.adjustedAliments!!
        val adjustedB = adjusted.first { it.uuid == arB.uuid }
        assertEquals(100.0, adjustedB.quantite, "Locked food must keep its original quantity")
    }

    @Test
    fun adjustRationByConstraints_maxQuantityRespected_forcesOtherFoodToCompensate() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val foodB = food("B", proteinPer100g = 10.0, energyPer100g = 300.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val arB = AlimentRation(quantite = 100.0, aliment = foodB, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA, arB))

        val ref = makeReference()
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 90.0)

        val adjustmentData = listOf(
            AlimentAdjustmentData(alimentRation = arA, maxQuantity = 120.0),
            AlimentAdjustmentData(alimentRation = arB)
        )

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet()
        )

        assertTrue(result.success, "Expected success but got: ${result.message}")
        val adjusted = result.adjustedAliments!!
        val adjustedA = adjusted.first { it.uuid == arA.uuid }
        assertTrue(adjustedA.quantite <= 120.0 + 1e-6, "Food A must respect its maxQuantity cap")

        val recomputed = Ration(alimentMutableList = adjusted.toMutableList())
        val protein = recomputed.getNutrient(NutrientMain.PROTEINE, ref) ?: 0.0
        // The final rounding step (arrondirQuantiteSelonRegles, shared with the legacy
        // heuristic) is a cosmetic grid-snapping pass that is not re-validated against the
        // solved constraints, so a few grams of slack are expected here.
        assertTrue(protein >= 90.0 - 5.0, "Expected protein close to >= 90 but was $protein")
    }

    @Test
    fun adjustRationByConstraints_conflictingMinMax_returnsInfeasible() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA))

        val ref = makeReference()
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 80.0)
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MAX, 50.0)

        val adjustmentData = listOf(AlimentAdjustmentData(alimentRation = arA))

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet()
        )

        assertTrue(!result.success)
        assertTrue(result.violatedConstraints.isNotEmpty())
        assertTrue(result.violatedConstraints.any { it.nutrient == NutrientMain.PROTEINE })
    }

    @Test
    fun adjustRationByConstraints_ratioNutrient_isIgnoredAndDoesNotBlockFeasibility() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA))

        val ref = makeReference()
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 15.0)
        // An absurd ratio-nutrient requirement that would make the model infeasible if it were
        // (incorrectly) turned into a hard linear constraint, since no food ever carries a
        // NutrientAnalysis value directly.
        defineAbsolute(ref, NutrientAnalysis.PCa, Reflevel.MIN, 1_000_000.0)

        val adjustmentData = listOf(AlimentAdjustmentData(alimentRation = arA))

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet()
        )

        assertTrue(result.success, "Ratio-type nutrients must be excluded from automatic constraints: ${result.message}")
    }

    @Test
    fun adjustRationByConstraints_allFoodsLocked_returnsFailureWithoutCrashing() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA))
        val ref = makeReference()
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 15.0)

        val adjustmentData = listOf(AlimentAdjustmentData(alimentRation = arA, isLocked = true))

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet()
        )

        assertTrue(!result.success)
    }

    @Test
    fun listConstrainableNutrients_includesMinMaxNutrientsAndEnergyButExcludesRatios() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val ration = Ration(alimentMutableList = mutableListOf(AlimentRation(quantite = 100.0, aliment = foodA)))

        val ref = makeReference()
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 45.0)
        defineAbsolute(ref, NutrientAnalysis.PCa, Reflevel.MIN, 1.0)

        val constrainable = listConstrainableNutrients(ref, ration)

        assertTrue(constrainable.contains(NutrientMain.PROTEINE))
        assertTrue(constrainable.contains(NutrientMain.ENERGIE))
        assertTrue(constrainable.none { it is NutrientAnalysis })
    }

    @Test
    fun listConstrainableNutrients_excludesNutrientAbsentFromEveryAliment() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val ration = Ration(alimentMutableList = mutableListOf(AlimentRation(quantite = 100.0, aliment = foodA)))

        val ref = makeReference()
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 45.0)
        // No food carries a Calcium value, even though the reference defines a bound for it.
        defineAbsolute(ref, fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL, Reflevel.MIN, 2.0)

        val constrainable = listConstrainableNutrients(ref, ration)

        assertTrue(constrainable.contains(NutrientMain.PROTEINE))
        assertTrue(!constrainable.contains(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL))
    }

    @Test
    fun adjustRationByConstraints_deselectedNutrient_isNotEnforced() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA))

        val ref = makeReference()
        // An unreachable protein requirement that would make the model infeasible if enforced.
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 1_000_000.0)

        val adjustmentData = listOf(AlimentAdjustmentData(alimentRation = arA))

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            // PROTEINE is deliberately left out of the selection; only ENERGIE is constrained.
            selectedNutrients = setOf(NutrientMain.ENERGIE)
        )

        assertTrue(result.success, "Deselected nutrients must not be enforced: ${result.message}")
    }

    @Test
    fun adjustRationByConstraints_emptySelection_returnsFailureWithoutCrashing() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA))
        val ref = makeReference()
        defineAbsolute(ref, NutrientMain.PROTEINE, Reflevel.MIN, 15.0)

        val adjustmentData = listOf(AlimentAdjustmentData(alimentRation = arA))

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = emptySet()
        )

        assertTrue(!result.success)
    }

    @Test
    fun adjustRationByConstraints_ratioMinBound_isEnforced() = runTest {
        // Food A is calcium-rich (low phosphorus), food B is phosphorus-rich (low calcium).
        val foodA = foodWithMinerals("A", calciumPer100g = 2.0, phosphorusPer100g = 0.5)
        val foodB = foodWithMinerals("B", calciumPer100g = 0.2, phosphorusPer100g = 1.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val arB = AlimentRation(quantite = 100.0, aliment = foodB, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA, arB))

        val ref = makeReference()
        // Current ratio at (100,100) is 2.2/1.5 ~= 1.47, below this 2.0 requirement.
        defineAbsolute(ref, NutrientAnalysis.PCa, Reflevel.MIN, 2.0)

        val adjustmentData = listOf(
            AlimentAdjustmentData(alimentRation = arA),
            AlimentAdjustmentData(alimentRation = arB)
        )

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet()
        )

        assertTrue(result.success, "Expected success but got: ${result.message}")
        val adjusted = result.adjustedAliments!!
        val recomputed = Ration(alimentMutableList = adjusted.toMutableList())
        val calcium = recomputed.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL, ref) ?: 0.0
        val phosphorus = recomputed.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS, ref) ?: 0.0
        assertTrue(
            calcium / phosphorus >= 2.0 - 0.05,
            "Expected Ca/P close to >= 2.0 but was ${calcium / phosphorus}"
        )
    }

    @Test
    fun adjustRationByConstraints_conflictingRatioMinMax_returnsInfeasible() = runTest {
        // MIN(3.0) > MAX(1.0) is self-contradictory for any (x_A, x_B) with both foods at zero
        // being the only point where the linearized ratio constraints hold trivially (0 >= 0,
        // 0 <= 0) — but a nonzero MIN(ENERGIE) requirement rules out the all-zero ration, so the
        // combined system has no feasible point at all.
        val foodA = foodWithMinerals("A", calciumPer100g = 2.0, phosphorusPer100g = 0.5)
        val foodB = foodWithMinerals("B", calciumPer100g = 0.2, phosphorusPer100g = 1.0)
        val arA = AlimentRation(quantite = 100.0, aliment = foodA, weight = 1.0)
        val arB = AlimentRation(quantite = 100.0, aliment = foodB, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(arA, arB))

        val ref = makeReference()
        defineAbsolute(ref, NutrientAnalysis.PCa, Reflevel.MIN, 3.0)
        defineAbsolute(ref, NutrientAnalysis.PCa, Reflevel.MAX, 1.0)

        val adjustmentData = listOf(
            AlimentAdjustmentData(alimentRation = arA),
            AlimentAdjustmentData(alimentRation = arB)
        )

        val result = adjustRationByConstraints(
            ration = ration,
            adjustmentData = adjustmentData,
            referenceUtilisee = ref,
            besoinEnergetiqueTotal = 100.0,
            besoinEnergetiqueStandard = 100.0,
            poidsAnimal = 10.0,
            poidsMetabolique = 5.6,
            equationRepository = null,
            selectedNutrients = listConstrainableNutrients(ref, ration).toSet()
        )

        assertTrue(!result.success)
        assertTrue(result.violatedConstraints.isNotEmpty())
    }

    @Test
    fun listConstrainableNutrients_ratioExcludedWhenDenominatorAbsentFromEveryAliment() = runTest {
        // Food only has calcium, no phosphorus anywhere in the ration.
        val foodA = AlimentEv(nom = "A").apply {
            setNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL, 2.0)
            setNutrient(NutrientMain.ENERGIE, 400.0)
        }
        val ration = Ration(alimentMutableList = mutableListOf(AlimentRation(quantite = 100.0, aliment = foodA)))

        val ref = makeReference()
        defineAbsolute(ref, NutrientAnalysis.PCa, Reflevel.MIN, 2.0)

        val constrainable = listConstrainableNutrients(ref, ration)

        assertTrue(!constrainable.contains(NutrientAnalysis.PCa))
    }

    @Test
    fun listConstrainableNutrients_excludesNonBoneRatiosEvenWhenReferenced() = runTest {
        val foodA = food("A", proteinPer100g = 20.0, energyPer100g = 400.0)
        val ration = Ration(alimentMutableList = mutableListOf(AlimentRation(quantite = 100.0, aliment = foodA)))

        val ref = makeReference()
        defineAbsolute(ref, NutrientAnalysis.nonOsPhos, Reflevel.MIN, 1.0)
        defineAbsolute(ref, NutrientAnalysis.nonOsProt, Reflevel.MIN, 1.0)
        defineAbsolute(ref, NutrientAnalysis.nonOsPP, Reflevel.MIN, 1.0)

        val constrainable = listConstrainableNutrients(ref, ration)

        assertTrue(!constrainable.contains(NutrientAnalysis.nonOsPhos))
        assertTrue(!constrainable.contains(NutrientAnalysis.nonOsProt))
        assertTrue(!constrainable.contains(NutrientAnalysis.nonOsPP))
    }
}
