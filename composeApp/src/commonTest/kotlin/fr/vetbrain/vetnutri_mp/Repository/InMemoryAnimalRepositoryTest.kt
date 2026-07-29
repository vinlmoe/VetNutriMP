package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class InMemoryAnimalRepositoryTest {

    private fun makeAnimal(
        uuid: String = "animal-uuid",
        nom: String = "Rex",
        specieId: String = Espece.CHIEN.label,
        race: String = ""
    ) = AnimalEv(uuid = uuid, nom = nom, specieId = specieId, race = race)

    // ── getAllAnimals ─────────────────────────────────────────────────────────

    @Test
    fun getAllAnimals_emptyRepo_returnsEmpty() = runTest {
        assertTrue(InMemoryAnimalRepository().getAllAnimals().isEmpty())
    }

    @Test
    fun getAllAnimals_afterSave_containsAnimal() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal())
        assertEquals(1, repo.getAllAnimals().size)
    }

    @Test
    fun getAllAnimals_multipleAnimals_returnsAll() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "a1"))
        repo.saveAnimal(makeAnimal(uuid = "a2"))
        repo.saveAnimal(makeAnimal(uuid = "a3"))
        assertEquals(3, repo.getAllAnimals().size)
    }

    // ── saveAnimal (insert) ───────────────────────────────────────────────────

    @Test
    fun saveAnimal_newAnimal_isInserted() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "a1", nom = "Minou"))
        assertEquals("Minou", repo.getAnimalById("a1")?.nom)
    }

    @Test
    fun saveAnimal_existingUuid_updatesInPlace() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "a1", nom = "Rex"))
        repo.saveAnimal(makeAnimal(uuid = "a1", nom = "MaxUpdated"))
        assertEquals(1, repo.getAllAnimals().size)
        assertEquals("MaxUpdated", repo.getAnimalById("a1")?.nom)
    }

    // ── getAnimalById ─────────────────────────────────────────────────────────

    @Test
    fun getAnimalById_existingUuid_returnsAnimal() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "abc"))
        assertNotNull(repo.getAnimalById("abc"))
    }

    @Test
    fun getAnimalById_nonExistentUuid_returnsNull() = runTest {
        assertNull(InMemoryAnimalRepository().getAnimalById("no-such-uuid"))
    }

    // ── updateAnimal ──────────────────────────────────────────────────────────

    @Test
    fun updateAnimal_existingAnimal_updatesName() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "u1", nom = "OldName"))
        repo.updateAnimal(makeAnimal(uuid = "u1", nom = "NewName"))
        assertEquals("NewName", repo.getAnimalById("u1")?.nom)
    }

    @Test
    fun updateAnimal_nonExistentAnimal_doesNotInsert() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.updateAnimal(makeAnimal(uuid = "ghost"))
        assertTrue(repo.getAllAnimals().isEmpty())
    }

    // ── deleteAnimal ──────────────────────────────────────────────────────────

    @Test
    fun deleteAnimal_existingAnimal_removesIt() = runTest {
        val repo = InMemoryAnimalRepository()
        val animal = makeAnimal(uuid = "del-uuid")
        repo.saveAnimal(animal)
        repo.deleteAnimal(animal)
        assertNull(repo.getAnimalById("del-uuid"))
    }

    @Test
    fun deleteAnimal_otherAnimalsUnaffected() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "keep"))
        val toDelete = makeAnimal(uuid = "del")
        repo.saveAnimal(toDelete)
        repo.deleteAnimal(toDelete)
        assertEquals(1, repo.getAllAnimals().size)
        assertNotNull(repo.getAnimalById("keep"))
    }

    // ── getRacesBySpecies ─────────────────────────────────────────────────────

    @Test
    fun getRacesBySpecies_noAnimals_returnsEmpty() = runTest {
        assertTrue(InMemoryAnimalRepository().getRacesBySpecies(Espece.CHIEN.label).isEmpty())
    }

    @Test
    fun getRacesBySpecies_matchingSpecies_returnsRaces() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "a1", specieId = Espece.CHIEN.label, race = "Labrador"))
        repo.saveAnimal(makeAnimal(uuid = "a2", specieId = Espece.CHIEN.label, race = "Beagle"))
        val races = repo.getRacesBySpecies(Espece.CHIEN.label)
        assertEquals(2, races.size)
        assertTrue(races.contains("Labrador"))
        assertTrue(races.contains("Beagle"))
    }

    @Test
    fun getRacesBySpecies_deduplicatesRaces() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "a1", specieId = Espece.CHIEN.label, race = "Labrador"))
        repo.saveAnimal(makeAnimal(uuid = "a2", specieId = Espece.CHIEN.label, race = "Labrador"))
        assertEquals(1, repo.getRacesBySpecies(Espece.CHIEN.label).size)
    }

    @Test
    fun getRacesBySpecies_otherSpeciesExcluded() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "a1", specieId = Espece.CHAT.label, race = "Siamois"))
        assertTrue(repo.getRacesBySpecies(Espece.CHIEN.label).isEmpty())
    }

    @Test
    fun getRacesBySpecies_blankRaceExcluded() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "a1", specieId = Espece.CHIEN.label, race = ""))
        assertTrue(repo.getRacesBySpecies(Espece.CHIEN.label).isEmpty())
    }

    @Test
    fun getRacesBySpecies_resultIsSorted() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(makeAnimal(uuid = "a1", specieId = Espece.CHIEN.label, race = "Labrador"))
        repo.saveAnimal(makeAnimal(uuid = "a2", specieId = Espece.CHIEN.label, race = "Beagle"))
        repo.saveAnimal(makeAnimal(uuid = "a3", specieId = Espece.CHIEN.label, race = "Akita"))
        assertEquals(listOf("Akita", "Beagle", "Labrador"), repo.getRacesBySpecies(Espece.CHIEN.label))
    }
}
