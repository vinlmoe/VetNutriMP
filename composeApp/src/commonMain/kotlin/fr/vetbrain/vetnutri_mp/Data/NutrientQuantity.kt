package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Utils.ensurePositiveValue
import kotlin.math.round

/**
 * Classe représentant une quantité de nutriment avec sa valeur et son unité. Cette classe stocke
 * l'information sur la quantité d'un nutriment dans un aliment.
 *
 * @property value La valeur numérique de la quantité du nutriment
 * @property unite L'unité dans laquelle cette valeur est exprimée
 */
data class NutrientQuantity(var value: Float, val unite: String) {
    init {
        // S'assurer que la valeur n'est jamais négative
        value = ensurePositiveValue(value)
        println(
                "DEBUG NutrientQuantity: Création d'une nouvelle instance avec valeur=$value, unite=$unite"
        )
    }

    /**
     * Convertit la valeur actuelle en utilisant l'unité fournie
     *
     * @param targetUnit L'unité cible pour la conversion
     * @return La valeur convertie dans la nouvelle unité
     */
    fun convertTo(targetUnit: UnitEnum): Float {
        println("DEBUG NutrientQuantity: Conversion de $value $unite vers ${targetUnit.name}")
        return when (targetUnit) {
            UnitEnum.BUmg -> value * 1000 // g -> mg
            UnitEnum.BUmu -> value * 1000000 // g -> μg
            UnitEnum.BUg -> value // g -> g (pas de conversion)
            UnitEnum.AUui -> value // UI -> UI (pas de conversion)
            UnitEnum.AUmu -> value // μg -> μg (pas de conversion)
            UnitEnum.DUui -> value // UI -> UI (pas de conversion)
            UnitEnum.DUmu -> value // μg -> μg (pas de conversion)
            UnitEnum.EUui -> value // UI -> UI (pas de conversion)
            UnitEnum.EUmg -> value // mg -> mg (pas de conversion)
            UnitEnum.KCAL -> value // kcal -> kcal (pas de conversion)
            UnitEnum.NO -> value // pas de conversion
        }
    }

    /**
     * Formate la valeur selon les règles d'affichage standard
     *
     * @return La valeur formatée sous forme de chaîne
     */
    fun formatValue(): String {
        println("DEBUG NutrientQuantity: Formatage de la valeur $value")
        // Règles de formatage :
        // - Valeurs < 0.001 : arrondi à 5 décimales
        // - Valeurs entre 0.001 et 0.01 : arrondi à 4 décimales
        // - Valeurs entre 0.01 et 0.1 : arrondi à 3 décimales
        // - Valeurs entre 0.1 et 1 : arrondi à 2 décimales
        // - Valeurs entre 1 et 10 : arrondi à 1 décimale
        // - Valeurs > 10 : arrondi à l'entier
        return when {
            value < 0.001F -> "%.5f".format(value)
            value < 0.01F -> "%.4f".format(value)
            value < 0.1F -> "%.3f".format(value)
            value < 1F -> "%.2f".format(value)
            value < 10F -> "%.1f".format(value)
            else -> "${round(value).toInt()}"
        }
    }
}

/** Object utilitaire pour la création d'instances de NutrientQuantity */
object NutrientQuantityFactory {
    /** Crée une instance de NutrientQuantity avec des valeurs par défaut sécurisées */
    fun createDefault(): NutrientQuantity {
        println("DEBUG NutrientQuantity: Création d'une instance par défaut")
        return NutrientQuantity(0.0f, "g")
    }
}
