package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.translate

/** Utilitaire pour gérer les nutriments et leurs catégories */
object NutrientUtils {

    /** Représente un nutriment avec ses informations d'affichage */
    data class NutrientInfo(
            val coef: Int,
            val label: String,
            val displayName: String,
            val unite: String,
            val category: MainNutrientEnum
    )

    /** Obtient tous les nutriments disponibles pour une catégorie */
    fun getNutrientsForCategory(category: MainNutrientEnum): List<NutrientInfo> {
        return when (category) {
            MainNutrientEnum.BASE -> {
                NutrientMain.values().map { nutrient ->
                    NutrientInfo(
                            coef = nutrient.coef,
                            label = nutrient.label,
                            displayName = nutrient.toString(),
                            unite = nutrient.unite,
                            category = category
                    )
                }
            }
            MainNutrientEnum.MACRO -> {
                NutrientMacro.values().map { nutrient ->
                    NutrientInfo(
                            coef = nutrient.coef,
                            label = nutrient.label,
                            displayName = nutrient.nameToString(),
                            unite = nutrient.unite,
                            category = category
                    )
                }
            }
            MainNutrientEnum.MIN -> {
                NutrientMin.values().map { nutrient ->
                    NutrientInfo(
                            coef = nutrient.coef,
                            label = nutrient.label,
                            displayName = nutrient.nameToString(),
                            unite = nutrient.unite,
                            category = category
                    )
                }
            }
            MainNutrientEnum.VITAM -> {
                NutrientVitam.values().map { nutrient ->
                    NutrientInfo(
                            coef = nutrient.coef,
                            label = nutrient.label,
                            displayName = nutrient.displayName,
                            unite = nutrient.unite,
                            category = category
                    )
                }
            }
            MainNutrientEnum.LIPID -> {
                NutrientLipid.values().map { nutrient ->
                    NutrientInfo(
                            coef = nutrient.coef,
                            label = nutrient.label,
                            displayName = nutrient.nameToString(),
                            unite = nutrient.unite,
                            category = category
                    )
                }
            }
            MainNutrientEnum.AMA -> {
                AAEnum.values().map { nutrient ->
                    NutrientInfo(
                            coef = nutrient.coef,
                            label = nutrient.label,
                            displayName = nutrient.nom,
                            unite = nutrient.unite,
                            category = category
                    )
                }
            }
            MainNutrientEnum.ANA -> {
                NutrientAnalysis.values().map { nutrient ->
                    NutrientInfo(
                            coef = nutrient.coef,
                            label = nutrient.label,
                            displayName = nutrient.displayName,
                            unite = nutrient.unite,
                            category = category
                    )
                }
            }
            MainNutrientEnum.OTHER -> {
                NutrientOther.values().map { nutrient ->
                    NutrientInfo(
                            coef = nutrient.coef,
                            label = nutrient.label,
                            displayName = nutrient.nameToString(),
                            unite = nutrient.unite,
                            category = category
                    )
                }
            }
            MainNutrientEnum.ENERGIE -> {
                NutrientEnergy.values().map { nutrient ->
                    NutrientInfo(
                            coef = nutrient.coef,
                            label = nutrient.label,
                            displayName = nutrient.nameToString(),
                            unite = nutrient.unite,
                            category = category
                    )
                }
            }
            else -> emptyList()
        }
    }

    /** Obtient toutes les catégories de nutriments pertinentes pour l'affichage */
    fun getRelevantCategories(): List<MainNutrientEnum> {
        return listOf(
                MainNutrientEnum.BASE, // Macronutriments principaux
                MainNutrientEnum.MACRO, // Minéraux essentiels
                MainNutrientEnum.MIN, // Oligo-éléments
                MainNutrientEnum.VITAM, // Vitamines
                MainNutrientEnum.LIPID, // Acides gras
                MainNutrientEnum.AMA, // Acides aminés
                MainNutrientEnum.ANA, // Analyses et ratios
                MainNutrientEnum.OTHER // Autres nutriments
        )
    }

    /** Obtient le nom d'affichage pour une catégorie */
    fun getCategoryDisplayName(category: MainNutrientEnum): String {
        return when (category) {
            MainNutrientEnum.BASE -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.BASE_NAME.translate()
            MainNutrientEnum.MACRO -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.MACRO_NAME.translate()
            MainNutrientEnum.MIN -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.MIN_NAME.translate()
            MainNutrientEnum.VITAM -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.VITAM_NAME.translate()
            MainNutrientEnum.LIPID -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.LIPID_NAME.translate()
            MainNutrientEnum.AMA -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.AMA_NAME.translate()
            MainNutrientEnum.ANA -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.ANA_NAME.translate()
            MainNutrientEnum.OTHER -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.OTHER_NAME.translate()
            MainNutrientEnum.ENERGIE -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.ENERGIE_NAME.translate()
            else -> category.label
        }
    }

    /** Obtient la description pour une catégorie */
    fun getCategoryDescription(category: MainNutrientEnum): String {
        return when (category) {
            MainNutrientEnum.BASE -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.BASE_DESC.translate()
            MainNutrientEnum.MACRO -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.MACRO_DESC.translate()
            MainNutrientEnum.MIN -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.MIN_DESC.translate()
            MainNutrientEnum.VITAM -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.VITAM_DESC.translate()
            MainNutrientEnum.LIPID -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.LIPID_DESC.translate()
            MainNutrientEnum.AMA -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.AMA_DESC.translate()
            MainNutrientEnum.ANA -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.ANA_DESC.translate()
            MainNutrientEnum.OTHER -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.OTHER_DESC.translate()
            MainNutrientEnum.ENERGIE -> fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.NutrientCategory.ENERGIE_DESC.translate()
            else -> ""
        }
    }
}
