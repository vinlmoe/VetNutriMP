package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import kotlin.math.pow
import kotlin.math.round
import fr.vetbrain.vetnutri_mp.Data.RationNutrimentData
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

// Fonction pour récupérer la valeur d'un nutriment depuis RationNutrimentData
fun RationNutrimentData.getNutrimentValue(key: String): Double {
        return when (key) {
                "proteine" -> proteines
                "lipide" -> lipides
                "glucide" -> glucides
                "energie" -> energie
                "calcium" -> calcium
                "phosphore" -> phosphore
                "magnesium" -> magnesium
                "sodium" -> sodium
                "potassium" -> potassium
                else -> 0.0
        }
}

/**
 * Calcule une plage adaptative pour les axes Y des histogrammes basée sur la distribution des
 * données - Version robuste et flexible
 */
fun calculateAdaptiveRange(
        values: List<Float>,
        paddingPercent: Float = 0.08f
): ClosedFloatingPointRange<Float> {
        if (values.isEmpty()) return 0f..1f

        val minValue = values.minOf { it }
        val maxValue = values.maxOf { it }

        // Valeurs de sécurité pour éviter tout problème
        val safeMinValue = maxOf(0f, minValue) // Pas de valeurs négatives
        val safeMaxValue = maxOf(safeMinValue + 0.1f, maxValue) // Au moins une petite plage

        // Calcul de la plage de données
        val dataRange = safeMaxValue - safeMinValue

        // Padding adaptatif basé sur la plage des données
        val adaptivePadding =
                when {
                        dataRange == 0f -> maxOf(safeMaxValue * 0.2f, 1f) // Valeurs identiques
                        dataRange < 10f -> dataRange * 0.3f // Petite plage
                        dataRange < 100f -> dataRange * 0.15f // Plage moyenne
                        else -> dataRange * 0.1f // Grande plage
                }

        // Calcul des bornes avec padding adaptatif
        val lowerBound = maxOf(0f, safeMinValue - adaptivePadding)
        val upperBound = safeMaxValue + adaptivePadding

        // Garantie finale : plage d'au moins 0.1f et au plus raisonnable
        val finalLowerBound = lowerBound
        val finalUpperBound = maxOf(upperBound, lowerBound + 0.1f)

        return finalLowerBound..finalUpperBound
}

/**
 * Arrondit une valeur float à un nombre raisonnable de décimales pour l'affichage
 */
fun arrondirPourAffichage(value: Float, maxDecimals: Int = 2): Float {
    val multiplier = 10.0.pow(maxDecimals.toDouble()).toFloat()
    return (round(value * multiplier) / multiplier)
}

/**
 * Arrondit une plage pour éviter les problèmes d'arrondi dans les labels d'axes
 */
fun arrondirPlage(range: ClosedFloatingPointRange<Float>, maxDecimals: Int = 2): ClosedFloatingPointRange<Float> {
    val start = arrondirPourAffichage(range.start, maxDecimals)
    val endInclusive = arrondirPourAffichage(range.endInclusive, maxDecimals)
    return start..endInclusive
}

// Fonction pour calculer la plage zoomée/panée
fun calculateZoomedRangeView(
        originalRange: ClosedFloatingPointRange<Float>,
        zoomPanState: ZoomPanStateView,
        isXAxis: Boolean = true
): ClosedFloatingPointRange<Float> {
        val scale = if (isXAxis) zoomPanState.scaleX else zoomPanState.scaleY
        val pan = if (isXAxis) zoomPanState.panX else zoomPanState.panY
        
        val originalSize = originalRange.endInclusive - originalRange.start
        val newSize = originalSize / scale
        
        val center = (originalRange.start + originalRange.endInclusive) / 2f
        val panOffset = pan
        
        val newStart = center - newSize / 2f + panOffset
        val newEnd = center + newSize / 2f + panOffset
        
        return arrondirPlage(newStart..newEnd)
}

// Fonction pour formater l'âge en années et mois
fun formatAge(ageInYears: Double, ageInMonths: Double): String {
        return when {
                ageInYears >= 1.0 -> {
                        val years = ageInYears.toInt()
                        val remainingMonths = ((ageInYears - years) * 12).toInt()
                        val yearUnit = if (years > 1) translate(LocalizationKeys.General.UNIT_YEARS) else translate(LocalizationKeys.General.UNIT_YEAR)
                        
                        if (remainingMonths > 0) {
                                "$years $yearUnit $remainingMonths ${translate(LocalizationKeys.General.UNIT_MONTH)}"
                        } else {
                                "$years $yearUnit"
                        }
                }
                else -> {
                        val months = ageInMonths.toInt()
                        "$months ${translate(LocalizationKeys.General.UNIT_MONTH)}"
                }
        }
}

// Fonction helper pour normaliser le texte de poids (virgule -> point)
fun normaliserTextePoids(texte: String): String {
        // Remplacer la virgule par un point pour la conversion
        return texte.replace(',', '.')
}

// Fonction helper pour convertir le texte en poids (gère virgule et point)
fun convertirTexteEnPoids(texte: String): Double? {
        val texteNormalise = normaliserTextePoids(texte)
        return texteNormalise.toDoubleOrNull()
}
