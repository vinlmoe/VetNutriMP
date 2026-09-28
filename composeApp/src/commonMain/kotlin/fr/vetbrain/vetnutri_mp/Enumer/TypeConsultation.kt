package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/**
 * Type de consultation.
 * - STANDARD : une ou plusieurs rations, toutes calculées au poids de la consultation.
 * - EVOLUTIVE : un plan de rations par étapes (croissance, gestation, lactation, activité...),
 *   chaque étape ayant son propre poids et ses propres variables d'énergie.
 */
enum class TypeConsultation(override val label: String) : Labelable {
    STANDARD("Standard"),
    EVOLUTIVE("Évolutive");

    override fun toString(): String = label

    companion object {
        /** Valeur persistée → enum ; STANDARD par défaut (anciennes données, valeur inconnue). */
        fun fromName(name: String?): TypeConsultation =
                entries.firstOrNull { it.name == name } ?: STANDARD
    }
}

/**
 * Profil d'une consultation évolutive. Sert uniquement de préréglage (libellés, variables
 * proposées) : les calculs sont pilotés exclusivement par les équations de la référence.
 */
enum class ProfilEvolutif(override val label: String) : Labelable {
    CROISSANCE("Croissance"),
    GESTATION("Gestation"),
    LACTATION("Lactation"),
    ACTIVITE("Activité"),
    AUTRE("Autre");

    override fun toString(): String = label

    companion object {
        fun fromName(name: String?): ProfilEvolutif? = entries.firstOrNull { it.name == name }
    }
}
