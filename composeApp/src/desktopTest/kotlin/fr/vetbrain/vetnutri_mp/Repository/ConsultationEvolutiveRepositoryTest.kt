package fr.vetbrain.vetnutri_mp.Repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.DataBase.AnimalEntity
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Enumer.ProfilEvolutif
import fr.vetbrain.vetnutri_mp.Enumer.TypeConsultation
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Persistance de la consultation évolutive (schéma v37) : type/profil de consultation, étapes
 * avec poids propre et variables d'énergie propres (table RATION_SUPPLEMENTAL_VARIABLES).
 */
class ConsultationEvolutiveRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DatabaseConsultationRepository

    @BeforeTest
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        val foodRepository = DatabaseFoodRepository(
            foodDao = db.foodDao(),
            nutrientValueDao = db.nutrientValueDao()
        )
        repository = DatabaseConsultationRepository(db.consultationDao(), foodRepository)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private suspend fun insertAnimal(uuid: String) {
        db.animalDao().insert(
            AnimalEntity(
                uuid = uuid, nom = "Chiot", id = null, specieId = "CHIEN", ownerName = null,
                birthdate = null, race = null, summary = null
            )
        )
    }

    private fun consultationEvolutive(): ConsultationEv = ConsultationEv(
        uuid = "c1",
        idAnim = "a1",
        weight = 5.2,
        typeConsultation = TypeConsultation.EVOLUTIVE,
        profilEvolutif = ProfilEvolutif.CROISSANCE,
        suppVarp = mutableListOf(SupplementalvariableP(VariableKind.AdultWeight, 25.0)),
        rations = mutableListOf(
            Ration(uuid = "r-reel", idConsult = "c1", name = "Réel", etapeEvolutive = true),
            Ration(
                uuid = "r-8", idConsult = "c1", name = "8 kg", etapeEvolutive = true, poids = 8.0,
                suppVarp = mutableListOf(
                    SupplementalvariableP(VariableKind.AdultWeight, 30.0),
                    SupplementalvariableP(VariableKind.Distance, 12.5)
                )
            ),
            Ration(uuid = "r-std", idConsult = "c1", name = "Actuelle", actual = true)
        )
    )

    @Test
    fun saveAndReload_preservesEvolutiveFields() = runTest {
        insertAnimal("a1")
        repository.saveConsultation(consultationEvolutive())

        val reloaded = repository.getConsultationById("c1")!!

        assertEquals(TypeConsultation.EVOLUTIVE, reloaded.typeConsultation)
        assertEquals(ProfilEvolutif.CROISSANCE, reloaded.profilEvolutif)
        assertEquals(setOf("r-reel", "r-8"), reloaded.etapesEvolutives.map { it.uuid }.toSet())

        val reel = reloaded.rations.first { it.uuid == "r-reel" }
        assertNull(reel.poids)
        assertTrue(reel.suppVarp.isEmpty())

        val etape8 = reloaded.rations.first { it.uuid == "r-8" }
        assertEquals(8.0, etape8.poids)
        assertEquals(
            setOf(
                SupplementalvariableP(VariableKind.AdultWeight, 30.0),
                SupplementalvariableP(VariableKind.Distance, 12.5)
            ),
            etape8.suppVarp.toSet()
        )

        // Les variables de la consultation restent distinctes de celles des étapes
        assertEquals(
            listOf(SupplementalvariableP(VariableKind.AdultWeight, 25.0)),
            reloaded.suppVarp
        )
        assertFalse(reloaded.rations.first { it.uuid == "r-std" }.etapeEvolutive)
    }

    @Test
    fun resave_replacesStepVariablesWithoutLeftovers() = runTest {
        insertAnimal("a1")
        val consultation = consultationEvolutive()
        repository.saveConsultation(consultation)

        val etape8 = consultation.rations.first { it.uuid == "r-8" }
        etape8.suppVarp = mutableListOf(SupplementalvariableP(VariableKind.Distance, 20.0))
        // Suppression d'une étape : ses variables doivent disparaître en cascade
        consultation.rations.removeAll { it.uuid == "r-reel" }
        repository.saveConsultation(consultation)

        val reloaded = repository.getConsultationById("c1")!!
        assertEquals(
            listOf(SupplementalvariableP(VariableKind.Distance, 20.0)),
            reloaded.rations.first { it.uuid == "r-8" }.suppVarp
        )
        assertTrue(db.consultationDao().getSupplementalVariablesForRation("r-reel").isEmpty())
    }

    @Test
    fun standardConsultation_defaultsAreKept() = runTest {
        insertAnimal("a1")
        repository.saveConsultation(
            ConsultationEv(
                uuid = "c2", idAnim = "a1", weight = 12.0,
                rations = mutableListOf(Ration(uuid = "r1", idConsult = "c2"))
            )
        )

        val reloaded = repository.getConsultationsForAnimal("a1").single()
        assertEquals(TypeConsultation.STANDARD, reloaded.typeConsultation)
        assertNull(reloaded.profilEvolutif)
        val ration = reloaded.rations.single()
        assertFalse(ration.etapeEvolutive)
        assertNull(ration.poids)
    }
}
