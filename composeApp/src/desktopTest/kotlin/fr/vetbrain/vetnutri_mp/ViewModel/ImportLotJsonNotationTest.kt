package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.Data.AnimalApi
import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Data.FichierJson
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Repository.*
import fr.vetbrain.vetnutri_mp.Service.FileService
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json

/**
 * Import d'un lot de copies JSON à la création d'une notation, de bout en bout : préparation,
 * import dans une vraie base Room (en mémoire), rattachement à l'examen et session d'import.
 */
class ImportLotJsonNotationTest {

    private val homeAvant = System.getProperty("user.home")
    private val home = Files.createTempDirectory("vetnutri-lot-json").toFile()
    private val db = Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()).build()

    @AfterTest
    fun fermer() {
        db.close()
        System.setProperty("user.home", homeAvant)
        home.deleteRecursively()
    }

    private fun copie(uuid: String, examen: String? = null, exercice: String? = null, etudiant: String? = null): String =
            Json { encodeDefaults = true }.encodeToString(ApiEnvelope.serializer(), ApiEnvelope(
                    version = "3", generatedAtEpochMs = 0,
                    animals = listOf(AnimalApi(uuid = uuid, name = "Copie $uuid", isDead = false, sexId = 0, specieId = "CHIEN",
                            ownerName = "", breed = "", summary = "", exam = examen != null,
                            examStudentNumber = examen, examExerciseId = exercice, examStudentId = etudiant))
            ))

    @Test
    fun lotJson_importeRattacheEtJournalise() = runBlocking {
        // Le cache des sessions d'import est écrit sous le dossier personnel : dossier jetable
        System.setProperty("user.home", home.absolutePath)
        val animaux = DatabaseAnimalRepository(db.animalDao(), db.foodDao(), db.nutrientValueDao())
        val aliments = DatabaseFoodRepository(db.foodDao(), db.nutrientValueDao(), db.customNutrientDao(), db.alimentBiblioRefDao(),
                db.biblioRefDao(), db.energyPerSpeciesDao())
        val exportImport = ExportImportRepository(
                animalRepository = animaux, foodRepository = aliments,
                equationRepository = DatabaseEquationRepository(db.equationDao(), db.biblioRefDao()),
                referenceRepository = DatabaseReferenceEvRepository(db.referenceEvDao(), db.equationDao(), db.biblioRefDao()),
                biblioRepository = DatabaseBiblioRefRepository(db.biblioRefDao()),
                consultationRepository = DatabaseConsultationRepository(db.consultationDao(), aliments),
                recipeRepository = RecipeRepository(db.recipeDao(), db.foodDao()),
                conseilRepository = ConseilRepository(db.htmlSectionDao())
        )
        val vm = ExamGradingViewModel(ExamGradingRepository(db.examGradingDao()), exportImport, FileService())

        val fini = CompletableDeferred<Unit>()
        vm.importerLotJson(
                listOf(
                        FichierJson("22042.json", copie("c1")),                                  // sans examen : rattachée
                        FichierJson("deja.json", "{\"record\": ${copie("c2", "NUT", "EX", "22001")}}"), // jsonbin, déjà rattachée
                        FichierJson("autre.json", copie("c3", "AUTRE", "EX9", "7")),             // autre examen : laissée
                        FichierJson("faux.json", "pas du json")                                  // erreur
                ),
                examId = "NUT", exerciseId = "EX", rattacher = true
        ) { fini.complete(Unit) }
        withTimeout(30_000) { fini.await() }

        val c1 = assertNotNull(animaux.getAnimalById("c1"))
        assertEquals("NUT", c1.examStudentNumber)
        assertEquals("EX", c1.examExerciseId)
        assertEquals("22042", c1.examStudentId)
        assertEquals("22001", animaux.getAnimalById("c2")?.examStudentId)
        assertEquals("AUTRE", animaux.getAnimalById("c3")?.examStudentNumber)

        val session = assertNotNull(vm.lastImportSession.value)
        assertEquals(3, session.successCount)
        assertEquals(1, session.errorCount)
        assertEquals("faux.json", session.entries.single { !it.success }.binId)
        // La session est aussi conservée dans l'historique de l'examen
        assertEquals(1, vm.importSessionHistory.value.size)
    }
}
