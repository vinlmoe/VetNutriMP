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
    HUMIDITE("Humidité", 0, "g", UnitEnum.BUg, "HUMIDITE", "#9CC9DD"),
    PROTEINE("Protéines", 1, "g", UnitEnum.BUg, "PROTEINE", "#008B8B"),
    LIPIDE("Lipides", 2, "g", UnitEnum.BUg, "LIPIDE", "#E0A030"),
    GLUCIDE("Glucides", 3, "g", UnitEnum.BUg, "GLUCIDE", "#C0645A"),
    ENA("ENA", 4, "g", UnitEnum.BUg, "ENA", "#C0645A"),
    CELLULOSE("Cellulose brute", 6, "g", UnitEnum.BUg, "CELLULOSE", "#527A3A"),
    CENDRE("Cendres", 7, "g", UnitEnum.BUg, "CENDRE", "#8C8C8C"),
    ENERGIE("Énergie", 8, "kcal", UnitEnum.KCAL, "ENERGIE", "#FF8C00"),
    SUCRE("Sucres", 9, "g", UnitEnum.BUg, "SUCRE", "#C3A900"),
    AMIDON("Amidon", 10, "g", UnitEnum.BUg, "AMIDON", "#820326"),
    FIBRESOL("Fibre soluble", 11, "g", UnitEnum.BUg, "FIBRESOL", "#20C300"),
    FIBRETOT("Fibre totale", 12, "g", UnitEnum.BUg, "FIBRETOT", "#1A7D07"),
    NDF("NDF", 13, "g", UnitEnum.BUg, "NDF", "#20C300"),
    ADF("ADFe", 14, "g", UnitEnum.BUg, "ADF", "#1A7D07"),
    DM("Matière sèche", 15, "g", UnitEnum.BUg, "DM", "#5DFFFA");
    companion object {
        // Maps statiques optimisées - accès O(1)
        private val coefMap by lazy { entries.associateBy { it.coef } }
        private val labelMap by lazy { entries.associateBy { it.label.lowercase() } }

        /** Recherche par coefficient - O(1) thread-safe */
        fun getByCoef(coef: Int): NutrientMain? = coefMap[coef]

        /** Recherche par label - O(1) thread-safe */
        fun getByLabel(label: String): NutrientMain? = labelMap[label.lowercase()]

        /** Vérification d'existence par label - O(1) */
        fun isByLabel(label: String): Boolean = labelMap.containsKey(label.lowercase())

        /** Taille de l'énumération */
        fun size(): Int = entries.size
    }

    override fun getMNE() = MainNutrientEnum.BASE

    fun nameToString() = displayName
}
