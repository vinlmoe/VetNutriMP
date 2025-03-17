package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import kotlinx.serialization.Serializable

/**
 * Classe représentant une quantité de nutriment avec son unité Cette classe étend la classe
 * NutrientQuantity existante
 */
@Serializable
data class NutrientQuantityExt(val value: Float, val unit: UnitEnum) {
    /**
     * Convertit cette quantité vers une autre unité
     *
     * @param targetUnit L'unité cible
     * @return La quantité convertie ou null si la conversion n'est pas possible
     */
    fun convertTo(targetUnit: UnitEnum): NutrientQuantityExt? {
        // Si les unités sont identiques, pas besoin de conversion
        if (unit == targetUnit) {
            return this
        }

        // TODO: Implémenter les conversions entre unités
        // Pour l'instant, nous retournons null pour indiquer que la conversion n'est pas supportée
        return null
    }

    /**
     * Additionne cette quantité avec une autre
     *
     * @param other L'autre quantité à additionner
     * @return La somme des deux quantités ou null si les unités sont incompatibles
     */
    fun plus(other: NutrientQuantityExt): NutrientQuantityExt? {
        // Si les unités sont identiques, addition directe
        if (unit == other.unit) {
            return NutrientQuantityExt(value + other.value, unit)
        }

        // Sinon, essayer de convertir l'autre quantité dans notre unité
        val converted = other.convertTo(unit)

        return if (converted != null) {
            NutrientQuantityExt(value + converted.value, unit)
        } else {
            null
        }
    }

    /**
     * Multiplie cette quantité par un facteur
     *
     * @param factor Le facteur de multiplication
     * @return La quantité multipliée
     */
    fun times(factor: Float): NutrientQuantityExt {
        return NutrientQuantityExt(value * factor, unit)
    }

    /**
     * Représentation textuelle de cette quantité
     *
     * @return La représentation textuelle
     */
    override fun toString(): String {
        return "$value ${unit.label}"
    }
}
