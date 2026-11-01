package fr.vetbrain.vetnutri_mp.Utils

/** Utilitaires pour le formatage des nombres multiplateformes */
object NumberUtils {
    /** Formate un nombre avec un nombre de décimales donné */
    fun format(number: Double, digits: Int): String {
        // Découpage sur l'entier arrondi : recalculer la partie décimale en Double la tronquait
        // ((1,38 − 1) × 100 = 37,999… → « 1.37 »)
        val multiplier = 10.0.pow(digits).toLong()
        val scaled = kotlin.math.round(kotlin.math.abs(number) * multiplier).toLong()
        val signe = if (number < 0 && scaled != 0L) "-" else ""
        val decimalPart = scaled % multiplier
        return if (decimalPart == 0L) {
            "$signe${scaled / multiplier}"
        } else {
            "$signe${scaled / multiplier}.${decimalPart.toString().padStart(digits, '0')}"
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
