package fr.vetbrain.vetnutri_mp.Enumerise

import fr.vetbrain.vetnutri_mp.Data.Nutrient

enum class NutrientMin(
    private val displayName: String,
    override val coef: Int,
    override val unite: String,
    override val ue: UnitEnum,
    override val label: String,
    val abr: String
) : Nutrient {
    FE("Fer", 0, "mg", UnitEnum.BUmg, "FE", "Fe"),
    CU("Cuivre", 1, "mg", UnitEnum.BUmg, "CU", "Cu"),
    ZN("Zinc", 2, "mg", UnitEnum.BUmg, "ZN", "Zn"),
    MN("Manganèse", 3, "mg", UnitEnum.BUmg, "MN", "Mn"),
    I("Iode", 4, "µg", UnitEnum.BUmu, "I", "I"),
    SE("Sélénium", 5, "µg", UnitEnum.BUmu, "SE", "Se");

    init {
        // Validation des invariants
        require(label.length == 2) { "Label must be exactly 2 characters (invalid: $label)" }
        require(abr.length in 1..2) { "Abbreviation must be 1-2 characters (invalid: $abr)" }
        require(coef >= 0) { "Coefficient must be positive (invalid: $coef)" }
        require(unite.isNotBlank()) { "Unit must not be blank" }
    }

    companion object {
        private val coefMap by lazy { entries.associateBy { it.coef } }
        private val labelMap by lazy { entries.associateBy { it.label } }

        fun getByCoef(coef: Int) = coefMap[coef]
            ?: throw IllegalArgumentException("No NutrientMin with coef=$coef")

        fun getByLabel(label: String) = labelMap[label]
            ?: throw IllegalArgumentException("No NutrientMin with label=$label")

        fun isByLabel(label: String) = label in labelMap
        fun size() = entries.size
    }

    override fun getMNE() = MainNutrientEnum.MIN
    fun nameToString() = displayName

}