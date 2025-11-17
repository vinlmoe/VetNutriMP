package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
data class Ration(
        var uuid: String = genUUID(),
        var idConsult: String = "",
        var name: String = "",
        var coef: Double = 1.0,
        var actual: Boolean = false,
        var number: Int = 1,
        var espece: String? = null,
        var recette: Boolean = false,
        var description: String = "",
        var alimentMutableList: MutableList<AlimentRation> = mutableListOf()
) {
        fun getAlimentByUUID(uuiDalim: String): AlimentRation {
                return alimentMutableList.last { al -> al.uuid == uuiDalim }
        }

        fun getEspece(): Espece {
                return Espece.getByLabel(espece ?: "") ?: Espece.CHIEN
        }

        fun setEspece(especeEnum: Espece) {
                this.espece = especeEnum.label
        }

        /**
         * Obtient la valeur d'un nutriment dans la ration
         *
         * @param nutrient Le nutriment à rechercher
         * @param referenceEv Référence optionnelle pour calculer l'énergie via les équations
         * @return La valeur du nutriment ou null si non trouvé
         */
        fun getNutrient(nutrient: Nutrient, referenceEv: ReferenceEv? = null): Double? {
                // Calculer la somme des valeurs du nutriment pour tous les aliments de la ration
                var total = 0.0

                for (aliment in alimentMutableList) {
                        val valeur = aliment.getNutrient(nutrient, referenceEv)
                        if (valeur != null) {
                                total += (valeur * aliment.quantite) / 100.0
                        }
                }

                return if (total > 0.0) total else null
        }

        /**
         * Calcule la quantité totale d'aliments dans la ration
         *
         * @return La quantité totale d'aliments
         */
        fun getQuantiteTotale(): Double {
                return alimentMutableList.sumOf { it.quantite }
        }

        /**
         * Calcule la densité énergétique moyenne de la ration
         *
         * @param referenceEv Référence optionnelle pour calculer l'énergie via les équations
         * @param equationRepository Repository des équations pour les équations énergétiques
         * @return La densité énergétique moyenne
         */
        suspend fun getDensiteEnergetiqueMoyenne(
                referenceEv: ReferenceEv? = null,
                equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null
        ): Double {
                val quantiteTotale = getQuantiteTotale()

                if (quantiteTotale <= 0.0) {
                        return 0.0
                }

                var totalEnergie = 0.0

                for (aliment in alimentMutableList) {
                        totalEnergie +=
                                aliment.getEnergie(referenceEv, equationRepository)
                }

                return totalEnergie / quantiteTotale
        }

        /**
         * Version non-suspend de getDensiteEnergetiqueMoyenne qui utilise les valeurs stockées
         * (pour compatibilité avec les contextes non-suspend)
         *
         * @return La densité énergétique moyenne basée sur les valeurs stockées
         */
        fun getDensiteEnergetiqueMoyenne(): Double {
                val quantiteTotale = getQuantiteTotale()

                if (quantiteTotale <= 0.0) {
                        return 0.0
                }

                var totalEnergie = 0.0

                for (aliment in alimentMutableList) {
                        totalEnergie += aliment.densiteEnergetique * aliment.quantite
                }

                return totalEnergie / quantiteTotale
        }
}
