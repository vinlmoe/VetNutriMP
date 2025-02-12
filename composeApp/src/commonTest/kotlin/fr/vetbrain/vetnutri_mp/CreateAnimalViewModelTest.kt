package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Repository.TestAnimalRepository
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
class CreateAnimalViewModelTest {
    private lateinit var viewModel: CreateAnimalViewModel
    private lateinit var repository: TestAnimalRepository

    @BeforeTest
    fun setup() {
        repository = TestAnimalRepository()
        viewModel = CreateAnimalViewModel(repository)
    }

    @Test
    fun testSaveAnimal() = runTest {
        // Arrange
        viewModel.name = "Rex"
        viewModel.dead = false
        viewModel.id = "123"
        viewModel.selectedSex = Sex.MALE
        viewModel.selectedEspece = Espece.CHIEN
        viewModel.ownerName = "John"
        viewModel.birthdate = LocalDate(2020, 1, 1)
        viewModel.race = "Labrador"
        viewModel.summary = "Good boy"

        // Act
        viewModel.saveAnimal()

        // Assert
        val savedAnimal = repository.lastSavedAnimal
        assertNotNull(savedAnimal)
        assertEquals("Rex", savedAnimal.name)
        assertEquals(false, savedAnimal.dead)
        assertEquals("123", savedAnimal.id)
        assertEquals(Sex.MALE.id, savedAnimal.sexId)
        assertEquals("dog", savedAnimal.specieId)
        assertEquals("John", savedAnimal.ownerName)
        assertEquals(LocalDate(2020, 1, 1), savedAnimal.birthdate)
        assertEquals("Labrador", savedAnimal.race)
        assertEquals("Good boy", savedAnimal.summary)
    }

    @Test
    fun testIsValid() {
        // Arrange & Act - Invalid state (missing required fields)
        assertEquals(false, viewModel.isValid())

        // Arrange & Act - Valid state
        viewModel.name = "Rex"
        viewModel.selectedSex = Sex.MALE
        viewModel.selectedEspece = Espece.CHIEN

        // Assert
        assertEquals(true, viewModel.isValid())
    }
}
