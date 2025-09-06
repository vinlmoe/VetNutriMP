package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Utils.ExpressionMathematique
import fr.vetbrain.vetnutri_mp.Utils.genUUID

/** Classe représentant un aliment évalué Basée sur la classe AlimentEv du projet Java original */
data class AlimentEv(
        val uuid: String = genUUID(),
        val group: GroupAlim? = null,
        val typeAliment: FoodKind? = null,
        val ingredients: String? = null,
        val price: Double? = null,
        val categPrice: String? = null,
        val brand: String? = null,
        val gamme: String? = null,
        val nom: String? = null,
        val consistent: Boolean = false,
        val cont: ContEnum? = null,
        var quantInt: Double? = null,
        var deprecated: Boolean = false,
        var dataB: String? = null,
        var especes: MutableList<String> = mutableListOf(),
        var indicat: MutableList<AlimIndic> = mutableListOf(),
        var valMap: MutableMap<Nutrient, NutrientQuantity> = mutableMapOf(),
        val rationUUID: String? = null
) {
        /**
         * Obtient la valeur d'un nutriment dans cet aliment
         *
         * @param nutrient Le nutriment à rechercher
         * @param referenceEv Référence optionnelle pour calculer l'énergie via les équations
         * @return La valeur du nutriment ou null si non trouvé
         */
        fun getNutrient(nutrient: Nutrient, referenceEv: ReferenceEv? = null): Double? {
                // Protection de l'aminogramme : retourner null pour les acides aminés
                // si la base de données est VF24
                if (nutrient is AAEnum && dataB == "VF24") {
                        return null
                }

                // Si c'est l'énergie (NutrientEnergy ou NutrientMain.ENERGIE) et qu'on a une
                // référence avec des équations, utiliser les équations
                if (referenceEv != null &&
                                (nutrient is NutrientEnergy || nutrient == NutrientMain.ENERGIE)
                ) {
                        return calculerEnergieViaReference(referenceEv)
                }

                // Sinon, retourner la valeur stockée
                val quantity = valMap[nutrient]
                return quantity?.value
        }

        /** Calcule l'énergie via les équations de ReferenceEv */
        private fun calculerEnergieViaReference(referenceEv: ReferenceEv): Double? {
                // Déterminer si l'aliment est commercial (complet/complémentaire) ou brut
                val estCommercial =
                        indicat.any { indication ->
                                indication.name == "COMP" || indication.name == "COMPL"
                        }

                // Choisir l'équation appropriée
                val equation =
                        if (estCommercial) {
                                referenceEv.equationDEcom
                        } else {
                                referenceEv.equationDEraw
                        }

                if (equation == null || equation.equationScript.isEmpty()) {
                        return null
                }

                // Créer les variables pour l'évaluation
                val variables = mutableMapOf<String, Double>()

                // Ajouter les nutriments principaux nécessaires aux formules
                valMap.forEach { (nutrient, quantity) ->
                        variables[nutrient.label] = quantity.value
                }

                // Évaluer l'équation
                return try {
                        fr.vetbrain.vetnutri_mp.Utils.ExpressionMathematique.evaluer(
                                equation.equationScript,
                                variables
                        )
                                ?: null
                } catch (e: Exception) {
                        null
                }
        }

        /**
         * Définit la valeur d'un nutriment dans cet aliment
         *
         * @param nutrient Le nutriment à définir
         * @param value La valeur du nutriment
         */
        fun setNutrient(nutrient: Nutrient, value: Double) {
                valMap[nutrient] = NutrientQuantity(value, nutrient.ue.label)
        }

        /**
         * Vérifie si cet aliment contient un nutriment donné
         *
         * @param nutrient Le nutriment à vérifier
         * @return true si l'aliment contient le nutriment, false sinon
         */
        fun hasNutrient(nutrient: Nutrient): Boolean {
                return valMap.containsKey(nutrient)
        }

        /**
         * Obtient la liste des espèces pour lesquelles cet aliment est adapté
         *
         * @return La liste des espèces
         */
        fun getEspecesList(): List<Espece> {
                return especes.mapNotNull { especeStr -> Espece.getFromString(especeStr) }
        }

        /**
         * Vérifie si cet aliment est adapté pour une espèce donnée
         *
         * @param espece L'espèce à vérifier
         * @return true si l'aliment est adapté pour cette espèce, false sinon
         */
        fun isForEspece(espece: Espece): Boolean {
                return getEspecesList().contains(espece)
        }

        /**
         * Obtient la liste des indications pour cet aliment
         *
         * @return La liste des indications
         */
        fun getIndications(): List<AlimIndic> {
                return indicat.toList()
        }

        /**
         * Vérifie si cet aliment a une indication donnée
         *
         * @param indication L'indication à vérifier
         * @return true si l'aliment a cette indication, false sinon
         */
        fun hasIndication(indication: AlimIndic): Boolean {
                return indicat.contains(indication)
        }
}
