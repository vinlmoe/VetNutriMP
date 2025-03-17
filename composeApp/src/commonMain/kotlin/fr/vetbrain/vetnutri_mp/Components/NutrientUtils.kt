package fr.vetbrain.vetnutri_mp.Components

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient

/**
 * Calcule les valeurs nutritionnelles totales d'une ration
 *
 * @param alimentsRation Liste des aliments de la ration
 * @param nutriments Liste des nutriments à considérer
 * @return Map associant chaque nutriment à sa valeur totale dans la ration
 */
fun calculerValeursNutritionnelles(
        alimentsRation: List<AlimentRation>,
        nutriments: List<Nutrient>
): Map<Nutrient, Float> {
    val resultat = mutableMapOf<Nutrient, Float>()

    println(
            "Calcul des valeurs nutritionnelles pour ${alimentsRation.size} aliments et ${nutriments.size} nutriments"
    )

    // Initialiser tous les nutriments à 0
    nutriments.forEach { nutriment -> resultat[nutriment] = 0f }

    // Pour chaque aliment, ajouter sa contribution pour chaque nutriment
    alimentsRation.forEachIndexed { index, alimentRation ->
        // Si l'aliment a des informations nutritionnelles
        try {
            // Utilisation sécurisée pour gérer le cas où ces propriétés pourraient ne
            // pas exister
            val aliment = alimentRation.aliment
            if (aliment == null) {
                println("Aliment #$index: NULL - Ignoré")
                return@forEachIndexed
            }

            println(
                    "Traitement de l'aliment #$index: ${aliment.nom ?: "Sans nom"} (${aliment.uuid})"
            )
            println("  - Quantité: ${alimentRation.quantity}g")
            println("  - valMap contient ${aliment.valMap.size} nutriments")

            // Accès direct aux valeurs nutritionnelles dans valMap
            nutriments.forEach { nutriment ->
                val valeurNutritive = aliment.valMap[nutriment]?.value
                if (valeurNutritive != null) {
                    // La valeur est en g/kg ou unités/kg, donc pour obtenir la
                    // valeur réelle:
                    // valeur * quantité(g) / 1000
                    val quantiteEnKg = alimentRation.quantity / 1000f
                    val contributionNutriment = valeurNutritive * quantiteEnKg
                    val valeurCourante = resultat[nutriment] ?: 0f
                    val nouvelleValeur = valeurCourante + contributionNutriment
                    resultat[nutriment] = nouvelleValeur

                    println(
                            "  - Nutriment ${nutriment.label} trouvé dans valMap: $valeurNutritive -> contribution: $contributionNutriment"
                    )
                } else {
                    // Vérifier si le nutriment existe avec une clé similaire
                    // (insensible à la casse)
                    val nutrimentTrouve =
                            aliment.valMap.keys.find {
                                it.label.equals(nutriment.label, ignoreCase = true)
                            }

                    if (nutrimentTrouve != null) {
                        val valeurNutritiveAlt = aliment.valMap[nutrimentTrouve]?.value
                        if (valeurNutritiveAlt != null) {
                            val quantiteEnKg = alimentRation.quantity / 1000f
                            val contributionNutriment = valeurNutritiveAlt * quantiteEnKg
                            val valeurCourante = resultat[nutriment] ?: 0f
                            val nouvelleValeur = valeurCourante + contributionNutriment
                            resultat[nutriment] = nouvelleValeur

                            println(
                                    "  - Nutriment ${nutriment.label} trouvé via ${nutrimentTrouve.label}: $valeurNutritiveAlt -> contribution: $contributionNutriment"
                            )
                        }
                    }
                }
            }

            // Si la valMap est vide ou si certains nutriments n'y sont pas,
            // essayer d'accéder via getNutrient pour compatibilité
            if (aliment.valMap.isEmpty() ||
                            nutriments.any { nutriment ->
                                nutriment !in aliment.valMap.keys &&
                                        !aliment.valMap.keys.any {
                                            it.label.equals(nutriment.label, ignoreCase = true)
                                        }
                            }
            ) {
                println(
                        "  - Utilisation de getNutrient() comme fallback pour les nutriments manquants"
                )

                // Accès direct aux propriétés nutritionnelles
                // Pour chaque nutriment demandé
                nutriments.forEach { nutriment ->
                    // Vérifier si la valeur existe déjà dans le résultat final
                    // pour cet aliment
                    val valeurExistante = resultat[nutriment]
                    if (valeurExistante != null && valeurExistante > 0f) {
                        println(
                                "  - Nutriment ${nutriment.label} déjà traité, valeur actuelle: $valeurExistante"
                        )
                        return@forEach
                    }

                    // Obtenir la valeur du nutriment directement depuis
                    // l'aliment
                    val valeurNutriment = aliment.getNutrient(nutriment)

                    // Si la valeur existe, l'ajouter au total en prenant en
                    // compte la quantité de l'aliment
                    if (valeurNutriment != null) {
                        // La valeur est en g/kg ou unités/kg, donc pour
                        // obtenir la valeur réelle:
                        // valeur * quantité(g) / 1000
                        val quantiteEnKg = alimentRation.quantity / 1000f
                        // Calcul sécurisé de la contribution
                        val contributionNutriment = valeurNutriment * quantiteEnKg
                        // Mise à jour sécurisée du résultat
                        val valeurCourante = resultat[nutriment] ?: 0f
                        val nouvelleValeur = valeurCourante + contributionNutriment
                        resultat[nutriment] = nouvelleValeur

                        println(
                                "  - Nutriment ${nutriment.label} obtenu via getNutrient(): $valeurNutriment -> contribution: $contributionNutriment"
                        )
                    } else {
                        println("  - Nutriment ${nutriment.label} non trouvé via getNutrient()")
                    }
                }
            }
        } catch (e: Exception) {
            // Ignorer les erreurs et continuer avec les autres aliments
            println(
                    "Erreur lors du calcul des valeurs nutritionnelles pour l'aliment #$index: ${e.message}"
            )
            e.printStackTrace()
        }
    }

    // Afficher un résumé des résultats
    println("Résultats du calcul des valeurs nutritionnelles:")
    nutriments.forEach { nutriment ->
        val valeur = resultat[nutriment] ?: 0f
        println("  - ${nutriment.label}: $valeur ${nutriment.unite}")
    }

    return resultat
}
