package fr.vetbrain.vetnutri_mp.Repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.DataBase.FoodEntity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

/**
 * Vérifie la protection contre l'écrasement lors d'un (ré)import JSON/Excel via
 * `importFoods` (utilisée notamment par le réimport de `food.json` au démarrage) :
 * une fiche modifiée localement plus récemment que la donnée importée ne doit jamais
 * être écrasée quand `importOnlyIfNewer = true`.
 */
class DatabaseFoodRepositoryImportOnlyIfNewerTest {

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

    private suspend fun seedExistingFood(
        uuid: String,
        nom: String,
        lastUpdateDate: String?,
        deprecated: Int = 0
    ) {
        db.foodDao().insertFood(
            FoodEntity(
                uuid = uuid,
                groupAlim = 0,
                typeAlim = 0,
                ingredients = "",
                price = 0.0,
                categPrice = "",
                brand = "",
                gamme = "",
                cont = "NO",
                unitPres = 0,
                quantityPres = 0.0,
                version = 1,
                date = "",
                nameDef = nom,
                consistent = 0,
                deprecated = deprecated,
                DataB = "6",
                name = nom,
                lastUpdateDate = lastUpdateDate
            )
        )
    }

    private fun incomingFood(uuid: String, nom: String, dateMaj: String) = AlimentEvJson(
        UUID = uuid,
        nom = nom,
        group = "AutreCereal",
        foodKind = "COMPLET",
        espece = 0,
        dateMaj = dateMaj
    )

    @Test
    fun importFoods_olderIncomingData_withImportOnlyIfNewer_doesNotOverwriteLocalEdit() = runTest {
        seedExistingFood("food-1", "Nom modifié localement", "2024-06-01")

        val result = repository.importFoods(
            listOf(incomingFood("food-1", "Nom obsolète de l'import", "2024-01-01")),
            mergeNutrients = false,
            importOnlyIfNewer = true
        )

        assertEquals(0, result.updatedCount, "l'import plus ancien ne doit déclencher aucune mise à jour")
        assertEquals("Nom modifié localement", db.foodDao().getFoodById("food-1")?.nameDef)
    }

    @Test
    fun importFoods_newerIncomingData_withImportOnlyIfNewer_appliesUpdate() = runTest {
        seedExistingFood("food-1", "Ancien nom", "2024-01-01")

        val result = repository.importFoods(
            listOf(incomingFood("food-1", "Nom mis à jour", "2024-06-01")),
            mergeNutrients = false,
            importOnlyIfNewer = true
        )

        assertEquals(1, result.updatedCount)
        assertEquals("Nom mis à jour", db.foodDao().getFoodById("food-1")?.nameDef)
    }

    @Test
    fun importFoods_incomingDataWithoutDate_withImportOnlyIfNewer_doesNotOverwrite() = runTest {
        seedExistingFood("food-1", "Ancien nom", "2024-06-01")

        val result = repository.importFoods(
            listOf(incomingFood("food-1", "Nom sans date", dateMaj = "")),
            mergeNutrients = false,
            importOnlyIfNewer = true
        )

        assertEquals(0, result.updatedCount)
        assertEquals("Ancien nom", db.foodDao().getFoodById("food-1")?.nameDef)
    }

    @Test
    fun importFoods_withImportOnlyIfNewerDisabled_alwaysOverwritesRegardlessOfDate() = runTest {
        seedExistingFood("food-1", "Nom modifié localement", "2024-06-01")

        val result = repository.importFoods(
            listOf(incomingFood("food-1", "Nom obsolète de l'import", "2024-01-01")),
            mergeNutrients = false,
            importOnlyIfNewer = false
        )

        assertEquals(1, result.updatedCount)
        assertEquals("Nom obsolète de l'import", db.foodDao().getFoodById("food-1")?.nameDef)
    }

    @Test
    fun importFoodsDomain_existingFood_updatesBrandAndDatabase() = runTest {
        seedExistingFood("food-1", "Aliment existant", "2024-01-01")

        val result =
            repository.importFoodsDomain(
                listOf(
                    AlimentEv(
                        uuid = "food-1",
                        nom = "Aliment existant",
                        brand = "Nouvelle marque",
                        dataB = "VF2026",
                        lastUpdateDate = "2026-01-01"
                    )
                ),
                importOnlyIfNewer = false
            )

        val updated = db.foodDao().getFoodById("food-1")
        assertEquals(1, result.updatedCount)
        assertEquals("Nouvelle marque", updated?.brand)
        assertEquals("VF2026", updated?.DataB)
    }

    @Test
    fun importFoodsDomain_existingDeprecatedFood_incomingActiveDoesNotReactivateIt() = runTest {
        seedExistingFood("food-1", "Aliment obsolète", "2024-01-01", deprecated = 1)

        val result =
            repository.importFoodsDomain(
                listOf(
                    AlimentEv(
                        uuid = "food-1",
                        nom = "Aliment obsolète",
                        brand = "Marque importée",
                        deprecated = false,
                        lastUpdateDate = "2026-01-01"
                    )
                ),
                importOnlyIfNewer = true
            )

        val updated = db.foodDao().getFoodById("food-1")
        assertEquals(1, result.updatedCount)
        assertEquals("Marque importée", updated?.brand)
        assertEquals(1, updated?.deprecated)
    }
}
