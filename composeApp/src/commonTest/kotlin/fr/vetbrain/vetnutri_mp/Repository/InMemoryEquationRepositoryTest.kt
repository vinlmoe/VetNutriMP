package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Equation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class InMemoryEquationRepositoryTest {

    private fun makeEquation(uuid: String = "test-uuid", name: String = "TestEq") =
        Equation(uuid = uuid, name = name)

    @Test
    fun getAllEquations_emptyRepo_returnsEmpty() = runTest {
        val repo = InMemoryEquationRepository()
        assertTrue(repo.getAllEquations().isEmpty())
    }

    @Test
    fun saveEquation_thenGetAll_containsEquation() = runTest {
        val repo = InMemoryEquationRepository()
        repo.saveEquation(makeEquation())
        val all = repo.getAllEquations()
        assertEquals(1, all.size)
        assertEquals("TestEq", all.first().name)
    }

    @Test
    fun saveMultipleEquations_getAllReturnsAll() = runTest {
        val repo = InMemoryEquationRepository()
        repo.saveEquation(makeEquation("u1", "Eq1"))
        repo.saveEquation(makeEquation("u2", "Eq2"))
        assertEquals(2, repo.getAllEquations().size)
    }

    @Test
    fun getEquationById_existingUuid_returnsEquation() = runTest {
        val repo = InMemoryEquationRepository()
        repo.saveEquation(makeEquation(uuid = "uuid-abc"))
        val found = repo.getEquationById("uuid-abc")
        assertNotNull(found)
        assertEquals("uuid-abc", found.uuid)
    }

    @Test
    fun getEquationById_nonExistentUuid_returnsNull() = runTest {
        val repo = InMemoryEquationRepository()
        assertNull(repo.getEquationById("does-not-exist"))
    }

    @Test
    fun updateEquation_changesName() = runTest {
        val repo = InMemoryEquationRepository()
        val eq = makeEquation(uuid = "upd-uuid", name = "Original")
        repo.saveEquation(eq)
        repo.updateEquation(eq.copy(name = "Updated"))
        assertEquals("Updated", repo.getEquationById("upd-uuid")?.name)
    }

    @Test
    fun deleteEquation_removesIt() = runTest {
        val repo = InMemoryEquationRepository()
        repo.saveEquation(makeEquation(uuid = "del-uuid"))
        repo.deleteEquation("del-uuid")
        assertNull(repo.getEquationById("del-uuid"))
    }

    @Test
    fun deleteEquation_otherEquationsUnaffected() = runTest {
        val repo = InMemoryEquationRepository()
        repo.saveEquation(makeEquation("keep"))
        repo.saveEquation(makeEquation("del"))
        repo.deleteEquation("del")
        assertEquals(1, repo.getAllEquations().size)
        assertNotNull(repo.getEquationById("keep"))
    }

    @Test
    fun clearAllEquations_returnsCount() = runTest {
        val repo = InMemoryEquationRepository()
        repo.saveEquation(makeEquation("u1"))
        repo.saveEquation(makeEquation("u2"))
        assertEquals(2, repo.clearAllEquations())
    }

    @Test
    fun clearAllEquations_emptiesRepo() = runTest {
        val repo = InMemoryEquationRepository()
        repo.saveEquation(makeEquation("u1"))
        repo.clearAllEquations()
        assertTrue(repo.getAllEquations().isEmpty())
    }

    @Test
    fun clearAllEquations_emptyRepo_returnsZero() = runTest {
        val repo = InMemoryEquationRepository()
        assertEquals(0, repo.clearAllEquations())
    }
}
