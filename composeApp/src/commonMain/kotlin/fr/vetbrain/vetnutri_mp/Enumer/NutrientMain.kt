package fr.vetbrain.vetnutri_mp.Enumer

/**
 * Énumération représentant les nutriments principaux. Cette classe remplace NutrientBaseExt en
 * intégrant toutes ses fonctionnalités.
 */
enum class NutrientMain(
        private val displayName: String,
        override val coef: Int,
        override val unite: String,
        override val ue: UnitEnum,
        override val label: String,
        val color: String
) : Nutrient {
    HUMIDITE("Humidité", 0, "g", UnitEnum.BUg, "humidité", "#5DFFFA"),
    PROTEINE("Protéines", 1, "g", UnitEnum.BUg, "protéine", "#3358FF"),
    LIPIDE("Lipides", 2, "g", UnitEnum.BUg, "lipide", "#F8FF5D"),
    GLUCIDE("Glucides", 3, "g", UnitEnum.BUg, "glucide", "#FF5D5D"),
    ENA("ENA", 4, "g", UnitEnum.BUg, "ENA", "#FF5D5D"),
    FIBRE("Fibres brutes", 5, "g", UnitEnum.BUg, "fibre", "#4BE715"),
    CELLULOSE("Cellulose brute", 6, "g", UnitEnum.BUg, "CELLULOSE", "#4BE715"),
    CENDRE("Cendres", 7, "g", UnitEnum.BUg, "cendre", "#820326"),
    ENERGIE("Énergie", 8, "kcal", UnitEnum.KCAL, "énergie", "#FF8C00"),
    SUCRE("Sucres", 9, "g", UnitEnum.BUg, "SUCRE", "#C3A900"),
    AMIDON("Amidon", 10, "g", UnitEnum.BUg, "AMIDON", "#820326"),
    FIBRESOL("Fibre soluble", 11, "g", UnitEnum.BUg, "FIBRESOL", "#20C300"),
    FIBRETOT("Fibre totale", 12, "g", UnitEnum.BUg, "FIBRETOT", "#1A7D07"),
    NDF("NDF", 13, "g", UnitEnum.BUg, "NDF", "#20C300"),
    ADF("ADFe", 14, "g", UnitEnum.BUg, "ADF", "#1A7D07");

    companion object {
        private val coefMap by lazy { entries.associateBy { it.coef } }
        private val labelMap by lazy { entries.associateBy { it.label.lowercase() } }

        fun getByCoef(coef: Int) = coefMap[coef]

        fun getByLabel(label: String): NutrientMain {
            return labelMap[label.lowercase()] ?: PROTEINE
        }

        fun isByLabel(label: String) = entries.any { it.label.equals(label, ignoreCase = true) }

        fun size() = entries.size
    }

    override fun getMNE() = MainNutrientEnum.BASE

    fun nameToString() = displayName
}
