package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin

/**
 * Préférences spécifiques à une espèce
 * @param espece L'espèce concernée
 * @param typeExpressionBesoinId ID du type d'expression des besoins (voir TypeExpressionBesoin.id)
 * @param nutrimentsSelectionnes Map des catégories de nutriments vers les listes de nutriments
 * sélectionnés
 */
data class PreferencesEspece(
        val espece: String = Espece.CHIEN.name,
        val typeExpressionBesoinId: Int = TypeExpressionBesoin.DEFAULT.id,
        val nutrimentsSelectionnes: Map<String, List<Int>> = getDefaultNutrients()
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

    companion object {
        /** Crée des préférences par défaut pour une espèce */
        fun createDefault(espece: Espece): PreferencesEspece {
            return PreferencesEspece(
                    espece = espece.name,
                    typeExpressionBesoinId = TypeExpressionBesoin.DEFAULT.id,
                    nutrimentsSelectionnes = getDefaultNutrients()
            )
        }

        /** Nutriments par défaut sélectionnés */
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
        val versionPreferences: Int = 1
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
}
 