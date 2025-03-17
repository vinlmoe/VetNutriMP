package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientEnergy
import kotlinx.serialization.Serializable

/**
 * Classe représentant une référence de nutriment Basée sur la classe NutrientRef du projet Java
 * original
 */
@Serializable
data class NutrientRef(
        val nutrient: Nutrient,
        val value: Float,
        val biblioRef: BiblioRef = BiblioRef(),
        val consistent: Boolean = true
) {
    /**
     * Vérifie si cette référence est cohérente
     *
     * @return true si la référence est cohérente, false sinon
     */
    fun isConsistent(): Boolean {
        return consistent
    }

    companion object {
        val EMPTY =
                NutrientRef(nutrient = NutrientEnergy.TOT, value = 0.0f, biblioRef = BiblioRef())
    }
}
