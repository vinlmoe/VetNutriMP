package fr.vetbrain.vetnutri_mp.Enumer

enum class EquationKind(
        private val description: String,
        private val nom: String,
        private val uuid: Int
) {
    ENERGYNEED("energyNeedDesc", "energyNeed", 0),
    ENERGYDENSITY("energyDensityDesc", "energyDensity", 1),
    MW("metabolicWeightDesc", "metabolicWeight", 2),
    INDICATOR("indicatorDesc", "indicator", 3),
    NEED("NeedDesc", "NeedEq", 4),
    COMPLEMENTARY_NUTRIENT("complementaryNutrientDesc", "complementaryNutrient", 5),
      ENERCOMP("energyCompDesc", "energyComp", 6);

    fun getDescription() = description
    fun getNom() = nom
    fun getUuid() = uuid

    companion object {
        fun getById(i: Int) = values().find { it.uuid == i } ?: ENERGYNEED
    }
}
