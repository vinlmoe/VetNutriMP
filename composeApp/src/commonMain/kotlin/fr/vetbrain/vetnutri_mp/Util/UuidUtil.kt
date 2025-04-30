package fr.vetbrain.vetnutri_mp.Util

import kotlin.random.Random

/**
 * Classe utilitaire pour la génération d'identifiants uniques (UUID) dans un contexte
 * multiplateforme.
 */
object UuidUtil {
    /**
     * Génère un identifiant unique simple basé sur le temps système et un nombre aléatoire. Note:
     * Cette implémentation simple peut être utilisée en l'absence d'une bibliothèque complète
     * d'UUID et est compatible avec toutes les plateformes.
     *
     * @return Un identifiant unique sous forme de chaîne
     */
    fun generateUuid(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random.nextInt(100000)
        return "$timestamp-$random"
    }
}
