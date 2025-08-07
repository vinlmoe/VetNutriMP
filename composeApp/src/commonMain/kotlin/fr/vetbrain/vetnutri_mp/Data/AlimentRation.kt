package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
data class AlimentRation(
        val uuid: String = genUUID(),
        val uuidUnif: String = "",
        val quantite: Float = 0f,
        val proportion: Float = 0f,
        var aliment: AlimentEv? = null,
        val weight: Float = 1f,
        val category: Int = 0,
        val densiteEnergetique: Double = 0.0,
        val refAlimUnif: String? = null,
        var refRation: String? = null,
        val refTarget: Int? = null
) {
        /**
         * Propriété pour la compatibilité avec les références à quantity Cette propriété est un
         * alias pour quantite
         */
        val quantity: Float
                get() = quantite

        /**
         * Obtient la valeur d'un nutriment dans cet aliment
         *
         * @param nutrient Le nutriment à rechercher
         * @return La valeur du nutriment ou null si non trouvé
         */
        fun getNutrient(nutrient: Nutrient): Float? {
                // Déléguer à l'aliment sous-jacent s'il existe
                return aliment?.getNutrient(nutrient)
        }

        /**
         * Obtient la valeur d'un nutriment dans cet aliment, en utilisant une équation
         * complémentaire si nécessaire
         *
         * @param nutrient Le nutriment à rechercher
         * @param preferences Les préférences de l'espèce (pour les équations complémentaires)
         * @param equationRepository Le repository des équations (pour les équations
         * complémentaires)
         * @return La valeur du nutriment ou null si non trouvé et pas d'équation complémentaire
         */
        suspend fun getNutrientWithComplementary(
                nutrient: Nutrient,
                preferences: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece? = null,
                equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null
        ): Float? {
                // D'abord, essayer d'obtenir la valeur directement
                val valeurDirecte = getNutrient(nutrient)
                if (valeurDirecte != null && valeurDirecte > 0f) {
                        return valeurDirecte
                }

                // Si pas de valeur directe et qu'on a les dépendances pour les équations
                // complémentaires
                if (preferences != null && equationRepository != null) {
                        return fr.vetbrain.vetnutri_mp.Utils.ComplementaryNutrientCalculator
                                .calculerNutrimentComplementaire(
                                        nutrient,
                                        this,
                                        preferences,
                                        equationRepository
                                )
                }

                return null
        }

        /**
         * Calcule la quantité d'énergie fournie par cet aliment
         *
         * @return La quantité d'énergie
         */
        fun getEnergie(): Float {
                return (densiteEnergetique * quantite).toFloat()
        }

        /**
         * Vérifie si cet aliment a une densité énergétique définie pour une équation donnée
         *
         * @param equationScript Le script de l'équation à vérifier
         * @return true si la densité est définie, false sinon
         */
        fun isDE(equationScript: String): Boolean {
                // Cette méthode devrait vérifier si la densité énergétique a déjà été calculée
                // avec l'équation spécifiée
                return densiteEnergetique > 0.0
        }

        /**
         * Obtient la densité énergétique calculée avec une équation donnée
         *
         * @param equationScript Le script de l'équation utilisée
         * @return La densité énergétique
         */
        fun getDE(equationScript: String): Float {
                // Cette méthode devrait retourner la densité énergétique calculée
                // avec l'équation spécifiée
                return densiteEnergetique.toFloat()
        }

        /**
         * Définit la densité énergétique calculée avec une équation donnée
         *
         * @param densite La densité énergétique
         * @param equationScript Le script de l'équation utilisée
         */
        fun setDE(densite: Float, equationScript: String) {
                // Cette méthode devrait stocker la densité énergétique calculée
                // avec l'équation spécifiée
                // Comme densiteEnergetique est un val, on ne peut pas le modifier directement
                // Dans une implémentation réelle, il faudrait utiliser une map pour stocker
                // les densités par équation
        }
}
