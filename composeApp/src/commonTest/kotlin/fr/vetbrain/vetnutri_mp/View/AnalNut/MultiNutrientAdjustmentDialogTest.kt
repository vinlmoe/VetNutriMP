package fr.vetbrain.vetnutri_mp.View.AnalNut

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class MultiNutrientAdjustmentDialogTest {

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
}
