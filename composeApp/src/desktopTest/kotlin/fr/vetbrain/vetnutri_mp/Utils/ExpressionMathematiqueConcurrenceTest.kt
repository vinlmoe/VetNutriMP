package fr.vetbrain.vetnutri_mp.Utils

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionMathematiqueConcurrenceTest {

    @Test
    fun evaluationsSimultanees_chacuneSonResultat() {
        // Un parseur partagé mélangeait expression, position et variables d'un thread à l'autre :
        // une équation BEE valide sortait « absente ou invalide » (analyse du troupeau)
        val cas = (1..8).map { i ->
            val poids = 5.0 * i
            Triple(if (i % 2 == 0) "130*BW^0.75" else "(70*BW^0.75)+BW*2", mapOf("BW" to poids),
                    if (i % 2 == 0) 130 * poids.pow(0.75) else 70 * poids.pow(0.75) + poids * 2)
        }
        val pool = Executors.newFixedThreadPool(8)
        try {
            val erreurs = pool.invokeAll(cas.map { (expr, vars, attendu) ->
                Callable {
                    (1..3000).count { abs((ExpressionMathematique.evaluer(expr, vars) ?: Double.NaN) - attendu) > 1e-9 }
                }
            }).sumOf { it.get() }
            assertEquals(0, erreurs, "évaluations fausses ou nulles sous concurrence")
        } finally {
            pool.shutdown()
        }
    }
}
