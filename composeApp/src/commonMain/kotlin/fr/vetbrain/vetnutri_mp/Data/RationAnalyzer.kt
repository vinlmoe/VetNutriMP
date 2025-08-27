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
        val quantiteTotale: Double = 0.0,
        val densiteEnergetique: Double = 0.0,
        val macronutriments: Map<String, Double> = mapOf(),
        val mineraux: Map<String, Double> = mapOf(),
        val vitamines: Map<String, Double> = mapOf(),
        val lipides: Map<String, Double> = mapOf(),
        val ratios: Map<String, Double> = mapOf(),
        val completude: Double = 0.0,
        val equilibre: Double = 0.0,
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
        

        if (consultation != null) {
            consultation.suppVarp.forEach { variable -> }
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
        val macronutrimentsMap = mutableMapOf<String, Double>()
        val minerauxMap = mutableMapOf<String, Double>()
        val vitaminesMap = mutableMapOf<String, Double>()
        val lipidesMap = mutableMapOf<String, Double>()
        val ratiosMap = mutableMapOf<String, Double>()

        // Remplissage des maps de résultats
        macronutriments.forEach { nutriment ->
            macronutrimentsMap[nutriment.label] = valeurs[nutriment] ?: 0.0
        }

        mineraux.forEach { nutriment -> minerauxMap[nutriment.label] = valeurs[nutriment] ?: 0.0 }

        vitamines.forEach { nutriment -> vitaminesMap[nutriment.label] = valeurs[nutriment] ?: 0.0 }

        lipides.forEach { nutriment -> lipidesMap[nutriment.label] = valeurs[nutriment] ?: 0.0 }

        // Calcul des ratios nutritionnels
        val omega3 = valeurs[NutrientLipid.O3] ?: 0.0
        val omega6 = valeurs[NutrientLipid.O6] ?: 0.0
        val calcium = valeurs[NutrientMacro.CAL] ?: 0.0
        val phosphore = valeurs[NutrientMacro.PHOS] ?: 0.0
        val sodium = valeurs[NutrientMacro.NA] ?: 0.0
        val potassium = valeurs[NutrientMacro.K] ?: 0.0
        val zinc = valeurs[NutrientMin.ZN] ?: 0.0
        val cuivre = valeurs[NutrientMin.CU] ?: 0.0
        val proteines = valeurs[NutrientMain.PROTEINE] ?: 0.0

        // Calcul des ratios
        if (omega3 > 0.0 && omega6 > 0.0) {
            ratiosMap["Oméga-6/Oméga-3"] = omega6 / omega3
        }

        if (calcium > 0.0 && phosphore > 0.0) {
            ratiosMap["Calcium/Phosphore"] = calcium / phosphore
        }

        if (sodium > 0.0 && potassium > 0.0) {
            ratiosMap["Potassium/Sodium"] = potassium / sodium
        }

        if (zinc > 0.0 && cuivre > 0.0) {
            ratiosMap["Zinc/Cuivre"] = zinc / cuivre
        }

        if (proteines > 0.0 && phosphore > 0.0) {
            ratiosMap["Protéines/Phosphore"] = proteines / phosphore
        }

        // Liste des alertes nutritionnelles
        val alertes = mutableListOf<String>()

        // Vérification de l'équilibre des ratios (exemples de valeurs optimales)
        if (ratiosMap.containsKey("Oméga-6/Oméga-3") &&
                        (ratiosMap["Oméga-6/Oméga-3"]!! < 2.5 ||
                                ratiosMap["Oméga-6/Oméga-3"]!! > 10.0)
        ) {
            alertes.add("Le ratio Oméga-6/Oméga-3 est déséquilibré (optimal entre 2.5 et 10)")
        }

        if (ratiosMap.containsKey("Calcium/Phosphore") &&
                        (ratiosMap["Calcium/Phosphore"]!! < 1.0 ||
                                ratiosMap["Calcium/Phosphore"]!! > 2.0)
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
            if (valeurs[nutriment] == null || valeurs[nutriment]!! <= 0.0) {
                alertes.add("Nutriment essentiel manquant: ${nutriment.label}")
            }
        }

        // Calcul du score de complétude (% des nutriments essentiels présents)
        val nutrimentsPrésents = nutrimentsCles.count { valeurs[it] != null && valeurs[it]!! > 0.0 }
        val completude = (nutrimentsPrésents.toDouble() / nutrimentsCles.size) * 100.0

        // Calcul du score d'équilibre (basé sur les ratios)
        val ratiosOptimaux =
                mapOf(
                        "Oméga-6/Oméga-3" to (2.5..10.0),
                        "Calcium/Phosphore" to (1.0..2.0),
                        "Potassium/Sodium" to (2.0..4.0),
                        "Zinc/Cuivre" to (5.0..10.0),
                        "Protéines/Phosphore" to (15.0..30.0)
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
                    (ratiosEquilibres.toDouble() / ratiosCalcules) * 100.0
                } else {
                    0.0
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
            val valeur2 = analyse2.macronutriments[nutriment] ?: 0.0
            val difference = valeur2 - valeur1
            val pourcentage =
                    if (valeur1 > 0.0) {
                        difference / valeur1 * 100.0
                    } else if (valeur2 > 0.0) {
                        100.0
                    } else {
                        0.0
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
                                valeur1 = 0.0,
                                valeur2 = valeur2,
                                difference = valeur2,
                                pourcentageDifference = 100.0
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
            valeurs1: Map<String, Double>,
            valeurs2: Map<String, Double>,
            resultat: MutableMap<String, ComparaisonNutriment>
    ) {
        // Ajouter les nutriments de la première catégorie
        for ((nutriment, valeur1) in valeurs1) {
            val valeur2 = valeurs2[nutriment] ?: 0.0
            val difference = valeur2 - valeur1
            val pourcentage =
                    if (valeur1 > 0.0) {
                        difference / valeur1 * 100.0
                    } else if (valeur2 > 0.0) {
                        100.0
                    } else {
                        0.0
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
                                valeur1 = 0.0,
                                valeur2 = valeur2,
                                difference = valeur2,
                                pourcentageDifference = 100.0
                        )
            }
        }
    }
}

/** Classe représentant la comparaison entre deux valeurs nutritionnelles */
@Serializable
data class ComparaisonNutriment(
        val nomNutriment: String,
        val valeur1: Double,
        val valeur2: Double,
        val difference: Double,
        val pourcentageDifference: Double
)
