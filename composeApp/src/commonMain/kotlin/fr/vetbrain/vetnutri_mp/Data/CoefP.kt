package fr.vetbrain.vetnutri_mp.Data

/**
 * Classe représentant un coefficient de référence nutritionnelle
 *
 * @property uuid Identifiant unique du coefficient
 * @property description Description du coefficient
 * @property coef Valeur du coefficient
 * @property groupUUID Identifiant du groupe auquel appartient le coefficient (0-4)
 */
data class CoefP(
        val uuid: String = java.util.UUID.randomUUID().toString(),
        val description: String = "",
        val coef: Float = 1.0f,
        val groupUUID: Int = 0
)
