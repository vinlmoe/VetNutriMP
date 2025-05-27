package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
        var quantInt: Float? = null,
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
         * @return La valeur du nutriment ou null si non trouvé
         */
        fun getNutrient(nutrient: Nutrient): Float? {
                val quantity = valMap[nutrient]
                return quantity?.value
        }

        /**
         * Définit la valeur d'un nutriment dans cet aliment
         *
         * @param nutrient Le nutriment à définir
         * @param value La valeur du nutriment
         */
        fun setNutrient(nutrient: Nutrient, value: Float) {
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
