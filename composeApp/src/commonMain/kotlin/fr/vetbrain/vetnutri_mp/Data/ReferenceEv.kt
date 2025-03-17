package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * Classe représentant une référence évaluée Basée sur la classe ReferenceEv du projet Java original
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class ReferenceEv(
        val uuid: String = Uuid.random().toString(),
        val nutrient: Nutrient,
        val references: MutableList<NutrientRef> = mutableListOf()
) {
        /**
         * Ajoute une référence à cette référence évaluée
         *
         * @param reference La référence à ajouter
         */
        fun ajouterReference(reference: NutrientRef) {
                references.add(reference)
        }

        /**
         * Calcule la valeur de cette référence pour un animal donné
         *
         * @param bee Le besoin énergétique de base
         * @param bw Le poids de l'animal
         * @param mw Le poids métabolique
         * @param svp Liste des variables supplémentaires
         * @param ration La ration à évaluer
         * @return La valeur calculée
         */
        fun calculerValeur(
                bee: Float,
                bw: Float,
                mw: Float,
                svp: List<SupplementalvariableP>,
                ration: Ration
        ): Float {
                // Implémentation simplifiée pour l'instant
                return 0.0f
        }

        /**
         * Vérifie si cette référence contient des références
         *
         * @return true si la référence contient des références, false sinon
         */
        fun hasReferences(): Boolean {
                return references.isNotEmpty()
        }
}
