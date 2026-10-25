package fr.vetbrain.vetnutri_mp.Utils

/** Utilitaires pour le formatage des nombres multiplateformes */
object NumberUtils {
    /** Valeur affichée pour un nombre non fini (NaN, ±Infini) : un tiret, lisible dans l'UI et les exports. */
    const val NON_FINI: String = "—"

    /**
     * Formate un nombre avec au plus [digits] décimales.
     *
     * Règles :
     * - arrondi « demi vers l'infini » (half away from zero) : 2,25 → 2,3 ; -2,25 → -2,3 ;
     *   une tolérance de 1e-9 sur la valeur mise à l'échelle absorbe l'erreur binaire
     *   (2,3 × 10 = 22,999999999999996 ; 1,005 × 100 = 100,49999999999999) ;
     * - les décimales sont complétées par des zéros à gauche (4,05 → "4.05"), mais si la
     *   partie décimale arrondie est nulle elle est omise (5,0 → "5", 1,04 à 1 décimale → "1"),
     *   comportement attendu par les appelants (« 1 sachet », « 5 kg ») ;
     * - [digits] ≤ 0 : arrondi à l'entier (42,7 → "43") ;
     * - NaN ou ±Infini : [NON_FINI] ;
     * - séparateur décimal : point.
     */
    fun format(number: Double, digits: Int): String {
        if (number.isNaN() || number.isInfinite()) return NON_FINI
        val d = if (digits < 0) 0 else digits
        val multiplier = 10.0.pow(d)
        val negatif = number < 0.0
        val absolu = if (negatif) -number else number
        // Arrondi demi vers l'infini sur la valeur mise à l'échelle, avec tolérance binaire.
        val echelle = kotlin.math.floor(absolu * multiplier + 0.5 + 1e-9).toLong()
        val facteur = echelle10(d)
        val partieEntiere = echelle / facteur
        val partieDecimale = echelle % facteur
        val signe = if (negatif && echelle != 0L) "-" else ""
        return if (partieDecimale == 0L) {
            "$signe$partieEntiere"
        } else {
            "$signe$partieEntiere.${partieDecimale.toString().padStart(d, '0')}"
        }
    }

    private fun echelle10(n: Int): Long {
        var result = 1L
        repeat(n) { result *= 10 }
        return result
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
