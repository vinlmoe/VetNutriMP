package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Repository.TestAnimalRepository
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
class AnimalListViewModelTest {
        private lateinit var viewModel: AnimalListViewModel
        private lateinit var repository: TestAnimalRepository

        @BeforeTest
        fun setup() {
                repository = TestAnimalRepository()
                viewModel = AnimalListViewModel(repository)
        }

        @Test
        fun testLoadAnimals() = runTest {
                // Arrange
                val testDispatcher = StandardTestDispatcher(testScheduler)
                val animal1 =
                        AnimalEv(
                                name = "Rex",
                                dead = false,
                                id = "123",
                                sexId = Sex.MALE.id,
                                specieId = Espece.CHIEN.name,
                                ownerName = "John",
                                birthdate = LocalDate(2020, 1, 1),
                                race = "Labrador",
                                summary = "Good boy"
                        )
                val animal2 =
                        AnimalEv(
                                name = "Felix",
                                dead = false,
                                id = "456",
                                sexId = Sex.MALE.id,
                                specieId = Espece.CHAT.name,
                                ownerName = "Jane",
                                birthdate = LocalDate(2019, 6, 15),
                                race = "Siamese",
                                summary = "Nice cat"
                        )

                // Act
                repository.saveAnimal(animal1)
                repository.saveAnimal(animal2)
                viewModel.loadAnimals()

                // Assert
                assertEquals(2, viewModel.animals.size)
                assertEquals("Rex", viewModel.animals[0].name)
                assertEquals("Felix", viewModel.animals[1].name)
        }

        @Test
        fun testFilterAnimals() = runTest {
                // Arrange
                val animal1 =
                        AnimalEv(
                                name = "Rex",
                                dead = false,
                                id = "123",
                                sexId = Sex.MALE.id,
                                specieId = Espece.CHIEN.name,
                                ownerName = "John",
                                birthdate = LocalDate(2020, 1, 1),
                                race = "Labrador",
                                summary = "Good boy"
                        )
                val animal2 =
                        AnimalEv(
                                name = "Felix",
                                dead = false,
                                id = "456",
                                sexId = Sex.MALE.id,
                                specieId = Espece.CHAT.name,
                                ownerName = "Jane",
                                birthdate = LocalDate(2019, 6, 15),
                                race = "Siamese",
                                summary = "Nice cat"
                        )

                repository.saveAnimal(animal1)
                repository.saveAnimal(animal2)
                viewModel.loadAnimals()

                // Act
                viewModel.searchQuery = "Rex"

                // Assert
                assertEquals(1, viewModel.filteredAnimals.size)
                assertEquals("Rex", viewModel.filteredAnimals[0].name)
        }
}
