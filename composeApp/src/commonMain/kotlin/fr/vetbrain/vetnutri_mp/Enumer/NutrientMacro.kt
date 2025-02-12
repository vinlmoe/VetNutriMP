package fr.vetbrain.vetnutri_mp.Enumer

enum class NutrientMacro(
        private val displayName: String,
        override val coef: Int,
        override val unite: String,
        override val ue: UnitEnum,
        override val label: String,
        val abr: String
) : Nutrient {
    CAL("Calcium", 0, "g", UnitEnum.BUg, "CAL", "Ca"),
    PHOS("Phosphore", 1, "g", UnitEnum.BUg, "PHOS", "P"),
    MG("Magnésium", 2, "g", UnitEnum.BUg, "MG", "Mg"),
    NA("Sodium", 3, "g", UnitEnum.BUg, "NA", "Na"),
    K("Potassium", 4, "g", UnitEnum.BUg, "K", "K"),
    CHL("Chlore", 5, "g", UnitEnum.BUg, "CHL", "Cl");

    init {
        // Validation des invariants

        require(abr.length in 1..2) { "Abbreviation must be 1-2 characters (invalid: $abr)" }
        require(coef >= 0) { "Coefficient must be positive (invalid: $coef)" }
        require(unite.isNotBlank()) { "Unit must not be blank" }
    }

    companion object {
        private val coefMap by lazy { entries.associateBy { it.coef } }
        private val labelMap by lazy { entries.associateBy { it.label } }

        fun getByCoef(coef: Int) =
                coefMap[coef] ?: throw IllegalArgumentException("No NutrientMacro with coef=$coef")

        fun getByLabel(label: String) =
                labelMap[label]
                        ?: throw IllegalArgumentException("No NutrientMacro with label=$label")

        fun isByLabel(label: String) = label in labelMap
        fun size() = entries.size
    }

    override fun getMNE() = MainNutrientEnum.MACRO
    fun nameToString() = displayName
}
