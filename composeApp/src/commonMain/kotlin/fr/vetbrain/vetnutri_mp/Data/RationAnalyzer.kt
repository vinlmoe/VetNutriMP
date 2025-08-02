package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Components.calculerValeursNutritionnelles
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import kotlinx.serialization.Serializable

/** Classe permettant d'analyser les rations alimentaires et leurs valeurs nutritionnelles */
@Serializable
data class AnalyseResultat(
        val rationId: String = "",
        val rationName: String = "",
        val quantiteTotale: Float = 0f,
        val densiteEnergetique: Float = 0f,
        val macronutriments: Map<String, Float> = mapOf(),
        val mineraux: Map<String, Float> = mapOf(),
        val vitamines: Map<String, Float> = mapOf(),
        val lipides: Map<String, Float> = mapOf(),
        val ratios: Map<String, Float> = mapOf(),
        val completude: Float = 0f,
        val equilibre: Float = 0f,
        val alertes: List<String> = listOf()
)

/** Classe utilitaire pour effectuer des analyses nutritionnelles sur les rations */
class RationAnalyzer {
    /**
     * Analyse une ration et retourne un résultat détaillé
     *
     * @param ration La ration à analyser
     * @param consultation La consultation associée (pour les variables supplémentaires)
     * @return Le résultat de l'analyse
     */
    fun analyserRation(ration: Ration, consultation: ConsultationEv? = null): AnalyseResultat {
        println("Analyse de la ration: ${ration.name} (${ration.uuid})")

        if (consultation != null) {
            consultation.suppVarp.forEach { variable ->
            }
        }

        // Listes des nutriments à analyser par catégorie
        val macronutriments = NutrientMain.entries.toList() as List<Nutrient>
        val mineraux =
                NutrientMacro.entries.toList() as List<Nutrient> +
                        NutrientMin.entries.toList() as List<Nutrient>
        val vitamines = NutrientVitam.entries.toList() as List<Nutrient>
        val lipides = NutrientLipid.entries.toList() as List<Nutrient>
        val ratiosNutriments = NutrientAnalysis.entries.toList() as List<Nutrient>

        // Liste complète des nutriments à analyser
        val tousNutriments = macronutriments + mineraux + vitamines + lipides + ratiosNutriments

        // Calcul des valeurs nutritionnelles de la ration
        val valeurs = calculerValeursNutritionnelles(ration.alimentMutableList, tousNutriments)

        // Préparation des résultats
        val macronutrimentsMap = mutableMapOf<String, Float>()
        val minerauxMap = mutableMapOf<String, Float>()
        val vitaminesMap = mutableMapOf<String, Float>()
        val lipidesMap = mutableMapOf<String, Float>()
        val ratiosMap = mutableMapOf<String, Float>()

        // Remplissage des maps de résultats
        macronutriments.forEach { nutriment ->
            macronutrimentsMap[nutriment.label] = valeurs[nutriment] ?: 0f
        }

        mineraux.forEach { nutriment -> minerauxMap[nutriment.label] = valeurs[nutriment] ?: 0f }

        vitamines.forEach { nutriment -> vitaminesMap[nutriment.label] = valeurs[nutriment] ?: 0f }

        lipides.forEach { nutriment -> lipidesMap[nutriment.label] = valeurs[nutriment] ?: 0f }

        // Calcul des ratios nutritionnels
        val omega3 = valeurs[NutrientLipid.O3] ?: 0f
        val omega6 = valeurs[NutrientLipid.O6] ?: 0f
        val calcium = valeurs[NutrientMacro.CAL] ?: 0f
        val phosphore = valeurs[NutrientMacro.PHOS] ?: 0f
        val sodium = valeurs[NutrientMacro.NA] ?: 0f
        val potassium = valeurs[NutrientMacro.K] ?: 0f
        val zinc = valeurs[NutrientMin.ZN] ?: 0f
        val cuivre = valeurs[NutrientMin.CU] ?: 0f
        val proteines = valeurs[NutrientMain.PROTEINE] ?: 0f

        // Calcul des ratios
        if (omega3 > 0f && omega6 > 0f) {
            ratiosMap["Oméga-6/Oméga-3"] = omega6 / omega3
        }

        if (calcium > 0f && phosphore > 0f) {
            ratiosMap["Calcium/Phosphore"] = calcium / phosphore
        }

        if (sodium > 0f && potassium > 0f) {
            ratiosMap["Potassium/Sodium"] = potassium / sodium
        }

        if (zinc > 0f && cuivre > 0f) {
            ratiosMap["Zinc/Cuivre"] = zinc / cuivre
        }

        if (proteines > 0f && phosphore > 0f) {
            ratiosMap["Protéines/Phosphore"] = proteines / phosphore
        }

        // Liste des alertes nutritionnelles
        val alertes = mutableListOf<String>()

        // Vérification de l'équilibre des ratios (exemples de valeurs optimales)
        if (ratiosMap.containsKey("Oméga-6/Oméga-3") &&
                        (ratiosMap["Oméga-6/Oméga-3"]!! < 2.5f ||
                                ratiosMap["Oméga-6/Oméga-3"]!! > 10f)
        ) {
            alertes.add("Le ratio Oméga-6/Oméga-3 est déséquilibré (optimal entre 2.5 et 10)")
        }

        if (ratiosMap.containsKey("Calcium/Phosphore") &&
                        (ratiosMap["Calcium/Phosphore"]!! < 1f ||
                                ratiosMap["Calcium/Phosphore"]!! > 2f)
        ) {
            alertes.add("Le ratio Calcium/Phosphore est déséquilibré (optimal entre 1.0 et 2.0)")
        }

        // Alerte pour les nutriments essentiels manquants
        val nutrimentsCles =
                listOf(
                        NutrientMain.PROTEINE,
                        NutrientMain.LIPIDE,
                        NutrientMain.GLUCIDE,
                        NutrientMacro.CAL,
                        NutrientMacro.PHOS,
                        NutrientVitam.VITA,
                        NutrientVitam.VITD,
                        NutrientVitam.VITE
                )

        for (nutriment in nutrimentsCles) {
            if (valeurs[nutriment] == null || valeurs[nutriment]!! <= 0f) {
                alertes.add("Nutriment essentiel manquant: ${nutriment.label}")
            }
        }

        // Calcul du score de complétude (% des nutriments essentiels présents)
        val nutrimentsPrésents = nutrimentsCles.count { valeurs[it] != null && valeurs[it]!! > 0f }
        val completude = (nutrimentsPrésents.toFloat() / nutrimentsCles.size) * 100f

        // Calcul du score d'équilibre (basé sur les ratios)
        val ratiosOptimaux =
                mapOf(
                        "Oméga-6/Oméga-3" to (2.5f..10f),
                        "Calcium/Phosphore" to (1f..2f),
                        "Potassium/Sodium" to (2f..4f),
                        "Zinc/Cuivre" to (5f..10f),
                        "Protéines/Phosphore" to (15f..30f)
                )

        var ratiosEquilibres = 0
        var ratiosCalcules = 0

        for ((ratio, plage) in ratiosOptimaux) {
            if (ratiosMap.containsKey(ratio)) {
                ratiosCalcules++
                if (ratiosMap[ratio]!! in plage) {
                    ratiosEquilibres++
                }
            }
        }

        val equilibre =
                if (ratiosCalcules > 0) {
                    (ratiosEquilibres.toFloat() / ratiosCalcules) * 100f
                } else {
                    0f
                }

        return AnalyseResultat(
                rationId = ration.uuid,
                rationName = ration.name,
                quantiteTotale = ration.getQuantiteTotale(),
                densiteEnergetique = ration.getDensiteEnergetiqueMoyenne(),
                macronutriments = macronutrimentsMap,
                mineraux = minerauxMap,
                vitamines = vitaminesMap,
                lipides = lipidesMap,
                ratios = ratiosMap,
                completude = completude,
                equilibre = equilibre,
                alertes = alertes
        )
    }

    /**
     * Compare deux rations et retourne les différences nutritionnelles
     *
     * @param ration1 Première ration à comparer
     * @param ration2 Seconde ration à comparer
     * @return Un rapport de comparaison entre les deux rations
     */
    fun comparerRations(ration1: Ration, ration2: Ration): Map<String, ComparaisonNutriment> {
        val analyse1 = analyserRation(ration1)
        val analyse2 = analyserRation(ration2)

        val resultat = mutableMapOf<String, ComparaisonNutriment>()

        // Comparer les macronutriments
        for ((nutriment, valeur1) in analyse1.macronutriments) {
            val valeur2 = analyse2.macronutriments[nutriment] ?: 0f
            val difference = valeur2 - valeur1
            val pourcentage =
                    if (valeur1 > 0f) {
                        difference / valeur1 * 100f
                    } else if (valeur2 > 0f) {
                        100f
                    } else {
                        0f
                    }

            resultat[nutriment] =
                    ComparaisonNutriment(
                            nomNutriment = nutriment,
                            valeur1 = valeur1,
                            valeur2 = valeur2,
                            difference = difference,
                            pourcentageDifference = pourcentage
                    )
        }

        // Ajouter les macronutriments qui sont dans la seconde ration mais pas dans la première
        for ((nutriment, valeur2) in analyse2.macronutriments) {
            if (!analyse1.macronutriments.containsKey(nutriment)) {
                resultat[nutriment] =
                        ComparaisonNutriment(
                                nomNutriment = nutriment,
                                valeur1 = 0f,
                                valeur2 = valeur2,
                                difference = valeur2,
                                pourcentageDifference = 100f
                        )
            }
        }

        // Faire de même pour les autres catégories (mineraux, vitamines, lipides)
        // Mineraux
        combinerCategorie(analyse1.mineraux, analyse2.mineraux, resultat)

        // Vitamines
        combinerCategorie(analyse1.vitamines, analyse2.vitamines, resultat)

        // Lipides
        combinerCategorie(analyse1.lipides, analyse2.lipides, resultat)

        return resultat
    }

    /** Combine les valeurs d'une catégorie nutritionnelle pour la comparaison */
    private fun combinerCategorie(
            valeurs1: Map<String, Float>,
            valeurs2: Map<String, Float>,
            resultat: MutableMap<String, ComparaisonNutriment>
    ) {
        // Ajouter les nutriments de la première catégorie
        for ((nutriment, valeur1) in valeurs1) {
            val valeur2 = valeurs2[nutriment] ?: 0f
            val difference = valeur2 - valeur1
            val pourcentage =
                    if (valeur1 > 0f) {
                        difference / valeur1 * 100f
                    } else if (valeur2 > 0f) {
                        100f
                    } else {
                        0f
                    }

            resultat[nutriment] =
                    ComparaisonNutriment(
                            nomNutriment = nutriment,
                            valeur1 = valeur1,
                            valeur2 = valeur2,
                            difference = difference,
                            pourcentageDifference = pourcentage
                    )
        }

        // Ajouter les nutriments qui sont dans la seconde catégorie mais pas dans la première
        for ((nutriment, valeur2) in valeurs2) {
            if (!valeurs1.containsKey(nutriment)) {
                resultat[nutriment] =
                        ComparaisonNutriment(
                                nomNutriment = nutriment,
                                valeur1 = 0f,
                                valeur2 = valeur2,
                                difference = valeur2,
                                pourcentageDifference = 100f
                        )
            }
        }
    }
}

/** Classe représentant la comparaison entre deux valeurs nutritionnelles */
@Serializable
data class ComparaisonNutriment(
        val nomNutriment: String,
        val valeur1: Float,
        val valeur2: Float,
        val difference: Float,
        val pourcentageDifference: Float
)
