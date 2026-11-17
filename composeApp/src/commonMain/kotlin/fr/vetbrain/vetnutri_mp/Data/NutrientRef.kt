package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum

/** Besoin nutritionnel (entrée plate) lié à une `ReferenceEv`. */
data class NutrientRef(
        val id: String = "",
        val referenceEvId: String = "",
        val name: String = "",
        val value: String = "",
        val nutrientType: MainNutrientEnum,
        val nutrientCode: Int = 0,
        val unitReq: UnitReqEnum = UnitReqEnum.PERKG,
        val unitEnum: UnitEnum = UnitEnum.BUg,
        val biblioRef: BiblioRef? = null
)
