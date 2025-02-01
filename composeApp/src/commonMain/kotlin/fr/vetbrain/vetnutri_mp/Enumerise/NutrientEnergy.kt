package fr.vetbrain.vetnutri_mp.Enumerise

import fr.vetbrain.vetnutri_mp.Data.Nutrient


enum class NutrientEnergy(
    private val displayName: String,
    override val coef: Int,

    override val unite: String,
    override val label: String,
    val colr: String
) : Nutrient {
    TOT("tot", 0, "kcal", "TOT", "#5DFFFA"),
    DE("iDE", 1, "kcal/100g", "DE", "#3358FF"),
    DEDM("DEDM", 2, "Kcal/100g", "DEDM", "#F8FF5D"),
    K("K", 4, "", "Kener", "#F8FF5D"),
    PERC("PERC", 4, "%", "PERC", "#F8FF5D"),
    BEE("BEE", 4, "kcal", "BEE", "#F8FF5D"),
    BE("BE", 4, "kcal", "BE", "#F8FF5D"),
    MW("MW", 4, "kg", "MW", "#F8FF5D"),
    KPRED("KPRend", 4, "", "KPRED", "#F8FF5D");

    companion object {
        private val coefMap by lazy {
            entries.groupBy { it.coef }
                .onEach { (k, v) -> if (v.size > 1) error("Duplicates for coef $k") }
        }

        fun getByCoef(coef: Int) = coefMap[coef]?.firstOrNull()
        fun getAllByCoef(coef: Int) = coefMap[coef] ?: emptyList()
    }
    override val ue  = UnitEnum.NO

    override fun getMNE() = MainNutrientEnum.ENERGIE
    fun nameToString() = displayName
}