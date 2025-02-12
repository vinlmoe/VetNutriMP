package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

interface Nutrient : Labelable {
    val ue: UnitEnum
    fun getMNE(): MainNutrientEnum
    val coef: Int
    val unite: String
}
