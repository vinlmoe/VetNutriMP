package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum

/**
 * Data class représentant une valeur nutritionnelle avec ses propriétés
 *
 * @param nutriment Le nutriment associé à cette valeur
 * @param unite L'unité de mesure de la valeur
 * @param valeur La valeur numérique du nutriment
 * @param description Description textuelle de la valeur nutritionnelle
 * @param complete Indique si cette valeur nutritionnelle est complète
 */
data class ValeurNutritionnelle(
        val nutriment: Nutrient,
        val unite: UnitEnum,
        val valeur: Double,
        val description: String,
        val complete: Boolean
)
