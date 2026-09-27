package fr.vetbrain.vetnutri_mp.View.AnalNut

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class MultiNutrientAdjustmentDialogTest {

    @Test
    fun adjustmentNeedMultiplier_celluloseTargetsFiveTimesTheReferenceNeed() {
        assertEquals(5.0, adjustmentNeedMultiplier(NutrientMain.CELLULOSE))
        assertEquals(1.0, adjustmentNeedMultiplier(NutrientMacro.CAL))
    }

    private fun referenceFor(vararg nutrients: fr.vetbrain.vetnutri_mp.Enumer.Nutrient): ReferenceEv =
            ReferenceEv().also { reference ->
                nutrients.forEach { nutrient ->
                    reference.definirNutriment(
                            1.0,
                            nutrient,
                            Reflevel.MIN,
                            UnitReqEnum.PERKG,
                            BiblioRef(firstAuthor = "Test", year = 2026, consistent = 1)
                    )
                }
            }

    @Test
    fun suggestDefaultTargetNutrient_sodiumSuperieurA20Pourcent_selectionneNa() {
        val sel = AlimentEv(nom = "Sel").also {
            it.setNutrient(NutrientMacro.NA, 39.1)
        }
        val reference = ReferenceEv().also {
            it.definirNutriment(
                    1.0,
                    NutrientMacro.NA,
                    Reflevel.MIN,
                    UnitReqEnum.PERKG,
                    BiblioRef(firstAuthor = "Test", year = 2026, consistent = 1)
            )
        }

        val target = suggestDefaultTargetNutrient(
                AlimentRation(aliment = sel, quantite = 1.0),
                reference
        )

        assertEquals(NutrientMacro.NA.label, target)
    }

    @Test
    fun suggestDefaultTargetNutrient_haricotHumideEtFibre_neSelectionnePasCalcium() {
        val haricotVertAppertise = AlimentEv(nom = "Haricot vert, appertisé, égoutté").also {
            it.setNutrient(NutrientMain.HUMIDITE, 92.0)
            it.setNutrient(NutrientMain.CENDRE, 1.22)
            it.setNutrient(NutrientMacro.CAL, 0.0427)
            it.setNutrient(NutrientMain.CELLULOSE, 3.36)
        }

        val target = suggestDefaultTargetNutrient(
                AlimentRation(aliment = haricotVertAppertise, quantite = 1.0),
                referenceFor(NutrientMacro.CAL, NutrientMain.CELLULOSE)
        )

        assertEquals(NutrientMain.CELLULOSE.label, target)
    }

    @Test
    fun suggestDefaultTargetNutrient_sourceConcentreeEnCalcium_selectionneCalcium() {
        val poudreDeCoquilles = AlimentEv(nom = "Poudre de coquilles").also {
            it.setNutrient(NutrientMacro.CAL, 37.0)
        }

        val target = suggestDefaultTargetNutrient(
                AlimentRation(aliment = poudreDeCoquilles, quantite = 1.0),
                referenceFor(NutrientMacro.CAL)
        )

        assertEquals(NutrientMacro.CAL.label, target)
    }
}
