package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.TestDispatchers
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class ConsultationViewModelTest : BaseTest() {
        private lateinit var viewModel: AnimalDetailViewModel
        private lateinit var animalRepository: AnimalRepository
        private lateinit var consultationRepository: ConsultationRepository
        private lateinit var foodRepository: FoodRepository
        private val testDispatcher = StandardTestDispatcher()
        private lateinit var testDispatchers: TestDispatchers

        @BeforeTest
        override fun setUp() {
                super.setUp()
                testDispatchers = TestDispatchers(testDispatcher)
                AppDispatchers.setDispatchers(
                        io = testDispatchers.io,
                        default = testDispatchers.default,
                        main = testDispatchers.main
                )

                // Initialisation des repositories avec des mocks
                animalRepository = MockAnimalRepository()
                consultationRepository = MockConsultationRepository()
                foodRepository = MockFoodRepository()

                // Initialisation du ViewModel
                viewModel = AnimalDetailViewModel(consultationRepository, animalRepository)
        }

        /**
         * Test le chargement des aliments dans les rations lors de la sélection d'une consultation
         */
        @Test
        fun `test chargement des aliments dans les rations lors de la sélection d'une consultation`() =
                runTest(testDispatcher) {
                        // Given - Création d'aliments de test
                        val aliment1 =
                                AlimentEv(
                                        uuid = Uuid.random().toString(),
                                        nom = "Croquettes pour chien",
                                        group = GroupAlim.CEREALPD,
                                        typeAliment = FoodKind.COMPLET,
                                        rationUUID = null
                                )
                        val aliment2 =
                                AlimentEv(
                                        uuid = Uuid.random().toString(),
                                        nom = "Pâtée pour chien",
                                        group = GroupAlim.DESSERTLAIT,
                                        typeAliment = FoodKind.COMPLET,
                                        rationUUID = null
                                )

                        // Ajouter les aliments au mock repository
                        (foodRepository as MockFoodRepository).addFood(aliment1)
                        (foodRepository as MockFoodRepository).addFood(aliment2)

                        // Création d'un animal
                        val animal =
                                AnimalEv(
                                        uuid = Uuid.random().toString(),
                                        nom = "Rex",
                                        birthdate = LocalDate(2020, 1, 1),
                                        specieId = "CHIEN"
                                )

                        // Création d'une consultation avec une ration
                        val consultation =
                                ConsultationEv(
                                        uuid = Uuid.random().toString(),
                                        idAnim = animal.uuid,
                                        date = LocalDate(2024, 3, 15),
                                        objectConsult = "Bilan nutritionnel"
                                )

                        // Création d'une ration
                        val ration =
                                Ration(
                                        uuid = Uuid.random().toString(),
                                        idConsult = consultation.uuid,
                                        name = "Ration quotidienne",
                                        actual = true
                                )

                        // Ajout des aliments à la ration
                        ration.alimentMutableList.add(
                                AlimentRation(
                                        uuid = Uuid.random().toString(),
                                        refRation = ration.uuid,
                                        refAlimUnif = aliment1.uuid,
                                        quantity = 300.0f,
                                        aliment = aliment1
                                )
                        )
                        ration.alimentMutableList.add(
                                AlimentRation(
                                        uuid = Uuid.random().toString(),
                                        refRation = ration.uuid,
                                        refAlimUnif = aliment2.uuid,
                                        quantity = 100.0f,
                                        aliment = aliment2
                                )
                        )

                        // Ajout de la ration à la consultation
                        consultation.rations.add(ration)

                        // Ajout de la consultation à l'animal
                        animal.consultations.add(consultation)

                        // Configurer le ViewModel avec l'animal
                        (animalRepository as MockAnimalRepository).addAnimal(animal)
                        (consultationRepository as MockConsultationRepository).saveConsultation(
                                consultation
                        )

                        viewModel.setAnimal(animal)
                        testDispatcher.scheduler.advanceUntilIdle()

                        // When - Sélectionner la consultation
                        viewModel.selectConsultation(consultation)
                        testDispatcher.scheduler.advanceUntilIdle()

                        // Then - Vérifier que la consultation est sélectionnée avec ses rations et
                        // aliments
                        val selectedConsultation = viewModel.selectedConsultation.value
                        assertNotNull(
                                selectedConsultation,
                                "Une consultation devrait être sélectionnée"
                        )
                        assertEquals(
                                consultation.uuid,
                                selectedConsultation.uuid,
                                "La bonne consultation devrait être sélectionnée"
                        )
                        assertEquals(
                                1,
                                selectedConsultation.rations.size,
                                "La consultation devrait avoir une ration"
                        )

                        // Vérifier que la ration contient les aliments avec leurs détails
                        val selectedRation = selectedConsultation.rations.first()
                        assertEquals(
                                2,
                                selectedRation.alimentMutableList.size,
                                "La ration devrait avoir deux aliments"
                        )

                        // Vérifier que les aliments sont correctement chargés avec leurs détails
                        val selectedAliment1 =
                                selectedRation.alimentMutableList.find {
                                        it.refAlimUnif == aliment1.uuid
                                }
                        val selectedAliment2 =
                                selectedRation.alimentMutableList.find {
                                        it.refAlimUnif == aliment2.uuid
                                }

                        assertNotNull(
                                selectedAliment1,
                                "Le premier aliment devrait être présent dans la ration"
                        )
                        assertNotNull(
                                selectedAliment2,
                                "Le deuxième aliment devrait être présent dans la ration"
                        )

                        // Vérifier que les aliments ont leurs informations complètes chargées
                        assertNotNull(
                                selectedAliment1.aliment,
                                "Les détails de l'aliment 1 devraient être chargés"
                        )
                        assertNotNull(
                                selectedAliment2.aliment,
                                "Les détails de l'aliment 2 devraient être chargés"
                        )

                        assertEquals(
                                aliment1.nom,
                                selectedAliment1.aliment?.nom,
                                "Le nom de l'aliment 1 devrait correspondre"
                        )
                        assertEquals(
                                aliment2.nom,
                                selectedAliment2.aliment?.nom,
                                "Le nom de l'aliment 2 devrait correspondre"
                        )

                        // Vérifier que les quantités sont correctes
                        assertEquals(
                                300.0f,
                                selectedAliment1.quantity,
                                "La quantité de l'aliment 1 devrait être préservée"
                        )
                        assertEquals(
                                100.0f,
                                selectedAliment2.quantity,
                                "La quantité de l'aliment 2 devrait être préservée"
                        )
                }

        /** Classes mock pour les tests */
        private class MockConsultationRepository : ConsultationRepository {
                val consultations = mutableMapOf<String, List<ConsultationEv>>()
                val consultationDetails = mutableMapOf<String, ConsultationEv>()

                override suspend fun saveConsultation(consultation: ConsultationEv) {
                        consultationDetails[consultation.uuid] = consultation
                        val animalConsultations =
                                consultations[consultation.idAnim]?.toMutableList()
                                        ?: mutableListOf()
                        val existingIndex =
                                animalConsultations.indexOfFirst { it.uuid == consultation.uuid }
                        if (existingIndex >= 0) {
                                animalConsultations[existingIndex] = consultation
                        } else {
                                animalConsultations.add(consultation)
                        }
                        consultations[consultation.idAnim] = animalConsultations
                }

                override suspend fun getConsultationsForAnimal(
                        animalId: String
                ): List<ConsultationEv> {
                        return consultations[animalId] ?: emptyList()
                }

                override suspend fun getConsultationById(id: String): ConsultationEv? {
                        return consultationDetails[id]
                }

                override suspend fun deleteConsultation(consultation: ConsultationEv) {
                        consultationDetails.remove(consultation.uuid)
                        consultations[consultation.idAnim] =
                                consultations[consultation.idAnim]?.filter {
                                        it.uuid != consultation.uuid
                                }
                                        ?: emptyList()
                }
        }

        private class MockAnimalRepository : AnimalRepository {
                private val animals = mutableMapOf<String, AnimalEv>()

                fun addAnimal(animal: AnimalEv) {
                        animals[animal.uuid] = animal
                }

                override suspend fun saveAnimal(animal: AnimalEv) {
                        animals[animal.uuid] = animal
                }

                override suspend fun getAllAnimals(): List<AnimalEv> {
                        return animals.values.toList()
                }

                override suspend fun getAnimalById(id: String): AnimalEv? {
                        return animals[id]
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

        private class MockFoodRepository : FoodRepository {
                private val foods = mutableMapOf<String, AlimentEv>()

                fun addFood(food: AlimentEv) {
                        foods[food.uuid] = food
                }

                override suspend fun insert(food: AlimentEv) {
                        foods[food.uuid] = food
                }

                override suspend fun update(food: AlimentEv) {
                        foods[food.uuid] = food
                }

                override suspend fun delete(food: AlimentEv) {
                        foods.remove(food.uuid)
                }

                override suspend fun getAllFoods(): List<AlimentEv> {
                        return foods.values.toList()
                }

                override suspend fun getFoodById(id: String): AlimentEv? {
                        return foods[id]
                }

                override fun observeAllFoods(): Flow<List<AlimentEv>> {
                        return flow { emit(foods.values.toList()) }
                }

                override suspend fun importFoods(foods: List<AlimentEvJson>): Int {
                        // Implémentation simplifiée pour les tests
                        return foods.size
                }

                override suspend fun insertFood(food: AlimentEv) {
                        foods[food.uuid] = food
                }

                override suspend fun getFood(uuid: String): AlimentEv? {
                        return foods[uuid]
                }

                override suspend fun deleteFood(uuid: String) {
                        foods.remove(uuid)
                }

                override suspend fun updateFood(food: AlimentEv) {
                        foods[food.uuid] = food
                }

                override suspend fun clearAllFoods(): Int {
                        val count = foods.size
                        foods.clear()
                        return count
                }
        }
}
