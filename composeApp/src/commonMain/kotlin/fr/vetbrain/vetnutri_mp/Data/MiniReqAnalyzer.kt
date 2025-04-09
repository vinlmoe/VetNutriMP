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
                    for (nutrientName in mainNutrient.getSousNutrients()) {
                        val listRef = value.obtenirReferences(nutrientName)
                        if (listRef.isNotEmpty()) {
                            mapRef[nutrientName] = listRef
                        }
                    }
                }
                MainNutrientEnum.BASE,
                MainNutrientEnum.LIPID,
                MainNutrientEnum.MACRO,
                MainNutrientEnum.MIN,
                MainNutrientEnum.VITAM,
                MainNutrientEnum.ANA -> {
                    for (nutrientName in mainNutrient.getSousNutrients()) {
                        val listRef = value.obtenirReferences(nutrientName)
                        if (listRef.isNotEmpty()) {
                            mapRef[nutrientName] = listRef
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
        return obtenirReferences(nutrient.toString())
    }

    /**
     * Obtient les références pour un nutriment donné par son nom
     *
     * @param nutrientName Le nom du nutriment pour lequel obtenir les références
     * @return La liste des références pour ce nutriment ou une liste vide si aucune référence
     */
    fun obtenirReferences(nutrientName: String): List<NutrientRef> {
        return mapRef[nutrientName] ?: emptyList()
    }
}
