package fr.vetbrain.vetnutri_mp.Utils

/** Utilitaires pour le formatage des nombres multiplateformes */
object NumberUtils {
    /** Formate un nombre avec un nombre de décimales donné */
    fun format(number: Double, digits: Int): String {
        return if (digits == 0) {
            number.toLong().toString()
        } else {
            val multiplier = 10.0.pow(digits)
            val rounded = kotlin.math.round(number * multiplier) / multiplier
            val integerPart = rounded.toLong()
            val decimalPart = ((rounded - integerPart) * multiplier).toLong()
            
            if (decimalPart == 0L) {
                integerPart.toString()
            } else {
                val decimalStr = decimalPart.toString().padStart(digits, '0')
                "$integerPart.$decimalStr"
            }
        }
    }
    
    private fun Double.pow(n: Int): Double {
        var result = 1.0
        repeat(n) { result *= this }
        return result
    }

    /** Formate un nombre entier */
    fun format(number: Int): String {
        return number.toString()
    }
}
