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
        val complete: Boolean,
        /**
         * Borne basse agrégée (somme des min CALNUT des ingrédients), ou null si aucun ingrédient
         * ne porte de plage. Permet d'afficher la plage min–max sur les bullet graphs.
         */
        val valeurMin: Double? = null,
        /** Borne haute agrégée (somme des max CALNUT des ingrédients), ou null si aucune plage. */
        val valeurMax: Double? = null
) {
        /** Indique qu'une vraie plage min/max (issue de CALNUT) est disponible. */
        val hasRange: Boolean
                get() = valeurMin != null || valeurMax != null
}
