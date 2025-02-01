package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumerise.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumerise.UnitEnum

interface Nutrient {
    val ue: UnitEnum
    fun getMNE(): MainNutrientEnum
    val label:String
    val coef:Int
    val unite: String

}