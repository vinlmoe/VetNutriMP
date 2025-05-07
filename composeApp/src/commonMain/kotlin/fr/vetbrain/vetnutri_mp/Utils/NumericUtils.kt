package fr.vetbrain.vetnutri_mp.Utils

/**
 * S'assure qu'une valeur Float est positive. Si la valeur est négative, elle est remplacée par
 * zéro.
 * @param value La valeur à vérifier
 * @return La valeur d'origine si elle est positive, ou zéro si elle est négative
 */
fun ensurePositiveValue(value: Float): Float {
    if (value < 0) {
        println("DEBUG NumericUtils: Valeur négative détectée: $value, remplacée par 0")
        return 0f
    }
    return value
}

/**
 * S'assure qu'une valeur Double est positive. Si la valeur est négative, elle est remplacée par
 * zéro.
 * @param value La valeur à vérifier
 * @return La valeur d'origine si elle est positive, ou zéro si elle est négative
 */
fun ensurePositiveValue(value: Double): Double {
    if (value < 0) {
        println("DEBUG NumericUtils: Valeur négative détectée: $value, remplacée par 0")
        return 0.0
    }
    return value
}

/**
 * S'assure qu'une valeur Int est positive. Si la valeur est négative, elle est remplacée par zéro.
 * @param value La valeur à vérifier
 * @return La valeur d'origine si elle est positive, ou zéro si elle est négative
 */
fun ensurePositiveValue(value: Int): Int {
    if (value < 0) {
        println("DEBUG NumericUtils: Valeur négative détectée: $value, remplacée par 0")
        return 0
    }
    return value
}

/**
 * Fonction utilitaire pour arrondir un Float à un nombre spécifié de décimales
 * @param value La valeur à arrondir
 * @param decimals Le nombre de décimales à conserver
 * @return La valeur arrondie
 */
fun roundToDecimals(value: Float, decimals: Int): Float {
    val factor = Math.pow(10.0, decimals.toDouble())
    return (Math.round(value * factor) / factor).toFloat()
}

/**
 * Fonction utilitaire pour arrondir un Double à un nombre spécifié de décimales
 * @param value La valeur à arrondir
 * @param decimals Le nombre de décimales à conserver
 * @return La valeur arrondie
 */
fun roundToDecimals(value: Double, decimals: Int): Double {
    val factor = Math.pow(10.0, decimals.toDouble())
    return Math.round(value * factor) / factor
}
