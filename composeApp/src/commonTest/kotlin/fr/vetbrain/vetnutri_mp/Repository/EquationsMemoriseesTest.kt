package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Equation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EquationsMemoriseesTest {

    /** Dépôt en mémoire qui compte les lectures par identifiant. */
    private class DepotCompteur : EquationRepository by InMemoryEquationRepository() {
        val depot = InMemoryEquationRepository()
        var lectures = 0
        override suspend fun getEquationById(uuid: String): Equation? {
            lectures++
            return depot.getEquationById(uuid)
        }
    }

    @Test
    fun uneLectureParEquation() = runTest {
        val source = DepotCompteur()
        source.depot.saveEquation(Equation(uuid = "a", equationScript = "FIBRETOT"))
        val equations = EquationsMemorisees(source)

        repeat(50) { assertEquals("FIBRETOT", equations.getEquationById("a")?.equationScript) }
        repeat(50) { assertNull(equations.getEquationById("absente")) }

        assertEquals(2, source.lectures)
    }
}
