package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Ration(
        var uuid: String = Uuid.random().toString(),
        var idConsult: String = "",
        var name: String = "",
        var coef: Float = 1.0f,
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
         * @return La valeur du nutriment ou null si non trouvé
         */
        fun getNutrient(nutrient: Nutrient): Float? {
                // Calculer la somme des valeurs du nutriment pour tous les aliments de la ration
                var total = 0.0f

                for (aliment in alimentMutableList) {
                        val valeur = aliment.getNutrient(nutrient)
                        if (valeur != null) {
                                total += valeur * aliment.quantite / 100f
                        }
                }

                return if (total > 0.0f) total else null
        }

        /**
         * Calcule la quantité totale d'aliments dans la ration
         *
         * @return La quantité totale d'aliments
         */
        fun getQuantiteTotale(): Float {
                return alimentMutableList.sumOf { it.quantite.toDouble() }.toFloat()
        }

        /**
         * Calcule la densité énergétique moyenne de la ration
         *
         * @return La densité énergétique moyenne
         */
        fun getDensiteEnergetiqueMoyenne(): Float {
                val quantiteTotale = getQuantiteTotale()

                if (quantiteTotale <= 0.0f) {
                        return 0.0f
                }

                var totalEnergie = 0.0f

                for (aliment in alimentMutableList) {
                        totalEnergie += aliment.densiteEnergetique.toFloat() * aliment.quantite
                }

                return totalEnergie / quantiteTotale
        }
}
