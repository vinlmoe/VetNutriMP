package fr.vetbrain.vetnutri_mp.Enumer

enum class NutrientVitam(
        override val label: String,
        override val coef: Int,
        override val unite: String,
        override val ue: UnitEnum,
        val abr: String
) : Nutrient {
    VITA("Vitamine A", 0, "UI", UnitEnum.AUui, "Vit A"),
    VITC("Vitamine C", 1, "mg", UnitEnum.BUmg, "Vit C"),
    VITD("Vitamine D", 2, "UI", UnitEnum.DUui, "Vit D"),
    VITE("Vitamine E", 3, "UI", UnitEnum.EUui, "Vit E"),
    VITK("Vitamine K", 4, "mg", UnitEnum.BUmg, "Vit K"),
    VITB1("Thiamine (B1)", 5, "mg", UnitEnum.BUmg, "Vit B1"),
    VITB2("Riboflavine (B2)", 6, "mg", UnitEnum.BUmg, "Vit B2"),
    VITB3("Nicotinamide, Niacine (B3/PP)", 7, "mg", UnitEnum.BUmg, "Vit B3"),
    VITB5("Acide pantothénique (B5)", 8, "mg", UnitEnum.BUmg, "Vit B5"),
    VITB6("Pyridoxine (B6)", 9, "mg", UnitEnum.BUmg, "Vit B6"),
    VITB8("Biotine (B8)", 10, "µg", UnitEnum.BUmu, "Vit B8"),
    VITB9("Acide folique (B9)", 11, "µg", UnitEnum.BUmu, "Vit B9"),
    VITB12("Cyanocobalamine (B12)", 12, "µg", UnitEnum.BUmu, "Vit B12"),
    CHOLINE("Choline", 13, "mg", UnitEnum.BUmg, "CHL"),
    RETINOL("Retinol", 14, "µg", UnitEnum.BUmu, "Ret"),
    BETACAR("Bêta-carotène", 15, "µg", UnitEnum.BUmu, "Bet");


    override fun getMNE() = MainNutrientEnum.VITAM

    companion object {
        private val coefMap = values().associateBy { it.coef }
        private val labelMap = values().associateBy { it.label }

        fun isByLabel(label: String) = values().any { it.label == label }
        fun getByCoef(coef: Int) = coefMap[coef]
        fun getByLabel(label: String) = labelMap[label] ?: VITA
        fun size() = values().size
    }
}
