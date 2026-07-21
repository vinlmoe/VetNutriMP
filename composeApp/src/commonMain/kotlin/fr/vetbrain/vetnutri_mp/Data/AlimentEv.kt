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
        var lastUpdateDate: String? = null,
        var imageRef: String? = null,
        var especes: MutableList<String> = mutableListOf(),
        var indicat: MutableList<AlimIndic> = mutableListOf(),
        var valMap: MutableMap<Nutrient, NutrientQuantity> = mutableMapOf(),
        val rationUUID: String? = null,
        val biblioRefs: List<BiblioRef> = emptyList(),
        var energieParEspece: Map<String, Double> = emptyMap()
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

                // Si c'est l'énergie (NutrientEnergy ou NutrientMain.ENERGIE)
                if (nutrient is NutrientEnergy || nutrient == NutrientMain.ENERGIE) {
                        val especeNom = referenceEv?.espece?.name
                        if (especeNom != null) {
                                // Priorité 1 : valeur définie pour cette espèce précise
                                val vEspece = energieParEspece[especeNom]
                                if (vEspece != null && vEspece > 0.0) return vEspece
                                // Si des valeurs par espèce existent mais pas pour celle-ci → équation
                                if (energieParEspece.isNotEmpty()) {
                                        return calculerEnergieViaReference(referenceEv)
                                                ?.let { if (it < 0.0) 0.0 else it }
                                }
                        }
                        // Rétro-compat : pas de valeurs par espèce → valeur générique puis équation
                        val generique = valMap[nutrient]?.value
                        if (generique != null && generique > 0.0) return generique
                        if (referenceEv != null) {
                                return calculerEnergieViaReference(referenceEv)
                                        ?.let { if (it < 0.0) 0.0 else it }
                        }
                        return null
                }

                // Sinon, retourner la valeur stockée (jamais négative)
                val quantity = valMap[nutrient]
                return quantity?.value?.let { if (it < 0.0) 0.0 else it }
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

                val variables: MutableMap<String, Double> = mutableMapOf()
                valMap.forEach { (nutrient, quantity) ->
                        variables[nutrient.label] = quantity.value
                }
                return try {
                        fr.vetbrain.vetnutri_mp.Utils.ExpressionMathematique.evaluer(
                                equation.equationScript,
                                variables
                        )?.let { if (it < 0.0) 0.0 else it }
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
         * Définit la valeur d'un nutriment avec sa plage min/max (base CALNUT 2020).
         *
         * @param nutrient Le nutriment à définir
         * @param value La valeur moyenne (borne centrale MB)
         * @param min La borne basse (LB), ou null si aucune plage n'est connue
         * @param max La borne haute (UB), ou null si aucune plage n'est connue
         */
        fun setNutrient(nutrient: Nutrient, value: Double, min: Double?, max: Double?) {
                valMap[nutrient] = NutrientQuantity(value, nutrient.ue.label, min, max)
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
