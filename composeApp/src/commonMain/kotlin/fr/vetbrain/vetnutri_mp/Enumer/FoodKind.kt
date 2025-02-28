package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable
import kotlinx.serialization.Serializable

@Serializable
enum class FoodKind(val coef: Int, override val label: String) : Labelable {
    ALL(100, "all"),
    COMPLET(2, "complete"),
    COMPLEMENTAIRE(3, "complementary"),
    MEN(4, "household"),
    BARF(5, "barf");

    fun nameToString() = label

    companion object {
        fun byCoef(coef: Int): FoodKind = entries.find { it.coef == coef } ?: MEN

        fun valuesExcept(): List<FoodKind> = entries.filter { it != ALL }
    }

    override fun toString() = label
}
