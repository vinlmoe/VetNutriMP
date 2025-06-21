package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Enumer.*

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
            MainNutrientEnum.BASE -> "Macronutriments"
            MainNutrientEnum.MACRO -> "Minéraux essentiels"
            MainNutrientEnum.MIN -> "Oligo-éléments"
            MainNutrientEnum.VITAM -> "Vitamines"
            MainNutrientEnum.LIPID -> "Acides gras"
            MainNutrientEnum.AMA -> "Acides aminés"
            MainNutrientEnum.ANA -> "Analyses et ratios"
            MainNutrientEnum.OTHER -> "Autres nutriments"
            MainNutrientEnum.ENERGIE -> "Énergie"
            else -> category.label
        }
    }

    /** Obtient la description pour une catégorie */
    fun getCategoryDescription(category: MainNutrientEnum): String {
        return when (category) {
            MainNutrientEnum.BASE -> "Protéines, lipides, glucides, fibres, énergie"
            MainNutrientEnum.MACRO -> "Calcium, phosphore, sodium, potassium, magnésium, chlore"
            MainNutrientEnum.MIN -> "Fer, zinc, cuivre, manganèse, iode, sélénium"
            MainNutrientEnum.VITAM -> "Vitamines liposolubles et hydrosolubles"
            MainNutrientEnum.LIPID -> "Acides gras saturés, mono-insaturés, poly-insaturés"
            MainNutrientEnum.AMA -> "Acides aminés essentiels et non essentiels"
            MainNutrientEnum.ANA -> "Ratios et analyses nutritionnelles"
            MainNutrientEnum.OTHER -> "Taurine, carnitine, prébiotiques"
            MainNutrientEnum.ENERGIE -> "Densité énergétique et métabolisme"
            else -> ""
        }
    }
}
