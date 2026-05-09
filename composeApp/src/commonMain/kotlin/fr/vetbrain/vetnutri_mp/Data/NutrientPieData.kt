package fr.vetbrain.vetnutri_mp.Data

import kotlinx.serialization.Serializable

/**
 * Données d'un nutriment pour le graphique en secteurs
 * Note: On utilise Long pour la couleur pour éviter les problèmes de classe inline (Color) dans les data classes
 */
@Serializable
data class NutrientPieData(
    val name: String,
    val value: Double,
    val colorValue: Long,
    val percentage: Double
)




