package fr.vetbrain.vetnutri_mp.Utils

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

/**
 * Utilitaires de formatage intelligent pour les graphiques
 */
object GraphFormattingUtils {

    /**
     * Formate un nombre avec un nombre intelligent de décimales selon la valeur
     * 
     * @param value La valeur à formater
     * @param maxDecimals Nombre maximum de décimales (défaut: 3)
     * @return String formatée
     */
    fun formatSmartDecimal(value: Double, maxDecimals: Int = 3): String {
        if (value.isNaN() || value.isInfinite()) {
            return "N/A"
        }

        val absValue = abs(value)
        
        return when {
            // Valeur zéro : pas de décimales
            absValue == 0.0 -> {
                "0"
            }
            // Valeurs très petites (< 0.01) : 3-4 décimales
            absValue < 0.01 -> {
                formatDecimal(value, 4)
            }
            // Valeurs petites (< 0.1) : 3 décimales
            absValue < 0.1 -> {
                formatDecimal(value, 3)
            }
            // Valeurs moyennes (< 1) : 2 décimales
            absValue < 1.0 -> {
                formatDecimal(value, 2)
            }
            // Valeurs normales (< 10) : 1 décimale
            absValue < 10.0 -> {
                formatDecimal(value, 1)
            }
            // Valeurs >= 10 : pas de décimales
            else -> {
                formatDecimal(value, 0)
            }
        }
    }

    /**
     * Formate un nombre avec un nombre fixe de décimales
     * 
     * @param value La valeur à formater
     * @param decimals Nombre de décimales
     * @return String formatée
     */
    fun formatDecimal(value: Double, decimals: Int): String {
        if (value.isNaN() || value.isInfinite()) {
            return "N/A"
        }

        val multiplier = 10.0.pow(decimals)
        val rounded = (value * multiplier).let { 
            if (it >= 0) kotlin.math.floor(it + 0.5) else kotlin.math.ceil(it - 0.5)
        } / multiplier

        return if (decimals == 0) {
            rounded.toInt().toString()
        } else {
            // Approche compatible iOS - formatage manuel
            val integerPart = rounded.toInt()
            val fractionalPart = ((rounded - integerPart) * multiplier).toInt()
            val fractionalString = fractionalPart.toString().padStart(decimals, '0')
            "$integerPart.$fractionalString"
        }
    }

    /**
     * Formate un pourcentage avec un nombre intelligent de décimales
     * 
     * @param value La valeur en pourcentage (0-100)
     * @return String formatée avec le symbole %
     */
    fun formatPercentage(value: Double): String {
        val formatted = formatSmartDecimal(value, 1)
        return "$formatted%"
    }

    /**
     * Formate une valeur énergétique (kcal)
     * 
     * @param value La valeur énergétique
     * @return String formatée avec l'unité kcal
     */
    fun formatEnergy(value: Double): String {
        val formatted = formatSmartDecimal(value, 1)
        return "${formatted} kcal"
    }

    /**
     * Formate une densité énergétique (kcal/100g)
     * 
     * @param value La densité énergétique
     * @return String formatée avec l'unité
     */
    fun formatEnergyDensity(value: Double): String {
        val formatted = formatSmartDecimal(value, 1)
        return "${formatted} kcal/100g"
    }

    /**
     * Formate une valeur de nutriment (g/1000kcal)
     * 
     * @param value La valeur du nutriment
     * @return String formatée avec l'unité
     */
    fun formatNutrientPer1000Kcal(value: Double): String {
        val formatted = formatSmartDecimal(value, 2)
        return "${formatted} g/1000kcal"
    }

    /**
     * Formate un poids (kg)
     * 
     * @param value Le poids
     * @return String formatée avec l'unité kg
     */
    fun formatWeight(value: Double): String {
        val formatted = formatSmartDecimal(value, 1)
        return "${formatted} kg"
    }

    /**
     * Formate un âge en mois avec décimales intelligentes
     * 
     * @param ageInMonths L'âge en mois
     * @return String formatée
     */
    fun formatAgeInMonths(ageInMonths: Double): String {
        return when {
            ageInMonths < 1.0 -> {
                val days = (ageInMonths * 30.44).toInt()
                "${days} jours"
            }
            ageInMonths < 12.0 -> {
                val formatted = formatSmartDecimal(ageInMonths, 1)
                "${formatted} mois"
            }
            else -> {
                val years = ageInMonths / 12.0
                val formatted = formatSmartDecimal(years, 1)
                "${formatted} ans"
            }
        }
    }
}
