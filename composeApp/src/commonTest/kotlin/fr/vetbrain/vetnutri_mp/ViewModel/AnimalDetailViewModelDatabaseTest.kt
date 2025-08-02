package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.DataBase.TestAnimalDao
import fr.vetbrain.vetnutri_mp.DataBase.TestConsultationDao
import fr.vetbrain.vetnutri_mp.DataBase.TestFoodDao
import fr.vetbrain.vetnutri_mp.DataBase.TestFoodRepository
import fr.vetbrain.vetnutri_mp.DataBase.TestNutrientValueDao
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseConsultationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.TestDispatchers
import kotlin.test.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AnimalDetailViewModelDatabaseTest : BaseTest() {
        private lateinit var viewModel: AnimalDetailViewModel
        private lateinit var animalRepository: DatabaseAnimalRepository
        private lateinit var consultationRepository: DatabaseConsultationRepository
        private val testDispatcher = StandardTestDispatcher()
        private lateinit var testDispatchers: TestDispatchers
        private lateinit var testAnimalDao: TestAnimalDao
        private lateinit var testConsultationDao: TestConsultationDao
        private lateinit var testFoodDao: TestFoodDao
        private lateinit var testNutrientValueDao: TestNutrientValueDao
        private lateinit var testFoodRepository: TestFoodRepository

        @BeforeTest
        override fun setUp() {
                super.setUp()
                testDispatchers = TestDispatchers(testDispatcher)
                AppDispatchers.setDispatchers(
                        io = testDispatchers.io,
                        default = testDispatchers.default,
                        main = testDispatchers.main
                )

                // Initialisation des DAOs de test
                testAnimalDao = TestAnimalDao()
                testConsultationDao = TestConsultationDao()
                testFoodDao = TestFoodDao()
                testNutrientValueDao = TestNutrientValueDao()
                testFoodRepository = TestFoodRepository(testFoodDao, testNutrientValueDao)

                // Initialisation des repositories
                animalRepository = DatabaseAnimalRepository(testAnimalDao, testFoodDao)
                consultationRepository =
                        DatabaseConsultationRepository(testConsultationDao, testFoodRepository)

                // Initialisation du ViewModel
                viewModel =
                        AnimalDetailViewModel(
                                animalRepository = animalRepository,
                                consultationRepository = consultationRepository
                        )
        }

        @Test
        fun `test sauvegarde et récupération d'un animal avec consultation`() =
                runTest(testDispatcher) {
                        // Given
                        val animal = AnimalEv.createTestAnimal()

                        val consultation =
                                ConsultationEv(
                                        idAnim = animal.uuid,
                                        date = LocalDate(2024, 1, 1),
                                        objectConsult = "Test consultation"
                                )

                        animal.consultations.add(consultation)

                        // When - Sauvegarde de l'animal avec sa consultation
                        animalRepository.saveAnimal(animal)

                        // Récupérer les consultations directement du DAO pour vérifier
                        val consultationsFromDao =
                                testAnimalDao.getConsultationsForAnimal(animal.uuid)
                        consultationsFromDao.forEach {
                        }

                        // Au lieu d'utiliser setAnimal directement, récupérons l'animal du
                        // repository
                        val savedAnimal = animalRepository.getAnimalById(animal.uuid)
                        if (savedAnimal != null) {
                                savedAnimal.consultations.forEach {
                                }
                        }

                        assertNotNull(savedAnimal, "L'animal devrait être récupéré du repository")

                        // Configurer le ViewModel avec l'animal récupéré
                        viewModel.setAnimal(savedAnimal!!)
                        testDispatcher.scheduler.advanceUntilIdle()

                        // Then
                        val animalValue = viewModel.animal.value
                        assertNotNull(animalValue, "L'animal ne devrait pas être null")
                        animalValue?.consultations?.forEach {
                        }

                        assertEquals(
                                animal.uuid,
                                animalValue.uuid,
                                "Les UUIDs des animaux devraient correspondre"
                        )
                        assertEquals(
                                1,
                                animalValue.consultations.size,
                                "L'animal devrait avoir une consultation"
                        )

                        val savedConsultation = animalValue.consultations.first()
                        assertEquals(
                                consultation.uuid,
                                savedConsultation.uuid,
                                "Les UUIDs des consultations devraient correspondre"
                        )
                        assertEquals(
                                consultation.date,
                                savedConsultation.date,
                                "Les dates des consultations devraient correspondre"
                        )
                        assertEquals(
                                consultation.objectConsult,
                                savedConsultation.objectConsult,
                                "Les objets des consultations devraient correspondre"
                        )
                }

        @Test
        fun `test mise à jour d'une consultation`() =
                runTest(testDispatcher) {
                        // Given
                        val animal = AnimalEv.createTestAnimal()
                        val consultation =
                                ConsultationEv(
                                        idAnim = animal.uuid,
                                        date = LocalDate(2024, 1, 1),
                                        objectConsult = "Test consultation"
                                )
                        animal.consultations.add(consultation)

                        // Sauvegarde de l'animal avec sa consultation
                        animalRepository.saveAnimal(animal)

                        // Récupérer l'animal du repository
                        val savedAnimal = animalRepository.getAnimalById(animal.uuid)
                        assertNotNull(savedAnimal, "L'animal devrait être récupéré du repository")

                        // Configurer le ViewModel avec l'animal récupéré
                        viewModel.setAnimal(savedAnimal!!)
                        testDispatcher.scheduler.advanceUntilIdle()

                        // When - Mettre à jour la consultation
                        val updatedConsultation =
                                consultation.copy(objectConsult = "Consultation mise à jour")
                        viewModel.updateConsultation(updatedConsultation)
                        testDispatcher.scheduler.advanceUntilIdle()

                        // Then
                        val animalValue = viewModel.animal.value
                        assertNotNull(animalValue, "L'animal ne devrait pas être null")
                        val savedConsultation = animalValue.consultations.first()
                        assertEquals(
                                "Consultation mise à jour",
                                savedConsultation.objectConsult,
                                "L'objet de la consultation devrait être mis à jour"
                        )
                }

        @Test
        fun `test suppression d'un animal`() =
                runTest(testDispatcher) {
                        // Given
                        val animal = AnimalEv.createTestAnimal()
                        animalRepository.saveAnimal(animal)
                        viewModel.setAnimal(animal)
                        testDispatcher.scheduler.advanceUntilIdle()

                        // When
                        val result = viewModel.deleteAnimal()
                        testDispatcher.scheduler.advanceUntilIdle()

                        // Then
                        assertTrue(result, "La suppression devrait réussir")
                        assertNull(
                                viewModel.animal.value,
                                "L'animal devrait être null après la suppression"
                        )
                        assertNull(
                                animalRepository.getAnimalById(animal.uuid),
                                "L'animal ne devrait plus exister dans la base de données"
                        )
                }

        @AfterTest
        override fun tearDown() {
                super.tearDown()
                AppDispatchers.resetDispatchers()
        }
}
