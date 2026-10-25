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

    /**
     * Arrondit [valeur] au multiple de [pas] le plus proche. Le produit n × pas d'un pas décimal
     * (0,1 ; 0,3) porte une erreur binaire (3 × 0,1 = 0,30000000000000004) : le résultat est
     * ramené au double le plus proche de la valeur décimale attendue.
     */
    fun arrondirAuPas(valeur: Double, pas: Double): Double = multipleDuPas(kotlin.math.round(valeur / pas), pas)

    /** Multiple de [pas] immédiatement supérieur ou égal à [valeur] (tolérance 1e-9 sur le quotient). */
    fun arrondirAuPasSuperieur(valeur: Double, pas: Double): Double =
        multipleDuPas(kotlin.math.ceil(valeur / pas - 1e-9), pas)

    /** n × pas, nettoyé au milliardième (assez fin pour des grammes, exact jusqu'à 10⁶ g). */
    private fun multipleDuPas(n: Double, pas: Double): Double = kotlin.math.round(n * pas * 1e9) / 1e9

    /** Formate un nombre entier */
    fun format(number: Int): String {
        return number.toString()
    }
}
