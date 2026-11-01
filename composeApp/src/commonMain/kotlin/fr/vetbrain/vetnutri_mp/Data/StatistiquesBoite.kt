package fr.vetbrain.vetnutri_mp.Data

import kotlin.math.floor
import kotlin.math.sqrt

/**
 * Résumé d'une boîte à moustaches (convention de Tukey) : quartiles par interpolation linéaire
 * (comme R par défaut et Excel QUARTILE.INC), moustaches jusqu'à la valeur la plus éloignée
 * restant à 1,5 × l'écart interquartile de la boîte ; au-delà, valeurs isolées. Moyenne et
 * écart-type complètent le résumé (tableau des statistiques de l'export).
 */
data class StatistiquesBoite(
        val n: Int,
        val q1: Double,
        val mediane: Double,
        val q3: Double,
        val moustacheBasse: Double,
        val moustacheHaute: Double,
        val isolees: List<Double>,
        val moyenne: Double,
        /** Écart-type de l'échantillon (dénominateur n − 1) ; 0 pour une seule valeur. */
        val ecartType: Double
) {
    val minimum: Double get() = minOf(moustacheBasse, isolees.minOrNull() ?: moustacheBasse)
    val maximum: Double get() = maxOf(moustacheHaute, isolees.maxOrNull() ?: moustacheHaute)

    companion object {
        /** Null s'il n'y a aucune valeur finie. */
        fun de(valeurs: List<Double>): StatistiquesBoite? {
            val triees = valeurs.filter { it.isFinite() }.sorted()
            if (triees.isEmpty()) return null
            val q1 = quantile(triees, 0.25)
            val mediane = quantile(triees, 0.5)
            val q3 = quantile(triees, 0.75)
            val ecart = q3 - q1
            val basse = q1 - 1.5 * ecart
            val haute = q3 + 1.5 * ecart
            val dedans = triees.filter { it in basse..haute }
            val moyenne = triees.average()
            return StatistiquesBoite(
                    n = triees.size,
                    q1 = q1,
                    mediane = mediane,
                    q3 = q3,
                    moustacheBasse = dedans.first(),
                    moustacheHaute = dedans.last(),
                    isolees = triees.filter { it < basse || it > haute },
                    moyenne = moyenne,
                    ecartType =
                            if (triees.size < 2) 0.0
                            else sqrt(triees.sumOf { (it - moyenne) * (it - moyenne) } / (triees.size - 1))
            )
        }

        /** Quantile p de valeurs triées, interpolation linéaire entre les rangs. */
        internal fun quantile(triees: List<Double>, p: Double): Double {
            val position = p * (triees.size - 1)
            val bas = floor(position).toInt()
            val haut = minOf(bas + 1, triees.size - 1)
            return triees[bas] + (position - bas) * (triees[haut] - triees[bas])
        }
    }
}
