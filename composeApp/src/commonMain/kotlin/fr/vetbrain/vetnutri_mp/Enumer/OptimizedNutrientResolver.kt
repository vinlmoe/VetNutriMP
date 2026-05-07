package fr.vetbrain.vetnutri_mp.Enumer

/**
 * Version optimisée du NutrientResolver pour éviter les problèmes d'optimisation statique. Utilise
 * des maps pré-calculées au lieu de recherches linéaires répétées.
 */
object OptimizedNutrientResolver {

    // Cache simple pour les résolutions répétitives
    private val resolutionCache = mutableMapOf<String, Nutrient?>()

    // Maps pré-calculées pour accès O(1)
    private val labelToNutrient: Map<String, Nutrient> by lazy { initializeLabelMap() }
    private val altLabelToNutrient: Map<String, Nutrient> by lazy { initializeAltLabelMap() }
    private val normalizedLabelToStandard: Map<String, String> by lazy {
        initializeNormalizationMap()
    }

    /** Initialise la map label -> nutriment avec une seule passe sur toutes les énumérations */
    private fun initializeLabelMap(): Map<String, Nutrient> {
        return buildMap {
            // Une seule passe sur chaque énumération
            NutrientMain.entries.forEach { put(it.label.lowercase(), it) }
            NutrientMacro.entries.forEach { put(it.label.lowercase(), it) }
            NutrientMin.entries.forEach { put(it.label.lowercase(), it) }
            NutrientLipid.entries.forEach { put(it.label.lowercase(), it) }
            NutrientVitam.entries.forEach { nutrient ->
                put(nutrient.label.lowercase(), nutrient)
                // Ajouter aussi les altLabels
                nutrient.altLabels.forEach { altLabel -> put(altLabel.lowercase(), nutrient) }
            }
            NutrientOther.entries.forEach { put(it.label.lowercase(), it) }
            AAEnum.entries.forEach { put(it.label.lowercase(), it) }
            NutrientEnergy.entries.forEach { put(it.label.lowercase(), it) }
            NutrientAnalysis.entries.forEach { put(it.label.lowercase(), it) }
        }
    }

    /** Initialise la map des labels alternatifs (plus complexe, mappings spécifiques) */
    private fun initializeAltLabelMap(): Map<String, Nutrient> {
        return mapOf(
                // Cas spéciaux de CHOL (cholestérol vs chlore)
                "chol" to NutrientLipid.CHOL,

                // Mappings spécifiques identifiés dans le code original
                "fibretot" to NutrientMain.FIBRETOT,
                "fibresol" to NutrientMain.FIBRESOL,
                "fibrinso" to NutrientMain.FIBRETOT, // Approximation

                // Vitamines avec mappings complexes
                "vitamina" to NutrientVitam.VITA,
                "vitamin_a" to NutrientVitam.VITA,
                "retinol" to NutrientVitam.RETINOL,
                "betacar" to NutrientVitam.BETACAR,
                "vitaminc" to NutrientVitam.VITC,
                "vitamin_c" to NutrientVitam.VITC,
                "vitamind" to NutrientVitam.VITD,
                "vitamin_d" to NutrientVitam.VITD,
                "cholecalciferol" to NutrientVitam.VITD,
                "vitaminee" to NutrientVitam.VITE,
                "vitamin_e" to NutrientVitam.VITE,
                "tocopherol" to NutrientVitam.VITE,
                "vitaminek" to NutrientVitam.VITK,
                "vitamin_k" to NutrientVitam.VITK,
                "phylloquinone" to NutrientVitam.VITK,

                // Vitamines B complexes
                "vitamineb1" to NutrientVitam.VITB1,
                "vitamin_b1" to NutrientVitam.VITB1,
                "thiamine" to NutrientVitam.VITB1,
                "vitamineb2" to NutrientVitam.VITB2,
                "vitamin_b2" to NutrientVitam.VITB2,
                "riboflavine" to NutrientVitam.VITB2,
                "riboflavin" to NutrientVitam.VITB2,

                // Continuer pour toutes les vitamines B...
                "vitamineb3" to NutrientVitam.VITB3,
                "vitamin_b3" to NutrientVitam.VITB3,
                "niacine" to NutrientVitam.VITB3,
                "niacin" to NutrientVitam.VITB3,

                // Ajouter d'autres mappings selon les besoins
                )
    }

    /** Initialise la map de normalisation pour les labels complexes */
    private fun initializeNormalizationMap(): Map<String, String> {
        return mapOf(
                // Nutriments principaux
                "humidite" to "HUMIDITE",
                "humidité" to "HUMIDITE",
                "water" to "HUMIDITE",
                "h2o" to "HUMIDITE",
                "agua" to "HUMIDITE",
                "eau" to "HUMIDITE",
                "moisture" to "HUMIDITE",
                "proteine" to "PROTEINE",
                "protéine" to "PROTEINE",
                "proteines" to "PROTEINE",
                "protéines" to "PROTEINE",
                "protein" to "PROTEINE",
                "proteins" to "PROTEINE",
                "mat" to "PROTEINE",
                "prot" to "PROTEINE",
                "cp" to "PROTEINE",
                "lipide" to "LIPIDE",
                "lipides" to "LIPIDE",
                "fat" to "LIPIDE",
                "fats" to "LIPIDE",
                "ee" to "LIPIDE",
                "matiere_grasse" to "LIPIDE",
                "matières_grasses" to "LIPIDE",
                "glucide" to "GLUCIDE",
                "glucides" to "GLUCIDE",
                "carbohydrate" to "GLUCIDE",
                "carbohydrates" to "GLUCIDE",
                "carbs" to "GLUCIDE",
                "cho" to "GLUCIDE",
                "cendre" to "CENDRE",
                "cendres" to "CENDRE",
                "ash" to "CENDRE",
                "ashes" to "CENDRE",
                "mm" to "CENDRE",
                "minerals" to "CENDRE",
                "energie" to "ENERGIE",
                "énergie" to "ENERGIE",
                "energy" to "ENERGIE",
                "calories" to "ENERGIE",
                "eb" to "ENERGIE",
                "ed" to "ENERGIE",
                "em" to "ENERGIE",
                "me" to "ENERGIE",
                "de" to "ENERGIE",
                "ge" to "ENERGIE",

                // Ajouter d'autres mappings selon les besoins
                )
    }

    /** Résout un nutriment de manière optimisée O(1) */
    fun resolve(label: String): Nutrient? {
        if (label.isBlank()) return null

        val key = label.lowercase()

        // Vérifier le cache d'abord
        resolutionCache[key]?.let { return it }

        // Normaliser le label si nécessaire
        val normalizedLabel = normalizedLabelToStandard[key] ?: label

        // Chercher dans les maps pré-calculées
        val result = labelToNutrient[normalizedLabel.lowercase()]
                ?: altLabelToNutrient[normalizedLabel.lowercase()]
                ?: fuzzyMatchOptimized(normalizedLabel)

        // Mettre en cache le résultat
        resolutionCache[key] = result

        return result
    }

    /** Version fuzzy matching optimisée (utilisée seulement si pas trouvée dans les maps) */
    private fun fuzzyMatchOptimized(label: String): Nutrient? {
        // Recherche par similarité dans les labels principaux seulement
        val candidates = labelToNutrient.keys.filter { it.contains(label, ignoreCase = true) }

        return if (candidates.isNotEmpty()) {
            // Prendre le plus similaire
            val bestMatch = candidates.minByOrNull { levenshteinDistance(it, label) }
            bestMatch?.let { labelToNutrient[it] }
        } else {
            null
        }
    }

    /** Distance de Levenshtein optimisée */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        val matrix = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) matrix[i][0] = i
        for (j in 0..s2.length) matrix[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                matrix[i][j] =
                        minOf(
                                matrix[i - 1][j] + 1, // suppression
                                matrix[i][j - 1] + 1, // insertion
                                matrix[i - 1][j - 1] + cost // substitution
                        )
            }
        }

        return matrix[s1.length][s2.length]
    }

    /** Normalise un label de manière simple et efficace */
    fun normalizeLabel(label: String): String {
        val trimmed = label.trim().replace("[\\[\\]\"]".toRegex(), "")

        // Nettoyer et normaliser
        val cleaned = trimmed.replace("[^A-Za-z0-9]".toRegex(), "").lowercase()

        return normalizedLabelToStandard[cleaned] ?: cleaned.uppercase()
    }

    /** Statistiques du cache pour monitoring */
    fun getCacheStats(): MapStats {
        return MapStats(
            size = resolutionCache.size,
            maxSize = 1000,
            hitRate = 0.0 // Simplified for non-suspend version
        )
    }

    /** Vide le cache si nécessaire */
    fun clearCache() {
        resolutionCache.clear()
    }

    data class MapStats(
        val size: Int,
        val maxSize: Int,
        val hitRate: Double
    )
}
