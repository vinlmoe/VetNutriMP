package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Utils.NutrientUtils

/**
 * Fonction qui analyse une ration et retourne une Map de tous les nutriments avec en clés le label
 * de chaque nutriment et en valeur ValeurNutritionnelle
 *
 * @param ration La ration à analyser
 * @return Map<String, ValeurNutritionnelle> contenant l'analyse nutritionnelle complète
 */
fun analyserValeursNutritionnellesRation(ration: Ration): Map<String, ValeurNutritionnelle> {
    val resultat = mutableMapOf<String, ValeurNutritionnelle>()

    // Obtenir tous les nutriments disponibles
    val tousLesNutriments = obtenirTousLesNutriments()

    // Pour chaque nutriment, calculer sa valeur dans la ration
    tousLesNutriments.forEach { nutriment ->
        val valeurNutritionnelle = calculerValeurNutrimentDansRation(ration, nutriment)
        resultat[nutriment.label] = valeurNutritionnelle
    }

    return resultat
}

/**
 * Calcule la valeur d'un nutriment spécifique dans une ration
 *
 * @param ration La ration à analyser
 * @param nutriment Le nutriment à calculer
 * @return ValeurNutritionnelle pour ce nutriment
 */
private fun calculerValeurNutrimentDansRation(
        ration: Ration,
        nutriment: Nutrient
): ValeurNutritionnelle {
    var valeurTotale = 0.0
    val contributrionsIngredients = mutableListOf<String>()
    var tousLesIngredientsOntUneValeur = true
    var auMoinsUnIngredientAUneValeur = false

    // Pour chaque ingrédient de la ration
    ration.alimentMutableList.forEach { alimentRation ->
        val nomIngredient = alimentRation.aliment?.nom ?: "Ingrédient inconnu"
        val quantiteIngredient = alimentRation.quantite

        // Obtenir la valeur du nutriment pour cet ingrédient (pour 100g)
        val valeurNutrimentPour100g = alimentRation.aliment?.getNutrient(nutriment)

        if (valeurNutrimentPour100g != null) {
            // Calculer la contribution pondérée par la quantité
            val contributionIngredient = (valeurNutrimentPour100g * quantiteIngredient) / 100.0
            valeurTotale += contributionIngredient

            // Calculer le pourcentage d'apport de cet ingrédient
            auMoinsUnIngredientAUneValeur = true

            // On stocke temporairement la contribution pour calculer les pourcentages après
            contributrionsIngredients.add("$nomIngredient:$contributionIngredient")
        } else {
            // L'ingrédient n'a pas de valeur pour ce nutriment
            tousLesIngredientsOntUneValeur = false
            contributrionsIngredients.add("$nomIngredient:NA")
        }
    }

    // Calculer les pourcentages d'apport et construire la description
    val description =
            if (auMoinsUnIngredientAUneValeur && valeurTotale > 0) {
                contributrionsIngredients
                        .map { contrib ->
                            val parts = contrib.split(":")
                            val nom = parts[0]
                            val valeur = parts[1]

                            if (valeur == "NA") {
                                "$nom: NA"
                            } else {
                                val pourcentage =
                                        ((valeur.toDouble() / valeurTotale) * 100).let {
                                            String.format("%.1f", it)
                                        }
                                "$nom: $pourcentage%"
                            }
                        }
                        .joinToString(", ")
            } else {
                "Aucune valeur disponible pour ce nutriment"
            }

    return ValeurNutritionnelle(
            nutriment = nutriment,
            unite = nutriment.ue,
            valeur = valeurTotale,
            description = description,
            complete = tousLesIngredientsOntUneValeur && auMoinsUnIngredientAUneValeur
    )
}

/**
 * Obtient la liste de tous les nutriments disponibles dans le système
 *
 * @return Liste de tous les nutriments
 */
private fun obtenirTousLesNutriments(): List<Nutrient> {
    val nutriments = mutableListOf<Nutrient>()

    // Ajouter tous les types de nutriments
    nutriments.addAll(NutrientMain.entries)
    nutriments.addAll(NutrientMacro.entries)
    nutriments.addAll(NutrientMin.entries)
    nutriments.addAll(NutrientVitam.entries)
    nutriments.addAll(NutrientLipid.entries)
    nutriments.addAll(NutrientAnalysis.entries)
    nutriments.addAll(AAEnum.entries)
    nutriments.addAll(NutrientOther.entries)

    return nutriments
}

/**
 * Obtient une liste filtrée de nutriments selon les préférences utilisateur
 *
 * @param nutrimentsSelectionnes Liste des labels des nutriments sélectionnés dans les préférences
 * @return Liste des nutriments correspondant aux préférences
 */
fun obtenirNutrimentsSelonPreferences(nutrimentsSelectionnes: List<String>): List<Nutrient> {
    // Obtenir tous les nutriments disponibles
    val tousLesNutriments = obtenirTousLesNutriments()

    // Filtrer selon les préférences (comparaison par label)
    return tousLesNutriments.filter { nutriment ->
        nutrimentsSelectionnes.contains(nutriment.label)
    }
}

/**
 * Obtient une liste filtrée de nutriments selon les préférences utilisateur (version alternative
 * avec enum names)
 *
 * @param nutrimentsSelectionnes Liste des noms d'enum des nutriments sélectionnés dans les
 * préférences
 * @return Liste des nutriments correspondant aux préférences
 */
fun obtenirNutrimentsSelonPreferencesParNom(nutrimentsSelectionnes: List<String>): List<Nutrient> {
    // Obtenir tous les nutriments disponibles
    val tousLesNutriments = obtenirTousLesNutriments()

    // Filtrer selon les préférences (comparaison par label puisque les nutriments sont labelable)
    return tousLesNutriments.filter { nutriment ->
        nutrimentsSelectionnes.contains(nutriment.label)
    }
}

/**
 * Version modifiée de analyserValeursNutritionnellesRation qui ne traite que les nutriments
 * sélectionnés
 *
 * @param ration La ration à analyser
 * @param nutrimentsSelectionnes Liste des labels des nutriments à analyser
 * @return Map<String, ValeurNutritionnelle> contenant uniquement l'analyse des nutriments
 * sélectionnés
 */
fun analyserValeursNutritionnellesRationSelective(
        ration: Ration,
        nutrimentsSelectionnes: List<String>
): Map<String, ValeurNutritionnelle> {
    val resultat = mutableMapOf<String, ValeurNutritionnelle>()

    // Obtenir seulement les nutriments sélectionnés selon les préférences
    val nutrimentsAAnalyser = obtenirNutrimentsSelonPreferences(nutrimentsSelectionnes)

    // Pour chaque nutriment sélectionné, calculer sa valeur dans la ration
    nutrimentsAAnalyser.forEach { nutriment ->
        val valeurNutritionnelle = calculerValeurNutrimentDansRation(ration, nutriment)
        resultat[nutriment.label] = valeurNutritionnelle
    }

    return resultat
}

/**
 * Fonction d'exemple montrant comment utiliser analyserValeursNutritionnellesRation
 *
 * @param ration La ration à analyser
 */
fun exempleUtilisationAnalyseRation(ration: Ration) {
    println("=== Analyse nutritionnelle de la ration: ${ration.name} ===")

    val valeursNutritionnelles = analyserValeursNutritionnellesRation(ration)

    // Afficher les résultats pour quelques nutriments clés
    val nutrimentsImportants =
            listOf("PROTEINE", "LIPIDE", "GLUCIDE", "ENERGIE", "CAL", "PHOS", "FE", "VITA")

    nutrimentsImportants.forEach { labelNutriment ->
        val valeur = valeursNutritionnelles[labelNutriment]
        if (valeur != null) {
            println("\n--- $labelNutriment ---")
            println("Valeur totale: ${String.format("%.2f", valeur.valeur)} ${valeur.unite.label}")
            println("Complet: ${if (valeur.complete) "Oui" else "Non"}")
            println("Répartition: ${valeur.description}")
        }
    }

    // Statistiques générales
    val nutrimentsComplets = valeursNutritionnelles.values.count { it.complete }
    val nutrimentsAvecValeur = valeursNutritionnelles.values.count { it.valeur > 0 }
    val totalNutriments = valeursNutritionnelles.size

    println("\n=== Statistiques générales ===")
    println("Nutriments avec données complètes: $nutrimentsComplets/$totalNutriments")
    println("Nutriments avec au moins une valeur: $nutrimentsAvecValeur/$totalNutriments")
    println(
            "Pourcentage de complétude: ${String.format("%.1f", (nutrimentsComplets.toDouble() / totalNutriments) * 100)}%"
    )
}

/**
 * Exemple d'utilisation des nouvelles fonctions de filtrage par préférences
 *
 * @param ration La ration à analyser
 */
fun exempleUtilisationAnalyseRationSelective(ration: Ration) {
    println("=== Analyse nutritionnelle sélective de la ration: ${ration.name} ===")

    // Exemple de préférences utilisateur : seulement les nutriments principaux et quelques
    // vitamines
    val preferencesUtilisateur =
            listOf(
                    "PROTEINE",
                    "LIPIDE",
                    "GLUCIDE",
                    "ENERGIE",
                    "CAL",
                    "PHOS",
                    "VITA",
                    "VITD",
                    "VITE",
                    "FE",
                    "ZN",
                    "CU"
            )

    // Analyser seulement les nutriments sélectionnés
    val valeursNutritionnellesSelectives =
            analyserValeursNutritionnellesRationSelective(ration, preferencesUtilisateur)

    // Afficher les résultats
    println(
            "Nutriments analysés selon les préférences (${valeursNutritionnellesSelectives.size} nutriments):"
    )
    valeursNutritionnellesSelectives.forEach { (label, valeur) ->
        println(
                "- $label: ${String.format("%.2f", valeur.valeur)} ${valeur.unite.label} " +
                        "(Complet: ${if (valeur.complete) "Oui" else "Non"})"
        )
    }

    // Comparaison avec l'analyse complète
    val valeursNutritionnellesCompletes = analyserValeursNutritionnellesRation(ration)
    println("\nComparaison:")
    println("- Analyse complète: ${valeursNutritionnellesCompletes.size} nutriments")
    println("- Analyse sélective: ${valeursNutritionnellesSelectives.size} nutriments")
    println(
            "- Réduction: ${String.format("%.1f", 
        (1.0 - valeursNutritionnellesSelectives.size.toDouble() / valeursNutritionnellesCompletes.size) * 100)}%"
    )
}

/**
 * Convertit les préférences d'une espèce en liste de labels de nutriments sélectionnés
 *
 * @param preferencesEspece Les préférences pour l'espèce
 * @return Liste des labels des nutriments sélectionnés
 */
fun convertirPreferencesVersLabelsNutriments(preferencesEspece: PreferencesEspece): List<String> {
    val labelsSelectionnes = mutableListOf<String>()

    // Parcourir chaque catégorie de nutriments dans les préférences
    preferencesEspece.nutrimentsSelectionnes.forEach { (categorieNom, coefsSelectionnes) ->
        // Convertir le nom de catégorie en enum
        val categorie =
                try {
                    MainNutrientEnum.valueOf(categorieNom)
                } catch (e: IllegalArgumentException) {
                    return@forEach // Ignorer les catégories non reconnues
                }

        // Obtenir tous les nutriments de cette catégorie
        val nutrimentsCategorie = NutrientUtils.getNutrientsForCategory(categorie)

        // Filtrer les nutriments sélectionnés et récupérer leurs labels
        coefsSelectionnes.forEach { coefSelectionne ->
            val nutrimentTrouve = nutrimentsCategorie.find { it.coef == coefSelectionne }
            nutrimentTrouve?.let { nutriment -> labelsSelectionnes.add(nutriment.label) }
        }
    }

    return labelsSelectionnes.distinct() // Éliminer les doublons éventuels
}
