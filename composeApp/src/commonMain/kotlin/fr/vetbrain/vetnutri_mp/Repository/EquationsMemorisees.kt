package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Equation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Lectures d'équations mémorisées le temps d'un calcul en série (analyse de centaines d'aliments) :
 * chaque aliment relit les mêmes équations du référentiel, sinon une requête à chaque fois.
 * Les écritures passent au dépôt d'origine. Ne pas garder l'instance au-delà du calcul : elle ne
 * voit pas les équations modifiées entre-temps.
 */
class EquationsMemorisees(private val source: EquationRepository) : EquationRepository by source {
    private val verrou = Mutex()
    private val parId = HashMap<String, Equation?>()
    private var toutes: List<Equation>? = null

    override suspend fun getEquationById(uuid: String): Equation? {
        verrou.withLock { if (parId.containsKey(uuid)) return parId[uuid] }
        val equation = source.getEquationById(uuid)
        verrou.withLock { parId[uuid] = equation }
        return equation
    }

    override suspend fun getAllEquations(): List<Equation> {
        verrou.withLock { toutes?.let { return it } }
        val liste = source.getAllEquations()
        verrou.withLock { toutes = liste }
        return liste
    }
}
