package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.Animal
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
class AnimalListViewModelTest {
    private lateinit var viewModel: AnimalListViewModel
    private lateinit var repository: TestAnimalRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = TestAnimalRepository()
        viewModel = AnimalListViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        repository.clear()
    }

    @Test
    fun `test chargement initial des animaux`() = runTest {
        val animauxTest =
                listOf(
                        Animal(
                                nom = "Rex",
                                espece = Animal.Espece.CHIEN,
                                dateNaissance = LocalDate(2020, 1, 1)
                        ),
                        Animal(
                                nom = "Félix",
                                espece = Animal.Espece.CHAT,
                                dateNaissance = LocalDate(2021, 6, 15)
                        )
                )

        animauxTest.forEach { repository.saveAnimal(it) }
        viewModel.loadAnimals()

        val animauxChargés = viewModel.animals.first()
        assertEquals(2, animauxChargés.size)
        assertEquals("Rex", animauxChargés[0].nom)
        assertEquals("Félix", animauxChargés[1].nom)
    }
}
