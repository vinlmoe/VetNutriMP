package fr.vetbrain.vetnutri_mp.Components

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain

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
): Map<Nutrient, Double> {
    val resultat = mutableMapOf<Nutrient, Double>()


    // Initialiser tous les nutriments à 0
    nutriments.forEach { nutriment -> resultat[nutriment] = 0.0 }

    // Pour chaque aliment, ajouter sa contribution pour chaque nutriment
    alimentsRation.forEachIndexed { index, alimentRation ->
        // Si l'aliment a des informations nutritionnelles
        try {
            // Utilisation sécurisée pour gérer le cas où ces propriétés pourraient ne
            // pas exister
            val aliment = alimentRation.aliment
            if (aliment == null) {
                return@forEachIndexed
            }


            // Accès direct aux valeurs nutritionnelles dans valMap
            nutriments.forEach { nutriment ->
                val valeurNutritive = aliment.valMap[nutriment]?.value
                if (valeurNutritive != null) {
                    // La valeur est en g/kg ou unités/kg, donc pour obtenir la
                    // valeur réelle:
                    // valeur * quantité(g) / 1000
                    val quantiteEnKg = alimentRation.quantity / 1000.0
                    val contributionNutriment = if (nutriment is AAEnum) {
                        // Pour les acides aminés, les valeurs sont en % de protéines
                        // Il faut multiplier par la teneur en protéines de l'aliment
                        val teneurProteines = aliment.getNutrient(NutrientMain.PROTEINE) ?: 0.0
                        val valeurAminoAcideEnPourcentAliment = (valeurNutritive * teneurProteines) / 100.0
                        valeurAminoAcideEnPourcentAliment * quantiteEnKg
                    } else {
                        // Pour les autres nutriments, calcul normal
                        valeurNutritive * quantiteEnKg
                    }
                    val valeurCourante = resultat[nutriment] ?: 0.0
                    val nouvelleValeur = valeurCourante + contributionNutriment
                    resultat[nutriment] = nouvelleValeur

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
                            val quantiteEnKg = alimentRation.quantity / 1000.0
                            val contributionNutriment = valeurNutritiveAlt * quantiteEnKg
                            val valeurCourante = resultat[nutriment] ?: 0.0
                            val nouvelleValeur = valeurCourante + contributionNutriment
                            resultat[nutriment] = nouvelleValeur

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

                // Accès direct aux propriétés nutritionnelles
                // Pour chaque nutriment demandé
                nutriments.forEach { nutriment ->
                    // Vérifier si la valeur existe déjà dans le résultat final
                    // pour cet aliment
                    val valeurExistante = resultat[nutriment]
                    if (valeurExistante != null && valeurExistante > 0.0) {
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
                        val quantiteEnKg = alimentRation.quantity / 1000.0
                        // Calcul sécurisé de la contribution
                        val contributionNutriment = if (nutriment is AAEnum) {
                            // Pour les acides aminés, les valeurs sont en % de protéines
                            // Il faut multiplier par la teneur en protéines de l'aliment
                            val teneurProteines = aliment.getNutrient(NutrientMain.PROTEINE) ?: 0.0
                            val valeurAminoAcideEnPourcentAliment = (valeurNutriment * teneurProteines) / 100.0
                            valeurAminoAcideEnPourcentAliment * quantiteEnKg
                        } else {
                            // Pour les autres nutriments, calcul normal
                            valeurNutriment * quantiteEnKg
                        }
                        // Mise à jour sécurisée du résultat
                        val valeurCourante = resultat[nutriment] ?: 0.0
                        val nouvelleValeur = valeurCourante + contributionNutriment
                        resultat[nutriment] = nouvelleValeur

                    } else {
                        
                    }
                }
            }
        } catch (e: Exception) {
            // Ignorer les erreurs et continuer avec les autres aliments
            e.printStackTrace()
        }
    }

    // Afficher un résumé des résultats
    nutriments.forEach { nutriment ->
        val valeur = resultat[nutriment] ?: 0.0
    }

    return resultat
}
