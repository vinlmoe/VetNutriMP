package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class AnimalDetailViewModelTest : BaseTest() {
    private lateinit var viewModel: AnimalDetailViewModel
    private lateinit var mockRepository: MockConsultationRepository

    @BeforeTest
    fun setup() {
        mockRepository = MockConsultationRepository()
        viewModel = AnimalDetailViewModel(mockRepository)
    }

    @Test
    fun `setAnimal should load consultations`() = runTest {
        // Given
        val animal = AnimalEv.createTestAnimal()
        val consultation = ConsultationEv(idAnim = animal.uuid, date = LocalDate(2024, 1, 1))
        mockRepository.consultations[animal.uuid] = listOf(consultation)

        // When
        viewModel.setAnimal(animal)

        // Then
        assertEquals(consultation, viewModel.animal.value?.consultations?.firstOrNull())
    }

    @Test
    fun `selectConsultation should load full consultation details`() = runTest {
        // Given
        val consultation = ConsultationEv(date = LocalDate(2024, 1, 1))
        mockRepository.consultationDetails[consultation.uuid] = consultation

        // When
        viewModel.selectConsultation(consultation)

        // Then
        assertEquals(consultation, viewModel.selectedConsultation.value)
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
