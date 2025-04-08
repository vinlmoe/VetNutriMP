package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient

/**
 * Version simplifiée de RequirementAnalyzer qui ne conserve que les références pour faciliter la
 * sérialisation et l'accès aux références de nutriments
 */
class MiniReqAnalyzer {
    private val mapRef = mutableMapOf<String, List<NutrientRef>>()

    /**
     * Constructeur qui initialise la map à partir d'un RequirementAnalyzer
     *
     * @param value RequirementAnalyzer à partir duquel initialiser
     */
    constructor(value: RequirementAnalyzer) {
        // Parcourir tous les types de nutriments
        for (mainNutrient in MainNutrientEnum.entries) {
            when (mainNutrient) {
                MainNutrientEnum.AMA -> {
                    for (nutrient in mainNutrient.getSousNutrients()) {
                        val listRef = value.obtenirReferences(nutrient)
                        if (listRef.isNotEmpty()) {
                            mapRef[nutrient.toString()] = listRef
                        }
                    }
                }
                MainNutrientEnum.BASE,
                MainNutrientEnum.LIPID,
                MainNutrientEnum.MACRO,
                MainNutrientEnum.MIN,
                MainNutrientEnum.VITAM,
                MainNutrientEnum.ANA -> {
                    for (nutrient in mainNutrient.getSousNutrients()) {
                        val listRef = value.obtenirReferences(nutrient)
                        if (listRef.isNotEmpty()) {
                            mapRef[nutrient.toString()] = listRef
                        }
                    }
                }
                else -> {
                    // Ne rien faire pour les autres types
                }
            }
        }
    }

    /**
     * Obtient les références pour un nutriment donné
     *
     * @param nutrient Le nutriment pour lequel obtenir les références
     * @return La liste des références pour ce nutriment ou une liste vide si aucune référence
     */
    fun obtenirReferences(nutrient: Nutrient): List<NutrientRef> {
        return mapRef[nutrient.toString()] ?: emptyList()
    }
}
