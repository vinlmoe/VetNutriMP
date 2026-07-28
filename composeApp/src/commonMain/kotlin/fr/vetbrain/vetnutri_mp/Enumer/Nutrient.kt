package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/** Interface représentant un nutriment Étendue pour le système d'équation */
interface Nutrient : Labelable {
    val ue: UnitEnum
    val coef: Int
    val unite: String

    fun getMNE(): MainNutrientEnum
}
