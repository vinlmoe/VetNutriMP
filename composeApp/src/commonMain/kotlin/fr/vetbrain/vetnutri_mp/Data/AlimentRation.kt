package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
data class AlimentRation(
        val uuid: String = genUUID(),
        val uuidUnif: String = "",
        val quantite: Double = 0.0,
        val proportion: Double = 0.0,
        var aliment: AlimentEv? = null,
        val weight: Double = 1.0,
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
        val quantity: Double
                get() = quantite

        /**
         * Obtient la valeur d'un nutriment dans cet aliment
         *
         * @param nutrient Le nutriment à rechercher
         * @return La valeur du nutriment ou null si non trouvé
         */
        fun getNutrient(nutrient: Nutrient): Double? {
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
                equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null,
                referenceEv: ReferenceEv? = null
        ): Double? {
                // D'abord, essayer d'obtenir la valeur directement
                val valeurDirecte = getNutrient(nutrient)
                if (valeurDirecte != null && valeurDirecte > 0.0) {
                        return valeurDirecte
                }

                // Si pas de valeur directe et qu'on a les dépendances pour les équations
                // complémentaires
                if ((preferences != null || referenceEv != null) && equationRepository != null) {
                        // 1) Essayer via mapping direct nutriment -> uuid (anciens prefs)
                        val eqUuidDirect = preferences?.getEquationComplementaire(nutrient.label)
                        if (eqUuidDirect != null) {
                                val eq = equationRepository.getEquationById(eqUuidDirect)
                                if (eq != null) {
                                        val res =
                                                fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator
                                                        .evaluerBesoinNutritionnelPourAliment(
                                                                expression = eq.equationScript,
                                                                aliment = this,
                                                                preferences = preferences,
                                                                equationRepository =
                                                                        equationRepository,
                                                                referenceEv = referenceEv
                                                        )
                                        if (res != null) return res
                                }
                        }

                        // 2) Sinon, utiliser la sélection actuelle (liste d'UUID) et filtrer par
                        // nutriment/espèce. On combine ReferenceEv (si fournie) et Préférences.
                        val selectedUuids =
                                buildList {
                                                referenceEv?.equationsNut?.let { list ->
                                                        addAll(list.map { it.uuid })
                                                }
                                                preferences?.getSelectedEquationUuids()?.let {
                                                        addAll(it)
                                                }
                                        }
                                        .distinct()

                        if (selectedUuids.isNotEmpty()) {
                                var accum: Double? = null
                                val especePref =
                                        try {
                                                referenceEv?.espece
                                                        ?: preferences?.let {
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .Espece.valueOf(it.espece)
                                                        }
                                        } catch (e: Exception) {
                                                null
                                        }
                                selectedUuids.forEach { uuid ->
                                        val eq = equationRepository.getEquationById(uuid)
                                        if (eq != null) {
                                                val kindOk =
                                                        eq.kind ==
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .EquationKind
                                                                        .COMPLEMENTARY_NUTRIENT
                                                val nutrientOk =
                                                        eq.nutrient == nutrient ||
                                                                eq.nutrient?.label == nutrient.label
                                                val specieOk =
                                                        especePref == null ||
                                                                eq.specie == especePref ||
                                                                eq.specie ==
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.Espece.CH
                                                if (!kindOk || !nutrientOk || !specieOk) {
                                                        println()
                                                }
                                                if (kindOk && nutrientOk && specieOk) {
                                                        val res =
                                                                fr.vetbrain.vetnutri_mp.Utils
                                                                        .EquationEvaluator
                                                                        .evaluerBesoinNutritionnelPourAliment(
                                                                                expression =
                                                                                        eq.equationScript,
                                                                                aliment = this,
                                                                                preferences =
                                                                                        preferences,
                                                                                equationRepository =
                                                                                        equationRepository,
                                                                                referenceEv =
                                                                                        referenceEv
                                                                        )
                                                                        ?: 0.0
                                                        println()
                                                        accum =
                                                                if (eq.ratio) res
                                                                else (accum ?: 0.0) + res
                                                }
                                        }
                                }

                                // Si ratio: la dernière valeur res est la valeur par 100g
                                // Si somme: accum contient la somme des contributions par 100g
                                if (accum != null) return accum
                        }

                        // 3) Fallback: calculateur existant (micro-ration)
                        return if (preferences != null) {
                                fr.vetbrain.vetnutri_mp.Utils.ComplementaryNutrientCalculator
                                        .calculerNutrimentComplementaire(
                                                nutrient,
                                                this,
                                                preferences,
                                                equationRepository
                                        )
                        } else null
                }

                return null
        }

        /**
         * Calcule la quantité d'énergie fournie par cet aliment
         *
         * @return La quantité d'énergie
         */
        fun getEnergie(): Double {
                return densiteEnergetique * quantite
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
        fun getDE(equationScript: String): Double {
                // Cette méthode devrait retourner la densité énergétique calculée
                // avec l'équation spécifiée
                return densiteEnergetique
        }

        /**
         * Définit la densité énergétique calculée avec une équation donnée
         *
         * @param densite La densité énergétique
         * @param equationScript Le script de l'équation utilisée
         */
        fun setDE(densite: Double, equationScript: String) {
                // Cette méthode devrait stocker la densité énergétique calculée
                // avec l'équation spécifiée
                // Comme densiteEnergetique est un val, on ne peut pas le modifier directement
                // Dans une implémentation réelle, il faudrait utiliser une map pour stocker
                // les densités par équation
        }
}
