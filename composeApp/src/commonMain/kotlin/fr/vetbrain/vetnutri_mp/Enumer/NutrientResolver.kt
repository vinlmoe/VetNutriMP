package fr.vetbrain.vetnutri_mp.Enumer

/** Classe utilitaire permettant de résoudre les nutriments à partir de leur label. */
object NutrientResolver {

    /**
     * Résout un nutriment à partir de son label. Cette fonction cherche dans toutes les classes
     * d'énumération de nutriments pour trouver celle qui correspond au label donné.
     *
     * @param label Le label du nutriment à rechercher
     * @return Le nutriment correspondant au label, ou null si aucun nutriment ne correspond
     */
    fun AllNutrientResolver(label: String): Nutrient? {
        // Nettoyage plus approfondi du label
        val cleanedLabel = normalizeLabel(label)

        println("Résolution du nutriment: original='$label', normalisé='$cleanedLabel'")

        // Vérifier dans NutrientMain
        if (NutrientMain.isByLabel(cleanedLabel)) {
            val nutrient = NutrientMain.getByLabel(cleanedLabel)
            println("  → Résolu comme NutrientMain: ${nutrient?.label}")
            return nutrient
        }

        // Vérifier dans NutrientMacro
        try {
            if (NutrientMacro.isByLabel(cleanedLabel)) {
                val nutrient = NutrientMacro.getByLabel(cleanedLabel)
                println("  → Résolu comme NutrientMacro: ${nutrient?.label}")
                return nutrient
            }
        } catch (e: IllegalArgumentException) {
            // Ignorer, continuer à chercher dans les autres enums
        }

        // Vérifier dans NutrientMin
        try {
            if (NutrientMin.isByLabel(cleanedLabel)) {
                val nutrient = NutrientMin.getByLabel(cleanedLabel)
                println("  → Résolu comme NutrientMin: ${nutrient?.label}")
                return nutrient
            }
        } catch (e: IllegalArgumentException) {
            // Ignorer, continuer à chercher dans les autres enums
        }

        // Vérifier dans NutrientLipid
        if (NutrientLipid.isByLabel(cleanedLabel)) {
            val nutrient = NutrientLipid.getByLabel(cleanedLabel)
            println("  → Résolu comme NutrientLipid: ${nutrient?.label}")
            return nutrient
        }

        // Vérification standard dans NutrientVitam
        if (NutrientVitam.isByLabel(cleanedLabel)) {
            val nutrient = NutrientVitam.getByLabel(cleanedLabel)
            println("  → Résolu comme NutrientVitam: ${nutrient?.label}")
            return nutrient
        }

        // Vérifier dans NutrientOther
        try {
            if (NutrientOther.isByLabel(cleanedLabel)) {
                val nutrient = NutrientOther.getByLabel(cleanedLabel)
                println("  → Résolu comme NutrientOther: ${nutrient?.label}")
                return nutrient
            }
        } catch (e: IllegalArgumentException) {
            // Ignorer, continuer à chercher dans les autres enums
        }

        try {
            if (AAEnum.isByLabel(cleanedLabel)) {
                val nutrient = AAEnum.getByLabel(cleanedLabel)
                println("  → Résolu comme AAEnum: ${nutrient?.label}")
                return nutrient
            }
        } catch (e: IllegalArgumentException) {
            // Ignorer, continuer à chercher dans les autres enums
        }

        // Vérifier dans NutrientEnergy - pas de isByLabel direct, utiliser une approche différente
        val nutrientEnergy = NutrientEnergy.entries.find { it.label == cleanedLabel }
        if (nutrientEnergy != null) {
            println("  → Résolu comme NutrientEnergy: ${nutrientEnergy.label}")
            return nutrientEnergy
        }

        // Vérifier dans NutrientAnalysis - pas de isByLabel direct, utiliser une approche
        // différente
        val nutrientAnalysis = NutrientAnalysis.entries.find { it.label == cleanedLabel }
        if (nutrientAnalysis != null) {
            println("  → Résolu comme NutrientAnalysis: ${nutrientAnalysis.label}")
            return nutrientAnalysis
        }

        // Aucun nutriment trouvé
        println("  × Non résolu. Labels disponibles dans les énumérations:")
        println("    - NutrientMain: ${NutrientMain.entries.map { it.label }}")
        println("    - NutrientVitam: ${NutrientVitam.entries.map { it.label }}")
        println("    - NutrientMacro: ${NutrientMacro.entries.map { it.label }}")
        println("    - NutrientMin: ${NutrientMin.entries.map { it.label }}")
        println("    - NutrientLipid: ${NutrientLipid.entries.map { it.label }}")
        println("    - NutrientOther: ${NutrientOther.entries.map { it.label }}")

        return null
    }

    /**
     * Normalise le label d'un nutriment pour faciliter sa résolution. Supprime les caractères
     * spéciaux, met en majuscules, et effectue d'autres normalisations pour faciliter la
     * correspondance avec les labels d'énumération.
     *
     * @param label Le label à normaliser
     * @return Le label normalisé
     */
    private fun normalizeLabel(label: String): String {
        return label.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace(" ", "") // Supprime les espaces
                .replace("-", "") // Supprime les tirets
                .replace("_", "") // Supprime les underscores
                .replace(".", "") // Supprime les points
                .uppercase() // Convertit en majuscules pour correspondre aux conventions
        // d'énumération
    }
}
