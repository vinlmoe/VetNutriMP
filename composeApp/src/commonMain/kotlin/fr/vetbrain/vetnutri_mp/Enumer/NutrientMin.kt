package fr.vetbrain.vetnutri_mp.Enumer

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
        require(unite.isNotBlank()) { "Unit must not be blank" }
    }

    companion object {
        private val coefMap by lazy { entries.associateBy { it.coef } }
        private val labelMap by lazy { entries.associateBy { it.label } }

        fun getByCoef(coef: Int) =
                coefMap[coef] ?: throw IllegalArgumentException("No NutrientMin with coef=$coef")

        fun getByLabel(label: String) = labelMap[label]

        fun isByLabel(label: String) = label in labelMap
        fun size() = entries.size
    }

    override fun getMNE() = MainNutrientEnum.MIN
    fun nameToString() = displayName
}
