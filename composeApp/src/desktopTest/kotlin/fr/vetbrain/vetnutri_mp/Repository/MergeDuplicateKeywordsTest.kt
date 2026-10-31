package fr.vetbrain.vetnutri_mp.Repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.DataBase.AnimalEntity
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

/**
 * Fusion des mots-clés livrés en double (« hyperthyroidie » / « Hyperthyroïdie »,
 * « Insufisance hépatique »…) dans une base existante.
 */
class MergeDuplicateKeywordsTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DatabaseConsultationRepository

    private val hyperthyroidie = ConsultationKeyword("5353ca07-557b-4b46-afef-8e32d0acdac4", "Hyperthyroïdie")
    private val hyperthyroidieMinuscule = ConsultationKeyword("6af3a494-32c9-42cc-9e14-b49065e84708", "hyperthyroidie")
    private val insufisance = ConsultationKeyword("0e83dbdd-007b-4c1c-a702-141e2f130cf6", "Insufisance hépatique")
    private val celiaque = ConsultationKeyword("053e3e77-361d-48ae-9aaa-49f9e2e283d1", "Maladie celiaque")

    @BeforeTest
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()).build()
        val foodRepository = DatabaseFoodRepository(foodDao = db.foodDao(), nutrientValueDao = db.nutrientValueDao())
        repository = DatabaseConsultationRepository(db.consultationDao(), foodRepository)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private suspend fun consultation(uuid: String, motsCles: List<String>) {
        db.animalDao().insert(
            AnimalEntity(
                uuid = "a-$uuid", nom = "Rex", id = null, specieId = "CHIEN", ownerName = null,
                birthdate = null, race = null, summary = null
            )
        )
        repository.saveConsultation(ConsultationEv(uuid = uuid, idAnim = "a-$uuid", keywordIds = motsCles.toMutableList()))
    }

    @Test
    fun doublonRemplaceParLeMotCleConserveSansRepetition() = runTest {
        listOf(hyperthyroidie, hyperthyroidieMinuscule, celiaque).forEach { repository.saveKeyword(it) }
        consultation("c1", listOf(hyperthyroidieMinuscule.uuid))
        consultation("c2", listOf(hyperthyroidie.uuid, hyperthyroidieMinuscule.uuid, celiaque.uuid))

        assertEquals(2, repository.mergeDuplicateKeywords())

        assertEquals(listOf(hyperthyroidie.uuid), repository.getConsultationById("c1")!!.keywordIds)
        assertEquals(listOf(hyperthyroidie.uuid, celiaque.uuid), repository.getConsultationById("c2")!!.keywordIds)
        assertEquals(
            listOf("Hyperthyroïdie", "Maladie cœliaque"),
            repository.getAllKeywords().map { it.label }
        )
    }

    @Test
    fun motCleConserveCreeQuandSeulLeDoublonExiste() = runTest {
        repository.saveKeyword(insufisance)
        consultation("c1", listOf(insufisance.uuid))

        repository.mergeDuplicateKeywords()

        val conserve = "85b8a2d1-4c3b-419b-abd9-1c472f3bf324"
        assertEquals(listOf(conserve), repository.getConsultationById("c1")!!.keywordIds)
        assertEquals(listOf(ConsultationKeyword(conserve, "Insuffisance hépatique")), repository.getAllKeywords())
    }

    @Test
    fun baseDejaPropreInchangee() = runTest {
        repository.saveKeyword(hyperthyroidie)
        consultation("c1", listOf(hyperthyroidie.uuid))

        assertEquals(0, repository.mergeDuplicateKeywords())
        assertEquals(0, repository.mergeDuplicateKeywords())
        assertEquals(listOf(hyperthyroidie.uuid), repository.getConsultationById("c1")!!.keywordIds)
    }
}
