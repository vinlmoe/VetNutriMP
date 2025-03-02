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
        // Vérifier dans NutrientMain
        if (NutrientMain.isByLabel(label)) {
            return NutrientMain.getByLabel(label)
        }

        // Vérifier dans NutrientMacro
        try {
            if (NutrientMacro.isByLabel(label)) {
                return NutrientMacro.getByLabel(label)
            }
        } catch (e: IllegalArgumentException) {
            // Ignorer, continuer à chercher dans les autres enums
        }

        // Vérifier dans NutrientMin
        try {
            if (NutrientMin.isByLabel(label)) {
                return NutrientMin.getByLabel(label)
            }
        } catch (e: IllegalArgumentException) {
            // Ignorer, continuer à chercher dans les autres enums
        }

        // Vérifier dans NutrientLipid
        if (NutrientLipid.isByLabel(label)) {
            return NutrientLipid.getByLabel(label)
        }

        // Vérifier dans NutrientVitam
        if (NutrientVitam.isByLabel(label)) {
            return NutrientVitam.getByLabel(label)
        }

        // Vérifier dans NutrientOther
        try {
            if (NutrientOther.isByLabel(label)) {
                return NutrientOther.getByLabel(label)
            }
        } catch (e: IllegalArgumentException) {
            // Ignorer, continuer à chercher dans les autres enums
        }

        // Vérifier dans NutrientEnergy - pas de isByLabel direct, utiliser une approche différente
        val nutrientEnergy = NutrientEnergy.entries.find { it.label == label }
        if (nutrientEnergy != null) {
            return nutrientEnergy
        }

        // Vérifier dans NutrientAnalysis - pas de isByLabel direct, utiliser une approche
        // différente
        val nutrientAnalysis = NutrientAnalysis.entries.find { it.label == label }
        if (nutrientAnalysis != null) {
            return nutrientAnalysis
        }

        // Aucun nutriment trouvé
        return null
    }
}
        



