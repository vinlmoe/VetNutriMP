package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/**
 * Énumération des types d'équations Équivalent à EquationKind mais avec une interface
 * supplémentaire Labelable
 */
enum class EquationType(
        val description: String,
        val nom: String,
        val uuid: Int,
        override val label: String
) : Labelable {
    ENERGYNEED("energyNeedDesc", "energyNeed", 0, "Besoin énergétique"),
    ENERGYDENSITY("energyDensityDesc", "energyDensity", 1, "Densité énergétique"),
    MW("metabolicWeightDesc", "metabolicWeight", 2, "Poids métabolique"),
    INDICATOR("indicatorDesc", "indicator", 3, "Indicateur"),
    NEED("NeedDesc", "NeedEq", 4, "Besoin nutritionnel"),
    COMPLEMENTARY_NUTRIENT("complementaryNutrientDesc", "complementaryNutrient", 5, "Nutriment complémentaire");

    companion object {
        fun getById(id: Int) = values().find { it.uuid == id } ?: ENERGYNEED
    }

    // Conversion vers EquationKind
    fun toEquationKind(): EquationKind {
        return when (this) {
            ENERGYNEED -> EquationKind.ENERGYNEED
            ENERGYDENSITY -> EquationKind.ENERGYDENSITY
            MW -> EquationKind.MW
            INDICATOR -> EquationKind.INDICATOR
            NEED -> EquationKind.NEED
            COMPLEMENTARY_NUTRIENT -> EquationKind.COMPLEMENTARY_NUTRIENT
        }
    }

    override fun toString(): String {
        return label
    }
}
