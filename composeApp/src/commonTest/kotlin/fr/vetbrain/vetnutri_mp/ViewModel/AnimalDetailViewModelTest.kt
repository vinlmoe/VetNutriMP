package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.TestDispatchers
import kotlin.test.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AnimalDetailViewModelTest : BaseTest() {
    private lateinit var viewModel: AnimalDetailViewModel
    private lateinit var mockRepository: MockConsultationRepository
    private lateinit var mockAnimalRepository: MockAnimalRepository
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testDispatchers: TestDispatchers

    @BeforeTest
    fun setup() {
        // Configurer les dispatchers de test
        testDispatchers = TestDispatchers(testDispatcher)
        // Injecter les dispatchers de test dans AppDispatchers
        AppDispatchers.setDispatchers(
                io = testDispatchers.io,
                default = testDispatchers.default,
                main = testDispatchers.main
        )

        mockRepository = MockConsultationRepository()
        mockAnimalRepository = MockAnimalRepository()
        viewModel = AnimalDetailViewModel(mockRepository, mockAnimalRepository)
    }

    @Test
    fun `setAnimal should load consultations`() =
            runTest(testDispatcher) {
                // Given
                val animal = AnimalEv.createTestAnimal()
                val consultation =
                        ConsultationEv(idAnim = animal.uuid, date = LocalDate(2024, 1, 1))
                mockRepository.consultations[animal.uuid] = listOf(consultation)

                // Ajouter la consultation directement à l'animal pour le test
                val animalWithConsultation = animal.copy()
                animalWithConsultation.consultations.add(consultation)

                // When
                viewModel.setAnimal(animalWithConsultation)
                testDispatcher.scheduler
                        .advanceUntilIdle() // Avancer le temps pour laisser les coroutines se
                // terminer

                // Then
                val animalValue = viewModel.animal.value
                assertNotNull(animalValue, "L'animal ne devrait pas être null")
                val firstConsultation = animalValue.consultations.firstOrNull()
                assertNotNull(firstConsultation, "La consultation ne devrait pas être null")
                assertEquals(
                        consultation.uuid,
                        firstConsultation.uuid,
                        "Les UUIDs des consultations devraient correspondre"
                )
                assertEquals(
                        consultation.date,
                        firstConsultation.date,
                        "Les dates des consultations devraient correspondre"
                )
            }

    @Test
    fun `selectConsultation should load full consultation details`() =
            runTest(testDispatcher) {
                // Given
                val consultation = ConsultationEv(date = LocalDate(2024, 1, 1))
                mockRepository.consultationDetails[consultation.uuid] = consultation

                // When
                viewModel.selectConsultation(consultation)
                testDispatcher.scheduler
                        .advanceUntilIdle() // Avancer le temps pour laisser les coroutines se
                // terminer

                // Then
                val selectedConsultation = viewModel.selectedConsultation.value
                assertNotNull(
                        selectedConsultation,
                        "La consultation sélectionnée ne devrait pas être null"
                )
                assertEquals(
                        consultation.uuid,
                        selectedConsultation.uuid,
                        "Les UUIDs des consultations devraient correspondre"
                )
                assertEquals(
                        consultation.date,
                        selectedConsultation.date,
                        "Les dates des consultations devraient correspondre"
                )
            }

    @AfterTest
    override fun tearDown() {
        super.tearDown()
        AppDispatchers.resetDispatchers()
    }
}

private class MockConsultationRepository : ConsultationRepository {
    val consultations = mutableMapOf<String, List<ConsultationEv>>()
    val consultationDetails = mutableMapOf<String, ConsultationEv>()

    override suspend fun saveConsultation(consultation: ConsultationEv) {
        consultationDetails[consultation.uuid] = consultation
        val animalConsultations =
                consultations[consultation.idAnim]?.toMutableList() ?: mutableListOf()
        animalConsultations.add(consultation)
        consultations[consultation.idAnim] = animalConsultations
    }

    override suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEv> {
        return consultations[animalId] ?: emptyList()
    }

    override suspend fun getConsultationById(id: String): ConsultationEv? {
        return consultationDetails[id]
    }

    override suspend fun deleteConsultation(consultation: ConsultationEv) {
        consultationDetails.remove(consultation.uuid)
        consultations[consultation.idAnim] =
                consultations[consultation.idAnim]?.filter { it.uuid != consultation.uuid }
                        ?: emptyList()
    }
}

private class MockAnimalRepository : AnimalRepository {
    val animals = mutableMapOf<String, AnimalEv>()

    override suspend fun saveAnimal(animal: AnimalEv) {
        animals[animal.uuid] = animal
    }

    override suspend fun getAnimalById(id: String): AnimalEv? {
        return animals[id]
    }

    override suspend fun getAllAnimals(): List<AnimalEv> {
        return animals.values.toList()
    }

    override suspend fun deleteAnimal(animal: AnimalEv) {
        animals.remove(animal.uuid)
    }

    override suspend fun updateAnimal(animal: AnimalEv) {
        animals[animal.uuid] = animal
    }

    override suspend fun importAnimals(animalsJson: List<AnimalEvJson>): Int {
        return 0 // Pas d'implémentation nécessaire pour les tests
    }

    override fun getFoodRepository(): FoodRepository? {
        return null // Pas d'implémentation nécessaire pour les tests
    }
}
