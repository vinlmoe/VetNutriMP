package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Repository.TestAnimalRepository
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class CreateAnimalViewModelTest {
    private lateinit var viewModel: CreateAnimalViewModel
    private lateinit var repository: TestAnimalRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = TestAnimalRepository()
        viewModel = CreateAnimalViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        repository.clear()
    }

    @Test
    fun `test mise à jour animal`() = runTest {
        val nouvelAnimal =
                Animal(
                        nom = "Rex",
                        espece = Animal.Espece.CHIEN,
                        sexe = Animal.Sexe.MALE,
                        dateNaissance = LocalDate(2020, 1, 1)
                )

        viewModel.updateAnimal(nouvelAnimal)
        val animalMisAJour = viewModel.animal.first()

        assertEquals("Rex", animalMisAJour.nom)
        assertEquals(Animal.Espece.CHIEN, animalMisAJour.espece)
        assertEquals(Animal.Sexe.MALE, animalMisAJour.sexe)
        assertEquals(LocalDate(2020, 1, 1), animalMisAJour.dateNaissance)
    }

    @Test
    fun `test sauvegarde animal`() = runTest {
        val animal =
                Animal(
                        nom = "Rex",
                        espece = Animal.Espece.CHIEN,
                        dateNaissance = LocalDate(2020, 1, 1)
                )
        viewModel.updateAnimal(animal)

        viewModel.saveAnimal()

        val animalSauvegarde = repository.lastSavedAnimal
        assertNotNull(animalSauvegarde)
        assertEquals("Rex", animalSauvegarde.nom)
        assertTrue(viewModel.saveSuccess.first())
        assertFalse(viewModel.isSaving.first())
    }

    @Test
    fun `test reset statut sauvegarde`() = runTest {
        viewModel.saveAnimal()
        assertTrue(viewModel.saveSuccess.first())

        viewModel.resetSaveStatus()
        assertFalse(viewModel.saveSuccess.first())
    }
}
