package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumerise.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumerise.UnitEnum

interface Nutrient : Labelable{
    val ue: UnitEnum
    fun getMNE(): MainNutrientEnum
    val coef:Int
    val unite: String

}