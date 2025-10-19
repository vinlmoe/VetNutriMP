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
    HUMIDITE("Humidité", 0, "g", UnitEnum.BUg, "HUMIDITE", "#5DFFFA"),
    PROTEINE("Protéines", 1, "g", UnitEnum.BUg, "PROTEINE", "#3358FF"),
    LIPIDE("Lipides", 2, "g", UnitEnum.BUg, "LIPIDE", "#F8FF5D"),
    GLUCIDE("Glucides", 3, "g", UnitEnum.BUg, "GLUCIDE", "#FF5D5D"),
    ENA("ENA", 4, "g", UnitEnum.BUg, "ENA", "#FF5D5D"),
    FIBRE("Fibres brutes", 5, "g", UnitEnum.BUg, "FIBRE", "#4BE715"),
    CELLULOSE("Cellulose brute", 6, "g", UnitEnum.BUg, "CELLULOSE", "#4BE715"),
    CENDRE("Cendres", 7, "g", UnitEnum.BUg, "CENDRE", "#820326"),
    ENERGIE("Énergie", 8, "kcal", UnitEnum.KCAL, "ENERGIE", "#FF8C00"),
    SUCRE("Sucres", 9, "g", UnitEnum.BUg, "SUCRE", "#C3A900"),
    AMIDON("Amidon", 10, "g", UnitEnum.BUg, "AMIDON", "#820326"),
    FIBRESOL("Fibre soluble", 11, "g", UnitEnum.BUg, "FIBRESOL", "#20C300"),
    FIBRETOT("Fibre totale", 12, "g", UnitEnum.BUg, "FIBRETOT", "#1A7D07"),
    NDF("NDF", 13, "g", UnitEnum.BUg, "NDF", "#20C300"),
    ADF("ADFe", 14, "g", UnitEnum.BUg, "ADF", "#1A7D07");

    companion object {
        // Optimisation : utiliser des maps statiques lazy pour réduire la complexité du compilateur
        private val coefMap by lazy { entries.associateBy { it.coef } }
        private val labelMap by lazy { entries.associateBy { it.label.lowercase() } }

        // Cache pour les recherches fréquentes avec gestion automatique de la mémoire
        private val coefCache = mutableMapOf<Int, NutrientMain?>()
        private val labelCache = mutableMapOf<String, NutrientMain?>()
        private val maxCacheSize = 15

        // Compteurs pour optimiser les performances
        private var cacheHits = 0
        private var cacheMisses = 0

        fun getByCoef(coef: Int): NutrientMain? {
            // Vérifier le cache d'abord
            coefCache[coef]?.let {
                cacheHits++
                return it
            }

            cacheMisses++
            val result = coefMap[coef]

            // Nettoyer et ajouter au cache si nécessaire (stratégie simple)
            if (coefCache.size >= maxCacheSize) {
                // Supprimer les entrées les moins utilisées (approximation)
                if (cacheHits > cacheMisses * 2) {
                    coefCache.clear()
                    cacheHits = 0
                    cacheMisses = 0
                }
            }

            if (coefCache.size < maxCacheSize && result != null) {
                coefCache[coef] = result
            }

            return result
        }

        fun getByLabel(label: String): NutrientMain {
            val lowerLabel = label.lowercase()

            // Vérifier le cache d'abord
            labelCache[lowerLabel]?.let {
                cacheHits++
                return it ?: PROTEINE
            }

            cacheMisses++
            val result = labelMap[lowerLabel]

            // Nettoyer et ajouter au cache si nécessaire
            if (labelCache.size >= maxCacheSize) {
                if (cacheHits > cacheMisses * 2) {
                    labelCache.clear()
                    cacheHits = 0
                    cacheMisses = 0
                }
            }

            if (labelCache.size < maxCacheSize && result != null) {
                labelCache[lowerLabel] = result
            }

            return result ?: PROTEINE
        }

        fun isByLabel(label: String) = labelMap.containsKey(label.lowercase())

        fun size() = entries.size

        // Méthode utilitaire pour vider le cache si nécessaire
        fun clearCache() {
            coefCache.clear()
            labelCache.clear()
        }
    }

    override fun getMNE() = MainNutrientEnum.BASE

    fun nameToString() = displayName
}
