package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.*
import kotlin.random.Random
import kotlinx.serialization.Serializable

@Serializable
data class AlimentEv(
        var uuid: String = Random.nextInt().toString(),
        var nom: String = "",
        var presentation: String = "",
        var marque: String = "",
        var gamme: String = "",
        var ingredients: String = "",
        var typeAliment: FoodKind = FoodKind.MEN,
        var group: GroupAlim = GroupAlim.AUTRES,
        internal val valMap: MutableMap<Nutrient, Double> = mutableMapOf(),
        var prix: Double = 0.0,
        var categoriePrix: String = "i",
        var indication: ArrayList<String> = ArrayList(),
        var especes: ArrayList<String> = ArrayList(),
        var quantInt: Float = 0.0f,
        var cont: ContEnum = ContEnum.NO,
        var deprecated: Boolean = false,
        var dataB: String = "6"
) {
        fun getNutrient(nutrient: Nutrient): Double {
                return valMap[nutrient] ?: 0.0
        }

        fun isNutrient(nutrient: Nutrient): Boolean {
                return valMap.containsKey(nutrient)
        }

        fun setNutrient(nutrient: Nutrient, value: Double) {
                valMap[nutrient] = value
        }
}
