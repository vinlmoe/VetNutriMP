package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin

/**
 * Préférences par espèce (type d'expression des besoins, nutriments sélectionnés, équations compl.).
 * - `nutrimentsSelectionnes`: clés = catégories (enum name), valeurs = coefs de nutriments.
 * - `equationsComplementaires`: nutriment -> UUID d'équation complémentaire.
 */
data class PreferencesEspece(
        val espece: String = Espece.CHIEN.name,
        val typeExpressionBesoinId: Int = TypeExpressionBesoin.DEFAULT.id,
        val nutrimentsSelectionnes: Map<String, List<Int>> = DefaultPreferencesConfig.DefaultNutrients.toMap(),
        val equationsComplementaires: Map<String, String> = emptyMap() // nutriment -> equation UUID
) {
    /** Obtient l'énumération TypeExpressionBesoin correspondante */
    fun getTypeExpressionBesoinEnum(): TypeExpressionBesoin {
        return TypeExpressionBesoin.getById(typeExpressionBesoinId)
    }

    /** Obtient le nom d'affichage du type d'expression */
    fun getTypeExpressionBesoinDisplayName(): String {
        return getTypeExpressionBesoinEnum().displayName
    }

    /** Obtient l'énumération Espece correspondante */
    fun getEspeceEnum(): Espece {
        return try {
            Espece.valueOf(espece)
        } catch (e: IllegalArgumentException) {
            Espece.CHIEN
        }
    }

    /** Obtient le nombre total de nutriments sélectionnés */
    fun getTotalSelectedNutrients(): Int {
        return nutrimentsSelectionnes.values.sumOf { it.size }
    }

    /** Obtient le nombre de nutriments sélectionnés pour une catégorie */
    fun getSelectedNutrientsCount(category: String): Int {
        return nutrimentsSelectionnes[category]?.size ?: 0
    }

    /** Vérifie si un nutriment est sélectionné dans une catégorie */
    fun isNutrientSelected(category: String, nutrientId: Int): Boolean {
        return nutrimentsSelectionnes[category]?.contains(nutrientId) == true
    }

    /** Ajoute un nutriment à la sélection */
    fun addNutrient(category: MainNutrientEnum, nutrientCoef: Int): PreferencesEspece {
        val currentList = nutrimentsSelectionnes[category.name]?.toMutableList() ?: mutableListOf()
        if (!currentList.contains(nutrientCoef)) {
            currentList.add(nutrientCoef)
        }
        val newMap = nutrimentsSelectionnes.toMutableMap()
        newMap[category.name] = currentList
        return copy(nutrimentsSelectionnes = newMap)
    }

    /** Retire un nutriment de la sélection */
    fun removeNutrient(category: MainNutrientEnum, nutrientCoef: Int): PreferencesEspece {
        val currentList = nutrimentsSelectionnes[category.name]?.toMutableList() ?: return this
        currentList.remove(nutrientCoef)
        val newMap = nutrimentsSelectionnes.toMutableMap()
        newMap[category.name] = currentList
        return copy(nutrimentsSelectionnes = newMap)
    }

    /** Met à jour la sélection complète pour une catégorie */
    fun updateNutrientsForCategory(
            category: MainNutrientEnum,
            selectedCoefs: List<Int>
    ): PreferencesEspece {
        val newMap = nutrimentsSelectionnes.toMutableMap()
        newMap[category.name] = selectedCoefs
        return copy(nutrimentsSelectionnes = newMap)
    }

    /** Obtient l'équation complémentaire pour un nutriment */
    fun getEquationComplementaire(nutriment: String): String? {
        return equationsComplementaires[nutriment]
    }

    /** Définit l'équation complémentaire pour un nutriment */
    fun setEquationComplementaire(nutriment: String, equationUuid: String): PreferencesEspece {
        val newMap = equationsComplementaires.toMutableMap()
        newMap[nutriment] = equationUuid
        return copy(equationsComplementaires = newMap)
    }

    /** Supprime l'équation complémentaire pour un nutriment */
    fun removeEquationComplementaire(nutriment: String): PreferencesEspece {
        val newMap = equationsComplementaires.toMutableMap()
        newMap.remove(nutriment)
        return copy(equationsComplementaires = newMap)
    }

    /** Vérifie si un nutriment a une équation complémentaire */
    fun hasEquationComplementaire(nutriment: String): Boolean {
        return equationsComplementaires.containsKey(nutriment)
    }

    /** Vérifie si une équation est sélectionnée (par UUID) */
    fun isEquationSelected(equationUuid: String): Boolean {
        return equationsComplementaires.values.contains(equationUuid)
    }

    /** Ajoute une équation (par UUID) */
    fun addEquation(equationUuid: String): PreferencesEspece {
        val newMap = equationsComplementaires.toMutableMap()
        // Utiliser l'UUID comme clé pour éviter les doublons
        newMap[equationUuid] = equationUuid
        return copy(equationsComplementaires = newMap)
    }

    /** Retire une équation (par UUID) */
    fun removeEquation(equationUuid: String): PreferencesEspece {
        val newMap = equationsComplementaires.toMutableMap()
        newMap.remove(equationUuid)
        return copy(equationsComplementaires = newMap)
    }

    /** Obtient la liste des UUIDs d'équations sélectionnées */
    fun getSelectedEquationUuids(): List<String> {
        return equationsComplementaires.keys.toList()
    }

    companion object {
        /** Crée des préférences par défaut pour une espèce */
        fun createDefault(espece: Espece): PreferencesEspece {
            return DefaultPreferencesConfig.getDefaultPreferencesForSpecies(espece)
        }

        /** Nutriments par défaut sélectionnés (maintenant gérés par DefaultPreferencesConfig) */
        @Deprecated("Utiliser DefaultPreferencesConfig.DefaultNutrients à la place")
        private fun getDefaultNutrients(): Map<String, List<Int>> {
            return mapOf(
                    "BASE" to listOf(1, 2, 4, 5, 8, 0), // MS, PB, MG, FB, Cendres, ENA
                    "MACRO" to listOf(10, 11, 12, 13), // Ca, P, Mg, Na
                    "MIN" to listOf(14, 15, 16), // K, Cl, S
                    "VITAM" to listOf(45, 46, 47), // Vit A, D, E
                    "LIPID" to listOf(25, 26), // AG saturés, AG insaturés
                    "AMA" to emptyList(),
                    "ANA" to emptyList(),
                    "OTHER" to emptyList()
            )
        }
    }
}

/** Ensemble des préférences de toutes les espèces */
data class PreferencesApplication(
        val preferencesParEspece: Map<String, PreferencesEspece> = emptyMap(),
        val versionPreferences: Int = DefaultPreferencesConfig.DEFAULT_VERSION,
        val nomUtilisateur: String = "",
        val numeroOrdre: String = "",
        val adressePostale: String = "",
        val codePostal: String = "",
        val ville: String = "",
        val telephone: String = "",
        val email: String = ""
) {
    /** Obtient les préférences pour une espèce donnée */
    fun getPreferencesEspece(espece: Espece): PreferencesEspece {
        return preferencesParEspece[espece.name] ?: PreferencesEspece.createDefault(espece)
    }

    /** Met à jour les préférences pour une espèce */
    fun updatePreferencesEspece(preferences: PreferencesEspece): PreferencesApplication {
        val nouvellesPreferences = preferencesParEspece.toMutableMap()
        nouvellesPreferences[preferences.espece] = preferences
        return copy(preferencesParEspece = nouvellesPreferences)
    }

    companion object {
        /** Crée une instance avec toutes les préférences par défaut */
        fun createDefault(): PreferencesApplication {
            return DefaultPreferencesConfig.createDefaultPreferencesApplication()
        }
    }
}
