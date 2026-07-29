package fr.vetbrain.vetnutri_mp.Repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefEntity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Vérifie que `DatabaseFoodRepository.importFoodsDomain` (utilisée par les imports JSON et
 * Excel) persiste bien les jonctions ALIMENT_BIBLIO_REFS, contrairement au comportement
 * précédent qui ne les écrivait que pour insertFood/updateFood unitaires.
 *
 * Première utilisation d'une base Room en mémoire dans les tests du projet (JVM/desktop
 * uniquement) : importFoodsDomain est du Kotlin commun sans branche par plateforme, la
 * couverture desktop suffit à valider la logique.
 */
class DatabaseFoodRepositoryBiblioImportTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DatabaseFoodRepository

    @BeforeTest
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        repository = DatabaseFoodRepository(
            foodDao = db.foodDao(),
            nutrientValueDao = db.nutrientValueDao(),
            customNutrientDao = db.customNutrientDao(),
            alimentBiblioRefDao = db.alimentBiblioRefDao(),
            biblioRefDao = db.biblioRefDao(),
            energyPerSpeciesDao = db.energyPerSpeciesDao()
        )
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private suspend fun insertBiblioRef(ref: BiblioRef) {
        db.biblioRefDao().insertBiblioRef(
            BiblioRefEntity(
                uuid = ref.uuid,
                firstAuthor = ref.firstAuthor,
                year = ref.year,
                completeRef = ref.completeRef,
                comments = ref.comments,
                bibtex = ref.bibtex,
                consistent = ref.consistent
            )
        )
    }

    @Test
    fun importFoodsDomain_newFoodWithBiblioRefs_persistsJunctions() = runTest {
        val ref = BiblioRef(uuid = "b1", firstAuthor = "Dupont", year = 2020, completeRef = "Ref 1")
        insertBiblioRef(ref)

        val aliment = AlimentEv(uuid = "food-1", nom = "Test", biblioRefs = listOf(ref))
        repository.importFoodsDomain(listOf(aliment))

        assertEquals(listOf("b1"), db.alimentBiblioRefDao().getBiblioRefUuids("food-1"))
    }

    @Test
    fun importFoodsDomain_noBiblioRefs_createsNoJunctionsAndDoesNotThrow() = runTest {
        val aliment = AlimentEv(uuid = "food-1", nom = "Test")
        repository.importFoodsDomain(listOf(aliment))

        assertTrue(db.alimentBiblioRefDao().getBiblioRefUuids("food-1").isEmpty())
    }

    @Test
    fun importFoodsDomain_updateWithDifferentBiblioRefs_replacesJunctions() = runTest {
        val refA = BiblioRef(uuid = "a", firstAuthor = "Auteur A", year = 2019, completeRef = "Ref A")
        val refB = BiblioRef(uuid = "b", firstAuthor = "Auteur B", year = 2022, completeRef = "Ref B")
        insertBiblioRef(refA)
        insertBiblioRef(refB)

        // Premier import : uniquement refA
        repository.importFoodsDomain(listOf(AlimentEv(uuid = "food-1", nom = "Test", biblioRefs = listOf(refA))))
        assertEquals(listOf("a"), db.alimentBiblioRefDao().getBiblioRefUuids("food-1"))

        // Second import (mise à jour) : uniquement refB -> refA doit disparaître, sans doublon
        repository.importFoodsDomain(listOf(AlimentEv(uuid = "food-1", nom = "Test", biblioRefs = listOf(refB))))
        assertEquals(listOf("b"), db.alimentBiblioRefDao().getBiblioRefUuids("food-1"))
    }
}
