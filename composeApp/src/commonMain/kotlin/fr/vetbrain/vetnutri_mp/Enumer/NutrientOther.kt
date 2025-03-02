package fr.vetbrain.vetnutri_mp.Enumer

enum class NutrientOther(
        private val displayName: String,
        override val coef: Int,
        override val unite: String,
        override val ue: UnitEnum,
        override val label: String
) : Nutrient {
    TAURINE("Taurine", 0, "g", UnitEnum.BUg, "TAURINE"),
    CARNITINE("L-Carnitine", 1, "mg", UnitEnum.BUmg, "CARNITINE"),
    FOS("FOS", 2, "g", UnitEnum.BUg, "FOS"),
    MOS("MOS", 3, "g", UnitEnum.BUg, "MOS"),
    SUCR("Saccharose", 5, "g", UnitEnum.BUg, "SACC"),
    FRUCT("Fructose", 6, "g", UnitEnum.BUg, "FRUCT"),
    LACT("Lactose", 7, "g", UnitEnum.BUg, "LACTO"),
    MALT("Maltose", 8, "g", UnitEnum.BUg, "MALT"),
    AcOx("Acide Oxalique", 9, "mg", UnitEnum.BUmg, "AcOx"),
    GAL("Galactose", 10, "g", UnitEnum.BUg, "GAL"),
    GLUCOSE("Glucose", 11, "g", UnitEnum.BUg, "GLUCOSE"),
    DEXTROSE("Dextrose", 12, "g", UnitEnum.BUg, "DEXTROSE");

    init {
        // Validation des invariants
        require(label.isNotBlank()) { "Label must not be blank" }
        require(coef >= 0) { "Coefficient must be positive (invalid: $coef)" }
        require(unite.isNotBlank()) { "Unit must not be blank" }
    }

    companion object {
        private val coefMap by lazy { entries.associateBy { it.coef } }
        private val labelMap by lazy { entries.associateBy { it.label } }

        fun getByCoef(coef: Int) =
                coefMap[coef] ?: throw IllegalArgumentException("No NutrientOther with coef=$coef")

        fun getByLabel(label: String) = labelMap[label]

        fun isByLabel(label: String) = label in labelMap
        fun size() = entries.size
    }

    override fun getMNE() = MainNutrientEnum.OTHER
    fun nameToString() = displayName
}
