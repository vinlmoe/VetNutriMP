package fr.vetbrain.vetnutri_mp.Repository

import androidx.room.Room
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.TypeAnimalTroupeau
import fr.vetbrain.vetnutri_mp.Data.WeightDate
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

/**
 * [DatabaseAnimalRepository] sur une vraie base Room (BundledSQLite) ouverte par `getRoomDatabase`
 * dans un répertoire temporaire : CRUD d'un animal, chargement des poids de tous les animaux en
 * une requête par lot (`getAllAnimals`), races distinctes d'une espèce (`getRacesBySpecies`),
 * identifiants (`getAllAnimalIds`), troupeau (`ANIMALS.herdTypesJson`) et suppression en cascade
 * des consultations.
 */
class DatabaseAnimalRepositoryTest {

    private lateinit var tempDir: File
    private lateinit var db: AppDatabase
    private lateinit var repository: DatabaseAnimalRepository

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("vetnutri-animal-repo-test").toFile()
        val dbPath = File(tempDir, "vetnutri.db").absolutePath
        db = getRoomDatabase(Room.databaseBuilder<AppDatabase>(name = dbPath), dbPath)
        repository = DatabaseAnimalRepository(db.animalDao(), db.foodDao(), db.nutrientValueDao())
    }

    @AfterTest
    fun tearDown() {
        db.close()
        tempDir.deleteRecursively()
    }

    private fun poids(animal: String, date: LocalDate, valeur: Double) =
            WeightDate(uuid = "w-$animal-$date", refAnimal = animal, date = date, value = valeur)

    private fun animal(uuid: String, nom: String, race: String = "", espece: Espece = Espece.CHIEN) =
            AnimalEv(uuid = uuid, nom = nom, race = race, specieId = espece.label)

    @Test
    fun saveGetUpdateDelete_animal() = runTest {
        val rex =
                animal("a1", "Rex", race = "Labrador").apply {
                    ownerName = "Jean Dupont"
                    birthdate = LocalDate(2020, 3, 15)
                    summary = "Chien de famille"
                    weightHistory = mutableListOf(poids("a1", LocalDate(2024, 1, 1), 30.0))
                }
        repository.saveAnimal(rex)

        val relu = repository.getAnimalById("a1")
        assertNotNull(relu)
        assertEquals("Rex", relu.nom)
        assertEquals("Jean Dupont", relu.ownerName)
        assertEquals("Labrador", relu.race)
        assertEquals(LocalDate(2020, 3, 15), relu.birthdate)
        assertEquals("Chien de famille", relu.summary)
        assertEquals(Espece.CHIEN, relu.getEspece())
        assertFalse(relu.estTroupeau)
        assertEquals(listOf(30.0), relu.weightHistory.map { it.value })
        assertEquals(1, repository.getAnimalsCount())

        // Mise à jour : nom et historique de poids remplacés (DELETE + INSERT transactionnels)
        relu.nom = "Rex II"
        relu.weightHistory = mutableListOf(
                poids("a1", LocalDate(2024, 1, 1), 30.0),
                poids("a1", LocalDate(2024, 6, 1), 31.5)
        )
        repository.updateAnimal(relu)
        val misAJour = repository.getAnimalById("a1")!!
        assertEquals("Rex II", misAJour.nom)
        assertEquals(listOf(30.0, 31.5), misAJour.weightHistory.sortedBy { it.date }.map { it.value })
        assertEquals(2, db.animalDao().getWeightsForAnimal("a1").size, "aucun poids orphelin ni doublon")

        // Suppression : l'animal et ses poids disparaissent
        repository.deleteAnimal(misAJour)
        assertNull(repository.getAnimalById("a1"))
        assertEquals(0, repository.getAnimalsCount())
        assertTrue(db.animalDao().getWeightsForAnimal("a1").isEmpty(), "poids supprimés en cascade")
    }

    @Test
    fun getAllAnimals_chargeLesPoidsDeChaqueAnimalParLot() = runTest {
        val dates = listOf(LocalDate(2024, 1, 10), LocalDate(2024, 7, 10))
        val attendus = mutableMapOf<String, List<Pair<LocalDate, Double>>>()
        for (i in 1..3) {
            val uuid = "a$i"
            val valeurs = dates.mapIndexed { j, d -> d to (10.0 * i + j) }
            attendus[uuid] = valeurs
            repository.saveAnimal(
                    animal(uuid, "Animal $i").apply {
                        weightHistory = valeurs.map { (d, v) -> poids(uuid, d, v) }.toMutableList()
                    }
            )
        }

        val tous = repository.getAllAnimals()
        assertEquals(setOf("a1", "a2", "a3"), tous.map { it.uuid }.toSet())
        for (a in tous) {
            assertEquals(2, a.weightHistory.size, "chaque animal a exactement ses deux poids")
            assertTrue(a.weightHistory.all { it.refAnimal == a.uuid }, "jamais les poids d'un autre animal")
            assertEquals(
                    attendus.getValue(a.uuid),
                    a.weightHistory.sortedBy { it.date }.map { it.date to it.value },
                    "poids de ${a.uuid} dans l'ordre chronologique"
            )
            assertTrue(a.consultations.isEmpty(), "getAllAnimals ne charge pas les consultations")
        }
    }

    @Test
    fun getAllAnimalIds_etCount_sansChargerLesAnimaux() = runTest {
        assertTrue(repository.getAllAnimalIds().isEmpty())
        repository.saveAnimal(animal("a1", "Rex"))
        repository.saveAnimal(animal("a2", "Milo"))
        repository.saveAnimal(animal("a3", "Félix", espece = Espece.CHAT))
        assertEquals(setOf("a1", "a2", "a3"), repository.getAllAnimalIds().toSet())
        assertEquals(3, repository.getAnimalsCount())
    }

    @Test
    fun getRacesBySpecies_distinctesTrieesSansBlancs() = runTest {
        repository.saveAnimal(animal("a1", "Rex", race = "Labrador"))
        repository.saveAnimal(animal("a2", "Milo", race = "Beagle"))
        repository.saveAnimal(animal("a3", "Nala", race = "Labrador"))
        repository.saveAnimal(animal("a4", "Sans race", race = ""))
        repository.saveAnimal(animal("a5", "Félix", race = "Siamois", espece = Espece.CHAT))

        assertEquals(listOf("Beagle", "Labrador"), repository.getRacesBySpecies(Espece.CHIEN.label))
        assertEquals(listOf("Siamois"), repository.getRacesBySpecies(Espece.CHAT.label))
        assertTrue(repository.getRacesBySpecies(Espece.RAT.label).isEmpty())
    }

    @Test
    fun troupeau_herdTypesJson_allerRetour() = runTest {
        val types = mutableListOf(
                TypeAnimalTroupeau(id = "t1", nom = "Vaches", nombre = 40, poids = 650.0, espece = Espece.CHIEN.name, race = "Holstein"),
                TypeAnimalTroupeau(id = "t2", nom = "Génisses", nombre = 12, poids = 380.0, espece = Espece.CHAT.name, sexId = 1)
        )
        repository.saveAnimal(animal("troupeau", "Élevage Dupont").apply { typesTroupeau = types })

        val relu = repository.getAnimalById("troupeau")!!
        assertTrue(relu.estTroupeau)
        assertEquals(types, relu.typesTroupeau)
        assertEquals(52, relu.effectifTroupeau)
        assertNotNull(db.animalDao().getAnimalById("troupeau")?.herdTypesJson, "colonne herdTypesJson renseignée")

        // Redevenir un individu : la colonne repasse à NULL
        relu.typesTroupeau = null
        repository.updateAnimal(relu)
        assertFalse(repository.getAnimalById("troupeau")!!.estTroupeau)
        assertNull(db.animalDao().getAnimalById("troupeau")?.herdTypesJson)

        // Un individu n'a jamais de types
        repository.saveAnimal(animal("individu", "Rex"))
        assertNull(repository.getAnimalById("individu")!!.typesTroupeau)
        assertTrue(repository.getAllAnimals().none { it.uuid == "individu" && it.estTroupeau })
    }

    @Test
    fun deleteAnimal_supprimeSesConsultationsEnCascade() = runTest {
        repository.saveAnimal(
                animal("a1", "Rex").apply {
                    consultations = mutableListOf(
                            ConsultationEv(uuid = "c1", idAnim = "a1", weight = 30.0, date = LocalDate(2024, 1, 1)),
                            ConsultationEv(uuid = "c2", idAnim = "a1", weight = 31.0, date = LocalDate(2024, 6, 1))
                    )
                }
        )
        repository.saveAnimal(animal("a2", "Milo").apply {
            consultations = mutableListOf(ConsultationEv(uuid = "c3", idAnim = "a2", weight = 12.0))
        })

        val rex = repository.getAnimalById("a1")!!
        assertEquals(setOf("c1", "c2"), rex.consultations.map { it.uuid }.toSet(), "consultations rechargées avec l'animal")
        assertEquals(2, db.consultationDao().getConsultationsForAnimal("a1").size)

        repository.deleteAnimal(rex)

        assertNull(repository.getAnimalById("a1"))
        assertTrue(db.consultationDao().getConsultationsForAnimal("a1").isEmpty(), "consultations supprimées en cascade")
        assertNull(db.consultationDao().getConsultationById("c1"))
        assertEquals(1, db.consultationDao().getConsultationsForAnimal("a2").size, "l'autre animal est intact")
        assertEquals(listOf("a2"), repository.getAllAnimalIds())
    }
}
