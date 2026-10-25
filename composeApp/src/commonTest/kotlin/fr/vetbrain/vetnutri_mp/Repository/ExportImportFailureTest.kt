package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Data.toApi
import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest

class ExportImportFailureTest {
    private fun envelope(version: String = "2.0.0") = ApiEnvelope(version, 0, listOf(
        AnimalEv(uuid = "one", nom = "Rex").toApi(),
        AnimalEv(uuid = "two", nom = "Milo").toApi()
    ))

    @Test fun countsEverySuccessfullyImportedAnimal() = runTest {
        val repo = InMemoryAnimalRepository()
        val counts = ExportImportRepository(repo).importAll(envelope())
        assertEquals(2, counts.animals)
        assertEquals(0, counts.errorCount)
        assertEquals(2, repo.getAllAnimals().size)
    }

    @Test fun partialFailureIsVisibleWithoutListener() = runTest {
        val backing = InMemoryAnimalRepository()
        val failing = object : AnimalRepository by backing {
            override suspend fun saveAnimal(animal: AnimalEv) {
                if (animal.uuid == "two") error("write failed")
                backing.saveAnimal(animal)
            }
        }
        val counts = ExportImportRepository(failing).importAll(envelope())
        assertEquals(1, counts.animals)
        assertEquals(1, counts.errorCount)
        assertFailsWith<IllegalStateException> { counts.requireComplete() }
        assertNotNull(backing.getAnimalById("one"))
        assertNull(backing.getAnimalById("two"))
    }

    @Test fun strictModeFailsAsAWholeWhenOneItemFails() = runTest {
        val backing = InMemoryAnimalRepository()
        val failing = object : AnimalRepository by backing {
            override suspend fun saveAnimal(animal: AnimalEv) {
                if (animal.uuid == "two") error("write failed")
                backing.saveAnimal(animal)
            }
        }
        assertFailsWith<IllegalStateException> {
            ExportImportRepository(failing).importAll(envelope(), strict = true)
        }
        val counts = ExportImportRepository(failing).importAll(envelope(), strict = false)
        assertEquals(1, counts.errorCount)
    }

    @Test fun newerMajorEnvelopeVersionIsRefusedBeforeWriting() = runTest {
        val repo = InMemoryAnimalRepository()
        val exception = assertFailsWith<UnsupportedEnvelopeVersionException> {
            ExportImportRepository(repo).importAll(envelope(version = "3.0.0"))
        }
        assertEquals("3.0.0", exception.version)
        assertTrue(exception.message.orEmpty().contains("3.0.0"))
        assertEquals(0, repo.getAllAnimals().size)
    }

    @Test fun currentOlderAndMissingEnvelopeVersionsAreAccepted() = runTest {
        for (version in listOf("2.0.0", "2.9.1", "1.0.0", "", "abc")) {
            val repo = InMemoryAnimalRepository()
            val counts = ExportImportRepository(repo).importAll(envelope(version = version))
            assertEquals(2, counts.animals, "version « $version »")
            assertEquals(0, counts.errorCount)
        }
    }

    @Test fun cancellationIsNotConvertedIntoImportSuccess() = runTest {
        val backing = InMemoryAnimalRepository()
        val cancelled = object : AnimalRepository by backing {
            override suspend fun saveAnimal(animal: AnimalEv) { throw CancellationException("cancelled") }
        }
        assertFailsWith<CancellationException> { ExportImportRepository(cancelled).importAll(envelope()) }
    }
}
